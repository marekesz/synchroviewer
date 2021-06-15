
package AutomatonModels;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import Viewer.AutomatonHelper;

public class Automaton extends AbstractNFA {

    private int K, N; // max number of out edges for one state / number of states
    private int[][] matrix;
    private int[] selectedStates;

    private final PropertyChangeSupport PCS;

    public Automaton(String code) throws IllegalArgumentException {
        PCS = new PropertyChangeSupport(this);

        // Parse code
        String[] tokens = code.trim().replace('\n', ' ').split("\\s+");
        if (tokens.length < 2)
            throw new IllegalArgumentException("Invalid automaton code (expected K N).");

        K = Integer.parseInt(tokens[0]);
        N = Integer.parseInt(tokens[1]);

        if (tokens.length != 2 + K * N)
            throw new IllegalArgumentException(
                    "Invalid automaton code (expected " + (2 + K * N) + " numbers but read " + tokens.length + ").");

        matrix = new int[N][K];
        for (int n = 0; n < N; n++) {
            for (int k = 0; k < K; k++) {
                matrix[n][k] = Integer.parseInt(tokens[2 + n * K + k]);
                if (matrix[n][k] < 0 || matrix[n][k] >= N) {
                    throw new IllegalArgumentException("Invalid automaton code (number " + tokens[2 + n * K + k]
                            + " at position " + (2 + n * K + k) + " is outside the range [0," + (N - 1) + "])");
                }
            }
        }

        selectedStates = new int[N];
    }

    @Override
    public String toString() {
        String str = Integer.toString(K) + " " + Integer.toString(N) + " ";
        for (int n = 0; n < N; n++) {
            for (int k = 0; k < K; k++)
                str += Integer.toString(matrix[n][k]) + " ";
        }

        return str.trim();
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    @Override
    public int[] getTransitions(int i, int k) {
        return new int[] { matrix[i][k] };
    }

    public void update(Automaton automaton) {
        this.K = automaton.K;
        this.N = automaton.N;
        this.matrix = automaton.matrix;
        this.selectedStates = new int[N];
    }

    public void addState() {
        int[][] temp = new int[N + 1][K];
        for (int n = 0; n < N; n++)
            System.arraycopy(matrix[n], 0, temp[n], 0, K);

        for (int k = 0; k < K; k++)
            temp[N][k] = N;
        matrix = temp;

        int[] newSelectedStates = new int[N + 1];
        System.arraycopy(selectedStates, 0, newSelectedStates, 0, N);
        selectedStates = newSelectedStates;

        N++;
        automatonChanged();
    }

    public void removeState(int state) {
        int[][] temp = new int[N - 1][K];
        for (int n = 0; n < N - 1; n++) {
            int z = (n < state) ? n : n + 1;
            temp[n] = matrix[z];
            for (int k = 0; k < K; k++) {
                if (temp[n][k] == state)
                    temp[n][k] = n;
                else if (temp[n][k] > state)
                    temp[n][k]--;
            }
        }
        matrix = temp;

        int[] newSelectedStates = new int[N - 1];
        for (int i = 0; i < N - 1; i++) {
            if (i < state)
                newSelectedStates[i] = selectedStates[i];
            else
                newSelectedStates[i] = selectedStates[i + 1];
        }
        selectedStates = newSelectedStates;

        N--;
        automatonChanged();
    }

    public void replaceStates(int state1, int state2) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < K; j++) {
                if (matrix[i][j] == state1)
                    matrix[i][j] = state2;
                else if (matrix[i][j] == state2)
                    matrix[i][j] = state1;
            }
        }

        int[] temp = matrix[state1];
        matrix[state1] = matrix[state2];
        matrix[state2] = temp;

        automatonChanged();
    }

    public void addTransition(int out, int in, int k) {
        if (k < K) // edit transition
            matrix[out][k] = in;
        else if (k == K)// create new transition
        {
            createNewTransition();
            matrix[out][k] = in;
        }
        automatonChanged();
    }

    public void createNewTransition() {
        int[][] temp = new int[N][K + 1];
        for (int n = 0; n < N; n++) {
            System.arraycopy(matrix[n], 0, temp[n], 0, K);
            temp[n][K] = n;
        }
        matrix = temp;
        K++;
        automatonChanged();
    }

    public void removeTransition() {
        if (K > 0) {
            int[][] temp = new int[N][K - 1];
            for (int n = 0; n < N; n++)
                System.arraycopy(matrix[n], 0, temp[n], 0, K - 1);
            matrix = temp;
            K--;
            automatonChanged();
        }
    }

    public void selectState(int state) {
        selectedStates[state] = 1;
        automatonChanged();
    }

    public void selectStates(int[] selectedStates) {
        if (selectedStates.length == N) {
            this.selectedStates = selectedStates;
            automatonChanged();
        }
    }

    public void unselectState(int state) {
        selectedStates[state] = 0;
        automatonChanged();
    }

    public void clearSelectedStates() {
        Arrays.fill(selectedStates, 0);
        automatonChanged();
    }

    public boolean isSelected(int state) {
        return selectedStates[state] == 1;
    }

    public int getSelectedStatesNumber() {
        int count = 0;
        for (int i : selectedStates) {
            if (i == 1)
                count++;
        }

        return count;
    }

    public int[] getSelectedStates() {
        return selectedStates;
    }

    public void automatonChanged() {
        PCS.firePropertyChange("automatonChanged", false, true);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
        PCS.addPropertyChangeListener(propertyName, propertyChangeListener);
    }

    public void reset() {
        update(new Automaton("0 0"));
        PCS.firePropertyChange("automatonReset", false, true);
    }
}
