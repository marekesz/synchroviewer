package main;

import algorithms.Helper;
import algorithms.Primitivity;
import models.DFA;
import models.MergeAutomation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


public class PrimitivityToolbar extends DockToolbar {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final JLabel isPrimitiveLabel, isCongruenceLabel;
  private final JLabel isPrimitiveLabelYesNO, isCongruenceLabelYesNO;
  private final JButton findCongruenceButton, refineToCongruenceButton, makeQuotientButton;

  public void refineMethod(ProgramState programState) {
    int N = programState.dfa.getN();

    Helper.UnionFind unionFind = new Helper.UnionFind(N);
    for (int k = 0; k < Config.STATES_COLORS.length; k++) {
      boolean[] a = programState.getSelectedStates(k);
      int state = -1;
      for (int j = 0; j < N; j++) {
        if (a[j]) {
          if (state == -1)
            state = j;
          int t1 = unionFind.find(state);
          int t2 = unionFind.find(j);
          unionFind.union(t1, t2);
        }
      }
    }

    int[] arr = new int[N];  // union -> arr
    for (int i = 0; i < N; i++) {
      arr[i] = unionFind.find(i);
    }

    Primitivity.refineToCongruence(programState.dfa, arr);

    boolean[][] ssbc = programState.getSelectedStatesByColor();
    ArrayList<Integer> freeColors = new ArrayList<>();

    for (int i = 0; i < Config.STATES_COLORS.length; i++) {
      for (int j = 0; j < N; j = j + 1) {
        if (ssbc[i][j]) break;
        if (j == N - 1) freeColors.add(i);
      }
    }

    for (int i = 0; i < N; i++) {
      int motherly = arr[i];
      for (int j = 0; j < Config.STATES_COLORS.length; j++) {
        if (ssbc[j][i]) {
          programState.selectState(motherly, j);
        }
      }
    }
    for (int i = 0; i < N; i++) {
      int motherly = arr[i];

      int hisColor = -1;
      for (int j = 0; j < Config.STATES_COLORS.length; j++) {
        if (ssbc[j][motherly]) {
          hisColor = j;
          break;
        }
      }

      if (hisColor == -1) {
        if (freeColors.isEmpty()) {
          JOptionPane.showMessageDialog(PrimitivityToolbar.this, "Not enough colors!");
          break;
        }

        hisColor = freeColors.get(0);
        freeColors.remove(0);
        programState.selectState(i, hisColor);
        programState.selectState(motherly, hisColor);

      } else {
        for (int j = 0; j < Config.STATES_COLORS.length; j++) {
          if (ssbc[j][motherly]) {
            programState.selectState(i, j);
          }
        }
      }
    }


  }

  public PrimitivityToolbar(String name, boolean visibleOnStart, ProgramState programState) {
    super(name, visibleOnStart, programState);
    JPanel panel = getPanel();
    isPrimitiveLabel = new JLabel();
    isPrimitiveLabelYesNO = new JLabel();
    Font font = isPrimitiveLabel.getFont().deriveFont((float) getDeafultFont().getSize());
    isPrimitiveLabel.setFont(font);
    isPrimitiveLabelYesNO.setFont(font);

    isCongruenceLabel = new JLabel();
    isCongruenceLabelYesNO = new JLabel();
    isCongruenceLabel.setFont(font);
    isCongruenceLabelYesNO.setFont(font);
    isCongruenceLabel.setText("Coloring is congruence: ");
    isPrimitiveLabel.setText("Primitive: ");

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.NONE;
    c.gridx = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;

    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new GridBagLayout());
    labelPanel.add(isPrimitiveLabel, c);
    c.insets.left = 12;
    labelPanel.add(isPrimitiveLabelYesNO, c);
    c.insets.left = 2;
    labelPanel.add(isCongruenceLabel, c);
    c.weighty = 1.0;
    c.insets.left = 12;
    labelPanel.add(isCongruenceLabelYesNO, c);
    panel.add(labelPanel, BorderLayout.WEST);

    findCongruenceButton = new JButton("Find non-trivial congruence");

    refineToCongruenceButton = new JButton("Refine to congruence");
    refineToCongruenceButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        firePropertyChange("saveForUndoRefine", false, true);
        refineMethod(programState);
      }
    });

    findCongruenceButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        firePropertyChange("saveForUndoFind", false, true);
        int N = programState.dfa.getN();
        int maks = 1;
        int saveI = 0, saveJ = 0;
        programState.clearSelectedStates();
        for (int i = 0; i < N; i++)
          for (int j = i + 1; j < N; j++) {
            Helper.UnionFind unionFind = new Helper.UnionFind(N);
            unionFind.union(i, j);
            int[] arr = new int[N];
            for (int k = 0; k < N; k++) {
              arr[k] = unionFind.find(k);
            }
            Primitivity.refineToCongruence(programState.dfa, arr);
            int ile = 0;
            for (int k = 0; k < N; k++)
              if (arr[k] == k) ile++;

            if (ile > maks) {
              maks = ile;
              saveI = i;
              saveJ = j;
            }
          }
        programState.selectState(saveI, 0);
        programState.selectState(saveJ, 0);
        refineMethod(programState);
      }
    });

    makeQuotientButton = new JButton("Make quotient automaton");
    makeQuotientButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        firePropertyChange("saveForUndoMakeQuotient", false, true);
        int N = programState.dfa.getN();
        Helper.UnionFind unionFind = new Helper.UnionFind(N);// Find equivalence closure
        for (int k = 0; k < Config.STATES_COLORS.length; k++) {
          boolean[] a = programState.getSelectedStates(k);
          int state = -1;
          for (int j = 0; j < N; j++) {
            if (a[j]) {
              if (state == -1)
                state = j;
              int t1 = unionFind.find(state);
              int t2 = unionFind.find(j);
              unionFind.union(t1, t2);
            }
          }
        }
        int[] arr = new int[N];
        for (int i = 0; i < N; i++) {
          arr[i] = unionFind.find(i);
        }
        boolean[][] ssbc = programState.getSelectedStatesByColor();
        DFA a = MergeAutomation.merge(arr, programState.dfa);
        programState.update(a);

        try {
          firePropertyChange("updateAndRepaintCenterAutomaton", false, true);
          firePropertyChange("updateTransitions", false, true);
        } catch (IllegalArgumentException e) {
          JOptionPane.showMessageDialog(null, e.toString());
        }

        MergeAutomation.loadColor(arr, programState, ssbc);
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 0.0;
    c.insets.left = 2;
    buttonPanel.add(findCongruenceButton, c);
    buttonPanel.add(makeQuotientButton, c);
    c.weighty = 1.0;
    buttonPanel.add(refineToCongruenceButton, c);
    panel.add(buttonPanel, BorderLayout.EAST);
  }

  @Override
  protected void update() {
    ProgramState programState = getProgramState();

    if (Primitivity.isPrimitive(programState.dfa)) {
      isPrimitiveLabelYesNO.setText("YES");
      findCongruenceButton.setEnabled(false);
    } else {
      isPrimitiveLabelYesNO.setText("NO");
      findCongruenceButton.setEnabled(true);
    }

    if (Primitivity.isCongruence(programState.dfa, programState.getSelectedStatesByColor())) {

      isCongruenceLabelYesNO.setText("YES");
      makeQuotientButton.setEnabled(true);
      refineToCongruenceButton.setEnabled(false);
    } else {

      isCongruenceLabelYesNO.setText("NO");
      makeQuotientButton.setEnabled(false);
      refineToCongruenceButton.setEnabled(true);
    }


  }

}