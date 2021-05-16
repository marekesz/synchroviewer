
package Viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import AutomatonAlgorithms.ShortestResetWord;
import AutomatonAlgorithms.WordNotFoundException;
import AutomatonModels.Automaton;

public class ShortestResetWordToolbar extends DockToolbar {
    private final int MAX_STATES = 25; // max number of states in automaton

    private final JTextPane textPane;
    private final JLabel lengthLabel;

    public ShortestResetWordToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name, visibleOnStart, automaton);

        JPanel panel = getPanel();

        lengthLabel = new JLabel();
        Font font = lengthLabel.getFont().deriveFont((float) getDeafultFont().getSize());
        lengthLabel.setFont(font);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        labelPanel.add(lengthLabel);
        panel.add(labelPanel, BorderLayout.NORTH);

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

    @Override
    protected void update() {
        if (getAutomaton().getN() > MAX_STATES) {
            textPane.setText("");
            insertStringToTextPane(String.format("Automaton must have no more than %d states", MAX_STATES),
                    Color.BLACK);
            return;
        }

        try {
            int[] subset = new int[getAutomaton().getN()];
            Arrays.fill(subset, 1);
            ArrayList<Integer> transitions = ShortestResetWord.find(getAutomaton(), subset);
            textPane.setText("");
            for (int trans : transitions) {
                char letter = AutomatonHelper.TRANSITIONS_LETTERS[trans];
                Color color = AutomatonHelper.TRANSITIONS_COLORS[trans];
                insertStringToTextPane(Character.toString(letter), color);
            }
            lengthLabel.setText(String.format("%nLength: %d", transitions.size()));
        } catch (WordNotFoundException ex) {
            textPane.setText("");
            insertStringToTextPane("Word not found", Color.BLACK);
            lengthLabel.setText("Length: -");
        }
    }
}
