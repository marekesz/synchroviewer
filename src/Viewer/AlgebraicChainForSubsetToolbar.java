package Viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import AutomatonAlgorithms.AlgebraicModule;

import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;

public class AlgebraicChainForSubsetToolbar extends DockToolbar {
    private final int MAX_STATES = 25; // max number of states in automaton

    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final JLabel lengthLabel;

    // private final JRadioButton imageButton;
    // private final JRadioButton preImageButton;
    // private final JRadioButton weightedButton;
    // private final JRadioButton normalizedButton;
    // private final JCheckBox weightedCheckBox;
    // private final JCheckBox normalizedCheckBox;
    private final JComboBox<String> comboBox;

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

        comboBox = new JComboBox();
        comboBox.addItem("Image");
        comboBox.addItem("Preimage");
        comboBox.addItem("Weighted");
        comboBox.addItem("Normalized");
        comboBox.setPreferredSize(new Dimension(200, 20));
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        // outerPanel.add(weightedCheckBox, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        // outerPanel.add(normalizedCheckBox, c);
        // weightedCheckBox.setVisible(false);
        // normalizedCheckBox.setVisible(false);
        // c.gridwidth = 1;
        // if (preImageButton.isSelected()) {
        // outerPanel.add(imageButton, c);
        // c.gridwidth = GridBagConstraints.REMAINDER;
        // outerPanel.add(preImageButton, c);
        // }
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

    private Pair<ArrayList<Integer>, String> getChainDescription(ArrayList<String> words) {
        if (words.size() == 0)
            return new Pair<ArrayList<Integer>, String>(new ArrayList<>(), "");
        String text = "";
        int dimCnt = 0;
        ArrayList<Integer> dimensions = new ArrayList<Integer>();
        for (int i = 0; i < words.size(); i++) {
            dimCnt += 1;
            if (dimensions.size() == 0 || words.get(i).length() > words.get(i - 1).length())
                dimensions.add(dimCnt);
            else
                dimensions.set(dimensions.size() - 1, dimCnt);
        }
        text += "1: ";
        int dimId = 0;
        for (int i = 0; i < words.size(); i++) {
            if (i > 0 && words.get(i).length() > words.get(i - 1).length()) {
                dimId += 1;
                text += "\n" + dimensions.get(dimId) + ": ";
            } else if (i > 0)
                text += ", ";
            text += (words.get(i) == "") ? "." : words.get(i);
        }

        text = text + '\n';
        return new Pair<ArrayList<Integer>, String>(dimensions, text);
    }

    private void recalculate() {
        int[] subset = getAutomaton().getSelectedStates();
        ArrayList<String> words = AlgebraicModule.wordsForSubset(getAutomaton(), subset, null, false);
        ArrayList<String> inverseAutomatonWords = AlgebraicModule.wordsForSubset(new InverseAutomaton(getAutomaton()),
                subset, null, false);
        ArrayList<String> normalizedAutomatonWords = AlgebraicModule
                .wordsForSubset(new InverseAutomaton(getAutomaton()), subset, null, true);
        Pair<ArrayList<Integer>, String> chainDescription = getChainDescription(words);
        Pair<ArrayList<Integer>, String> inverseChainDescription = getChainDescription(inverseAutomatonWords);
        Pair<ArrayList<Integer>, String> normalizedChainDescription = getChainDescription(normalizedAutomatonWords);
        boolean imageSelected = (String) comboBox.getSelectedItem() == "Image";
        boolean preImageSelected = (String) comboBox.getSelectedItem() == "Preimage";
        boolean normalizedSelected = (String) comboBox.getSelectedItem() == "Normalized";
        ArrayList<Integer> dimensions;
        if (imageSelected)
            dimensions = chainDescription.first;
        else if (preImageSelected)
            dimensions = inverseChainDescription.first;
        else
            dimensions = normalizedChainDescription.first;
        super.setTitle("Algebraic chain for subset (length: " + Integer.toString(dimensions.size()) + ", dimension: "
                + Integer.toString(dimensions.isEmpty() ? 0 : dimensions.get(dimensions.size() - 1)) + ")");
        if (imageSelected && chainDescription.first.size() > 0)
            textPane.setText(chainDescription.second);
        else if (preImageSelected && inverseChainDescription.first.size() > 0)
            textPane.setText(inverseChainDescription.second);
        else if (normalizedSelected && normalizedChainDescription.first.size() > 0)
            textPane.setText(normalizedChainDescription.second);
        else
            textPane.setText("EMPTY SUBSET");
    } // to poprawić

    @Override
    protected void update() {
        if (getAutomaton().getN() > MAX_STATES) {
            textPane.setText("");
            insertStringToTextPane(String.format("Automaton must have no more than %d states", MAX_STATES),
                    Color.BLACK);
            return;
        }

        recalculate();
    }
}