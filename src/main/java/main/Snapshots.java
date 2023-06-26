package main;


import models.DFA;
import models.SnapshotEvent;

import javax.swing.*;
import java.util.Stack;

public class Snapshots {
  private final Stack<SnapshotEvent> snapsStackUndo;
  private final Stack<SnapshotEvent> snapsStackRedo;
  private final ProgramState programState;
  private final PaintPanel paintPanel;
  public JMenuItem undoItem, redoItem;

  public Snapshots(PaintPanel paintPanel) {
    this.programState = paintPanel.getProgramState();
    this.paintPanel = paintPanel;
    paintPanel.setAutomatonSnapshots(this);
    this.snapsStackUndo = new Stack<>();
    this.snapsStackRedo = new Stack<>();
  }

  public void saveSnap(String name) {
    snapsStackRedo.clear();
    redoItem.setEnabled(false);
    redoItem.setText("Redo");
    if (!snapsStackUndo.isEmpty() && snapsStackUndo.peek().name.equals(name))
      return;
    SnapshotEvent snapshotEvent = new SnapshotEvent(name, programState.dfa.toString(), paintPanel.getVertices(), programState.getSelectedStatesByColor());
    snapsStackUndo.add(snapshotEvent);
    undoItem.setEnabled(true);
    undoItem.setText("Undo (" + name + ")");
  }

  public void RedoSnap() {
    if (snapsStackRedo.isEmpty()) return;
    SnapshotEvent snapshotEvent = snapsStackRedo.pop();
    snapsStackUndo.add(new SnapshotEvent(snapshotEvent.name, programState.dfa.toString(), paintPanel.getVertices(), programState.getSelectedStatesByColor()));
    undoItem.setEnabled(true);
    undoItem.setText("Undo (" + snapshotEvent.name + ")");
    if (snapsStackRedo.isEmpty()) {
      redoItem.setEnabled(false);
      redoItem.setText("Redo");
    } else
      redoItem.setText("Redo (" + snapsStackRedo.peek().name + ")");
    DFA a = snapshotEvent.dfa;

    programState.update(a);


    try {
      paintPanel.updateProgramState();
      paintPanel.repaintCenterAutomaton();
      paintPanel.firePropertyChange("updateTransitions", false, true);

      for (int i = 0; i < a.getN(); i++)
        for (int j = 0; j < Config.STATES_COLORS.length; j++)
          if (snapshotEvent.selectedStatesByColor[j][i])
            programState.selectState(i, j);

      for (int i = 0; i < a.getN(); i++)
        paintPanel.setVerticesAt(snapshotEvent.vertices[i], i);
      paintPanel.repaint();

    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(null, e.toString());
    }


  }

  public void loadSnap() {
    if (snapsStackUndo.isEmpty()) return;
    SnapshotEvent snapshotEvent = snapsStackUndo.pop();
    snapsStackRedo.add(new SnapshotEvent(snapshotEvent.name, programState.dfa.toString(), paintPanel.getVertices(), programState.getSelectedStatesByColor()));
    if (snapsStackUndo.isEmpty()) {
      undoItem.setEnabled(false);
      undoItem.setText("Undo");
    } else
      undoItem.setText("Undo (" + snapsStackUndo.peek().name + ")");
    DFA a = snapshotEvent.dfa;

    programState.update(a);


    try {
      paintPanel.updateProgramState();
      paintPanel.repaintCenterAutomaton();
      paintPanel.firePropertyChange("updateTransitions", false, true);

      for (int i = 0; i < a.getN(); i++)
        for (int j = 0; j < Config.STATES_COLORS.length; j++)
          if (snapshotEvent.selectedStatesByColor[j][i])
            programState.selectState(i, j);

      for (int i = 0; i < a.getN(); i++)
        paintPanel.setVerticesAt(snapshotEvent.vertices[i], i);
      paintPanel.repaint();

    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(null, e.toString());
    }
    redoItem.setEnabled(true);
    redoItem.setText("Redo (" + snapshotEvent.name + ")");
  }

}
