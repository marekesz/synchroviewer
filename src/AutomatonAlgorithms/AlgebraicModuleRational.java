package AutomatonAlgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import AutomatonModels.AbstractNFA;
import Viewer.AutomatonHelper;

public class AlgebraicModuleRational {
    private static Rational ZERO = new Rational(0);

    // computes array L, where L[i] = dim(span({[S][w] | w in Sigma^<=i}))
    public static ArrayList<String> wordsForSubset(AbstractNFA automaton, int[] subset) {
        if (automaton.getN() == 0)
            return new ArrayList<String>();
        // int[] subset = automaton.getSelectedStates();
        ArrayList<String> result = new ArrayList<>();
        ArrayList<int[]> base = new ArrayList<>();
        ArrayList<String> candidates = new ArrayList<>();
        if (leadingZerosCount(subset) == subset.length)
            return result;
        result.add("");
        candidates.add("");
        base.add(matMul(subset, wordToMatrixNFA(automaton, "")));
        ArrayList<String> newCandidates = new ArrayList<>();
        while (candidates.size() > 0) {
            for (String x : candidates) {
                for (int k = 0; k < automaton.getK(); k++) {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    int[] vec = matMul(subset, wordToMatrixNFA(automaton, x + a));
                    if (!dependentFromBase(base, vec)) {
                        base.add(vec);
                        newCandidates.add(x + a);
                        result.add(x + a);
                    }
                }
            }
            moveArrayList(newCandidates, candidates);
            newCandidates.clear();
        }
        return result;
    }

    public static Rational firstNonZeroElement(Rational[] vector) {
        for (int i = 0; i < vector.length; i++)
            if (!vector[i].equals(ZERO))
                return vector[i];
        return ZERO;
    }

    private static int leadingZerosCount(Rational[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (!vector[i].equals(ZERO))
                return i;
        }
        return vector.length;
    }

    private static int leadingZerosCount(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != 0)
                return i;
        }
        return vector.length;
    }

    // vector greater if one vector is lexicographically with abs values greater
    public static boolean vectorGreater(Rational[] v1, Rational[] v2) {
        int cmp = 0;
        for (int i = 0; i < v1.length; i++) {
            cmp = v1[i].abs().compareTo(v2[i].abs());
            if (cmp > 0)
                return true;
            if (cmp < 0)
                return false;
        }
        return false;
    }

    public static Rational[] scalarMultiply(Rational scalar, Rational[] vector) {
        Rational[] result = vector.clone();
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i].multiply(scalar);
        }
        return result;
    }

    public static Rational[] vectorAdd(Rational[] v1, Rational[] v2) {
        Rational[] result = v1.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = v1[i].add(v2[i]);
        }
        return result;
    }

    public static Rational[] vectorSubtract(Rational[] v1, Rational[] v2) {
        return vectorAdd(v1, scalarMultiply(new Rational(-1), v2));
    }

    // reduces base to quasi-gaussian form.
    // After reduction each vector maintains its integer values and they don't need
    // to be ones.
    // Biginteger necessary, because we may count lcm of many vector values
    // obtaingin very big number
    public static void reduceBase(ArrayList<Rational[]> base) {
        for (int i = 0; i < base.size(); i++) {
            for (int j = i + 1; j < base.size(); j++) {
                if (vectorGreater(base.get(j), base.get(i)))
                    Collections.swap(base, i, j);
            }
            if (firstNonZeroElement(base.get(i)).compareTo(ZERO) < 0)
                base.set(i, scalarMultiply(new Rational(-1), base.get(i)));

            Rational nz1 = firstNonZeroElement(base.get(i));
            if (nz1.equals(ZERO))
                return;

            for (int j = i + 1; j < base.size(); j++) {
                // check if non zero
                if (firstNonZeroElement(base.get(j)).compareTo(ZERO) < 0) {
                    base.set(j, scalarMultiply(new Rational(-1), base.get(j)));
                }
                Rational nz2 = firstNonZeroElement(base.get(j));
                if (nz2.equals(ZERO) || leadingZerosCount(base.get(i)) != leadingZerosCount(base.get(j)))
                    continue;
                Rational factor = nz1.multiply(nz2.inverse());
                base.set(j, scalarMultiply(factor, base.get(j)));
                base.set(j, vectorSubtract(base.get(i), base.get(j)));
            }
        }
    }

    // checks if vector depenedent from base
    public static boolean dependentFromBase(ArrayList<int[]> base, int[] vec) {
        if (leadingZerosCount(vec) == vec.length)
            return true;
        if (base.size() == 0)
            return false;
        ArrayList<int[]> baseCopy = new ArrayList<>();
        base.forEach(e -> {
            baseCopy.add(e);
        });
        int before = getSpaceBase(baseCopy).size();
        baseCopy.add(vec);
        int after = getSpaceBase(baseCopy).size();
        return before == after;
    }

    public static Rational[] toRationalArray(int[] vector) {
        Rational[] result = new Rational[vector.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Rational(vector[i]);
        }
        return result;
    }

    // returns linear independent set of vectors reduced from original base
    public static ArrayList<Rational[]> getSpaceBase(ArrayList<int[]> base) {
        ArrayList<Rational[]> bigIntBase = new ArrayList<>();
        for (int i = 0; i < base.size(); i++)
            bigIntBase.add(toRationalArray(base.get(i)));
        reduceBase(bigIntBase);
        ArrayList<Rational[]> result = new ArrayList<>();
        for (int i = 0; i < bigIntBase.size(); i++) {
            if (!firstNonZeroElement(bigIntBase.get(i)).equals(ZERO))
                result.add(bigIntBase.get(i));
        }
        return result;
    }

    public static void moveArrayList(ArrayList<String> from, ArrayList<String> to) {
        to.clear();
        for (String s : from)
            to.add(s);
    }

    public static int[] matMul(int[] vector, int[][] matrix) {
        int[] result = new int[matrix[0].length];
        for (int i = 0; i < result.length; i++) {
            result[i] = 0;
            for (int j = 0; j < vector.length; j++) {
                result[i] += vector[j] * matrix[j][i];
            }
        }
        return result;
    }

    private static int charArrayIndexOf(char array[], char x) {
        int i = 0;
        for (char c : array) {
            if (c == x)
                return i;
            i++;
        }
        return -1;
    }

    public static int[][] wordToMatrixNFA(AbstractNFA automaton, String word) {
        int n = automaton.getN();
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            HashSet<Integer> phase = new HashSet<>();
            phase.add(i);
            for (int k = 0; k < word.length(); k++) {
                int char_id = charArrayIndexOf(AutomatonHelper.TRANSITIONS_LETTERS, word.charAt(k));
                HashSet<Integer> newPhase = new HashSet<>();
                phase.forEach(j -> {
                    for (int l : automaton.getTransitions(j, char_id)) {
                        newPhase.add(l);
                    }
                });
                phase = newPhase;
            }
            for (int j : phase)
                result[i][j] = 1;
        }
        return result;
    }

    public static void printArray(ArrayList<Integer> array) {
        System.out.println();
        for (int e : array) {
            System.out.print(e + ", ");
        }
        System.out.println();
    }

    public static void printArray(int[] array) {
        System.out.println();
        for (int e : array) {
            System.out.print(e + ", ");
        }
        System.out.println();
    }

    public static void printArrayOfArrays(ArrayList<int[]> array) {
        System.out.println();
        for (int[] a : array) {
            System.out.print("[");
            for (int e : a) {
                System.out.print(e + ", ");
            }
            System.out.println("]");
        }
        System.out.println();
    }

    public static void printMatrix(int matrix[][]) {
        int n = matrix.length;
        int m = matrix[0].length;
        for (int i = 0; i < n; i++) {
            System.out.print("|");
            for (int j = 0; j < m; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("|");
        }
    }

}
