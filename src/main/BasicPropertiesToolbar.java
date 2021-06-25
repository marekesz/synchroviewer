
package main;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import algorithms.Connectivity;
import algorithms.Synchronizability;
import models.Automaton;
import models.InverseAutomaton;

public class BasicPropertiesToolbar extends DockToolbar {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JLabel syncLabel;
    private final JLabel connectedLabel;

    private InverseAutomaton inverseAutomaton;

    public BasicPropertiesToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name, visibleOnStart, automaton);

        inverseAutomaton = new InverseAutomaton(automaton);

        JPanel panel = getPanel();

        syncLabel = new JLabel();
        connectedLabel = new JLabel();
        Font font = syncLabel.getFont().deriveFont((float) getDeafultFont().getSize());
        syncLabel.setFont(font);
        connectedLabel.setFont(font);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(syncLabel);
        labelPanel.add(connectedLabel);
        panel.add(labelPanel, BorderLayout.CENTER);
    }

    @Override
    protected void update() {
        inverseAutomaton = new InverseAutomaton(getAutomaton());

        if (Synchronizability.isIrreduciblySynchronizing(getAutomaton(), inverseAutomaton))
            syncLabel.setText("Synchronizing (irreducibly)");
        else if (Synchronizability.isSynchronizing(getAutomaton(), inverseAutomaton))
            syncLabel.setText("Synchronizing (not irreducibly)");
        else
            syncLabel.setText("Not synchronizing");

        if (Connectivity.isStronglyConnected(getAutomaton(), inverseAutomaton))
            connectedLabel.setText("Strongly connected");
        else if (Connectivity.isConnected(getAutomaton(), inverseAutomaton))
            connectedLabel.setText("Connected (not strongly)");
        else
            connectedLabel.setText("Not connected");
    }
}
