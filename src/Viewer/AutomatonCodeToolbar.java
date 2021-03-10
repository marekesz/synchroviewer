
package Viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;

import AutomatonModels.Automaton;


public class AutomatonCodeToolbar extends DockToolbar
{
    private JTextPane textPane;
    
    public AutomatonCodeToolbar(String name, boolean visibleOnStart, Automaton automaton)
    {
        super(name, visibleOnStart, automaton);
        
        JPanel panel = getPanel();
        textPane = new JTextPane();
        textPane.setFont(getDeafultFont());
        textPane.setPreferredSize(new Dimension(0, 60));
        
        // create popup menu for text pane
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.cut();
            }
        });
        menuItemCopy.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.copy();
            }
        });
        menuItemPaste.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.paste();
            }
        });

        popupMenu.add(menuItemCut);
        popupMenu.add(menuItemCopy);
        popupMenu.add(menuItemPaste);
        
        textPane.addMouseListener(new MouseAdapter() {
            
            @Override 
            public void mousePressed(MouseEvent ev) 
            {
                if (ev.isPopupTrigger()) 
                    popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
            }

            @Override 
            public void mouseReleased(MouseEvent ev) 
            {
                if (ev.isPopupTrigger()) 
                    popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
            }
        });
        
        panel.add(textPane, BorderLayout.CENTER);
        
        
        JButton realignButton = new JButton("Realign");
        realignButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                realign();
            }       
        });
        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(realignButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void setCode(String text)
    {
        textPane.setText(text);
    }
    
    public void realign()
    {
        String matrix = textPane.getText().trim();
        try 
        {
            if (getAutomaton().toString().equals(matrix))
                firePropertyChange("repaintCenterAutomaton", false, true);
            else
            {
                getAutomaton().update(new Automaton(matrix));
                firePropertyChange("updateAndRepaintCenterAutomaton", false, true);
                firePropertyChange("updateTransitions", false, true);
            }
        } 
        catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }

    @Override
    protected void update() 
    {
        textPane.setText(getAutomaton().toString());
    }
}