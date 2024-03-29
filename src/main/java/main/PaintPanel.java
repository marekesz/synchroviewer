package main;

import algorithms.Rational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class PaintPanel extends JPanel implements MouseListener, MouseMotionListener {

  private static final long serialVersionUID = 1L;
  // ************************************************************************
  // Content

  private boolean showRange;
  private boolean showAction;
  private int[] rangeStates;
  private HashMap<Integer, ArrayList<Integer>> actionStates;
  private Snapshots automatonSnapshots;

  private class Transition {
    public int stateOut;
    public int stateIn;
    public int k;
    boolean inverse;

    public Transition(int stateOut, int stateIn, int k, boolean inverse) {
      this.stateOut = stateOut;
      this.stateIn = stateIn;
      this.k = k;
      this.inverse = inverse;
    }
  }

  public enum Operation {
    MOVE_STATES(0), ADD_STATES(1), REMOVE_STATES(2), SWAP_STATES(3), ADD_TRANS(4), SELECT_STATES(5);

    private final int value;

    private Operation(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public void setAutomatonSnapshots(Snapshots automatonSnapshots) {
    this.automatonSnapshots = automatonSnapshots;
  }

  private ProgramState programState;
  private int[] orders;
  private int highlighted;

  private Operation operation;
  private Color selectedStateColor;
  private int selectedTransition;
  private int addTransFirstState;
  private int swapStatesFirstState;

  private int grabbed;
  private int grabX, grabY, grabShiftX, grabShiftY;
  private int oldWidth;
  private int oldHeight;

  private boolean verboseDrawing;
  private boolean showMarkovPbb;

  public Point2D.Double[] getVertices() {
    return programState.vertices;
  }

  public void setVerticesAt(Point2D.Double vertices, int n) {
    this.programState.vertices[n] = new Point2D.Double(vertices.x, vertices.y);
  }

  // ************************************************************************
  // Initialization
  public PaintPanel(ProgramState programState) {
    setMinimumSize(new Dimension(550, 400));

    this.grabbed = -1;
    this.highlighted = -1;
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.operation = Operation.MOVE_STATES;
    this.selectedStateColor = Config.DEFAULT_SELECTED_STATE_COLOR;
    this.oldWidth = 0;
    this.oldHeight = 0;
    this.verboseDrawing = false;
    this.showMarkovPbb = false;
    showRange = false;
    showAction = false;

    setProgramState(programState);

    programState.addPropertyChangeListener("automatonReset", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        updateProgramState();
        repaintCenterAutomaton();
        firePropertyChange("updateTransitions", false, true);
      }
    });

    programState.addPropertyChangeListener("automatonChanged", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent ev) {
        repaint();
      }
    });

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent ev) {
        int width = PaintPanel.this.getSize().width;
        int height = PaintPanel.this.getSize().height;

        for (Point2D.Double vertex : programState.vertices) {
          if (PaintPanel.this.oldWidth != 0) {
            vertex.x = (int) (vertex.x * (double) width / (double) PaintPanel.this.oldWidth);
            vertex.y = (int) (vertex.y * (double) height / (double) PaintPanel.this.oldHeight);
          }

          if (vertex.x + Config.VERTEX_RADIUS > width)
            vertex.x = width - Config.VERTEX_RADIUS;

          if (vertex.y + Config.VERTEX_RADIUS > height)
            vertex.y = height - Config.VERTEX_RADIUS;

          if (vertex.x - Config.VERTEX_RADIUS < 0)
            vertex.x = Config.VERTEX_RADIUS;

          if (vertex.y - Config.VERTEX_RADIUS < 0)
            vertex.y = Config.VERTEX_RADIUS;

        }

        PaintPanel.this.oldWidth = width;
        PaintPanel.this.oldHeight = height;
        repaint();
      }
    });
  }

  private int getN() {
    return programState.dfa.getN();
  }

  private int getK() {
    return programState.dfa.getK();
  }

  public int getOperation() {
    return this.operation.getValue();
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  public Color getSelectedStateColor() {
    return selectedStateColor;
  }

  public int getSelectedStatesNumber() {
    return programState.getSelectedStatesNumber(getColorId(selectedStateColor));
  }

  public ProgramState getProgramState() {
    return programState;
  }

  public void selectedStateColorChanged() {
    firePropertyChange("selectedStateColorChanged", true, false);
  }

  public int getColorId(Color color) {
    for (int c = 0; c < Config.STATES_COLORS.length; c++)
      if (color.equals(Config.STATES_COLORS[c]))
        return c;
    return 0;
  }

  public void setSelectedStateColor(Color color) {

    if (color.equals(Config.UNSELECTED_COLOR)) {
      JOptionPane.showMessageDialog(this, "Color 1 and Color 2 must be different.");
      return;
    }

    if (!color.equals(selectedStateColor)) {
      selectedStateColor = color;

      selectedStateColorChanged();
      repaint();
    }
  }

  public void setSelectedTransition(int trans) {
    selectedTransition = trans;
  }

  public void resetReplaceStatesFirstState() {
    swapStatesFirstState = -1;
  }

  public void setShowRange(boolean showRange) {
    this.showRange = showRange;
    repaint();
  }

  public void setShowAction(boolean showAction) {
    this.showAction = showAction;
    repaint();
  }

  public void showRange(int[] states) {
    showRange = true;
    rangeStates = states;
    repaint();
  }

  public void showAction(HashMap<Integer, ArrayList<Integer>> actionStates) {
    showAction = true;
    this.actionStates = actionStates;
    repaint();
  }

  public boolean getLoopEdgesVisible() {
    return this.verboseDrawing;
  }

  public boolean getMarkovPbbVisible() {
    return this.showMarkovPbb;
  }

  public void setMarkovPbbVisible(boolean enabled) {
    this.showMarkovPbb = enabled;
    repaint();
  }

  public void setLoopEdgesVisible(boolean visible) {
    this.verboseDrawing = visible;
    repaint();
  }

  private void setProgramState(ProgramState programState) {
    this.programState = programState;
    int N = getN();

    programState.vertices = new Point2D.Double[N];
    this.orders = new int[N];
    this.highlighted = -1;
    this.addTransFirstState = -1;
    this.swapStatesFirstState = -1;
    boolean[] selectedStates = new boolean[N];
    for (int n = 0; n < N; n++) {
      orders[n] = n;
      selectedStates[n] = true;
    }

    programState.selectStates(selectedStates);
    repaintCenterAutomaton();
  }

  public void updateProgramState() {
    int N = getN();
    programState.vertices = new Point2D.Double[N];
    this.orders = new int[N];
    this.highlighted = -1;
    this.addTransFirstState = -1;
    this.swapStatesFirstState = -1;
    for (int n = 0; n < N; n++) {
      orders[n] = n;
    }

    programState.clearSelectedStates();
  }

  // repaint automaton with states moved to center
  public void repaintCenterAutomaton() {
    int width = this.getWidth();
    int height = this.getHeight();
    int r = ((width < height ? width : height) - Config.VERTEX_RADIUS * 3) / 2;
    int cx = width / 2;
    int cy = height / 2;
    double angle = 0.0;
    int N = getN();
    for (int n = 0; n < N; n++) {
      programState.vertices[n] = new Point2D.Double((Math.sin(angle) * r + cx), (-Math.cos(angle) * r + cy));
      angle += 2 * Math.PI / N;
    }
    repaint();
  }

  // ************************************************************************
  // Interface
  @Override
  public void mouseMoved(MouseEvent ev) {
    grabX = (ev.getX() >= 0 && ev.getX() <= this.getSize().width) ? ev.getX() : grabX;
    grabY = (ev.getY() >= 0 && ev.getY() <= this.getSize().height) ? ev.getY() : grabY;
    highlighted = -1;
    for (int i = getN() - 1; i >= 0; i--) {
      int v = orders[i];
      double d = (grabX - programState.vertices[v].x) * (grabX - programState.vertices[v].x)
        + (grabY - programState.vertices[v].y) * (grabY - programState.vertices[v].y);
      if (d <= Config.VERTEX_RADIUS * Config.VERTEX_RADIUS) {
        highlighted = v;
        break;
      }
    }
    repaint();
  }

  @Override
  public void mouseDragged(MouseEvent ev) {
    grabX = (ev.getX() >= 0 && ev.getX() <= this.getSize().width) ? ev.getX() : grabX;
    grabY = (ev.getY() >= 0 && ev.getY() <= this.getSize().height) ? ev.getY() : grabY;
    mouseMoved(ev);

    if (grabbed == -1)
      return;

    if (operation == Operation.MOVE_STATES) {
      if (grabX + grabShiftX >= Config.VERTEX_RADIUS && grabX + grabShiftX <= this.getSize().width - Config.VERTEX_RADIUS)
        programState.vertices[grabbed].x = grabX + grabShiftX;
      if (grabY + grabShiftY >= Config.VERTEX_RADIUS && grabY + grabShiftY <= this.getSize().height - Config.VERTEX_RADIUS)
        programState.vertices[grabbed].y = grabY + grabShiftY;
      repaint();
    }
  }

  @Override
  public void mousePressed(MouseEvent ev) {
    if (grabbed != -1)
      return;

    mouseMoved(ev);
    if (ev.getButton() == MouseEvent.BUTTON1) {
      if (operation == Operation.ADD_STATES) {
        automatonSnapshots.saveSnap("Add states");
        programState.addState();
        int N = getN();

        this.orders = new int[N];
        for (int n = 0; n < N; n++)
          this.orders[n] = n;

        Point2D.Double[] temp2 = new Point2D.Double[N];
        System.arraycopy(this.programState.vertices, 0, temp2, 0, N - 1);
        temp2[N - 1] = new Point2D.Double(ev.getPoint().getX(), ev.getPoint().getY());
        this.programState.vertices = temp2;
      } else if (highlighted >= 0 && operation == Operation.REMOVE_STATES) {
        automatonSnapshots.saveSnap("Remove states");
        int N = getN();
        this.orders = new int[N - 1];
        for (int n = 0; n < N - 1; n++)
          this.orders[n] = n;

        Point2D.Double[] temp2 = new Point2D.Double[N - 1];
        for (int n = 0; n < N - 1; n++) {
          if (n < highlighted) {
            temp2[n] = this.programState.vertices[n];
          } else {
            temp2[n] = this.programState.vertices[n + 1];
          }
        }
        this.programState.vertices = temp2;

        programState.removeState(highlighted);
        highlighted = -1;
      } else if (highlighted >= 0 && operation == Operation.SWAP_STATES) {
        automatonSnapshots.saveSnap("Swap states");
        swapStatesFirstState = highlighted;
      } else if (highlighted >= 0 && operation == Operation.ADD_TRANS) {
        automatonSnapshots.saveSnap("Add transitions");
        addTransFirstState = highlighted;
      } else if (highlighted >= 0 && operation == Operation.SELECT_STATES) {
        automatonSnapshots.saveSnap("Select states");
        if (programState.isSelected(highlighted, getColorId(selectedStateColor)))
          programState.unselectState(highlighted, getColorId(selectedStateColor));
        else
          programState.selectState(highlighted, getColorId(selectedStateColor));
      } else if (highlighted >= 0) {
        automatonSnapshots.saveSnap("Move states");
        grabShiftX = (int) (programState.vertices[highlighted].x - grabX);
        grabShiftY = (int) (programState.vertices[highlighted].y - grabY);
        grabbed = highlighted;
        int N = getN();
        int i = N - 1;
        while (orders[i] != highlighted)
          i--;

        for (int j = i; j < N - 1; j++)
          orders[j] = orders[j + 1];

        orders[N - 1] = highlighted;
      }
    } else if (ev.getButton() == MouseEvent.BUTTON3) {

      if (highlighted >= 0 && operation == Operation.SELECT_STATES) {
        automatonSnapshots.saveSnap("Select states");
        for (int i = 0; i < Config.STATES_COLORS.length; i++) {
          programState.unselectState(highlighted, i);
        }
      }
    }

    repaint();
  }

  @Override
  public void mouseReleased(MouseEvent ev) {
    if (operation == Operation.ADD_TRANS) {
      if (addTransFirstState >= 0 && highlighted >= 0 && getK() > 0) {
        programState.addTransition(addTransFirstState, highlighted, selectedTransition);
        firePropertyChange("updateTransitions", false, true);
      }
      addTransFirstState = -1;
    } else if (operation == Operation.SWAP_STATES) {
      if (swapStatesFirstState >= 0 && highlighted >= 0 && swapStatesFirstState != highlighted) {
        programState.replaceStates(swapStatesFirstState, highlighted);
        Point2D.Double temp = programState.vertices[highlighted];
        programState.vertices[highlighted] = programState.vertices[swapStatesFirstState];
        programState.vertices[swapStatesFirstState] = temp;
        int temp2 = orders[highlighted];
        orders[highlighted] = orders[swapStatesFirstState];
        orders[swapStatesFirstState] = temp2;
      }
      swapStatesFirstState = -1;
    } else {
      grabbed = -1;
      mouseMoved(ev);
    }

    repaint();
  }

  private void drawCenteredString(Graphics g, String text, int x, int y) {
    FontMetrics metrics = g.getFontMetrics();
    x = x - metrics.stringWidth(text) / 2;
    y = y + metrics.getAscent() / 2;
    g.drawString(text, x, y);
  }

  private void drawRotatedString(Graphics2D g2d, double x, double y, double angle, String text) {
    g2d.translate((float) x, (float) y);
    g2d.rotate(angle);
    drawCenteredString(g2d, text, 0, 0);
    g2d.rotate(-angle);
    g2d.translate(-(float) x, -(float) y);
  }

  private void drawRotatedOval(Graphics2D g2d, double x, double y, double angle) {
    int height = 30;
    int width = 60;
    g2d.translate((float) x, (float) y);
    g2d.rotate(angle);
    g2d.drawOval(0, -height / 2, width, height);
    g2d.rotate(-angle);
    g2d.translate(-(float) x, -(float) y);
  }

  private double getNewFarAngle(ArrayList<Double> angles) {
    if (angles.size() == 0)
      return 0;
    Collections.sort(angles);
    int id = 0;
    double biggestDist = 0;
    for (int i = 0; i < angles.size() - 1; i++) {
      if (angles.get(i + 1) - angles.get(i) > biggestDist) {
        id = i;
        biggestDist = angles.get(i + 1) - angles.get(i);
      }
    }

    if (2 * Math.PI - (angles.get(angles.size() - 1) - angles.get(0)) > biggestDist) {
      id = angles.size() - 1;
      biggestDist = 2 * Math.PI - (angles.get(angles.size() - 1) - angles.get(0));
    }

    double result;
    if (id == angles.size() - 1) {
      result = (angles.get(id) + angles.get(0)) / 2 + Math.PI;
    } else
      result = (angles.get(id) + angles.get(id + 1)) / 2;

    if (result > Math.PI) {
      result -= 2 * Math.PI;
    }
    return result;
  }

  private double getAngle(int x1, int y1, int x2, int y2) {
    double dx = x2 - x1, dy = y2 - y1;
    double angle = Math.atan2(dy, dx);
    return angle;
  }

  // ************************************************************************
  // Drawing
  void drawEdge(Graphics2D g, int x1, int y1, int x2, int y2, int letterId, int k, int transQuantity,
                double loopAngle, boolean inverse, boolean marked, boolean centered) {
    final double K_SHIFT = (Config.VERTEX_RADIUS * 2) / (transQuantity + 1);
    AffineTransform oldTransform = g.getTransform();
    boolean isLoop = (x1 == x2 && y1 == y2);
    double dx = x2 - x1, dy = y2 - y1;
    double angle = (isLoop) ? loopAngle : Math.atan2(dy, dx);
    int len = (centered) ? (int) Math.sqrt(dx * dx + dy * dy) : (int) Math.sqrt(dx * dx + dy * dy) - Config.VERTEX_RADIUS;
    AffineTransform at = new AffineTransform(oldTransform);
    at.translate(x1, y1);
    at.concatenate(AffineTransform.getRotateInstance(angle));
    g.setTransform(at);

    int yshift = (int) ((k - transQuantity * 0.5) * K_SHIFT + K_SHIFT / 2);
    if (marked) {
      float dash1[] = {8.0f, 14.0f};
      final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1,
        0.0f);
      g.setStroke(dashed);
    }
    if (isLoop && this.verboseDrawing) {
      drawRotatedOval(g, 0, 0, 0);
      drawRotatedString(g, Config.VERTEX_RADIUS + 45, 0, -angle,
        Character.toString(Config.TRANSITIONS_LETTERS[letterId]));
      double theta1 = Math.PI / (double) 5;
      g.rotate(theta1);
      g.translate(Config.VERTEX_RADIUS, 0);
      double theta2 = Math.PI / (double) 5;
      g.rotate(-theta2);
      g.fillPolygon(new int[]{0, 0 + Config.ARROW_SIZE, 0 + Config.ARROW_SIZE}, new int[]{0, -Config.ARROW_SIZE / 2, Config.ARROW_SIZE / 2}, 3);
      g.rotate(theta2);
      g.translate(-Config.VERTEX_RADIUS, 0);
      g.rotate(-theta1);
    } else if (!isLoop) {
      g.drawLine(0, yshift, len, yshift);
      if (this.verboseDrawing) {
        drawRotatedString(g, (double) (len + Config.VERTEX_RADIUS) / (double) 2 - 10, (double) yshift - 10, -angle,
          Character.toString(Config.TRANSITIONS_LETTERS[letterId]));
      }
      g.setStroke(new BasicStroke());
      g.fillPolygon(new int[]{len, len - Config.ARROW_SIZE, len - Config.ARROW_SIZE},
        new int[]{yshift, -Config.ARROW_SIZE / 2 + yshift, Config.ARROW_SIZE / 2 + yshift}, 3);

      if (inverse) {
        g.fillPolygon(new int[]{Config.VERTEX_RADIUS, Config.VERTEX_RADIUS + Config.ARROW_SIZE, Config.VERTEX_RADIUS + Config.ARROW_SIZE},
          new int[]{yshift, -Config.ARROW_SIZE / 2 + yshift, Config.ARROW_SIZE / 2 + yshift}, 3);
      }
    }
    g.setTransform(oldTransform);
  }

  @Override
  public void paint(Graphics graphics) {
    Graphics2D g = (Graphics2D) graphics;
    int width = this.getWidth();
    int height = this.getHeight();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g.setFont(new Font("sans", Font.PLAIN, 14));
    int[][] matrix = programState.dfa.getMatrix();
    int N = getN();
    int K = getK();

    Rational[] markovPbbs = null;
    if (this.showMarkovPbb)
      markovPbbs = this.programState.getEigenVector();

    // draw oval of range states at the beginning
    ArrayList<ArrayList<Double>> anglesPerNode = new ArrayList<>();
    for (int i = 0; i < N; i++) {
      anglesPerNode.add(new ArrayList<>());
      int n = orders[i];
      if (showRange && rangeStates[n] == 1) {
        g.setStroke(new BasicStroke(5));
        g.setColor(selectedStateColor);
        if (highlighted != -1 && highlighted == n)
          g.setColor(g.getColor().brighter());
        g.drawOval((int) programState.vertices[n].x - Config.VERTEX_RADIUS - 3, (int) programState.vertices[n].y - Config.VERTEX_RADIUS - 3,
          (Config.VERTEX_RADIUS * 2) + 6, (Config.VERTEX_RADIUS * 2) + 6);
        g.setStroke(new BasicStroke());
        if (highlighted != -1 && highlighted == n)
          g.setColor(Color.LIGHT_GRAY);
        else
          g.setColor(Color.BLACK);
        g.drawOval((int) programState.vertices[n].x - Config.VERTEX_RADIUS - 4, (int) programState.vertices[n].y - Config.VERTEX_RADIUS - 4,
          (Config.VERTEX_RADIUS * 2) + 8, (Config.VERTEX_RADIUS * 2) + 8);
      }
    }

    g.setColor(Color.BLACK);
    g.setStroke(new BasicStroke());

    // prepare angles for non-inverse transitions - needed to set angles for loops
    // between them
    double angle = 0;
    for (int n1 = 0; n1 < N; n1++) {
      for (int n2 = 0; n2 < N; n2++) {
        for (int k = 0; k < K; k++) {
          if (n1 != n2 && matrix[n1][k] == n2) {
            angle = getAngle((int) programState.vertices[n1].x, (int) programState.vertices[n1].y, (int) programState.vertices[n2].x,
              (int) programState.vertices[n2].y);
            anglesPerNode.get(n1).add(angle);
            angle = getAngle((int) programState.vertices[n2].x, (int) programState.vertices[n2].y, (int) programState.vertices[n1].x,
              (int) programState.vertices[n1].y);
            anglesPerNode.get(n2).add(angle);
            break;
          }
        }
      }
    }

    // draw transitions
    for (int n = 0; n < N; n++) {
      for (int n2 = n; n2 < N; n2++) {
        ArrayList<Transition> transitions = new ArrayList<>();
        for (int k = 0; k < K; k++) {
          if (matrix[n][k] == n2)
            transitions.add(new Transition(n, n2, k, matrix[n2][k] == n));
        }
        for (int k = 0; k < K; k++) {
          if (matrix[n2][k] == n && matrix[n][k] != n2)
            transitions.add(new Transition(n2, n, k, false));
        }

        if (showAction) {
          if (actionStates.getOrDefault(n, new ArrayList<>()).contains(n2)) {
            if (actionStates.getOrDefault(n2, new ArrayList<>()).contains(n))
              transitions.add(new Transition(n, n2, -1, true));
            else
              transitions.add(new Transition(n, n2, -1, false));
          } else if (actionStates.getOrDefault(n2, new ArrayList<>()).contains(n))
            transitions.add(new Transition(n2, n, -1, false));
        }

        int transNumber = transitions.size();
        for (int i = 0; i < transNumber; i++) {
          Transition trans = transitions.get(i);
          int j = (trans.stateOut == n) ? i : transNumber - i - 1;
          if (trans.k == -1) {
            g.setStroke(new BasicStroke(2));
            g.setColor(Color.BLACK);
          } else {
            g.setStroke(new BasicStroke());
            g.setColor(Config.getTransitionColor(trans.k));
          }
          if (trans.inverse) {
            angle = getNewFarAngle(anglesPerNode.get(trans.stateOut));
            anglesPerNode.get(trans.stateOut).add(angle);
          }
          drawEdge(g, (int) programState.vertices[trans.stateOut].x, (int) programState.vertices[trans.stateOut].y,
            (int) programState.vertices[trans.stateIn].x, (int) programState.vertices[trans.stateIn].y, trans.k, j, transNumber,
            angle, trans.inverse, false, false);
        }
      }
    }

    // draw new transition when you are in ADD_TRANS mode
    if (addTransFirstState >= 0 && operation == Operation.ADD_TRANS && K > 0) {
      g.setColor(Config.getTransitionColor(selectedTransition));
      drawEdge(g, (int) programState.vertices[addTransFirstState].x, (int) programState.vertices[addTransFirstState].y, grabX, grabY,
        selectedTransition, 0, 1, 0, false, true, true);
    } else if (swapStatesFirstState >= 0 && operation == Operation.SWAP_STATES) {
      g.setColor(new Color(0, 0, 0, 0.1f));
      g.setStroke(new BasicStroke(2));
      drawEdge(g, (int) programState.vertices[swapStatesFirstState].x, (int) programState.vertices[swapStatesFirstState].y, grabX, grabY,
        selectedTransition, 0, 1, 0, true, false, true);
    }

    // draw states
    for (int i = 0; i < N; i++) {
      int n = orders[i];
      if (programState.isSelectedByAnyColor(n)) {
        g.setColor(selectedStateColor);
      }
      if (highlighted != -1 && highlighted == n)
        g.setColor(g.getColor().brighter());

      fillMultiColorState(g, (int) (programState.vertices[n].x - Config.VERTEX_RADIUS), (int) (programState.vertices[n].y - Config.VERTEX_RADIUS),
        Config.VERTEX_RADIUS * 2, Config.VERTEX_RADIUS * 2, getColorsOf(n));

      g.setColor(Color.BLACK);
      if (highlighted != -1 && highlighted == n)
        g.setColor(Color.LIGHT_GRAY);

      if (operation == Operation.SWAP_STATES && swapStatesFirstState == n) {
        g.setColor(Color.RED.darker());
        g.setStroke(new BasicStroke(4));
      }

      g.drawOval((int) (programState.vertices[n].x - Config.VERTEX_RADIUS), (int) (programState.vertices[n].y - Config.VERTEX_RADIUS), Config.VERTEX_RADIUS * 2,
        Config.VERTEX_RADIUS * 2);
      g.setStroke(new BasicStroke());
      g.setColor(Color.BLACK);
      String label = Integer.toString(n);
      if (!Objects.isNull(markovPbbs) && this.showMarkovPbb) {
        g.drawString(label, (int) (programState.vertices[n].x - g.getFontMetrics().stringWidth(label) / 2),
          (int) (programState.vertices[n].y - 5));
        String pbbl = markovPbbs[n].toString();
        g.drawString(pbbl, (int) (programState.vertices[n].x - g.getFontMetrics().stringWidth(pbbl) / 2),
          (int) (programState.vertices[n].y + 15));
      } else {
        g.drawString(label, (int) (programState.vertices[n].x - g.getFontMetrics().stringWidth(label) / 2),
          (int) (programState.vertices[n].y + 5));
      }
    }

  }

  private ArrayList<Color> getColorsOf(int state) {
    ArrayList<Color> result = new ArrayList<>();
    if (!programState.isSelectedByAnyColor(state)) {
      result.add(Config.UNSELECTED_COLOR);
      return result;
    }

    for (int c = 0; c < Config.STATES_COLORS.length; c++) {
      if (programState.isSelected(state, c))
        result.add(Config.STATES_COLORS[c]);
    }
    return result;
  }

  private void fillMultiColorState(Graphics g, int x, int y, int width, int height, ArrayList<Color> colors) {
    if (colors.size() == 0)
      return;
    float angle = 360 / colors.size();
    float rest = 360 % colors.size();
    Color oldColor = g.getColor();
    int startAngle = 0;
    for (int i = 0; i < colors.size(); i++) {
      g.setColor(colors.get(i));
      g.fillArc(x, y, width, height, (int) Math.round(startAngle + 0.5f),
        (int) Math.round(angle + (rest > 0 ? 1 : 0)));
      rest--;
      startAngle += angle;
    }
    g.setColor(oldColor);
  }

  @Override
  public void mouseClicked(MouseEvent ev) {
  }

  @Override
  public void mouseEntered(MouseEvent ev) {
  }

  @Override
  public void mouseExited(MouseEvent ev) {
  }
}
