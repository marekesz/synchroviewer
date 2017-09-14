
package Viewer;

import java.awt.Color;


public abstract class AutomatonHelper
{
    public static final Color defaultUnselectedStateColor = Color.WHITE;
    
    public static final Color defaultSelectedStateColor = Color.ORANGE;
    
    public static final char[] TRANSITIONS_LETTERS =
    {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'
    };
    
    public static final Color[] TRANSITIONS_COLORS =
    {
        Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE,
        Color.PINK, Color.GRAY, Color.CYAN, Color.BLACK,
        Color.YELLOW, Color.MAGENTA
    };
    
    private AutomatonHelper() {}
}