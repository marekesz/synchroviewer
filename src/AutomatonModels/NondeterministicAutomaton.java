package AutomatonModels;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import Viewer.AutomatonHelper;

public class NondeterministicAutomaton {
    private int K, N; // max number of out edges for one state / number of states
    private int[][][] matrix;
    private int[] selectedStates;

    public int[] getSelectedStates() {
        return selectedStates;
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public int[] getTransitions(int i, int k) {
        return matrix[i][k];
    }
}
