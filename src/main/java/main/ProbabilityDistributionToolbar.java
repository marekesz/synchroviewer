
package main;

import algorithms.Rational;
import models.Automaton;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.math.BigInteger;

public class ProbabilityDistributionToolbar extends DockToolbar implements ChangeListener {
	private static final long serialVersionUID = 1L;
	private JSpinner[] letterSpinners;
    private JLabel[] letterLabels;
    private JLabel denumLabel;
    private JPanel mainPanel;
    
    public ProbabilityDistributionToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name, visibleOnStart, automaton);
        letterSpinners = new JSpinner[0];
        letterLabels = new JLabel[0];
        denumLabel = new JLabel();

        mainPanel = getPanel();
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    protected void update() {
    	int k = getAutomaton().getK();
    	if (letterSpinners.length != k) {
	    	mainPanel.removeAll();
	    	
	        letterLabels = new JLabel[k];
	        letterSpinners = new JSpinner[k];
	        mainPanel.setLayout(new GridBagLayout());
	        GridBagConstraints c = new GridBagConstraints();
	        c.insets = new Insets(2, 2, 2, 2);
	        c.gridwidth = GridBagConstraints.REMAINDER;
	        c.fill = GridBagConstraints.HORIZONTAL;
	        for (int i = 0; i < k; i++) {
	        	c.gridwidth = 1;
	            c.weightx = 0.0;
	        	letterLabels[i] = new JLabel(Character.toString(AutomatonHelper.TRANSITIONS_LETTERS[i]));
	        	mainPanel.add(letterLabels[i], c);
	        	SpinnerNumberModel model = new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1);
	        	letterSpinners[i] = new JSpinner(model);
	        	letterSpinners[i].addChangeListener(this);
	        	c.gridwidth = GridBagConstraints.REMAINDER;
	        	c.weightx = 1.0;
	        	mainPanel.add(letterSpinners[i], c);
	        }
	        c.anchor = GridBagConstraints.EAST;
	        c.fill = GridBagConstraints.NONE;
	        mainPanel.add(denumLabel, c);
	        
	        setDistribution();
	        mainPanel.revalidate();
    	}
    }

    private void setDistribution() {
		int k = getAutomaton().getK();
		BigInteger denum = new BigInteger("0");
		BigInteger[] nums = new BigInteger[k];
		for (int i = 0; i < k; i++) {
			nums[i] = new BigInteger(letterSpinners[i].getValue().toString());
			denum = denum.add(nums[i]);
		}
		denumLabel.setText("Denum: " + denum.toString());
		Rational[] dist = new Rational[k];
		for (int i = 0; i < k; i++) {
			dist[i] = new Rational(nums[i], denum);
		}
		getAutomaton().setProbabilityDistribution(dist);
    }
    
	@Override
	public void stateChanged(ChangeEvent e) {
		setDistribution();
	}
}
