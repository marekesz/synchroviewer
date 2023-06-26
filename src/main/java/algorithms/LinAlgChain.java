package algorithms;

import main.Config;
import models.AbstractNFA;
import models.DFA;

import java.util.ArrayList;
import java.util.Objects;

public class LinAlgChain {
  public static Rational ONE = new Rational(1);

  // computes array L, where L[i] = span({[S][w] | w in Sigma^<=i, S in automaton
  // selected subsets}), returns
  // words used for each L[i], and dimensions of L[i]
  public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainForManySubsets(
    DFA dfa, boolean[][] selectedStatesByColor, Rational[] weights, boolean zeroSum, boolean eigenVectorPreprocess,
    boolean eigenVectorPostprocess, boolean eigenVectorZeroSum) {
    ArrayList<String> words = new ArrayList<>();
    ArrayList<Rational[]> base = new ArrayList<>();
    if (dfa.getN() == 0)
      return new Pair<>(words, base);
    if (weights == null)
      weights = AlgebraicModule.ones(dfa.getN());
    base = prepareL0Base(dfa, selectedStatesByColor, weights, zeroSum, eigenVectorPreprocess, eigenVectorZeroSum);
    for (int i = 0; i < base.size(); i++)
      words.add("");
    if (base.size() == 0)
      return new Pair<>(words, base);

    int candsIndex = 0;
    while (candsIndex <= base.size() - 1) {
      int currentBaseSize = base.size();
      while (candsIndex < currentBaseSize) {
        Rational[] baseVector = base.get(candsIndex);
        String baseWord = words.get(candsIndex);
        for (int k = 0; k < dfa.getK(); k++) {
          char a = Config.TRANSITIONS_LETTERS[k];
          Rational[] newVector = AlgebraicModule.matMul(baseVector,
            AlgebraicModule.wordToMatrix(dfa, Character.toString(a)));
          if (!AlgebraicModule.dependentFromBase(base, newVector)) {
            base.add(newVector);
            words.add(baseWord + a);
          }
        }
        candsIndex++;
      }
    }
    if (eigenVectorPostprocess)
      for (int i = 0; i < base.size(); i++)
        base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));

    return new Pair<ArrayList<String>, ArrayList<Rational[]>>(words, base);
  }

  // the same, but finds words, that increases or decreases sum of vector
  public static Pair<ArrayList<String>, ArrayList<Rational[]>> linAlgChainChangeSumForManySubsets(
    DFA dfa, boolean[][] selectedStatesByColor, Rational[] weights, boolean normalize, boolean eigenVectorPreprocess,
    boolean eigenVectorPostprocess, boolean eigenVectorZeroSum, boolean increaseDecrease) {

    boolean weighted = !Objects.isNull(weights);
    if (!weighted)
      weights = AlgebraicModule.ones(dfa.getN());

    ArrayList<String> words = new ArrayList<>();
    ArrayList<Rational[]> base = new ArrayList<>();
    if (dfa.getN() == 0)
      return new Pair<>(words, base);

    base = prepareL0Base(dfa, selectedStatesByColor, weights, normalize, eigenVectorPreprocess, eigenVectorZeroSum);
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
        for (int k = 0; k < dfa.getK(); k++) {
          char a = Config.TRANSITIONS_LETTERS[k];
          Rational[] newVector = AlgebraicModule.matMul(baseVector,
            AlgebraicModule.wordToMatrix(dfa, Character.toString(a)));
          Rational vectorWeight = AlgebraicModule.weightedSumOfSubset(newVector, weights);
          if (!extendingWordFound && (increaseDecrease ? (vectorWeight.compareTo(subsetWeight) < 0)
            : (vectorWeight.compareTo(subsetWeight) > 0))) {
            String b = findChangingSumLetter(dfa, baseVector, weights,
              increaseDecrease);

            if (!Objects.isNull(b)) {
              Rational[] alterVector = AlgebraicModule.matMul(baseVector,
                AlgebraicModule.wordToMatrix(dfa, b));
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

    if (eigenVectorPostprocess) {
      for (int i = 0; i < base.size(); i++) {
        base.set(i, AlgebraicModule.vectorMultiply(weights, base.get(i)));
      }
    }
    return new Pair<ArrayList<String>, ArrayList<Rational[]>>(words, base);
  }

  private static int[] boolArrayToIntArray(boolean boolArr[]) {
    int intArr[] = new int[boolArr.length];
    for (int i = 0; i < intArr.length; i++)
      intArr[i] = (boolArr[i] ? 1 : 0);
    return intArr;
  }

  public static ArrayList<Rational[]> getAllSubsets(boolean[][] selectedStatesByColor, Rational[] weights, boolean zeroSum,
                                                    boolean eigenVectorPreprocess, boolean eigenVectorZeroSum) {
    ArrayList<Rational[]> result = new ArrayList<>();
    for (boolean[] selectedStates : selectedStatesByColor) {
      int[] subset = boolArrayToIntArray(selectedStates);

      Rational[] rationalSubset = AlgebraicModule.toRationalArray(subset);

      if (eigenVectorPreprocess)
        rationalSubset = AlgebraicModule.vectorMultiply(weights, rationalSubset);
      if (zeroSum)
        rationalSubset = AlgebraicModule.normalize(rationalSubset);
      if (eigenVectorZeroSum)
        rationalSubset = AlgebraicModule.normalize(rationalSubset, weights);

      if (AlgebraicModule.leadingZerosCount(rationalSubset) < rationalSubset.length)
        result.add(rationalSubset);

    }
    return result;
  }

  public static ArrayList<Rational[]> prepareL0Base(DFA dfa, boolean[][] selectedStatesByColor,
                                                    Rational[] weights, boolean zeroSum,
                                                    boolean eigenVectorPreprocess, boolean eigenVectorZeroSum) {
    ArrayList<Rational[]> base = new ArrayList<>();
    ArrayList<Rational[]> subsets = getAllSubsets(selectedStatesByColor, weights, zeroSum, eigenVectorPreprocess,
      eigenVectorZeroSum);
    for (Rational[] subset : subsets) {
      Rational[] vector = AlgebraicModule.matMul(subset, AlgebraicModule.wordToMatrix(dfa, ""));
      if (!AlgebraicModule.dependentFromBase(base, vector))
        base.add(vector);
    }
    return base;
  }

  private static String findChangingSumLetter(AbstractNFA automaton, Rational[] baseVector,
                                              Rational[] weights, boolean increaseDecrease) {
    for (int k = 0; k < automaton.getK(); k++) {
      String b = Character.toString(Config.TRANSITIONS_LETTERS[k]);
      Rational[] vector = AlgebraicModule.matMul(baseVector, AlgebraicModule.wordToMatrix(automaton, b));
      Rational vectorWeight = AlgebraicModule.weightedSumOfSubset(vector, weights);
      Rational baseVectorWeight = AlgebraicModule.weightedSumOfSubset(baseVector, weights);

      if ((increaseDecrease ? (vectorWeight.compareTo(baseVectorWeight) > 0)
        : (vectorWeight.compareTo(baseVectorWeight) < 0)))
        return b;
    }
    return null;
  }

}
