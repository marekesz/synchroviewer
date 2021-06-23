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
import AutomatonAlgorithms.Connectivity;
import AutomatonAlgorithms.LinAlgChain;
import AutomatonAlgorithms.MarkovChains;
import AutomatonAlgorithms.Pair;
import AutomatonAlgorithms.Rational;
import AutomatonModels.AbstractNFA;
import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;

public class AlgebraicChainForSubsetToolbar extends DockToolbar {
    private static final long serialVersionUID = 1L;

    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final JCheckBox showVectorsButton;
    private final JComboBox<String> directionComboBox;
    private final JComboBox<String> preprocessComboBox;
    private final JComboBox<String> postprocessComboBox;

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

        directionComboBox = new JComboBox<String>();
        directionComboBox.addItem("Image");
        directionComboBox.addItem("Image (extend)");
        directionComboBox.addItem("Preimage");
        directionComboBox.addItem("Preimage (extend)");
        preprocessComboBox = new JComboBox<String>();
        preprocessComboBox.addItem("Raw");
        preprocessComboBox.addItem("Normalized to 0-sum");
        preprocessComboBox.addItem("Multiplied by eigenvector");
        postprocessComboBox = new JComboBox<String>();
        postprocessComboBox.addItem("Raw");
        postprocessComboBox.addItem("Weighted by eigenvector");

        // imageComboBox = new JComboBox<String>();
        // normalizationComboBox = new JComboBox<String>();
        // imageComboBox.addItem("Image");
        // imageComboBox.addItem("Preimage");
        // normalizationComboBox.addItem("Raw");
        // normalizationComboBox.addItem("Normalized to 0-sum");
        // normalizationComboBox.addItem("Weighted by steady state");
        // normalizationComboBox.addItem("Normalized by steady state");
        // imageComboBox.setPreferredSize(new Dimension(200, 20));

        directionComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.SELECTED)
                    recalculate();
            }
        });

        preprocessComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.SELECTED)
                    recalculate();
            }
        });

        postprocessComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.SELECTED)
                    recalculate();
            }
        });

        showVectorsButton = new JCheckBox("Show vectors");

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
        outerPanel.add(directionComboBox, c);
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.0;
        c.gridwidth = 1;
        outerPanel.add(preprocessComboBox, c);
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1.0;
        c.gridwidth = 1;
        outerPanel.add(postprocessComboBox, c);
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

    private int wordLengthWithoutExtraSymbols(String word) {
        int length = 0;
        for (char c : word.toCharArray())
            if (c >= 'a' && c <= 'z')
                length++;
        return length;
    }

    private Pair<ArrayList<Integer>, String> getChainDescription(ArrayList<String> words, ArrayList<Rational[]> vectors,
            boolean showVectors, boolean inverseWords) {
        if (words.size() == 0)
            return new Pair<ArrayList<Integer>, String>(new ArrayList<>(), "");
        String text = "";
        int dimCnt = 0;
        ArrayList<Integer> dimensions = new ArrayList<Integer>();

        for (int i = 0; i < words.size(); i++) {
            dimCnt += 1;
            if (dimensions.size() == 0
                    || wordLengthWithoutExtraSymbols(words.get(i)) > wordLengthWithoutExtraSymbols(words.get(i - 1)))
                dimensions.add(dimCnt);
            else
                dimensions.set(dimensions.size() - 1, dimCnt);
        }
        if (!showVectors) {
            int dimId = 0;
            text += "i=0 dim=" + dimensions.get(dimId) + ":\n";
            for (int i = 0; i < words.size(); i++) {
                if (i > 0 && wordLengthWithoutExtraSymbols(words.get(i)) > wordLengthWithoutExtraSymbols(
                        words.get(i - 1))) {
                    dimId += 1;
                    text += "\ni=" + wordLengthWithoutExtraSymbols(words.get(i)) + " dim=" + dimensions.get(dimId)
                            + ":\n";
                } else if (i > 0)
                    text += ", ";
                text += (words.get(i) == "") ? "." : (inverseWords ? rotate(words.get(i)) : words.get(i));
            }
        } else {
            int dimId = 0;
            text += "i=0 dim=" + dimensions.get(dimId) + ":\n";
            for (int i = 0; i < words.size(); i++) {
                if (i > 0 && wordLengthWithoutExtraSymbols(words.get(i)) > wordLengthWithoutExtraSymbols(
                        words.get(i - 1))) {
                    dimId += 1;
                    text += "i=" + wordLengthWithoutExtraSymbols(words.get(i)) + " dim=" + dimensions.get(dimId)
                            + ":\n";
                }
                text += words.get(i) == "" ? "." : (inverseWords ? rotate(words.get(i)) : words.get(i));
                text += "   " + AlgebraicModule.vectorToString(vectors.get(i)) + " sum="
                        + AlgebraicModule.sumOfVector(vectors.get(i)) + "\n";
            }
        }
        return new Pair<ArrayList<Integer>, String>(dimensions, text);
    }

    private String rotate(String string) {
        String result = "";
        for (int i = string.length() - 1; i >= 0; i--)
            result += string.charAt(i);
        return result;
    }

    private void recalculate() {
        boolean imageSelected = (directionComboBox.getSelectedIndex() == 0);// "image";
        boolean imageExtendedSumSelected = (directionComboBox.getSelectedIndex() == 1);// "image (extend sum)";
        boolean preImageSelected = (directionComboBox.getSelectedIndex() == 2);// "preimage";
        boolean preImageExtendedSumSelected = (directionComboBox.getSelectedIndex() == 3);// "preimage (extend sum)";
        boolean normalizedSelected = preprocessComboBox.getSelectedIndex() == 1; // normalized
        boolean normalizedBySteadyState = preprocessComboBox.getSelectedIndex() == 2; // multiplied by steady-state
        boolean weightedSelected = postprocessComboBox.getSelectedIndex() == 1; // weighted by steady-state
        boolean rotateWords = preImageSelected || preImageExtendedSumSelected;
        AbstractNFA automaton = imageSelected || imageExtendedSumSelected ? getAutomaton()
                : new InverseAutomaton(getAutomaton());
        Rational[] weights = null;
        int[] subset = getAutomaton().getSelectedStates();

        if (weightedSelected || normalizedBySteadyState) {
            weights = MarkovChains.getStationaryDistribution(MarkovChains.getTransitMatrix(getAutomaton()));
            firePropertyChange("setMarkovProbabilitiesVisible", false, true);
            // Strong connectivity exception
            if (!Connectivity.isStronglyConnected(getAutomaton(), new InverseAutomaton(getAutomaton()))) {
                super.setTitle("LinAlg chain (length: " + 0 + ", maxdim: " + 0);
                textPane.setText("Not strongly connected");
                return;
            }
        } else {
            firePropertyChange("setMarkovProbabilitiesVisible", true, false);
        }

        if (weightedSelected && AlgebraicModule.leadingZerosCount(weights) == weights.length) {
            super.setTitle("LinAlg chain (length: " + 0 + ", maxdim: " + 0);
            textPane.setText("Statioary distribution not found");
            return;
        }

        Pair<ArrayList<String>, ArrayList<Rational[]>> resultsBlueSubset = null;
        if (imageSelected || preImageSelected)
            resultsBlueSubset = LinAlgChain.linAlgChain(automaton, subset, weights, normalizedSelected,
                    normalizedBySteadyState);
        else
            resultsBlueSubset = LinAlgChain.linAlgChainExtendSum(automaton, subset, weights, normalizedSelected,
                    normalizedBySteadyState);
        Pair<ArrayList<String>, ArrayList<Rational[]>> resultsManySubsets = null;
        if (imageSelected || preImageSelected)
            resultsManySubsets = LinAlgChain.linAlgChainForManySubsets(automaton, weights, normalizedSelected,
                    normalizedBySteadyState);
        else
            resultsManySubsets = LinAlgChain.linAlgChainExtendSumForManySubsets(automaton, weights, normalizedSelected,
                    normalizedBySteadyState);

        Pair<ArrayList<Integer>, String> chainDescription = getChainDescription(resultsBlueSubset.first,
                resultsBlueSubset.second, showVectorsButton.isSelected() == true, rotateWords);
        Pair<ArrayList<Integer>, String> chainDescriptionManySubs = getChainDescription(resultsManySubsets.first,
                resultsManySubsets.second, showVectorsButton.isSelected() == true, rotateWords);
        ArrayList<Integer> dimensions = chainDescription.first;

        super.setTitle("LinAlg chain (length: " + Integer.toString(dimensions.size()) + ", maxdim: "
                + Integer.toString(dimensions.isEmpty() ? 0 : dimensions.get(dimensions.size() - 1)) + ")");

        if (chainDescriptionManySubs.first.size() > 0) {
            textPane.setText(chainDescription.second + "\n for many chains: \n" + chainDescriptionManySubs.second);
        } else
            textPane.setText("Empty subspace");
    }

    @Override
    protected void update() {
        /*
         * if (getAutomaton().getN() > MAX_STATES) { textPane.setText("");
         * insertStringToTextPane(String.
         * format("Automaton must have no more than %d states", MAX_STATES),
         * Color.BLACK); return; }
         */
        recalculate();
    }
}