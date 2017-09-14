
import java.awt.Dimension;
import javax.swing.JFrame;

public class SynchroViewer
{

    public static void main(String[] args) 
    {
        JFrame frame = new JFrame("Synchro Viewer");
        Viewer.SynchroViewer synchroViewer = new Viewer.SynchroViewer(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300,750);
        frame.setMinimumSize(new Dimension(700, 525));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        synchroViewer.repaint();
    }
}
