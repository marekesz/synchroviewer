package main;

import algorithms.Connectivity;
import algorithms.Synchronizability;
import models.InverseDFA;

import javax.swing.*;
import java.awt.*;

public class BasicPropertiesToolbar extends DockToolbar {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final JLabel syncLabel;
  private final JLabel connectedLabel, trahtLabel;

  private InverseDFA inverseAutomaton;

  public BasicPropertiesToolbar(String name, boolean visibleOnStart, ProgramState programState) {
    super(name, visibleOnStart, programState);

    inverseAutomaton = new InverseDFA(programState.dfa);

    JPanel panel = getPanel();

    syncLabel = new JLabel();
    connectedLabel = new JLabel();
    trahtLabel = new JLabel();
    Font font = syncLabel.getFont().deriveFont((float) getDeafultFont().getSize());
    syncLabel.setFont(font);
    connectedLabel.setFont(font);
    trahtLabel.setFont(font);

    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
    labelPanel.add(syncLabel);
    labelPanel.add(connectedLabel);
    labelPanel.add(trahtLabel);
    panel.add(labelPanel, BorderLayout.CENTER);
  }

  @Override
  protected void update() {
    inverseAutomaton = new InverseDFA(getProgramState().dfa);

    if (Synchronizability.isIrreduciblySynchronizing(getProgramState().dfa, inverseAutomaton))
      syncLabel.setText("Synchronizing (irreducibly)");
    else if (Synchronizability.isSynchronizing(getProgramState().dfa, inverseAutomaton))
      syncLabel.setText("Synchronizing (not irreducibly)");
    else
      syncLabel.setText("Not synchronizing");

    if (Connectivity.isStronglyConnected(getProgramState().dfa, inverseAutomaton))
      connectedLabel.setText("Strongly connected");
    else if (Connectivity.isConnected(getProgramState().dfa, inverseAutomaton))
      connectedLabel.setText("Connected (not strongly)");
    else
      connectedLabel.setText("Not connected");
  }
}
