
package Viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import AutomatonAlgorithms.AlgebraicModule;
import AutomatonAlgorithms.MarkovChains;
import AutomatonAlgorithms.Rational;
import AutomatonAlgorithms.ShortestCompressingWord;
import AutomatonAlgorithms.ShortestExtendingWord;
import AutomatonAlgorithms.ShortestResetWord;
import AutomatonAlgorithms.WordNotFoundException;
import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;

public class ShortestWordForSubsetToolbar extends DockToolbar {
    private final int MAX_STATES = 25; // max number of states in automaton

    private final JTextPane textPane;
    private final JLabel lengthLabel;
    private InverseAutomaton inverseAutomaton;

    private boolean weighted;

    private JComboBox<String> comboBox;

    public ShortestWordForSubsetToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name, visibleOnStart, automaton);
        inverseAutomaton = new InverseAutomaton(automaton);
        weighted = false;
        JPanel panel = getPanel();

        lengthLabel = new JLabel();
        Font font = lengthLabel.getFont().deriveFont((float) getDeafultFont().getSize());
        lengthLabel.setFont(font);
        JPanel labelPanel = new JPanel();
        // labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        // labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        // labelPanel.add(lengthLabel);
        // panel.add(labelPanel, BorderLayout.NORTH);

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
        comboBox.addItem("Weighted by steady-state");

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
            if (comboBox.getSelectedIndex() == 0)// compressing
                transitions = ShortestCompressingWord.find(getAutomaton(), inverseAutomaton, subset);
            else if (comboBox.getSelectedIndex() == 1)// reset
                transitions = ShortestResetWord.find(getAutomaton(), subset);
            else if (comboBox.getSelectedIndex() == 2) { // extending
                transitions = ShortestExtendingWord.find(getAutomaton(), inverseAutomaton, subset,
                        getAutomaton().getSelectedStatesNumber() + 1);
                Collections.reverse(transitions);
            } else if (comboBox.getSelectedIndex() == 3) { // totally extending
                transitions = ShortestExtendingWord.find(getAutomaton(), inverseAutomaton, subset,
                        getAutomaton().getN());
                Collections.reverse(transitions);
            } else if (comboBox.getSelectedIndex() == 4) { // weighted by steady-state
                Rational[] weights = MarkovChains.getStationaryDistribution(getAutomaton());

                transitions = ShortestExtendingWord.findWeighted(getAutomaton(), inverseAutomaton, subset, weights,
                        getAutomaton().getSelectedStates());
                Collections.reverse(transitions);
            }

            textPane.setText("");
            for (int trans : transitions) {
                char letter = AutomatonHelper.TRANSITIONS_LETTERS[trans];
                Color color = AutomatonHelper.TRANSITIONS_COLORS[trans];
                insertStringToTextPane(Character.toString(letter), color);
            }
            super.setTitle(this.getName() + String.format(" (length: %d)", transitions.size()));
            // lengthLabel.setText(String.format("%nLength: %d", transitions.size()));
        } catch (WordNotFoundException ex) {
            textPane.setText("");
            insertStringToTextPane("Word not found", Color.BLACK);
            // lengthLabel.setText("Length: -");
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
