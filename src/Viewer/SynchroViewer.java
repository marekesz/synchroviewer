
package Viewer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;


public class SynchroViewer
{
    
    private final JFrame frame;
    
    private final SplitPane splitPane;
    private final PaintPanel paintPanel;
    
    private JToolBar toolbar;
    private final String[] iconFiles = { 
        "icons/move_states.png", "icons/add_states.png", "icons/remove_states.png",
        "icons/swap_states.png", "icons/add_transitions.png"
    };
    private final String[] buttonLabels = { "Move states", "Add states", "Remove states", "Swap states (drag one state to another)", "Add/Remove transitions", "Select states" };
    private final JButton[] toolBarButtons = new JButton[buttonLabels.length];
    private JButton selectedColorsButton;
    
    private JButton addTransButton;
    private JButton removeTransButton;
    private JComboBox<String> transitions;

    public SynchroViewer(JFrame frame) 
    {
        this.frame = frame;
        splitPane = new SplitPane();
        paintPanel = splitPane.getPaintPanel();
        
        PropertyChangeListener pcl = new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                updateTransitionsComboBox();
            }
        };
        splitPane.getCodeToolbar().addPropertyChangeListener("updateTransitions", pcl);
        paintPanel.addPropertyChangeListener("updateTransitions", pcl);
        
        createMenuBar();
        createToolBar();

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(toolbar, BorderLayout.NORTH);
        container.add(splitPane, BorderLayout.CENTER);
    }

    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu automatonMenu = new JMenu("Automaton");
        menuBar.add(automatonMenu);
        
        JFileChooser fileChooser = new JFileChooser();
        JMenuItem saveMenuItem = new JMenuItem("Save Image... ");
        saveMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                int option = fileChooser.showSaveDialog(automatonMenu);
                if (option == JFileChooser.APPROVE_OPTION)
                {
                    File file = fileChooser.getSelectedFile();
                    String path = file.getPath();
                    String name = file.getName();
                    
                    BufferedImage img = new BufferedImage(paintPanel.getWidth(),paintPanel.getHeight(),BufferedImage.TYPE_INT_RGB);
                    Graphics g = img.getGraphics();
                    paintPanel.paint(g);
                    g.dispose();
                    try {
                        if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0)
                        {
                            String ext = name.substring(name.lastIndexOf(".") + 1);
                            if (!ext.equals("png"))
                                JOptionPane.showMessageDialog(frame, "Invalid file extension (.png expected)");
                            else
                                ImageIO.write(img, ext, fileChooser.getSelectedFile());
                        }
                        else
                            ImageIO.write(img, "png", new File(path + ".png"));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }   
        });
        
        JMenuItem realignMenuItem = new JMenuItem("Realign");
        realignMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                splitPane.realign();
            }
        });
        
        JMenuItem resetMenuItem = new JMenuItem("Reset");
        resetMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                splitPane.getAutomaton().reset();
            }
        });
        
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        
        automatonMenu.add(saveMenuItem);
        automatonMenu.addSeparator();
        automatonMenu.add(realignMenuItem);
        automatonMenu.add(resetMenuItem);
        automatonMenu.addSeparator();
        automatonMenu.add(exitMenuItem);
        menuBar.add(automatonMenu);
        
        JMenu toolbarsMenu = new JMenu("Toolbars");
        for (DockToolbar dockToolbar : splitPane.getDockToolbars())
        {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dockToolbar.getName());
            menuItem.setSelected(dockToolbar.isVisibleOnStart());
            dockToolbar.setVisible(dockToolbar.isVisibleOnStart());
            toolbarsMenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    dockToolbar.Dock();
                    dockToolbar.setVisible(menuItem.isSelected());
                }
            });
        }
        menuBar.add(toolbarsMenu);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                JFrame aboutFrame = new JFrame("About");
                aboutFrame.setLayout(new GridLayout());
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                
                String[] strings = {
                    "<html>Synchro Viewer version 1.0<br><br></html>",
                    "<html>Synchro Viewer is a graphical application for</html>",
                    "<html>analyzing synchronizing automata.<br><br></html>",
                    "<html>Authors:</html>",
                    "<html>Tomasz Jurkiewicz<br></html>",
                    "<html>Marek Szykuła<br><br></html>",
                    "<html>University of Wrocław<br></html>",
                    "<html>Institute of Computer Science<br><br></html>",
                    "<html>Copyright © 2016</html>"
                };
                
                panel.add(new Separator());
                for (String str : strings)
                {
                    JLabel label = new JLabel(str, JLabel.CENTER);
                    label.setFont(new Font("Arial", Font.ITALIC + Font.BOLD, 14));
                    panel.add(label);
                }
                panel.add(new Separator());
                
                aboutFrame.add(panel);
                aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                aboutFrame.setSize(350, 265);
                aboutFrame.setLocationRelativeTo(null);
                aboutFrame.setResizable(false);
                aboutFrame.setVisible(true);
            }
        });
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        
        frame.setJMenuBar(menuBar);
    }
    
    private void createToolBar()
    {
        toolbar = new JToolBar("Toolbar");
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(195, 195, 195));
        
        Color noBackground = (new JButton()).getBackground();
        Color selectedButtonColor = Color.CYAN;
        ActionListener actionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                for (PaintPanel.Operation op : PaintPanel.Operation.values())
                {
                    int i = op.getValue();
                    if (ev.getSource() == toolBarButtons[i])
                    {
                        if (paintPanel.getOperation() != i)
                        {
                            toolBarButtons[paintPanel.getOperation()].setBackground(noBackground);
                            paintPanel.setOperation(op);
                            paintPanel.repaint();
                            toolBarButtons[i].setBackground(selectedButtonColor);
                        }

                        if (paintPanel.getOperation() == PaintPanel.Operation.ADD_TRANS.getValue())
                        {
                            addTransButton.setVisible(true);
                            removeTransButton.setVisible(true);
                            transitions.setVisible(true);
                        }
                        else
                        {
                            addTransButton.setVisible(false);
                            removeTransButton.setVisible(false);
                            transitions.setVisible(false);
                        }
                        
                        if (paintPanel.getOperation() != PaintPanel.Operation.SWAP_STATES.getValue())
                            paintPanel.resetReplaceStatesFirstState();

                        break;
                    }
                }
            }
        };
        
        for (int i = 0; i < buttonLabels.length; i++) 
        {
            if (i != PaintPanel.Operation.SELECT_STATES.getValue())
            {
                ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(iconFiles[i]));
                Image image = icon.getImage();  
                Image newimage = image.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);  
                icon = new ImageIcon(newimage);
                toolBarButtons[i] = new JButton(icon);
            }
            else
            {
                toolBarButtons[i] = new JButton();
                selectedColorsButton = toolBarButtons[i];
                selectedColorsButton.setIcon(createSelectedColorsIcon(paintPanel.getSelectedStateColor(), paintPanel.getUnselectedStateColor(), 40, 40));
            }
            if (i != 0)
                toolbar.addSeparator();
            toolbar.add(toolBarButtons[i]);
            toolBarButtons[i].setToolTipText(buttonLabels[i]);
            toolBarButtons[i].addActionListener(actionListener);
        }     
        toolBarButtons[paintPanel.getOperation()].setBackground(selectedButtonColor);
        
        toolbar.addSeparator(new Dimension(50, 0));
        addTransButton = new JButton("Add");
        removeTransButton = new JButton("Remove");
        addTransButton.setToolTipText("Create new transition");
        removeTransButton.setToolTipText("Remove last transition");
        addTransButton.setVisible(false);
        removeTransButton.setVisible(false);
        
        addTransButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                if (splitPane.getAutomatonK() < AutomatonHelper.TRANSITIONS_LETTERS.length)
                {
                    splitPane.getAutomaton().createNewTransition();
                    updateTransitionsComboBox();
                }
            }
        });
        removeTransButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                if (splitPane.getAutomatonK() > 0)
                {
                    splitPane.getAutomaton().removeTransition();
                    updateTransitionsComboBox();
                }
            }
        });     
        toolbar.add(addTransButton);
        toolbar.addSeparator();
        toolbar.add(removeTransButton);
        toolbar.addSeparator();
        
        transitions = new JComboBox<>();
        transitions.setMaximumSize(new Dimension(100, 30));
        transitions.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                paintPanel.setSelectedTransition(transitions.getSelectedIndex());
            }
        });
        transitions.setPrototypeDisplayValue("                  ");
        transitions.setVisible(false);
        ComboBoxRenderer renderer = new ComboBoxRenderer(transitions);
        transitions.setRenderer(renderer);
        updateTransitionsComboBox();
        toolbar.add(transitions);
        
        toolbar.add(Box.createHorizontalGlue());
        
        JLabel label = new JLabel("Selected states:  ");
        JLabel selectedStatesLabel = new JLabel(Integer.toString(splitPane.getSelectedStatesNumber()));
        Font font = label.getFont().deriveFont(Font.PLAIN, 20);
        label.setFont(font);
        selectedStatesLabel.setFont(font);
        splitPane.getAutomaton().addPropertyChangeListener("automatonChanged", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                int selectedStatesNumber = splitPane.getSelectedStatesNumber();
                selectedStatesLabel.setText(Integer.toString(selectedStatesNumber));
            }
        });
        toolbar.add(label);
        toolbar.add(selectedStatesLabel);   
        toolbar.addSeparator(new Dimension(toolbar.getPreferredSize().height, 0));
        
        int cols = 5;
        int rows = PaintPanel.STATES_COLORS.length / cols;
        if (PaintPanel.STATES_COLORS.length % cols != 0)
            rows++;
        
        JPanel colorChoosersPanel = new JPanel(new GridLayout(rows, cols));
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                if (i*cols + j < PaintPanel.STATES_COLORS.length)
                {
                    Color stateColor = PaintPanel.STATES_COLORS[i*cols + j];
                    JButton chooseColorButton = new JButton(createIcon(stateColor, 15, 15));
                    chooseColorButton.addMouseListener(new MouseAdapter()
                    {

                        @Override
                        public void mousePressed(MouseEvent ev)
                        {
                            if (ev.getButton() == MouseEvent.BUTTON1)
                            {
                                paintPanel.setSelectedStateColor(stateColor);
                                selectedColorsButton.setIcon(createSelectedColorsIcon(paintPanel.getSelectedStateColor(), paintPanel.getUnselectedStateColor(), 40, 40));
                            }
                            else if (ev.getButton() == MouseEvent.BUTTON3)
                            {
                                paintPanel.setUnselectedStateColor(stateColor);
                                selectedColorsButton.setIcon(createSelectedColorsIcon(paintPanel.getSelectedStateColor(), paintPanel.getUnselectedStateColor(), 40, 40));
                            }
                        }
                    });
                    colorChoosersPanel.add(chooseColorButton);
                }
            }
        }
        toolbar.add(colorChoosersPanel);
        Dimension dim = new Dimension(toolbar.getPreferredSize().width / 3, toolbar.getPreferredSize().height);
        colorChoosersPanel.setMaximumSize(dim);
        
        toolbar.addSeparator();
    }
    
    private ImageIcon createIcon(Color color, int width, int height) 
    {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width - 1, height - 1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
    
    private ImageIcon createSelectedColorsIcon(Color selected, Color unselected, int width, int height) 
    {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TRANSLUCENT);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(unselected);
        graphics.fillRect(width/3, height/3, 2*width/3, 2*height/3);
        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(Color.BLACK);
        graphics.drawRect(width/3, height/3, 2*width/3, 2*height/3);
        graphics.setColor(selected);
        graphics.fillRect(0, 0, 2*width/3, 2*height/3);
        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, 2*width/3, 2*height/3);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
    
    private void updateTransitionsComboBox()
    {
        int K = splitPane.getAutomatonK();
        if (transitions.getItemCount() == K - 1)
        {
            transitions.addItem(Character.toString(AutomatonHelper.TRANSITIONS_LETTERS[K - 1]));
            transitions.setSelectedIndex(K - 1);
        }
        else if (transitions.getItemCount() == K + 1)
            transitions.removeItemAt(K);
        else if (transitions.getItemCount() != K)
        {
            transitions.removeAllItems();
            for (int i = 0; i < K; i++)
                transitions.addItem(Character.toString(AutomatonHelper.TRANSITIONS_LETTERS[i]));
        }
    }
    
    public void repaint()
    {
        paintPanel.repaintCenterAutomaton();
    }
    
    private class ComboBoxRenderer extends JPanel implements ListCellRenderer
    {
        JPanel textPanel;
        JLabel text;

        public ComboBoxRenderer(JComboBox combo) 
        {
            textPanel = new JPanel();
            text = new JLabel();
            text.setOpaque(true);
            textPanel.add(text);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
        {
            if (value == null)
                return text;
            
            if (isSelected)
                setBackground(list.getSelectionBackground());
            else
                setBackground(Color.WHITE);

            text.setBackground(getBackground());

            text.setText(value.toString());
            if (index > -1)
                text.setForeground(AutomatonHelper.TRANSITIONS_COLORS[index]);
            
            return text;
        }
    }
}
