package main;

import java.awt.*;

public abstract class Config {
  public static final int MAX_STATES_FOR_EXPONENTIAL_ALGORITHMS = 25;

  public static final char[] TRANSITIONS_LETTERS = {
    'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'
  };

  private static final Color[] TRANSITIONS_COLORS = {Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.PINK,
    Color.GRAY, Color.CYAN, Color.BLACK, Color.YELLOW, Color.MAGENTA};

  public static Color getTransitionColor(int index) {
    return TRANSITIONS_COLORS[index % TRANSITIONS_COLORS.length];
  }

  public static final Color UNSELECTED_COLOR = Color.WHITE;
  public static final Color[] STATES_COLORS = {
    new Color(96, 128, 255),
    new Color(255, 128, 96),
    new Color(96, 255, 96),
    new Color(255, 255, 96),
    new Color(96, 255, 255),
    new Color(255, 96, 255),
    new Color(255, 128, 0),
    new Color(160, 0, 210),
    new Color(0, 128, 0),
    new Color(128, 128, 128),
    new Color(255, 64, 64),
    new Color(64, 0, 255)
  };
  public static final Color DEFAULT_SELECTED_STATE_COLOR = STATES_COLORS[0];

  public static final int VERTEX_RADIUS = 25;
  public static final int ARROW_SIZE = 10;

  public static final int MIN_TOOLBAR_WIDTH = 500;
}
