package models;


import main.Config;

import java.awt.geom.Point2D;

public class SnapshotEvent {
  public String name;
  public DFA dfa;
  public Point2D.Double[] vertices;
  public boolean[][] selectedStatesByColor;

  public SnapshotEvent(String name, String automatoncode, Point2D.Double[] vertices, boolean[][] selectedStatesByColor) {
    this.name = name;

    this.dfa = new DFA(automatoncode);

    this.vertices = new Point2D.Double[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      this.vertices[i] = new Point2D.Double(vertices[i].x, vertices[i].y);
    }
    this.selectedStatesByColor = new boolean[Config.STATES_COLORS.length][this.dfa.getN()];
    for (int i = 0; i < selectedStatesByColor.length; i++)
      for (int j = 0; j < selectedStatesByColor[i].length; j++)
        this.selectedStatesByColor[i][j] = selectedStatesByColor[i][j];
  }
}
