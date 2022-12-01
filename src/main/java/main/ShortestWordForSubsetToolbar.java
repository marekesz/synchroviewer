
package main;

import algorithms.*;
import models.Automaton;
import models.InverseAutomaton;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class ShortestWordForSubsetToolbar extends DockToolbar {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final int MAX_STATES = 25; // max number of states in automaton

    private final JTextPane textPane;
    private final JLabel lengthLabel;
    private InverseAutomaton inverseAutomaton;

    private JComboBox<String> comboBox;

    public ShortestWordForSubsetToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name, visibleOnStart, automaton);
        inverseAutomaton = new InverseAutomaton(automaton);
        JPanel panel = getPanel();

        lengthLabel = new JLabel();
        Font font = lengthLabel.getFont().deriveFont((float) getDeafultFont().getSize());
        lengthLabel.setFont(font);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(getDeafultFont());
        textPane.setPreferredSize(new Dimension(0, 60));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemCopy;
        menuItemCopy = new JMenuItem("Copy");
        menuItemCopy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                textPane.copy();
            }
        });
        popupMenu.add(menuItemCopy);

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

        comboBox = new JComboBox<String>();
        comboBox.addItem("Compressing");
        comboBox.addItem("Reset");
        comboBox.addItem("Extending");
        comboBox.addItem("Totally extending");
        comboBox.addItem("Weighted by eigenvector");

        comboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.SELECTED)
                    recalculate();
            }
        });

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.gridwidth = 1;
        outerPanel.add(comboBox, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(outerPanel, BorderLayout.SOUTH);
    }

    private void insertStringToTextPane(String text, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Style", null);
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), text, style);
            textPane.removeStyle("Style");
        } catch (BadLocationException e) {
        }
    }

    private void recalculate() {
        int[] subset = getAutomaton().getSelectedStates();
        try {
            ArrayList<Integer> transitions = new ArrayList<>();
            switch (comboBox.getSelectedIndex()) {
                case 0:
                    transitions = ShortestCompressingWord.find(getAutomaton(), inverseAutomaton, subset);
                    break;
                case 1:
                    transitions = ShortestResetWord.find(getAutomaton(), subset);
                    break;
                case 2:
                    transitions = ShortestExtendingWord.find(getAutomaton(), inverseAutomaton, subset,
                            getAutomaton().getSelectedStatesNumber() + 1);
                    Collections.reverse(transitions);
                    break;
                case 3:
                    transitions = ShortestExtendingWord.find(getAutomaton(), inverseAutomaton, subset,
                            getAutomaton().getN());
                    Collections.reverse(transitions);
                    break;
                case 4:
                    Rational[] weights = getAutomaton().getEigenVector();
                    if (weights != null) {
                        transitions = ShortestExtendingWord.findWeighted(getAutomaton(), inverseAutomaton, subset, weights,
                                getAutomaton().getSelectedStates());
                        Collections.reverse(transitions);
                    } else
                        throw new NotStronglyConnectedException();
                default:
                    break;

            }


            textPane.setText("");
            for (int trans : transitions) {
                char letter = AutomatonHelper.TRANSITIONS_LETTERS[trans];
                Color color = AutomatonHelper.TRANSITIONS_COLORS[trans];
                insertStringToTextPane(Character.toString(letter), color);
            }
            super.setTitle(this.getName() + String.format(" (length: %d)", transitions.size()));
        } catch (WordNotFoundException | NotStronglyConnectedException ex) {
            textPane.setText("");
            insertStringToTextPane(ex.getMessage(), Color.BLACK);
            super.setTitle(this.getName() + String.format(" (length: --)"));
        }
    }

    @Override
    protected void update() {
        if (getAutomaton().getN() > MAX_STATES) {
            textPane.setText("");
            insertStringToTextPane(String.format("Automaton must have no more than %d states", MAX_STATES),
                    Color.BLACK);
            return;
        }

        inverseAutomaton = new InverseAutomaton(getAutomaton());
        recalculate();
    }
}
