package main;

import models.DFA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AutomatonCodeToolbar extends DockToolbar {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final JTextPane textPane;


  public AutomatonCodeToolbar(String name, boolean visibleOnStart, ProgramState programState) {
    super(name, visibleOnStart, programState);

    JPanel panel = getPanel();
    textPane = new JTextPane();
    textPane.setFont(getDeafultFont());
    textPane.setPreferredSize(new Dimension(0, 60));

    // Create popup menu for text pane
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
    menuItemCut = new JMenuItem("Cut");
    menuItemCopy = new JMenuItem("Copy");
    menuItemPaste = new JMenuItem("Paste");
    menuItemCut.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        textPane.cut();
      }
    });
    menuItemCopy.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        textPane.copy();
      }
    });
    menuItemPaste.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        textPane.paste();
      }
    });

    popupMenu.add(menuItemCut);
    popupMenu.add(menuItemCopy);
    popupMenu.add(menuItemPaste);

    textPane.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent ev) {
        if (ev.isPopupTrigger())
          popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
      }

      @Override
      public void mouseReleased(MouseEvent ev) {
        if (ev.isPopupTrigger())
          popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
      }
    });

    panel.add(textPane, BorderLayout.CENTER);

    JButton assignButton = new JButton("Assign");
    assignButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        firePropertyChange("saveForUndo", false, true);
        realign();
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonPanel.add(assignButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
  }

  public void realign() {
    String matrix = textPane.getText().trim();
    try {
      if (getProgramState().dfa.toString().equals(matrix))
        firePropertyChange("repaintCenterAutomaton", false, true);
      else {
        getProgramState().update(new DFA(matrix));
        firePropertyChange("updateAndRepaintCenterAutomaton", false, true);
        firePropertyChange("updateTransitions", false, true);
      }
    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(null, e.toString());
    }
  }

  @Override
  protected void update() {
    textPane.setText(getProgramState().dfa.toString());
  }

  protected void setTextPane(DFA a) {
    textPane.setText(a.toString());
  }
}
