package algorithms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import main.AutomatonHelper;
import models.AbstractNFA;

public abstract class AlgebraicModule {
    public static Rational ZERO = new Rational(0);
    public static Rational ONE = new Rational(1);

    public static Rational[] ones(int n) {
        Rational[] result = new Rational[n];
        for (int i = 0; i < result.length; i++)
            result[i] = ONE;
        return result;
    }

    public static Rational weightedSumOfSubset(Rational[] subset, Rational[] weights) {
        Rational result = ZERO;
        for (int i = 0; i < subset.length; i++)
            result = result.add(subset[i].multiply(weights[i]));
        return result;
    }

    public static Rational weightedSumOfSubset(int[] subset, Rational[] weights) {
        Rational result = ZERO;
        for (int i = 0; i < subset.length; i++)
            result = result.add(new Rational(subset[i]).multiply(weights[i]));
        return result;
    }

    public static Rational[] vectorMultiply(Rational[] v1, Rational[] v2) {
        int n = v1.length;
        Rational[] result = new Rational[n];
        for (int i = 0; i < n; i++)
            result[i] = v1[i].multiply(v2[i]);
        return result;
    }

    public static Rational[] normalize(Rational[] vector) {
        Rational sum = new Rational(0);
        Rational[] result = new Rational[vector.length];
        for (int i = 0; i < vector.length; i++)
            result[i] = vector[i].copy();

        for (Rational x : vector)
            sum = sum.add(x);

        for (int i = 0; i < result.length; i++)
            result[i] = result[i].subtract(sum.multiply(new Rational(vector.length).inverse()));

        return result;
    }

    public static Rational[] normalize(Rational[] vector, Rational[] weights) {
        Rational[] result = vector.clone();
        Rational weightedMean = ZERO;
        for (Rational x : vectorMultiply(vector, weights))
            weightedMean = weightedMean.add(x);

        Rational oneFactor = ONE;
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].subtract(weightedMean);
            if (result[i].compareTo(ZERO) > 0)
                oneFactor = result[i].inverse();
        }
        for (int i = 0; i < result.length; i++)
            result[i] = result[i].multiply(oneFactor);

        return result;
    }

    public static Rational firstNonZeroElement(Rational[] vector) {
        for (int i = 0; i < vector.length; i++)
            if (!vector[i].equals(ZERO))
                return vector[i];
        return ZERO;
    }

    public static int nonZeroCnt(Rational[] vector) {
        int result = 0;
        for (int i = 0; i < vector.length; i++)
            result += vector[i].equals(ZERO) ? 0 : 1;
        return result;
    }

    public static int leadingZerosCount(Rational[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (!vector[i].equals(ZERO))
                return i;
        }
        return vector.length;
    }

    public static int leadingZerosCount(int[] vector) {
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

    // reduces base to gaussian form.
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

            if (!nz1.equals(ONE))
                base.set(i, scalarMultiply(nz1.inverse(), base.get(i)));
            nz1 = ONE;

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

    // checks if vector dependent from base
    public static boolean dependentFromBase(ArrayList<Rational[]> base, Rational[] vec) {
        if (leadingZerosCount(vec) == vec.length)
            return true;
        if (base.size() == 0)
            return false;
        ArrayList<Rational[]> baseCopy = new ArrayList<>();
        base.forEach(e -> {
            baseCopy.add(e.clone());
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

    // returns linear independent set of vectors reduced from original base, without
    // zero vectors
    public static ArrayList<Rational[]> getSpaceBase(ArrayList<Rational[]> base) {
        reduceBase(base);
        ArrayList<Rational[]> result = new ArrayList<>();
        for (int i = 0; i < base.size(); i++) {
            if (!firstNonZeroElement(base.get(i)).equals(ZERO))
                result.add(base.get(i));
        }
        return result;
    }

    public static void moveArrayList(ArrayList<String> from, ArrayList<String> to) {
        to.clear();
        for (String s : from)
            to.add(s);
    }

    public static Rational[] matMul(Rational[] vector, Rational[][] matrix) {
        Rational[] result = new Rational[matrix[0].length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Rational(0);
            for (int j = 0; j < vector.length; j++) {
                result[i] = result[i].add(vector[j].multiply(matrix[j][i]));
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

    public static Rational[][] wordToMatrix(AbstractNFA automaton, String word) {
        int n = automaton.getN();
        Rational[][] result = new Rational[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = new Rational(0);
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
                result[i][j] = new Rational(1);
        }
        return result;
    }

    public static void printArray(Rational[] array) {
        System.out.println();
        for (Rational e : array) {
            System.out.print(e.toString() + ", ");
        }
        System.out.println();
    }

    public static void printArray(BigInteger[] array) {
        System.out.println();
        for (BigInteger e : array) {
            System.out.print(e.toString() + ", ");
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

    public static void printArrayOfArrays(ArrayList<Rational[]> array) {
        System.out.println();
        for (Rational[] a : array) {
            System.out.print("[");
            for (Rational e : a) {
                System.out.print(e.toString() + ", ");
            }
            System.out.println("]");
        }
        System.out.println();
    }

    public static void printMatrix(Rational matrix[][]) {
        int n = matrix.length;
        int m = matrix[0].length;
        for (int i = 0; i < n; i++) {
            System.out.print("|");
            for (int j = 0; j < m; j++) {
                System.out.print(matrix[i][j].toString() + " ");
            }
            System.out.println("|");
        }
    }

    public static Rational sumOfVector(Rational[] vector) {
        Rational result = ZERO;
        for (Rational x : vector)
            result = result.add(x);
        return result;
    }

    public static String vectorToString(Rational[] rationals) {
        String text = "[";
        for (int i = 0; i < rationals.length; i++) {
            text += rationals[i].toString() + (i < rationals.length - 1 ? "; " : "]");
        }
        return text;
    }

    public static boolean vectorsEqual(Rational[] v1, Rational[] v2) {
        if (v1.length != v2.length)
            return false;
        for (int i = 0; i < v1.length; i++)
            if (!v1[i].equals(v2[i]))
                return false;
        return true;
    }

    public static Rational[][] transpose(Rational[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;
        Rational[][] result = new Rational[m][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                result[i][j] = matrix[j][i];
        return result;
    }

    public static BigInteger[] rationalArrayByCommonDenominator(Rational[] array) {
        BigInteger commonDenom = new BigInteger("1");
        BigInteger[] result = new BigInteger[array.length];
        for (int i = 0; i < array.length; i++) {
            BigInteger denom = array[i].getDenominator().abs();
            BigInteger gcd = commonDenom.gcd(denom).abs();
            commonDenom = commonDenom.multiply(denom).divide(gcd);
        }
        for (int i = 0; i < array.length; i++)
            result[i] = array[i].getNominator().multiply(commonDenom.divide(array[i].getDenominator()));
        return result;
    }
}
