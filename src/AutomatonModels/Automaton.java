
package AutomatonModels;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import AutomatonAlgorithms.MarkovChains;
import AutomatonAlgorithms.Rational;

import java.awt.Color;

import Viewer.AutomatonHelper;
import Viewer.PaintPanel;

public class Automaton extends AbstractNFA {

    private int K, N; // max number of out edges for one state / number of states
    private int[][] matrix;
    private int[][] selectedStatesByColor;
    private Rational[] probabilityDistribution;
    private Rational[] eigenVector;
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

        selectedStatesByColor = new int[PaintPanel.STATES_COLORS.length][N];
        for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
            for (int i = 0; i < N; i++)
                selectedStatesByColor[c][i] = (c == 0 ? 1 : 0);

        resetProbabilityDistribution();
        resetEigenVector();
    }

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
    }

    public void addState() {
        int[][] temp = new int[N + 1][K];
        for (int n = 0; n < N; n++)
            System.arraycopy(matrix[n], 0, temp[n], 0, K);

        for (int k = 0; k < K; k++)
            temp[N][k] = N;
        matrix = temp;

        int[][] newSelectedStatesByColor = new int[PaintPanel.STATES_COLORS.length][N + 1];
        for (int i = 0; i < N; i++)
            for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
                newSelectedStatesByColor[c][i] = selectedStatesByColor[c][i];
        selectedStatesByColor = newSelectedStatesByColor;

        N++;
        resetEigenVector();
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

        int[][] newSelectedStatesByColor = new int[PaintPanel.STATES_COLORS.length][N - 1];
        for (int i = 0; i < N - 1; i++) {
            if (i < state) {
                for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
                    newSelectedStatesByColor[c][i] = selectedStatesByColor[c][i];
            } else {
                for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
                    newSelectedStatesByColor[c][i] = selectedStatesByColor[c][i + 1];
            }
        }
        selectedStatesByColor = newSelectedStatesByColor;
        N--;
        resetEigenVector();
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
        resetProbabilityDistribution();
        resetEigenVector();
        automatonChanged();
    }

    public void removeTransition() {
        if (K > 0) {
            int[][] temp = new int[N][K - 1];
            for (int n = 0; n < N; n++)
                System.arraycopy(matrix[n], 0, temp[n], 0, K - 1);
            matrix = temp;
            K--;
            resetProbabilityDistribution();
            resetEigenVector();
            automatonChanged();
        }
    }

    public void selectState(int state) {
        selectedStatesByColor[0][state] = 1;
        automatonChanged();
    }

    public void selectState(int state, int color) {
        selectedStatesByColor[color][state] = 1;
        automatonChanged();
    }

    public void selectStates(int[] selectedStates) {
        if (selectedStates.length == N) {
            this.selectedStatesByColor[0] = selectedStates;
            automatonChanged();
        }
    }

    public void selectStates(int[] selectedStates, int color) {
        if (selectedStates.length == N) {
            this.selectedStatesByColor[color] = selectedStates;
            automatonChanged();
        }
    }

    public void unselectState(int state) {
        for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
            selectedStatesByColor[c][state] = 0;
        automatonChanged();
    }

    public void unselectState(int state, int color) {
        selectedStatesByColor[color][state] = 0;
        automatonChanged();
    }

    public void clearSelectedStates() {
        for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
            Arrays.fill(selectedStatesByColor[c], 0);
        automatonChanged();
    }

    public boolean isSelected(int state) {
        return selectedStatesByColor[0][state] == 1;
    }

    public boolean isSelected(int state, int color) {
        return selectedStatesByColor[color][state] == 1;
    }

    public boolean isSelectedByAnyColor(int state) {
        for (int c = 0; c < PaintPanel.STATES_COLORS.length; c++)
            if (selectedStatesByColor[c][state] > 0)
                return true;
        return false;
    }

    public int getSelectedStatesNumber() {
        int count = 0;
        for (int i : selectedStatesByColor[0]) {
            if (i == 1)
                count++;
        }

        return count;
    }

    public int getSelectedStatesNumber(int c) {
        int count = 0;
        for (int i : selectedStatesByColor[c]) {
            if (i == 1)
                count++;
        }

        return count;
    }

    public int[] getSelectedStates() {
        return selectedStatesByColor[0];
    }

    public int[] getSelectedStates(int color) {
        return selectedStatesByColor[color];
    }

    @Override
    public int[][] getSelectedStatesByColor() {
        return selectedStatesByColor;
    }

    public void setProbabilityDistribution(Rational[] dist) {
        this.probabilityDistribution = dist;
        automatonChanged();
    }

    @Override
    public Rational[] getProbabilityDistribution() {
        return this.probabilityDistribution;
    }

    @Override
    public Rational[] getEigenVector() {
        return this.eigenVector;
    }

    private void resetProbabilityDistribution() {
        this.probabilityDistribution = new Rational[K];
        for (int i = 0; i < K; i++)
            this.probabilityDistribution[i] = new Rational(1, K);
    }

    private void resetEigenVector() {
        this.eigenVector = MarkovChains.getStationaryDistribution(this);
        automatonChanged();
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
