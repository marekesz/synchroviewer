package Viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import AutomatonAlgorithms.AlgebraicModule;

import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;

public class AlgebraicChainForSubsetToolbar extends DockToolbar {
    private final int MAX_STATES = 25; // max number of states in automaton

    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final JLabel lengthLabel;

    class Pair<U, V> {
        public final U first;
        public final V second;

        public Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }

    }

    public AlgebraicChainForSubsetToolbar(String name, boolean visibleOnStart, Automaton automaton,
            InverseAutomaton inverseAutomaton) {
        super(name, visibleOnStart, automaton);

        JPanel panel = getPanel();

        lengthLabel = new JLabel();

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(getDeafultFont());
        // textPane.setPreferredSize(new Dimension(0, 60));
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
        scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        panel.add(scrollPane, BorderLayout.CENTER);
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

    private Pair<Integer, String> getChainDescription(ArrayList<String> words) {
        if (words.size() == 0)
            return new Pair<Integer, String>(0, "");
        String text = "";
        int dimCnt = 0;
        ArrayList<Integer> dimensions = new ArrayList<Integer>();
        for (int i = 0; i < words.size(); i++) {
            dimCnt += 1;
            if (dimensions.size() == 0 || words.get(i).length() > words.get(i - 1).length())
                dimensions.add(dimCnt);
            else
                dimensions.set(dimensions.size() - 1, dimCnt);

            text += words.get(i) + '\n';
        }

        String description = Integer.toString(dimensions.size()) + " dimensions: ";
        for (int i = 0; i < dimensions.size(); i++)
            description += Integer.toString(dimensions.get(i)) + " ";

        text = description + "\n \n" + text;
        return new Pair<Integer, String>(dimensions.size(), text);

    }

    private void recalculate() {
        ArrayList<String> words = AlgebraicModule.wordsForSubset(getAutomaton());
        ArrayList<String> inverseAutomatonWords = AlgebraicModule.wordsForSubset(new InverseAutomaton(getAutomaton()));
        Pair<Integer, String> chainDescription = getChainDescription(words);
        Pair<Integer, String> inverseChainDescription = getChainDescription(inverseAutomatonWords);
        super.setTitle("Linear-algebraic ascending chain for subset - dimensions: "
                + Integer.toString(chainDescription.first));
        if (chainDescription.first > 0 || inverseChainDescription.first > 0)
            textPane.setText(chainDescription.second + "\ninverse Automaton: " + inverseChainDescription.second);
        else
            textPane.setText("");
    }

    @Override
    protected void update() {
        if (getAutomaton().getN() > MAX_STATES) {
            textPane.setText("");
            insertStringToTextPane(String.format("Automaton must have no more than %d states", MAX_STATES),
                    Color.BLACK);
            return;
        }

        recalculate();
        // AlgebraicModule.testAutomaton(getAutomaton());
        // AlgebraicModule.testInverseAutomaton(new InverseAutomaton(automaton));
    }
}