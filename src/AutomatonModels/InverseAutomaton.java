
package AutomatonModels;

import java.util.ArrayList;
import java.util.Arrays;

import Viewer.AutomatonHelper;

public class InverseAutomaton extends AbstractNFA {

    private int K, N;
    private int[][][] matrix; // matrix[state][transition] - array of states
    private int[] selectedStates;

    public InverseAutomaton(Automaton automaton) {
        K = automaton.getK();
        N = automaton.getN();
        matrix = new int[N][K][];

        for (int n = 0; n < N; n++) {
            for (int k = 0; k < K; k++) {
                ArrayList<Integer> arrayList = new ArrayList<>();
                for (int m = 0; m < N; m++) {
                    if (automaton.getMatrix()[m][k] == n)
                        arrayList.add(m);
                }
                matrix[n][k] = new int[arrayList.size()];
                for (int i = 0; i < matrix[n][k].length; i++)
                    matrix[n][k][i] = arrayList.get(i);
            }
        }
        selectedStates = new int[N];
        selectedStates = automaton.getSelectedStates();
    }

    @Override
    public int[] getSelectedStates() {
        return selectedStates;
    }

    @Override
    public int getK() {
        return K;
    }

    @Override
    public int getN() {
        return N;
    }

    public int[][][] getMatrix() {
        return matrix;
    }

    @Override
    public int[] getTransitions(int i, int k) {
        return matrix[i][k];
    }
}