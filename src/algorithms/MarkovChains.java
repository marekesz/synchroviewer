package algorithms;

import java.util.ArrayList;
import java.util.Arrays;

import models.AbstractNFA;

public class MarkovChains {
    public static Rational ZERO = new Rational(0);
    public static Rational ONE = new Rational(1);

    public static Rational[][] getTransitMatrix(AbstractNFA automaton) {
        int n = automaton.getN();
        Rational[][] result = new Rational[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = ZERO;

        for (int i = 0; i < n; i++)
            for (int k = 0; k < automaton.getK(); k++)
                for (int j : automaton.getTransitions(i, k))
                    result[i][j] = result[i][j].add(automaton.getProbabilityDistribution()[k]);

        return result;
    }

    public static Rational[] getStationaryDistribution(Rational[][] matrix) {
        int n = matrix.length;
        Rational[] result = new Rational[n];
        Rational[] zeroVector = new Rational[n];
        Rational[][] A = new Rational[n + 1][n + 1];
        for (int i = 0; i < n; i++) {
            result[i] = ZERO;
            zeroVector[i] = ZERO;
            for (int j = 0; j < n; j++) {
                A[j][i] = matrix[i][j];
                if (i == j)
                    A[i][j] = A[i][j].subtract(ONE);
            }
            A[n][i] = ONE;
            A[i][n] = ZERO;
        }
        A[n][n] = ONE;
        ArrayList<Rational[]> gaussianA = new ArrayList<>(Arrays.asList(A));
        AlgebraicModule.reduceBase(gaussianA);
        for (int i = n; i >= 0; i--) {
            int firstNonZeroId = AlgebraicModule.leadingZerosCount(gaussianA.get(i));
            if (firstNonZeroId == n + 1)
                continue;
            if (firstNonZeroId == n)
                return zeroVector;

            result[firstNonZeroId] = gaussianA.get(i)[n];
            for (int j = firstNonZeroId + 1; j < n; j++)
                result[firstNonZeroId] = result[firstNonZeroId].subtract(gaussianA.get(i)[j].multiply(result[j]));
        }
        if (!AlgebraicModule.vectorsEqual(AlgebraicModule.matMul(result, matrix), result))
            return zeroVector;

        return result;
    }

    public static Rational[] getStationaryDistribution(AbstractNFA automaton) {
        return getStationaryDistribution(getTransitMatrix(automaton));
    }
}
