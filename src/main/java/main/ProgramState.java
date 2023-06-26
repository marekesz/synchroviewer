package main;

import algorithms.Connectivity;
import algorithms.MarkovChains;
import algorithms.Rational;
import models.DFA;
import models.InverseDFA;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

public class ProgramState {
  public DFA dfa;
  public Point2D.Double[] vertices;
  private final PropertyChangeSupport PCS;
  public boolean[][] selectedStatesByColor;// [color][state]
  private Rational[] probabilityDistribution;

  private Rational[] eigenVector;

  public ProgramState(DFA dfa) {
    this.dfa = dfa;
    PCS = new PropertyChangeSupport(this);

    resetSelectedStatesByColor();
    resetProbabilityDistribution();
    resetEigenVector();
  }

  public void update(DFA automaton) {
    dfa.update(automaton);
    resetSelectedStatesByColor();
    resetProbabilityDistribution();
    resetEigenVector();
  }

  public void addState() {
    dfa.addState();
    int N = dfa.getN() - 1;
    boolean[][] newSelectedStatesByColor = new boolean[Config.STATES_COLORS.length][N + 1];
    for (int i = 0; i < N; i++)
      for (int c = 0; c < Config.STATES_COLORS.length; c++)
        newSelectedStatesByColor[c][i] = selectedStatesByColor[c][i];
    selectedStatesByColor = newSelectedStatesByColor;
    automatonChanged();
    resetEigenVector();
  }

  public void removeState(int state) {
    dfa.removeState(state);
    int N = dfa.getN() + 1;
    boolean[][] newSelectedStatesByColor = new boolean[Config.STATES_COLORS.length][N - 1];
    for (int i = 0; i < N - 1; i++) {
      if (i < state) {
        for (int c = 0; c < Config.STATES_COLORS.length; c++)
          newSelectedStatesByColor[c][i] = selectedStatesByColor[c][i];
      } else {
        for (int c = 0; c < Config.STATES_COLORS.length; c++)
          newSelectedStatesByColor[c][i] = selectedStatesByColor[c][i + 1];
      }
    }
    selectedStatesByColor = newSelectedStatesByColor;
    automatonChanged();
    resetEigenVector();
  }

  public void replaceStates(int state1, int state2) {
    dfa.replaceStates(state1, state2);
    automatonChanged();
    resetEigenVector();
  }

  public void setDfa(DFA dfa) {
    this.dfa = dfa;
    resetSelectedStatesByColor();
    resetProbabilityDistribution();
    resetEigenVector();
  }

  public void resetSelectedStatesByColor() {
    int N = dfa.getN();
    selectedStatesByColor = new boolean[Config.STATES_COLORS.length][N];
    for (int c = 0; c < Config.STATES_COLORS.length; c++)
      for (int i = 0; i < N; i++)
        selectedStatesByColor[c][i] = (c == 0);
  }

  private void resetEigenVector() {
    this.eigenVector = Connectivity.isStronglyConnected(dfa, new InverseDFA(dfa))
      ? MarkovChains.getStationaryDistribution(dfa, probabilityDistribution)
      : null;
  }

  public void createNewTransition() {
    dfa.createNewTransition();
    resetProbabilityDistribution();
    resetEigenVector();
    automatonChanged();
  }

  public void selectState(int state, int color) {
    selectedStatesByColor[color][state] = true;
    automatonChanged();
    resetEigenVector();
  }

  public void addTransition(int out, int in, int k) {
    dfa.addTransition(out, in, k);
    resetEigenVector();
    automatonChanged();
  }

  public void automatonChanged() {
    resetEigenVector();
    PCS.firePropertyChange("automatonChanged", false, true);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
    PCS.addPropertyChangeListener(propertyName, propertyChangeListener);
  }

  public void removeTransition() {
    dfa.removeTransition();
    resetProbabilityDistribution();
    resetEigenVector();
    automatonChanged();
  }

  public void selectStates(boolean[] selectedStates) {
    if (selectedStates.length == dfa.getN()) {
      this.selectedStatesByColor[0] = selectedStates;
      automatonChanged();
      resetEigenVector();

    }
  }

  public void selectStates(boolean[] selectedStates, int color) {
    if (selectedStates.length == dfa.getN()) {
      this.selectedStatesByColor[color] = selectedStates;
      automatonChanged();
      resetEigenVector();
    }
  }

  public void unselectState(int state) {
    for (int c = 0; c < Config.STATES_COLORS.length; c++)
      selectedStatesByColor[c][state] = false;
    automatonChanged();
    resetEigenVector();
  }

  public void unselectState(int state, int color) {
    selectedStatesByColor[color][state] = false;
    automatonChanged();
    resetEigenVector();
  }

  public void clearSelectedStates() {
    for (int c = 0; c < Config.STATES_COLORS.length; c++)
      Arrays.fill(selectedStatesByColor[c], false);
    automatonChanged();
    resetEigenVector();
  }

  public boolean isSelected(int state, int color) {
    return selectedStatesByColor[color][state];
  }

  public boolean isSelectedByAnyColor(int state) {
    for (int c = 0; c < Config.STATES_COLORS.length; c++)
      if (selectedStatesByColor[c][state])
        return true;
    return false;
  }

  public int getSelectedStatesNumber() {
    int count = 0;
    for (boolean i : selectedStatesByColor[0]) {
      if (i)
        count++;
    }
    return count;
  }

  public int getSelectedStatesNumber(int c) {
    int count = 0;
    for (boolean i : selectedStatesByColor[c]) {
      if (i)
        count++;
    }
    return count;
  }

  public boolean[] getSelectedStates() {
    return selectedStatesByColor[0];
  }

  public boolean[] getSelectedStates(int color) {
    return selectedStatesByColor[color];
  }


  public boolean[][] getSelectedStatesByColor() {
    return selectedStatesByColor;
  }

  public void setProbabilityDistribution(Rational[] dist) {
    this.probabilityDistribution = dist;
    automatonChanged();
    resetEigenVector();
  }

  public Rational[] getProbabilityDistribution() {
    return this.probabilityDistribution;
  }

  public Rational[] getEigenVector() {
    return this.eigenVector;
  }

  private void resetProbabilityDistribution() {
    int K = dfa.getK();
    this.probabilityDistribution = new Rational[K];
    for (int i = 0; i < K; i++)
      this.probabilityDistribution[i] = new Rational(1, K);
  }

  public void reset() {
    update(new DFA("0 0"));
    PCS.firePropertyChange("automatonReset", false, true);
  }

}
