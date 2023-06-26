package main;

import algorithms.ShortestResetWord;
import algorithms.WordNotFoundException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class ShortestResetWordToolbar extends DockToolbar {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final JTextPane textPane;
  private final JLabel lengthLabel;

  public ShortestResetWordToolbar(String name, boolean visibleOnStart, ProgramState programState) {
    super(name, visibleOnStart, programState);

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
    } catch (BadLocationException ignored) {
    }
  }

  @Override
  protected void update() {
    if (getProgramState().dfa.getN() > Config.MAX_STATES_FOR_EXPONENTIAL_ALGORITHMS) {
      textPane.setText("");
      insertStringToTextPane(String.format("Automaton must have no more than %d states", Config.MAX_STATES_FOR_EXPONENTIAL_ALGORITHMS),
        Color.BLACK);
      return;
    }

    try {
      boolean[] subset = new boolean[getProgramState().dfa.getN()];
      Arrays.fill(subset, true);
      ArrayList<Integer> transitions = ShortestResetWord.find(getProgramState().dfa, subset);
      textPane.setText("");
      for (int trans : transitions) {
        char letter = Config.TRANSITIONS_LETTERS[trans];
        Color color = Config.getTransitionColor(trans);
        insertStringToTextPane(Character.toString(letter), color);
      }
      super.setTitle(this.getName() + String.format(" (length: %d)", transitions.size()));
    } catch (WordNotFoundException ex) {
      textPane.setText("");
      insertStringToTextPane("Word not found", Color.BLACK);
      super.setTitle(this.getName() + String.format(" (length: --)"));
    }
  }
}
