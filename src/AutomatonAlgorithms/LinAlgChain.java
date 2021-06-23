package AutomatonAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import AutomatonModels.AbstractNFA;
import Viewer.AutomatonHelper;

public class LinAlgChain {
    private static Rational ZERO = new Rational(0);
    public static Rational ONE = new Rational(1);
    private static int COLORS_NUM = 10;

    // computes array L, where L[i] = span({[S][w] | w in Sigma^<=i}), returns
    // words used for each L[i], and dimensions of L[i]
    public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChain(AbstractNFA automaton, int[] subset,
            Rational[] weights, boolean normalize, boolean subsetPreprocessed) {
        ArrayList<String> words = new ArrayList<>();
        ArrayList<Rational[]> base = new ArrayList<>();
        ArrayList<String> candidates = new ArrayList<>();
        if (automaton.getN() == 0)
            return new Pair<>(words, base);
        Rational[] rationalSubset = AlgebraicModule.toRationalArray(subset);
        if (subsetPreprocessed)
            rationalSubset = AlgebraicModule.vectorMultiply(weights, rationalSubset);
        if (normalize)
            rationalSubset = AlgebraicModule.normalize(rationalSubset);
        if (AlgebraicModule.leadingZerosCount(rationalSubset) == rationalSubset.length)
            return new Pair<>(words, base);

        words.add("");
        candidates.add("");
        Rational[] vector = AlgebraicModule.matMul(rationalSubset, AlgebraicModule.wordToMatrix(automaton, ""));
        base.add(vector);
        ArrayList<String> newCandidates = new ArrayList<>();
        while (candidates.size() > 0) {
            for (String x : candidates) {
                for (int k = 0; k < automaton.getK(); k++) {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    vector = AlgebraicModule.matMul(rationalSubset, AlgebraicModule.wordToMatrix(automaton, x + a));
                    if (!AlgebraicModule.dependentFromBase(base, vector)) {
                        base.add(vector);
                        newCandidates.add(x + a);
                        words.add(x + a);
                    }
                }
            }
            AlgebraicModule.moveArrayList(newCandidates, candidates);
            newCandidates.clear();
        }
        if (!Objects.isNull(weights) && !subsetPreprocessed)
            for (int i = 0; i < base.size(); i++)
                base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

        return new Pair(words, base);
    }

    public static ArrayList<Rational[]> getAllSubsets(AbstractNFA automaton, Rational[] weights, boolean normalize,
            boolean subsetPreprocessed) {
        ArrayList<Rational[]> result = new ArrayList<>();
        for (int[] subset : subArray(automaton.getSelectedStatesByColor(), 1, COLORS_NUM)) {
            Rational[] rationalSubset = AlgebraicModule.toRationalArray(subset);
            if (subsetPreprocessed)
                rationalSubset = AlgebraicModule.vectorMultiply(weights, rationalSubset);
            if (normalize)
                rationalSubset = AlgebraicModule.normalize(rationalSubset);
            if (AlgebraicModule.leadingZerosCount(rationalSubset) < rationalSubset.length)
                result.add(rationalSubset);
        }
        return result;
    }

    public static ArrayList<Rational[]> prepareL0Base(AbstractNFA automaton, Rational[] weights, boolean normalize,
            boolean subsetPreprocessed) {
        ArrayList<Rational[]> base = new ArrayList<>();
        ArrayList<Rational[]> subsets = getAllSubsets(automaton, weights, normalize, subsetPreprocessed);
        for (Rational[] subset : subsets) {
            Rational[] vector = AlgebraicModule.matMul(subset, AlgebraicModule.wordToMatrix(automaton, ""));
            if (!AlgebraicModule.dependentFromBase(base, vector))
                base.add(vector);
        }
        return base;
    }

    // The same, but optimized and handling all colored subsets
    public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainForManySubsets(AbstractNFA automaton,
            Rational[] weights, boolean normalize, boolean preprocess) {
        ArrayList<String> words = new ArrayList<>();
        ArrayList<Rational[]> base = new ArrayList<>();
        if (automaton.getN() == 0)
            return new Pair<>(words, base);
        base = prepareL0Base(automaton, weights, normalize, preprocess);
        for (int i = 0; i < base.size(); i++)
            words.add("");
        int candsIndex = 0;
        while (candsIndex < base.size() - 1) {
            int currentBaseSize = base.size();
            while (candsIndex < currentBaseSize) {
                Rational[] baseVector = base.get(candsIndex);
                String baseWord = words.get(candsIndex);
                for (int k = 0; k < automaton.getK(); k++) {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    Rational[] newVector = AlgebraicModule.matMul(baseVector,
                            AlgebraicModule.wordToMatrix(automaton, Character.toString(a)));
                    if (!AlgebraicModule.dependentFromBase(base, newVector)) {
                        base.add(newVector);
                        words.add(baseWord + a);
                    }
                }
                candsIndex++;
            }
        }
        if (!Objects.isNull(weights) && !preprocess)
            for (int i = 0; i < base.size(); i++)
                base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

        return new Pair(words, base);
    }

    // the same, but finds words, that increases sum of vector
    public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainExtendSum(AbstractNFA automaton,
            int[] subset, Rational[] weights, boolean normalize, boolean subsetPreprocessed) {

        ArrayList<String> words = new ArrayList<>();
        ArrayList<Rational[]> base = new ArrayList<>();
        ArrayList<String> candidates = new ArrayList<>();
        boolean weighted = !Objects.isNull(weights);
        if (!weighted)
            weights = AlgebraicModule.ones(automaton.getN());

        Rational subsetWeight = AlgebraicModule.weightedSumOfSubset(subset, weights);
        if (automaton.getN() == 0)
            return new Pair<>(words, base);
        Rational[] rationalSubset = AlgebraicModule.toRationalArray(subset);
        if (subsetPreprocessed)
            rationalSubset = AlgebraicModule.vectorMultiply(weights, rationalSubset);
        if (normalize)
            rationalSubset = AlgebraicModule.normalize(rationalSubset);
        if (AlgebraicModule.leadingZerosCount(rationalSubset) == rationalSubset.length)
            return new Pair<>(words, base);

        words.add("");
        candidates.add("");
        Rational[] vector = AlgebraicModule.matMul(rationalSubset, AlgebraicModule.wordToMatrix(automaton, ""));
        base.add(vector);
        ArrayList<String> newCandidates = new ArrayList<>();

        boolean extendingWordFound = false;
        while (candidates.size() > 0) {
            for (String x : candidates) {
                for (int k = 0; k < automaton.getK(); k++) {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    vector = AlgebraicModule.matMul(rationalSubset, AlgebraicModule.wordToMatrix(automaton, x + a));
                    Rational vectorWeight = AlgebraicModule.weightedSumOfSubset(vector, weights);

                    if (!extendingWordFound && vectorWeight.compareTo(subsetWeight) < 0) {
                        String newWord = findHeavierWord(automaton, x + a, rationalSubset, subsetWeight, weights);
                        if (!Objects.isNull(newWord)) {
                            extendingWordFound = true;
                            base.add(AlgebraicModule.matMul(rationalSubset,
                                    AlgebraicModule.wordToMatrix(automaton, newWord)));
                            newCandidates.add(newWord);
                            words.add(newWord + '^');
                        }
                    }
                    if (!AlgebraicModule.dependentFromBase(base, vector)) {
                        base.add(vector);
                        newCandidates.add(x + a);
                        if (vectorWeight.compareTo(subsetWeight) > 0 && !extendingWordFound) {
                            extendingWordFound = true;
                            words.add(x + a + "*");
                        } else {
                            words.add(x + a);
                        }
                    }
                }
            }
            AlgebraicModule.moveArrayList(newCandidates, candidates);
            newCandidates.clear();
        }
        if (!subsetPreprocessed)
            for (int i = 0; i < base.size(); i++)
                base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

        return new Pair(words, base);
    }

    // the same, but finds words, that increases sum of vector
    public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainExtendSumForManySubsets(
            AbstractNFA automaton, int[] subset, Rational[] weights, boolean normalize, boolean preprocess) {

        boolean weighted = !Objects.isNull(weights);
        if (!weighted)
            weights = AlgebraicModule.ones(automaton.getN());

        Rational subsetWeight = AlgebraicModule.weightedSumOfSubset(subset, weights);

        ArrayList<String> words = new ArrayList<>();
        ArrayList<Rational[]> base = new ArrayList<>();
        if (automaton.getN() == 0)
            return new Pair<>(words, base);
        base = prepareL0Base(automaton, weights, normalize, preprocess);
        if (base.size() == 0)
            return new Pair<>(words, base);
        for (int i = 0; i < base.size(); i++)
            words.add("");
        int candsIndex = 0;

        boolean extendingWordFound = false;
        while (candsIndex < base.size() - 1) {
            int currentBaseSize = base.size();
            while (candsIndex < currentBaseSize) {
                Rational[] baseVector = base.get(candsIndex);
                String baseWord = words.get(candsIndex);
                for (int k = 0; k < automaton.getK(); k++) {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    Rational[] newVector = AlgebraicModule.matMul(baseVector,
                            AlgebraicModule.wordToMatrix(automaton, Character.toString(a)));
                    Rational vectorWeight = AlgebraicModule.weightedSumOfSubset(newVector, weights);

                    if (!extendingWordFound && vectorWeight.compareTo(subsetWeight) < 0) {
                        String b = findExtendingLetter(automaton, baseWord + a, baseVector, subsetWeight, weights);
                        if (!Objects.isNull(b)) {
                            extendingWordFound = true;
                            base.add(AlgebraicModule.matMul(baseVector, AlgebraicModule.wordToMatrix(automaton, b)));
                            words.add(baseWord + b + '*');
                        }
                    }
                    if (!AlgebraicModule.dependentFromBase(base, newVector)) {
                        base.add(newVector);
                        if (vectorWeight.compareTo(subsetWeight) > 0 && !extendingWordFound) {
                            extendingWordFound = true;
                            words.add(baseWord + a + "*");
                        } else
                            words.add(baseWord + a);
                    }
                }
                candsIndex++;
            }
        }
        if (!preprocess)
            for (int i = 0; i < base.size(); i++)
                base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

        return new Pair(words, base);
    }

    private static String findExtendingLetter(AbstractNFA automaton, String word, Rational[] baseVector,
            Rational subsetWeight, Rational[] weights) {
        for (int k = 0; k < automaton.getK(); k++) {
            String b = Character.toString(AutomatonHelper.TRANSITIONS_LETTERS[k]);
            Rational[] vector = AlgebraicModule.matMul(baseVector, AlgebraicModule.wordToMatrix(automaton, b));
            if (AlgebraicModule.weightedSumOfSubset(vector, weights).compareTo(subsetWeight) > 0)
                return b;
        }
        return null;
    }

    private static String findHeavierWord(AbstractNFA automaton, String word, Rational[] rationalSubset,
            Rational subsetWeight, Rational[] weights) {
        for (int k = 0; k < automaton.getK(); k++) {
            String potentialWord = word.substring(0, word.length() - 1) + AutomatonHelper.TRANSITIONS_LETTERS[k];
            Rational[] vector = AlgebraicModule.matMul(rationalSubset,
                    AlgebraicModule.wordToMatrix(automaton, potentialWord));
            if (AlgebraicModule.weightedSumOfSubset(vector, weights).compareTo(subsetWeight) > 0)
                return potentialWord;
        }
        return null;
    }

    private int wordLengthWithoutExtraSymbols(String word) {
        int length = 0;
        for (char c : word.toCharArray())
            if (c >= 'a' && c <= 'z')
                length++;
        return length;
    }

    public static <T> T[] subArray(T[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }
}
