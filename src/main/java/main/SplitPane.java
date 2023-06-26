package main;

import models.InverseDFA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class SplitPane extends JSplitPane {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final ProgramState programState;
  private PaintPanel paintPanel;
  private final AutomatonCodeToolbar codeToolbar;
  private PrimitivityToolbar primitiveToolbar;

  private ArrayList<DockToolbar> dockToolbars = new ArrayList<>();
  public Snapshots automatonSnapshots;

  public SplitPane(ProgramState programState, Snapshots automatonSnapshots, PaintPanel paintPanel) {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.programState = programState;
    this.automatonSnapshots = automatonSnapshots;
    this.paintPanel = paintPanel;
    setBackground(new Color(224, 224, 224));

    setTopComponent(paintPanel);

    JPanel rightPanel = new JPanel(new BorderLayout());
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
    rightPanel.add(new JScrollPane(innerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    Dimension rightPanelMinimumSize = new Dimension(Config.MIN_TOOLBAR_WIDTH, 0);
    rightPanel.setMinimumSize(rightPanelMinimumSize);
    setBottomComponent(rightPanel);
    setResizeWeight(1.0);

    codeToolbar = new AutomatonCodeToolbar("Automaton code", true, programState);

    ComputeImageToolbar computeImageToolbar = new ComputeImageToolbar("Compute image", false, programState);
    ComputePreimageToolbar computePreimageToolbar = new ComputePreimageToolbar("Compute preimage", false,
      programState);
    ShortestResetWordToolbar resetWordToolbar = new ShortestResetWordToolbar("Shortest reset word", true,
      programState);
    ShortestWordForSubsetToolbar shortestWordSubsetToolbar = new ShortestWordForSubsetToolbar(
      "Shortest word for subset", true, programState);
    AlgebraicChainForSubsetToolbar algebraicChainForSubsetToolbar = new AlgebraicChainForSubsetToolbar(
      "Linear-algebraic ascending chain for subset", false, programState, new InverseDFA(programState.dfa));
    BasicPropertiesToolbar basicPropertiesToolbar = new BasicPropertiesToolbar("Basic properties", false,
      programState);
    ProbabilityDistributionToolbar probabilityDistributionToolbar = new ProbabilityDistributionToolbar(
      "Probability distribution on letters", false, programState);
    primitiveToolbar = new PrimitivityToolbar("Primitivity", false, programState);

    addToolbar(codeToolbar, innerPanel);
    addToolbar(basicPropertiesToolbar, innerPanel);
    addToolbar(computeImageToolbar, innerPanel);
    addToolbar(computePreimageToolbar, innerPanel);
    addToolbar(resetWordToolbar, innerPanel);
    addToolbar(shortestWordSubsetToolbar, innerPanel);
    addToolbar(probabilityDistributionToolbar, innerPanel);
    addToolbar(algebraicChainForSubsetToolbar, innerPanel);
    addToolbar(primitiveToolbar, innerPanel);

    updateToolbars();

    codeToolbar.addPropertyChangeListener("saveForUndo", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        SplitPane.this.automatonSnapshots.saveSnap("assign");
      }
    });

    codeToolbar.addPropertyChangeListener("repaintCenterAutomaton", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        paintPanel.repaintCenterAutomaton();
      }
    });


    primitiveToolbar.addPropertyChangeListener("updateAndRepaintCenterAutomaton", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        paintPanel.updateProgramState();
        paintPanel.repaintCenterAutomaton();
      }
    });

    primitiveToolbar.addPropertyChangeListener("saveForUndoFind", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        SplitPane.this.automatonSnapshots.saveSnap("find");
      }
    });

    primitiveToolbar.addPropertyChangeListener("saveForUndoRefine", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        SplitPane.this.automatonSnapshots.saveSnap("refine");
      }
    });

    primitiveToolbar.addPropertyChangeListener("saveForUndoMakeQuotient", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        SplitPane.this.automatonSnapshots.saveSnap("make quotient");
      }
    });

    codeToolbar.addPropertyChangeListener("updateAndRepaintCenterAutomaton", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        paintPanel.updateProgramState();
        paintPanel.repaintCenterAutomaton();
      }
    });

    algebraicChainForSubsetToolbar.addPropertyChangeListener("setMarkovProbabilitiesVisible",
      new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
          paintPanel.setMarkovPbbVisible((boolean) ev.getNewValue());
        }
      });

    PropertyChangeListener showRangeListener = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getOldValue() == null) {
          if (ev.getSource().equals(computeImageToolbar))
            computePreimageToolbar.rangeCheckBoxSetSelected(false);
          else
            computeImageToolbar.rangeCheckBoxSetSelected(false);

          int[] states = (int[]) ev.getNewValue();
          paintPanel.showRange(states);
        } else
          paintPanel.setShowRange(false);
      }
    };

    computeImageToolbar.addPropertyChangeListener("showRange", showRangeListener);
    computePreimageToolbar.addPropertyChangeListener("showRange", showRangeListener);

    PropertyChangeListener showActionListener = new PropertyChangeListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getOldValue() == null) {
          if (ev.getSource().equals(computeImageToolbar))
            computePreimageToolbar.actionCheckBoxSetSelected(false);
          else
            computeImageToolbar.actionCheckBoxSetSelected(false);

          HashMap<Integer, ArrayList<Integer>> actions = (HashMap<Integer, ArrayList<Integer>>) ev
            .getNewValue();
          paintPanel.showAction(actions);
        } else
          paintPanel.setShowAction(false);
      }
    };

    computeImageToolbar.addPropertyChangeListener("showAction", showActionListener);
    computePreimageToolbar.addPropertyChangeListener("showAction", showActionListener);

    innerPanel.addContainerListener(new ContainerListener() {

      @Override
      public void componentAdded(ContainerEvent e) {
        if (innerPanel.getComponents().length == 1) {
          rightPanel.setMinimumSize(rightPanelMinimumSize);
          SplitPane.this.setDividerLocation(-1);
          SplitPane.this.setEnabled(true);
        }
      }

      @Override
      public void componentRemoved(ContainerEvent e) {
        if (innerPanel.getComponents().length == 0) {
          rightPanel.setMinimumSize(new Dimension(0, 0));
          SplitPane.this.setDividerLocation(1.0);
          SplitPane.this.setEnabled(false);
        }
      }
    });

    for (DockToolbar dockToolbar : dockToolbars) {
      dockToolbar.addPropertyChangeListener("setVisible", new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
          int visibleToolbars = 0;
          for (DockToolbar dt : dockToolbars) {
            if (dt.isVisible())
              visibleToolbars++;
          }

          if (visibleToolbars == 0) {
            rightPanel.setMinimumSize(new Dimension(0, 0));
            SplitPane.this.setDividerLocation(1.0);
            SplitPane.this.setEnabled(false);
          } else if (visibleToolbars == 1 && (boolean) ev.getNewValue()) {
            rightPanel.setMinimumSize(rightPanelMinimumSize);
            SplitPane.this.setDividerLocation(-1);
            SplitPane.this.setEnabled(true);
          }
        }
      });
    }
  }

  private void addToolbar(DockToolbar toolbar, JPanel panel) {
    panel.add(toolbar);
    dockToolbars.add(toolbar);
  }

  private void updateToolbars() {
    for (DockToolbar dockToolbar : dockToolbars)
      dockToolbar.updateToolbar();
  }

  public ProgramState getProgramState() {
    return programState;
  }

  public int getAutomatonK() {
    return programState.dfa.getK();
  }

  public String getAutomatonString() {
    return programState.dfa.toString();
  }

  public int getSelectedStatesNumber() {
    return programState.getSelectedStatesNumber();
  }

  public PaintPanel getPaintPanel() {
    return paintPanel;
  }

  public AutomatonCodeToolbar getCodeToolbar() {
    return codeToolbar;
  }

  public PrimitivityToolbar getPrimitiveToolbar() {
    return primitiveToolbar;
  }

  public ArrayList<DockToolbar> getDockToolbars() {
    return dockToolbars;
  }

  public void realign() {
    codeToolbar.realign();
  }
}
