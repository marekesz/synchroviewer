package main;

import models.AutomatonFactory;
import models.DFA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

public class PredefinedAutomataJMenuItem extends JMenuItem {


  public static class AutomationComboBoxEle {
    private Function<Integer, DFA> id;
    private String name;

    public AutomationComboBoxEle(Function<Integer, DFA> id, String name) {
      this.id = id;
      this.name = name;
    }

    public Function<Integer, DFA> getId() {
      return id;
    }

    //this method return the value to show in the JComboBox
    @Override
    public String toString() {
      return name;
    }
  }

  public PredefinedAutomataJMenuItem(MainFrame t) {
    super("Predefined automata...");
    this.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        JFrame factoryFrame = new JFrame("Predefined automata");
        factoryFrame.setLayout(new GridLayout());// TODO GridGagLayout

        JPanel panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Choose: ", JLabel.CENTER);
        panel.add(label);

        AutomationComboBoxEle[] predefinedAutomataList = {
          new AutomationComboBoxEle(AutomatonFactory.getCycle(), "Cycle"),
          new AutomationComboBoxEle(AutomatonFactory.getCerny(), "Černý automaton"),
          new AutomationComboBoxEle(AutomatonFactory.getSlowlySynchronizingWithSink(), "Slowly synchronizing with sink"),

        };
        JComboBox<AutomationComboBoxEle> cbAutomata = new JComboBox<>(predefinedAutomataList);
        panel.add(cbAutomata);

        JLabel label2 = new JLabel("States : ", JLabel.CENTER);
        panel.add(label2);

        JSpinner spiner = new JSpinner();
        spiner.setModel(new SpinnerNumberModel(5.0, 2, 30, 1.0));

        panel.add(spiner);
        JButton button = new JButton("Apply");
        panel.add(button);
        button.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            double d = (double) spiner.getValue();
            int i = (int) d;
            try {
              DFA dfa = (cbAutomata.getItemAt(cbAutomata.getSelectedIndex())).getId().apply(i);
              t.snapshots.saveSnap("predefined: " + (cbAutomata.getItemAt(cbAutomata.getSelectedIndex())).name.toString());
              t.splitPane.getCodeToolbar().setTextPane(dfa);
              t.splitPane.realign();
            } catch (Exception e) {
              JOptionPane.showMessageDialog(null, e.toString());
            }
          }
        });

        factoryFrame.add(panel);
        factoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        factoryFrame.setResizable(false);
        factoryFrame.pack();
        factoryFrame.setLocationRelativeTo(t.frame);
        factoryFrame.setVisible(true);
      }
    });
  }

}
