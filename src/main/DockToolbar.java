
package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import models.Automaton;

public abstract class DockToolbar extends JToolBar {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel; // content panel
    private final String name;
    private final boolean visibleOnStart;
    private Automaton automaton;

    private boolean floating;
    private Dimension dockSize;
    private Dimension floatSize;
    private TitledBorder titledBorder;

    DockToolbar(String name, boolean visibleOnStart, Automaton automaton) {
        super(name);
        this.name = name;
        this.visibleOnStart = visibleOnStart;
        this.automaton = automaton;
        floating = false;
        setLayout(new BorderLayout());
        setOrientation(javax.swing.SwingConstants.HORIZONTAL);

        this.titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), name);
        panel = new JPanel(new BorderLayout());
        panel.setBorder(titledBorder);
        add(panel);

        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (SwingUtilities.getWindowAncestor(DockToolbar.this) instanceof JDialog) {
                    floating = true;
                    JDialog toolBarDialog = (JDialog) SwingUtilities.getWindowAncestor(DockToolbar.this);
                    toolBarDialog.setResizable(true);
                    if (dockSize == null)
                        dockSize = floatSize = panel.getSize();
                    panel.setSize(floatSize);
                    DockToolbar.this.setVisible(false);
                    DockToolbar.this.remove(panel);
                    toolBarDialog.add(panel);
                    toolBarDialog.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent ev) {
                            toolBarDialog.setPreferredSize(toolBarDialog.getSize());
                        }
                    });
                    toolBarDialog.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            floating = false;
                            toolBarDialog.remove(panel);
                            DockToolbar.this.setVisible(true);
                            DockToolbar.this.add(panel);
                            floatSize = DockToolbar.this.getSize();
                            panel.setSize(dockSize);
                        }
                    });
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        automaton.addPropertyChangeListener("automatonChanged", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                updateToolbar();
            }
        });
    }

    protected JPanel getPanel() {
        return panel;
    }

    public void setTitle(String title) {
        this.titledBorder.setTitle(title);
        panel.setBorder(this.titledBorder);
        repaint();
    }

    @Override
    public String getName() {
        return name;
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    @Override
    public void setVisible(boolean b) {
        boolean oldValue = isVisible();
        super.setVisible(b);

        if (isVisible())
            update();

        firePropertyChange("setVisible", oldValue, b);
    }

    public void Dock() {
        if (floating) {
            JDialog toolBarDialog = (JDialog) SwingUtilities.getWindowAncestor(this);
            toolBarDialog.dispatchEvent(new WindowEvent(toolBarDialog, WindowEvent.WINDOW_CLOSING));
        }
    }

    public Font getDeafultFont() {
        return new Font(Font.MONOSPACED, Font.ITALIC + Font.BOLD, 14);
        //return new Font(Font.SANS_SERIF, Font.ITALIC + Font.BOLD, 14);
    }

    public boolean isVisibleOnStart() {
        return visibleOnStart;
    }

    // updates toolbar only if it is visible
    public void updateToolbar() {
        if (isVisible() || (floating && panel.isVisible()))
            update();
    }

    protected abstract void update();
}
