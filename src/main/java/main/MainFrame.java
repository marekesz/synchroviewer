package main;

import models.DFA;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

public class MainFrame {

  protected final JFrame frame;
  ProgramState programState;
  protected final SplitPane splitPane;
  private final PaintPanel paintPanel;

  private JToolBar toolbar;
  private final String[] iconFiles = {"icons/move_states.png", "icons/add_states.png", "icons/remove_states.png",
    "icons/swap_states.png", "icons/add_transitions.png"};
  private final String[] buttonLabels = {"Move states", "Add states", "Remove states",
    "Swap states (drag one state to another)", "Add/Remove transitions", "Select states"};
  private final JButton[] toolBarButtons = new JButton[buttonLabels.length];
  private JButton selectedColorsButton;

  private JButton addTransButton;
  private JButton removeTransButton;
  private JComboBox<String> transitions;

  public Snapshots snapshots;

  public MainFrame(JFrame frame) {
    this.frame = frame;

    programState = new ProgramState(new DFA("2 5 1 0 2 1 3 2 4 3 0 0"));
    paintPanel = new PaintPanel(programState);  //= splitPane.getPaintPanel();
    snapshots = new Snapshots(paintPanel);
    splitPane = new SplitPane(programState, snapshots, paintPanel);

    PropertyChangeListener pcl = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        updateTransitionsComboBox();
      }
    };
    splitPane.getCodeToolbar().addPropertyChangeListener("updateTransitions", pcl);
    splitPane.getPrimitiveToolbar().addPropertyChangeListener("updateTransitions", pcl);
    paintPanel.addPropertyChangeListener("updateTransitions", pcl);


    createMenuBar();
    createToolBar();

    Container container = frame.getContentPane();
    container.setLayout(new BorderLayout());
    container.add(toolbar, BorderLayout.NORTH);
    container.add(splitPane, BorderLayout.CENTER);
  }

  private void createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JFileChooser fileChooser = new JFileChooser();
    JMenuItem saveMenuItem = new JMenuItem("Save Image... ");
    saveMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        int option = fileChooser.showSaveDialog(fileMenu);
        if (option == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();
          String path = file.getPath();
          String name = file.getName();

          BufferedImage img = new BufferedImage(paintPanel.getWidth(), paintPanel.getHeight(),
            BufferedImage.TYPE_INT_RGB);
          Graphics g = img.getGraphics();
          paintPanel.paint(g);
          g.dispose();
          try {
            if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0) {
              String ext = name.substring(name.lastIndexOf(".") + 1);
              if (!ext.equals("png"))
                JOptionPane.showMessageDialog(frame, "Invalid file extension (.png expected)");
              else
                ImageIO.write(img, ext, fileChooser.getSelectedFile());
            } else
              ImageIO.write(img, "png", new File(path + ".png"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });

    PredefinedAutomataJMenuItem predefinedAutomataMenuItem = new PredefinedAutomataJMenuItem(this);

    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
      }
    });

    fileMenu.add(saveMenuItem);
    fileMenu.addSeparator();
    fileMenu.add(predefinedAutomataMenuItem);
    fileMenu.addSeparator();
    fileMenu.add(exitMenuItem);
    menuBar.add(fileMenu);

    // ****************************************************************

    JMenu editMenu = new JMenu("Edit");

    JMenuItem undoItem = new JMenuItem("Undo");
    editMenu.add(undoItem);
    undoItem.setEnabled(false);
    undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
    undoItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        snapshots.loadSnap();
      }
    });
    snapshots.undoItem = undoItem;

    JMenuItem redoItem = new JMenuItem("Redo");
    editMenu.add(redoItem);
    redoItem.setEnabled(false);
    redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
    redoItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        snapshots.RedoSnap();
      }
    });
    snapshots.redoItem = redoItem;

    editMenu.addSeparator();

    JMenuItem realignMenuItem = new JMenuItem("Realign");
    editMenu.add(realignMenuItem);
    realignMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        snapshots.saveSnap("realign");
        splitPane.realign();
      }
    });

    JMenuItem clearSelectionMenuItem = new JMenuItem("Clear selection");
    editMenu.add(clearSelectionMenuItem);
    clearSelectionMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        snapshots.saveSnap("Clear selection");
        splitPane.getProgramState().clearSelectedStates();
      }
    });
    JMenuItem clearAutomatonMenuItem = new JMenuItem("Clear automaton");
    editMenu.add(clearAutomatonMenuItem);
    clearAutomatonMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        snapshots.saveSnap("Clear automaton");
        splitPane.getProgramState().reset();
      }
    });

    menuBar.add(editMenu);

    // ****************************************************************

    JMenu viewMenu = new JMenu("View");
    for (DockToolbar dockToolbar : splitPane.getDockToolbars()) {
      JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dockToolbar.getName());
      menuItem.setSelected(dockToolbar.isVisibleOnStart());
      dockToolbar.setVisible(dockToolbar.isVisibleOnStart());
      viewMenu.add(menuItem);
      menuItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ev) {
          dockToolbar.Dock();
          dockToolbar.setVisible(menuItem.isSelected());
        }
      });
    }

    viewMenu.addSeparator();
    JCheckBoxMenuItem loopItem = new JCheckBoxMenuItem("Verbose drawing");
    loopItem.setVisible(true);
    loopItem.setSelected(paintPanel.getLoopEdgesVisible());
    loopItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        paintPanel.setLoopEdgesVisible(!paintPanel.getLoopEdgesVisible());
      }
    });
    viewMenu.add(loopItem);
    JCheckBoxMenuItem eigenvectorItem = new JCheckBoxMenuItem("Show eigenvector (if strongly connected)");
    eigenvectorItem.setVisible(true);
    eigenvectorItem.setSelected(paintPanel.getLoopEdgesVisible());
    eigenvectorItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        paintPanel.setMarkovPbbVisible(!paintPanel.getMarkovPbbVisible());
      }
    });
    viewMenu.add(eigenvectorItem);

    menuBar.add(viewMenu);

    // ****************************************************************

    JMenu helpMenu = new JMenu("Help");
    JMenuItem aboutMenuItem = new JMenuItem("About");
    aboutMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        JFrame aboutFrame = new JFrame("About");
        aboutFrame.setLayout(new GridLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] strings = {"<html><b>SynchroViewer version 1.3</b><br></html>",
          "<html>A graphical application for analyzing synchronizing automata.<br></html>",
          "<html><br></html>",
          "<html>Authors (chronological order):</html>",
          "<html>Marek Szykuła (chair) 2015--2022<br></html>",
          "<html>Tomasz Jurkiewicz 2016<br></html>",
          "<html>Grzogorz Klocek 2021<br></html>",
          "<html>Michał Hetnar 2022--2023<br></html>",
          "<html><br>University of Wrocław, Institute of Computer Science<br></html>",
          "<html>GPL3 license<br></html>"};

        panel.add(new Separator());
        for (String str : strings) {
          JLabel label = new JLabel(str, JLabel.CENTER);
          label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
          panel.add(label);
        }
        panel.add(new Separator());

        aboutFrame.add(panel);
        aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        aboutFrame.pack();
        aboutFrame.setLocationRelativeTo(null);
        aboutFrame.setResizable(false);
        aboutFrame.setVisible(true);
      }
    });
    helpMenu.add(aboutMenuItem);
    menuBar.add(helpMenu);

    frame.setJMenuBar(menuBar);
  }

  private void createToolBar() {
    toolbar = new JToolBar("Toolbar");
    toolbar.setFloatable(false);
    toolbar.setBackground(new Color(195, 195, 195));

    Color noBackground = (new JButton()).getBackground();
    Color selectedButtonColor = Color.CYAN;
    ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        for (PaintPanel.Operation op : PaintPanel.Operation.values()) {
          int i = op.getValue();
          if (ev.getSource() == toolBarButtons[i]) {
            if (paintPanel.getOperation() != i) {
              toolBarButtons[paintPanel.getOperation()].setBackground(noBackground);
              paintPanel.setOperation(op);
              paintPanel.repaint();
              toolBarButtons[i].setBackground(selectedButtonColor);
            }

            if (paintPanel.getOperation() == PaintPanel.Operation.ADD_TRANS.getValue()) {
              addTransButton.setVisible(true);
              removeTransButton.setVisible(true);
              transitions.setVisible(true);
            } else {
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

    for (int i = 0; i < buttonLabels.length; i++) {
      if (i != PaintPanel.Operation.SELECT_STATES.getValue()) {
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(iconFiles[i]));
        Image image = icon.getImage();
        Image newimage = image.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimage);
        toolBarButtons[i] = new JButton(icon);
      } else {
        toolBarButtons[i] = new JButton();
        selectedColorsButton = toolBarButtons[i];
        selectedColorsButton.setIcon(createSelectedColorsIcon(paintPanel.getSelectedStateColor(),
          Config.UNSELECTED_COLOR, 40, 40));
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

    addTransButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        if (splitPane.getAutomatonK() < Config.TRANSITIONS_LETTERS.length) {
          splitPane.getProgramState().createNewTransition();
          updateTransitionsComboBox();
        } else JOptionPane.showMessageDialog(null, "Too many letters (max " + Config.TRANSITIONS_LETTERS.length + ").");
      }
    });
    removeTransButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        if (splitPane.getAutomatonK() > 0) {
          snapshots.saveSnap("Remove transitions");
          splitPane.getProgramState().removeTransition();
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
      public void actionPerformed(ActionEvent e) {
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
    JLabel selectedStatesLabel = new JLabel(Integer.toString(paintPanel.getSelectedStatesNumber()));
    Font font = label.getFont().deriveFont(Font.PLAIN, 20);
    label.setFont(font);
    selectedStatesLabel.setFont(font);
    paintPanel.addPropertyChangeListener("selectedStateColorChanged", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        int selectedStatesNumber = paintPanel.getSelectedStatesNumber();
        selectedStatesLabel.setText(Integer.toString(selectedStatesNumber));
      }
    });

    paintPanel.getProgramState().addPropertyChangeListener("automatonChanged", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        int selectedStatesNumber = paintPanel.getSelectedStatesNumber();
        selectedStatesLabel.setText(Integer.toString(selectedStatesNumber));
      }
    });
    toolbar.add(label);
    toolbar.add(selectedStatesLabel);
    toolbar.addSeparator(new Dimension(toolbar.getPreferredSize().height, 0));

    int cols = 6;
    int rows = Config.STATES_COLORS.length / cols;
    if (Config.STATES_COLORS.length % cols != 0)
      rows++;

    JPanel colorChoosersPanel = new JPanel(new GridLayout(rows, cols));
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (i * cols + j < Config.STATES_COLORS.length) {
          Color stateColor = Config.STATES_COLORS[i * cols + j];
          JButton chooseColorButton = new JButton(createIcon(stateColor, 15, 15));
          chooseColorButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent ev) {
              if (ev.getButton() == MouseEvent.BUTTON1) {
                paintPanel.setSelectedStateColor(stateColor);
                selectedColorsButton
                  .setIcon(createSelectedColorsIcon(paintPanel.getSelectedStateColor(),
                    Config.UNSELECTED_COLOR, 40, 40));
              } else if (ev.getButton() == MouseEvent.BUTTON3) {
                // paintPanel.setUnselectedStateColor(stateColor);
                // selectedColorsButton
                //         .setIcon(createSelectedColorsIcon(paintPanel.getSelectedStateColor(),
                //                 paintPanel.getUnselectedStateColor(), 40, 40));
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

  private ImageIcon createIcon(Color color, int width, int height) {
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

  private ImageIcon createSelectedColorsIcon(Color selected, Color unselected, int width, int height) {
    BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TRANSLUCENT);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(unselected);
    graphics.fillRect(width / 3, height / 3, 2 * width / 3, 2 * height / 3);
    graphics.setStroke(new BasicStroke(2));
    graphics.setColor(Color.BLACK);
    graphics.drawRect(width / 3, height / 3, 2 * width / 3, 2 * height / 3);
    graphics.setColor(selected);
    graphics.fillRect(0, 0, 2 * width / 3, 2 * height / 3);
    graphics.setStroke(new BasicStroke(2));
    graphics.setColor(Color.BLACK);
    graphics.drawRect(0, 0, 2 * width / 3, 2 * height / 3);

    // For one color only
//    graphics.setColor(selected);
//    graphics.fillRect(0, 0, 2 * width, 2 * height);
//    graphics.setStroke(new BasicStroke(2));
//    graphics.setColor(Color.BLACK);
//    graphics.drawRect(0, 0, 2 * width, 2 * height);

    graphics.dispose();
    image.flush();
    ImageIcon icon = new ImageIcon(image);
    return icon;
  }

  private void updateTransitionsComboBox() {
    int K = splitPane.getAutomatonK();
    if (transitions.getItemCount() == K - 1) {
      transitions.addItem(Character.toString(Config.TRANSITIONS_LETTERS[K - 1]));
      transitions.setSelectedIndex(K - 1);
    } else if (transitions.getItemCount() == K + 1)
      transitions.removeItemAt(K);
    else if (transitions.getItemCount() != K) {
      transitions.removeAllItems();
      for (int i = 0; i < K; i++)
        transitions.addItem(Character.toString(Config.TRANSITIONS_LETTERS[i]));
    }
  }

  public void repaint() {
    paintPanel.repaintCenterAutomaton();
  }

  private class ComboBoxRenderer extends JPanel implements ListCellRenderer<Object> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    JPanel textPanel;
    JLabel text;

    public ComboBoxRenderer(JComboBox<String> combo) {
      textPanel = new JPanel();
      text = new JLabel();
      text.setOpaque(true);
      textPanel.add(text);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      if (value == null)
        return text;

      if (isSelected)
        setBackground(list.getSelectionBackground());
      else
        setBackground(Color.WHITE);

      text.setBackground(getBackground());

      text.setText(value.toString());
      if (index > -1)
        text.setForeground(Config.getTransitionColor(index));

      return text;
    }
  }
}
