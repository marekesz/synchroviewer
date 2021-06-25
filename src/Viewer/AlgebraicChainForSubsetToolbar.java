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
import java.awt.Insets;

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
    private final JComboBox<String> sumComboBox;

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
        directionComboBox.addItem("Preimage");
        preprocessComboBox = new JComboBox<String>();
        preprocessComboBox.addItem("--");
        preprocessComboBox.addItem("0-sum");
        preprocessComboBox.addItem("Eigenvector");
        preprocessComboBox.addItem("0-sum for eigenvector");

        postprocessComboBox = new JComboBox<String>();
        postprocessComboBox.addItem("--");
        postprocessComboBox.addItem("Eigenvector");

        sumComboBox = new JComboBox<String>();
        sumComboBox.addItem("--");
        sumComboBox.addItem("Increase");
        sumComboBox.addItem("Decrease");

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

        sumComboBox.addItemListener(new ItemListener() {

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
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        outerPanel.add(new JLabel("Direction:"), c);
        outerPanel.add(directionComboBox, c);
        outerPanel.add(new JLabel("Sum:"), c);
        c.weightx = 0.0;
        outerPanel.add(sumComboBox, c);
        c.weightx = 0.0;

        c.anchor = GridBagConstraints.WEST;
        c.gridy = 1;
        c.gridwidth = 1;
        outerPanel.add(new JLabel("Pre:"), c);
        outerPanel.add(preprocessComboBox, c);
        outerPanel.add(new JLabel("Post:"), c);
        outerPanel.add(postprocessComboBox, c);

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
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
            boolean showVectors, boolean inverseWords, boolean isExtendingWord) {
        if (words.size() == 0)
            return new Pair<ArrayList<Integer>, String>(new ArrayList<>(), "");
        String text = "";
        int dimCnt = 0;
        ArrayList<Integer> dimensions = new ArrayList<Integer>();
        boolean foundExtendingWord = false;
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).contains("*"))
                foundExtendingWord = true;
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
        if (!foundExtendingWord && isExtendingWord)
            text += "\nExtending word not found";
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
        boolean preImageSelected = (directionComboBox.getSelectedIndex() == 2);// "preimage";
        boolean zeroSum = preprocessComboBox.getSelectedIndex() == 1; // 0-sum
        boolean eigenVectorPre = preprocessComboBox.getSelectedIndex() == 2; // eigenvector
        boolean eigenVectorZeroSum = preprocessComboBox.getSelectedIndex() == 3; // eigenvector 0-sum
        boolean eigenVectorPost = postprocessComboBox.getSelectedIndex() == 1; // weighted by steady-state
        boolean rotateWords = preImageSelected;
        boolean increasingSum = sumComboBox.getSelectedIndex() == 1; // increasing
        boolean decreasingSum = sumComboBox.getSelectedIndex() == 2; // decreasing

        AbstractNFA automaton = imageSelected ? getAutomaton() : new InverseAutomaton(getAutomaton());

        Rational[] weights = null;
        // int[] subset = getAutomaton().getSelectedStates();

        if (eigenVectorPost || eigenVectorPre || eigenVectorZeroSum) {
            weights = getAutomaton().getEigenVector();
            // firePropertyChange("setMarkovProbabilitiesVisible", false, true);
            // Strong connectivity exception
            if (!Connectivity.isStronglyConnected(getAutomaton(), new InverseAutomaton(getAutomaton()))) {
                super.setTitle("LinAlg chain (length: " + 0 + ", maxdim: " + 0);
                textPane.setText("Not strongly connected");
                return;
            }
        } else {
            // firePropertyChange("setMarkovProbabilitiesVisible", true, false);
        }

        if (eigenVectorPost && AlgebraicModule.leadingZerosCount(weights) == weights.length) {
            super.setTitle("LinAlg chain (length: " + 0 + ", maxdim: " + 0);
            textPane.setText("Statioary distribution not found");
            return;
        }

        // Pair<ArrayList<String>, ArrayList<Rational[]>> resultsBlueSubset = null;
        // if (imageSelected || preImageSelected)
        // resultsBlueSubset = LinAlgChain.linAlgChain(automaton, subset, weights,
        // zeroSum, eigenVector,
        // eigenVectorZeroSum);
        // else
        // resultsBlueSubset = LinAlgChain.linAlgChainExtendSum(automaton, subset,
        // weights, zeroSum, eigenVector,
        // eigenVectorZeroSum);
        Pair<ArrayList<String>, ArrayList<Rational[]>> results = null;
        if (!increasingSum && !decreasingSum)
            results = LinAlgChain.linAlgChainForManySubsets(automaton, weights, zeroSum, eigenVectorPre,
                    eigenVectorPost, eigenVectorZeroSum);
        else
            results = LinAlgChain.linAlgChainChangeSumForManySubsets(automaton, weights, zeroSum, eigenVectorPre,
                    eigenVectorPost, eigenVectorZeroSum, increasingSum);

        Pair<ArrayList<Integer>, String> chainDescriptionManySubs = getChainDescription(results.first, results.second,
                showVectorsButton.isSelected() == true, rotateWords, increasingSum || decreasingSum);
        ArrayList<Integer> dimensions = chainDescriptionManySubs.first;

        super.setTitle("LinAlg chain (length: " + Integer.toString(dimensions.size()) + ", maxdim: "
                + Integer.toString(dimensions.isEmpty() ? 0 : dimensions.get(dimensions.size() - 1)) + ")");

        if (chainDescriptionManySubs.first.size() > 0) {
            textPane.setText(chainDescriptionManySubs.second);
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