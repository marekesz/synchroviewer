
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SynchroViewer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
	    	JFrame frame = new JFrame("Synchro Viewer");
	        main.MainFrame synchroViewer = new main.MainFrame(frame);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setSize(1300, 750);
	        frame.setMinimumSize(new Dimension(700, 525));
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
	        synchroViewer.repaint();
        }});
    }
}
