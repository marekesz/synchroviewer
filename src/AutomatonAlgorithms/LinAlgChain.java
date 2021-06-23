package AutomatonAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import AutomatonModels.AbstractNFA;
import Viewer.AutomatonHelper;

public class LinAlgChain {
    private static Rational ZERO = new Rational(0);
    public static Rational ONE = new Rational(1);
    private static int COLORS_NUM = 10;

    // computes array L, where L[i] = span({[S][w] | w in Sigma^<=i, S in automaton
    // selected subsets}), returns
    // words used for each L[i], and dimensions of L[i]
    public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainForManySubsets(AbstractNFA automaton,
            Rational[] weights, boolean zeroSum, boolean eigenVectorPreprocess, boolean eigenVectorZeroSum) {
        ArrayList<String> words = new ArrayList<>();
        ArrayList<Rational[]> base = new ArrayList<>();
        if (automaton.getN() == 0)
            return new Pair<>(words, base);
        if (weights == null)
            weights = AlgebraicModule.ones(automaton.getN());
        base = prepareL0Base(automaton, weights, zeroSum, eigenVectorPreprocess, eigenVectorZeroSum);
        for (int i = 0; i < base.size(); i++)
            words.add("");
        // System.out.println("base size: " + base.size());
        if (base.size() == 0)
            return new Pair<>(words, base);

        int candsIndex = 0;
        while (candsIndex <= base.size() - 1) {
            int currentBaseSize = base.size();
            while (candsIndex < currentBaseSize) {
                Rational[] baseVector = base.get(candsIndex);
                String baseWord = words.get(candsIndex);
                // System.out.println("word: " + baseWord);
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
        if (!Objects.isNull(weights) && !eigenVectorPreprocess)
            for (int i = 0; i < base.size(); i++)
                base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

        // System.out.println("result length: " + words.size());
        return new Pair(words, base);
    }

    // the same, but finds words, that increases or decreases sum of vector
    public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainChangeSumForManySubsets(
            AbstractNFA automaton, Rational[] weights, boolean normalize, boolean eigenVectorPreprocess,
            boolean eigenVectorZeroSum, boolean increaseDecrease) {

        boolean weighted = !Objects.isNull(weights);
        if (!weighted)
            weights = AlgebraicModule.ones(automaton.getN());

        ArrayList<String> words = new ArrayList<>();
        ArrayList<Rational[]> base = new ArrayList<>();
        if (automaton.getN() == 0)
            return new Pair<>(words, base);

        base = prepareL0Base(automaton, weights, normalize, eigenVectorPreprocess, eigenVectorZeroSum);
        for (int i = 0; i < base.size(); i++)
            words.add("");
        if (base.size() == 0)
            return new Pair<>(words, base);

        int candsIndex = 0;

        boolean extendingWordFound = false;
        while (candsIndex <= base.size() - 1) {
            int currentBaseSize = base.size();
            while (candsIndex < currentBaseSize) {
                Rational[] baseVector = base.get(candsIndex);
                String baseWord = words.get(candsIndex);
                if (baseWord.length() > 0 && baseWord.charAt(baseWord.length() - 1) == '*')
                    baseWord = baseWord.substring(0, baseWord.length() - 1);
                Rational subsetWeight = AlgebraicModule.weightedSumOfSubset(baseVector, weights);
                for (int k = 0; k < automaton.getK(); k++) {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    Rational[] newVector = AlgebraicModule.matMul(baseVector,
                            AlgebraicModule.wordToMatrix(automaton, Character.toString(a)));
                    Rational vectorWeight = AlgebraicModule.weightedSumOfSubset(newVector, weights);
                    if (!extendingWordFound && (increaseDecrease ? (vectorWeight.compareTo(subsetWeight) < 0)
                            : (vectorWeight.compareTo(subsetWeight) > 0))) {
                        String b = findChangingSumLetter(automaton, baseWord + a, baseVector, weights,
                                increaseDecrease);

                        if (!Objects.isNull(b)) {
                            Rational[] alterVector = AlgebraicModule.matMul(baseVector,
                                    AlgebraicModule.wordToMatrix(automaton, b));
                            if (!AlgebraicModule.dependentFromBase(base, alterVector)) {
                                extendingWordFound = true;
                                base.add(alterVector);
                                words.add(baseWord + b + '*');
                            }
                        }
                    }
                    if (!AlgebraicModule.dependentFromBase(base, newVector)) {
                        base.add(newVector);
                        if ((increaseDecrease ? (vectorWeight.compareTo(subsetWeight) > 0)
                                : (vectorWeight.compareTo(subsetWeight) < 0)) && !extendingWordFound) {
                            extendingWordFound = true;
                            words.add(baseWord + a + "*");
                        } else
                            words.add(baseWord + a);
                    }
                }
                candsIndex++;
            }
        }
        if (!eigenVectorPreprocess)
            for (int i = 0; i < base.size(); i++)
                base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

        return new Pair(words, base);
    }

    public static ArrayList<Rational[]> getAllSubsets(AbstractNFA automaton, Rational[] weights, boolean normalize,
            boolean eigenVectorPreprocess, boolean eigenVectorZeroSum) {
        ArrayList<Rational[]> result = new ArrayList<>();
        // System.out.println("subsets processed:");
        for (int[] subset : automaton.getSelectedStatesByColor()) {
            Rational[] rationalSubset = AlgebraicModule.toRationalArray(subset);

            if (eigenVectorPreprocess)
                rationalSubset = AlgebraicModule.vectorMultiply(weights, rationalSubset);
            if (normalize)
                rationalSubset = AlgebraicModule.normalize(rationalSubset);
            if (eigenVectorZeroSum)
                rationalSubset = AlgebraicModule.normalize(rationalSubset, weights);

            if (AlgebraicModule.leadingZerosCount(rationalSubset) < rationalSubset.length) {
                result.add(rationalSubset);
                // AlgebraicModule.printArray(rationalSubset);
            }
        }
        return result;
    }

    public static ArrayList<Rational[]> prepareL0Base(AbstractNFA automaton, Rational[] weights, boolean zeroSum,
            boolean eigenVectorPreprocess, boolean eigenVectorZeroSum) {
        ArrayList<Rational[]> base = new ArrayList<>();
        ArrayList<Rational[]> subsets = getAllSubsets(automaton, weights, zeroSum, eigenVectorPreprocess,
                eigenVectorZeroSum);
        for (Rational[] subset : subsets) {
            Rational[] vector = AlgebraicModule.matMul(subset, AlgebraicModule.wordToMatrix(automaton, ""));
            if (!AlgebraicModule.dependentFromBase(base, vector))
                base.add(vector);
        }
        return base;
    }

    private static String findChangingSumLetter(AbstractNFA automaton, String word, Rational[] baseVector,
            Rational[] weights, boolean increaseDecrease) {
        for (int k = 0; k < automaton.getK(); k++) {
            String b = Character.toString(AutomatonHelper.TRANSITIONS_LETTERS[k]);
            Rational[] vector = AlgebraicModule.matMul(baseVector, AlgebraicModule.wordToMatrix(automaton, b));
            Rational vectorWeight = AlgebraicModule.weightedSumOfSubset(vector, weights);
            Rational baseVectorWeight = AlgebraicModule.weightedSumOfSubset(baseVector, weights);

            if ((increaseDecrease ? (vectorWeight.compareTo(baseVectorWeight) > 0)
                    : (vectorWeight.compareTo(baseVectorWeight) < 0)))
                return b;
        }
        // System.out.println("heavier letter not found");
        return null;
    }

    private int wordLengthWithoutExtraSymbols(String word) {
        int length = 0;
        for (char c : word.toCharArray())
            if (c >= 'a' && c <= 'z')
                length++;
        return length;
    }

}
