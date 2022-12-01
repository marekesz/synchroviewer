
package main;

import models.Automaton;
import models.AutomatonFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

public class PredefinedAutomataJMenuItem extends JMenuItem {


    public class AutomationComboBoxEle {
        private Function<Integer,Automaton> id;
        private String name;

        public AutomationComboBoxEle(Function<Integer,Automaton> id, String name){
            this.id = id;
            this.name = name;
        }

        public Function<Integer,Automaton> getId() {
            return id;
        }

        //this method return the value to show in the JComboBox
        @Override
        public String toString(){
            return name;
        }
    }

    public PredefinedAutomataJMenuItem(MainFrame t){
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

                AutomationComboBoxEle[] predefinedAutomataList = {new AutomationComboBoxEle(AutomatonFactory.getCernySeries(),"Černý")};
                JComboBox cbAutomata = new JComboBox(predefinedAutomataList);
                panel.add(cbAutomata);

                JLabel label2 = new JLabel("States : ", JLabel.CENTER);
                panel.add(label2);

                JSpinner spiner = new JSpinner();
                spiner.setModel(new SpinnerNumberModel(5.0,2,30, 1.0));

                panel.add(spiner);
                JButton button = new JButton("Apply");
                panel.add(button);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        double d= (double) spiner.getValue();
                        int i = (int) d;
                        Automaton a = ( (AutomationComboBoxEle) cbAutomata.getItemAt(cbAutomata.getSelectedIndex())).getId().apply(i);
                        t.splitPane.getCodeToolbar().setTextPane(a);
                        t.splitPane.realign();
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
