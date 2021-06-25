
package models;

import java.util.ArrayList;

import algorithms.Rational;

public class InverseAutomaton extends AbstractNFA {

    private int K, N;
    private int[][][] matrix; // matrix[state][transition] - array of states
    private int[] selectedStates;
    private int[][] selectedStatesByColor;
    private Rational[] probabilityDistribution;
    private Rational[] eigenVector;

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
        selectedStatesByColor = automaton.getSelectedStatesByColor();
        probabilityDistribution = automaton.getProbabilityDistribution();
        eigenVector = automaton.getEigenVector();
    }

    public int[] getSelectedStates() {
        return selectedStates;
    }

    public int[] getSelectedStates(int color) {
        return selectedStatesByColor[color];
    }

    @Override
    public int[][] getSelectedStatesByColor() {
        return selectedStatesByColor;
    }

    public int getK() {
        return K;
    }

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

    @Override
    public Rational[] getProbabilityDistribution() {
        return this.probabilityDistribution;
    }

    @Override
    public Rational[] getEigenVector() {
        return this.eigenVector;
    }

}