package AutomatonModels;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import Viewer.AutomatonHelper;

public abstract class AbstractNFA {
    private int K, N; // max number of out edges for one state / number of states

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public abstract int[] getTransitions(int i, int k);

    public abstract int[][] getSelectedStatesByColor();
}
