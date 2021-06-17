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
import AutomatonAlgorithms.Pair;
import AutomatonAlgorithms.Rational;
import AutomatonModels.AbstractNFA;
import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;

public class AlgebraicChainForSubsetToolbar extends DockToolbar {
	private static final long serialVersionUID = 1L;
	private final int MAX_STATES = 25; // max number of states in automaton

    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final JCheckBox showVectorsButton;
    private final JComboBox<String> imageComboBox;
    private final JComboBox<String> normalizationComboBox;

    public AlgebraicChainForSubsetToolbar(String name, boolean visibleOnStart, Automaton automaton,
            InverseAutomaton inverseAutomaton) {
        super(name, visibleOnStart, automaton);

        JPanel panel = getPanel();

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(getDeafultFont().deriveFont(Font.BOLD));

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

        imageComboBox = new JComboBox<String>();
        normalizationComboBox = new JComboBox<String>();
        imageComboBox.addItem("Image");
        imageComboBox.addItem("Preimage");
        normalizationComboBox.addItem("Raw");
        normalizationComboBox.addItem("Normalized to 0-sum");
        normalizationComboBox.addItem("Weighted by steady state");
        // imageComboBox.setPreferredSize(new Dimension(200, 20));

        imageComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.SELECTED)
                    recalculate();
            }
        });

        normalizationComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.SELECTED)
                    recalculate();
            }
        });

        showVectorsButton = new JCheckBox("show vectors");

        showVectorsButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                recalculate();
            }
        });

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.0;
        c.gridwidth = 1;
        outerPanel.add(imageComboBox, c);
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.0;
        c.gridwidth = 1;
        outerPanel.add(normalizationComboBox, c);
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1.0;
        c.gridwidth = 1;
        outerPanel.add(showVectorsButton, c);
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

    private Pair<ArrayList<Integer>, String> getChainDescription(ArrayList<String> words, ArrayList<Rational[]> vectors,
            boolean showVectors) {
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
        if (!showVectors) {
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
            //text = text + '\n';
        } else {
            int dimId = 0;
            text += "i=0 dim=" + dimensions.get(dimId) + ":\n";
            for (int i = 0; i < words.size(); i++) {
                if (i > 0 && words.get(i).length() > words.get(i - 1).length()) {
                    dimId += 1;
                    text += "i=" + words.get(i).length() + " dim=" + dimensions.get(dimId) + ":\n";
                }
                text += words.get(i) == "" ? "." : words.get(i);
                text += "   " + AlgebraicModule.vectorToString(vectors.get(i)) + "\n";
            }
            //text = text + '\n';
        }
        return new Pair<ArrayList<Integer>, String>(dimensions, text);
    }

    private void recalculate() {
        int[] subset = getAutomaton().getSelectedStates();
        boolean imageSelected = (imageComboBox.getSelectedIndex() == 0);//((String) imageComboBox.getSelectedItem()) == "Image";
        boolean normalizedSelected = ((String) normalizationComboBox.getSelectedItem()) == "0-sum normalized";
        AbstractNFA automaton = imageSelected ? getAutomaton() : new InverseAutomaton(getAutomaton());
        Pair<ArrayList<String>, ArrayList<Rational[]>> results = AlgebraicModule.wordsForSubset(automaton, subset, null,
                normalizedSelected);
        Pair<ArrayList<Integer>, String> chainDescription = getChainDescription(results.first, results.second,
                showVectorsButton.isSelected() == true);
        ArrayList<Integer> dimensions = chainDescription.first;

        super.setTitle("LinAlg chain (length: " + Integer.toString(dimensions.size()) + ", dimension: "
                + Integer.toString(dimensions.isEmpty() ? 0 : dimensions.get(dimensions.size() - 1)) + ")");
        if (chainDescription.first.size() > 0)
            textPane.setText(chainDescription.second);
        else
            textPane.setText("Empty subspace");
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
    }
}