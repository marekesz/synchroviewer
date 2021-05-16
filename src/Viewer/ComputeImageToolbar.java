
package Viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import AutomatonModels.Automaton;

public class ComputeImageToolbar extends DockToolbar {
    private JTextPane textPane;
    private final HashMap<Character, Integer> hashMap;
    private JCheckBox rangeCheckBox;
    private JCheckBox actionCheckBox;

    private int prefix; // prefix of letters that we already applied
    private int[] startStates; // subset of states before we applied first letter
    private boolean resetPrefix;

    public ComputeImageToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name, visibleOnStart, automaton);

        prefix = -1;
        startStates = automaton.getSelectedStates();
        resetPrefix = true;
        hashMap = new HashMap<>();
        for (int i = 0; i < automaton.getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);

        JPanel panel = getPanel();

        JButton undoImageButton = new JButton("<<");
        JButton letterBackButton = new JButton("<");
        JButton letterForwardButton = new JButton(">");
        JButton imageButton = new JButton(">>");

        StyleContext cont = StyleContext.getDefaultStyleContext();
        AttributeSet attrHighlighted = cont.addAttribute(cont.getEmptySet(), StyleConstants.Background,
                Color.LIGHT_GRAY);
        DefaultStyledDocument doc = new DefaultStyledDocument() {

            @Override
            public void insertString(int offset, String str, AttributeSet a) {
                try {
                    super.insertString(offset, str, a);
                } catch (BadLocationException ex) {
                }

                resetTextPane();

                prefix = -1;
                startStates = getAutomaton().getSelectedStates();
                showRange();
                showAction();

                undoImageButton.setEnabled(false);
                letterBackButton.setEnabled(false);

                if (textPane.getText().trim().length() > 0) {
                    letterForwardButton.setEnabled(true);
                    imageButton.setEnabled(true);
                } else {
                    letterForwardButton.setEnabled(false);
                    imageButton.setEnabled(false);
                }
            }

            @Override
            public void remove(int offset, int len) throws BadLocationException {
                super.remove(offset, len);
                resetTextPane();

                prefix = -1;
                startStates = getAutomaton().getSelectedStates();
                showRange();
                showAction();

                undoImageButton.setEnabled(false);
                letterBackButton.setEnabled(false);

                if (textPane.getText().trim().length() > 0) {
                    letterForwardButton.setEnabled(true);
                    imageButton.setEnabled(true);
                } else {
                    letterForwardButton.setEnabled(false);
                    imageButton.setEnabled(false);
                }
            }
        };

        textPane = new JTextPane(doc);
        textPane.setFont(getDeafultFont());
        textPane.setPreferredSize(new Dimension(0, 60));

        // create popup menu for text pane
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                textPane.cut();
            }
        });
        menuItemCopy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                textPane.copy();
            }
        });
        menuItemPaste.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                textPane.paste();
            }
        });

        popupMenu.add(menuItemCut);
        popupMenu.add(menuItemCopy);
        popupMenu.add(menuItemPaste);

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

        undoImageButton.setEnabled(false);
        undoImageButton.setToolTipText("Undo image");
        undoImageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                prefix = -1;
                getAutomaton().selectStates(startStates); // states that we had before appling letters
                resetTextPane();

                undoImageButton.setEnabled(false);
                letterBackButton.setEnabled(false);
                letterForwardButton.setEnabled(true);
                imageButton.setEnabled(true);
            }
        });

        letterBackButton.setEnabled(false);
        letterBackButton.setToolTipText("Letter back");
        letterBackButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                String word = textPane.getText();
                prefix = (prefix == -1) ? -1 : prefix - 1;
                while (prefix >= 0 && Character.isWhitespace(word.charAt(prefix)))
                    prefix--;

                String subword = word.substring(0, prefix + 1).replaceAll("\\s+", "");
                resetPrefix = false;
                getAutomaton().selectStates(startStates); // states that we have before appling letters
                resetPrefix = false;
                apply(subword);

                resetTextPane();
                doc.setCharacterAttributes(0, prefix + 1, attrHighlighted, false);

                letterForwardButton.setEnabled(true);
                imageButton.setEnabled(true);

                if (prefix == -1) {
                    undoImageButton.setEnabled(false);
                    letterBackButton.setEnabled(false);
                }
            }
        });

        letterForwardButton.setEnabled(false);
        letterForwardButton.setToolTipText("Letter forward");
        letterForwardButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                String word = textPane.getText();
                for (int i = prefix + 1; i < word.length(); i++) {
                    if (hashMap.containsKey(word.charAt(i))) {
                        String letter = word.substring(prefix + 1, i + 1).replaceAll("\\s+", "");
                        prefix = i;
                        resetPrefix = false;
                        apply(letter);

                        StyledDocument doc = textPane.getStyledDocument();
                        doc.setCharacterAttributes(0, prefix + 1, attrHighlighted, false);
                        break;
                    } else if (!Character.isWhitespace(word.charAt(i))) {
                        JOptionPane.showMessageDialog(textPane, "Invalid letter found");
                        break;
                    }
                }

                undoImageButton.setEnabled(true);
                letterBackButton.setEnabled(true);

                if (prefix == word.length() - 1) {
                    letterForwardButton.setEnabled(false);
                    imageButton.setEnabled(false);
                }
            }
        });

        imageButton.setEnabled(false);
        imageButton.setToolTipText("Image");
        imageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                String word = textPane.getText().substring(prefix + 1).replaceAll("\\s+", "");
                if (check(word)) {
                    doc.setCharacterAttributes(prefix + 1, textPane.getText().length() - prefix - 1, attrHighlighted,
                            false);
                    prefix = textPane.getText().length() - 1;
                    resetPrefix = false;
                    apply(word);
                } else
                    JOptionPane.showMessageDialog(textPane, "Invalid word");

                if (prefix != -1) {
                    undoImageButton.setEnabled(true);
                    letterBackButton.setEnabled(true);
                }

                if (prefix == textPane.getText().length() - 1) {
                    letterForwardButton.setEnabled(false);
                    imageButton.setEnabled(false);
                }
            }
        });

        rangeCheckBox = new JCheckBox("Range");
        rangeCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                showRange();
                if (!rangeCheckBox.isSelected())
                    firePropertyChange("showRange", false, true);
            }
        });

        actionCheckBox = new JCheckBox("Action");
        actionCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev) {
                showAction();
                if (!actionCheckBox.isSelected())
                    firePropertyChange("showAction", false, true);
            }
        });

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        outerPanel.add(undoImageButton, c);
        outerPanel.add(letterBackButton, c);
        outerPanel.add(letterForwardButton, c);
        outerPanel.add(imageButton, c);
        c.weightx = 1.0;
        outerPanel.add(rangeCheckBox, c);
        outerPanel.add(actionCheckBox, c);
        panel.add(outerPanel, BorderLayout.SOUTH);
    }

    private boolean check(String word) {
        for (char letter : word.toCharArray()) {
            if (!hashMap.containsKey(letter))
                return false;
        }

        return true;
    }

    private void apply(String word) {
        getAutomaton().selectStates(getSubset(word, getAutomaton().getSelectedStates()));
    }

    private int[] getSubset(String word, int[] subset) {
        int N = getAutomaton().getN();
        for (char letter : word.toCharArray()) {
            int[] newSubset = new int[N];
            for (int i = 0; i < N; i++) {
                if (subset[i] == 1)
                    newSubset[getAutomaton().getMatrix()[i][hashMap.get(letter)]] = 1;
            }
            subset = newSubset;
        }

        return subset;
    }

    private HashMap<Integer, ArrayList<Integer>> getActions(String word) {
        int[] subset = getAutomaton().getSelectedStates();
        HashMap<Integer, ArrayList<Integer>> actions = new HashMap<>();
        for (int i = 0; i < subset.length; i++) {
            if (subset[i] == 1) {
                int[] state = new int[getAutomaton().getN()];
                state[i] = 1;
                int[] states = getSubset(word, state);
                ArrayList<Integer> statesList = new ArrayList<>();
                for (int j = 0; j < states.length; j++) {
                    if (states[j] == 1)
                        statesList.add(j);
                }
                actions.put(i, statesList);
            }
        }
        return actions;
    }

    private void showRange() {
        if (rangeCheckBox.isSelected()) {
            String word = textPane.getText().substring(prefix + 1).replaceAll("\\s+", "");
            if (check(word))
                firePropertyChange("showRange", null, getSubset(word, getAutomaton().getSelectedStates()));
            else
                firePropertyChange("showRange", null, new int[getAutomaton().getN()]);
        }
    }

    private void showAction() {
        if (actionCheckBox.isSelected()) {
            String word = textPane.getText().substring(prefix + 1).replaceAll("\\s+", "");
            if (check(word))
                firePropertyChange("showAction", null, getActions(word));
            else
                firePropertyChange("showAction", null, new HashMap<>());
        }
    }

    public void rangeCheckBoxSetSelected(boolean b) {
        rangeCheckBox.setSelected(b);
    }

    public void actionCheckBoxSetSelected(boolean b) {
        actionCheckBox.setSelected(b);
    }

    private void resetTextPane() {
        StyledDocument doc = textPane.getStyledDocument();
        StyleContext cont = StyleContext.getDefaultStyleContext();
        AttributeSet attrStrike = cont.addAttribute(cont.getEmptySet(), StyleConstants.StrikeThrough, true);
        AttributeSet attrDefault = cont.getStyle(StyleContext.DEFAULT_STYLE);
        String word = textPane.getText();
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            if (hashMap.containsKey(letter)) {
                AttributeSet attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
                        AutomatonHelper.TRANSITIONS_COLORS[hashMap.get(letter)]);
                doc.setCharacterAttributes(i, 1, attr, true);
            } else if (!Character.isWhitespace(letter))
                doc.setCharacterAttributes(i, 1, attrStrike, true);
            else
                doc.setCharacterAttributes(i, 1, attrDefault, true);
        }
    }

    @Override
    protected void update() {
        hashMap.clear();
        for (int i = 0; i < getAutomaton().getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);

        if (resetPrefix) {
            prefix = -1;
            startStates = getAutomaton().getSelectedStates();
        } else
            resetPrefix = true;

        resetTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        StyleContext cont = StyleContext.getDefaultStyleContext();
        AttributeSet attrHighlighted = cont.addAttribute(cont.getEmptySet(), StyleConstants.Background,
                Color.LIGHT_GRAY);
        doc.setCharacterAttributes(0, prefix + 1, attrHighlighted, false);

        showRange();
        showAction();
    }
}
