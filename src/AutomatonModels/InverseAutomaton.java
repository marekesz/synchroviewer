
package AutomatonModels;

import java.util.ArrayList;

public class InverseAutomaton {

    private int K, N;
    private int[][][] matrix; // matrix[state][transition] - array of states

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
    }

    public int[][][] getMatrix() {
        return matrix;
    }
}