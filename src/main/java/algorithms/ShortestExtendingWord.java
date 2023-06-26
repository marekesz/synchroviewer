package algorithms;

import models.DFA;
import models.InverseDFA;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class ShortestExtendingWord {

  private static BigInteger weightedSumOfSubset(int subset, BigInteger[] weights, DFA a) {
    BigInteger result = new BigInteger("0");
    for (int i = 0; i < a.getN(); i++)
      result = result.add(new BigInteger(Integer.toString(subset >> (a.getN() - 1 - i))).multiply(weights[i]));
    return result;
  }

  public static boolean checkWeightCondition(DFA automaton, int subsetValue, boolean selectedStates[],
                                             BigInteger[] weights) {
    // int[] subset = Helper.valueToSubset(automaton, subsetValue);
    return weightedSumOfSubset(subsetValue, weights, automaton).compareTo(weightedSumOfSubset(Helper.subsetToValue(automaton, selectedStates), weights, automaton)) > 0;
  }

  public static ArrayList<Integer> find(DFA automaton, InverseDFA inverseAutomaton, boolean[] subset,
                                        int destinationSize) throws WordNotFoundException {
    int N = automaton.getN();
    int K = automaton.getK();

    if (N == 0)
      throw new WordNotFoundException();

    boolean[] visited = new boolean[2 << automaton.getN()];
    int[] fromWhereSubsetVal = new int[visited.length];
    int[] fromWhereTransition = new int[visited.length];
    Arrays.fill(visited, false);
    Arrays.fill(fromWhereSubsetVal, -1);
    Arrays.fill(fromWhereTransition, -1);

    int[] queue = new int[visited.length];
    int start = 0;
    int end = 0;
    int subsetValue = Helper.subsetToValue(automaton, subset);
    queue[end] = subsetValue;
    end++;
    visited[subsetValue] = true;

    while (start < end) {
      subsetValue = queue[start];
      start++;

      if (Integer.bitCount(subsetValue) >= destinationSize) {
        ArrayList<Integer> transitions = new ArrayList<>();
        while (fromWhereSubsetVal[subsetValue] != -1) {
          transitions.add(fromWhereTransition[subsetValue]);
          subsetValue = fromWhereSubsetVal[subsetValue];
        }

        Collections.reverse(transitions);
        return transitions;
      } else {
        // subset = Helper.valueToSubset(automaton, subsetValue);
        for (int trans = 0; trans < K; trans++) {
          boolean[] newSubset = new boolean[N];
          for (int i = 0; i < subset.length; i++) {
            if ((subsetValue >> (automaton.getN() - 1 - i)) % 2 == 1) {
              int[] subset2 = inverseAutomaton.getMatrix()[i][trans];
              for (int j = 0; j < subset2.length; j++)
                newSubset[subset2[j]] = true;
            }
          }

          int newSubsetValue = Helper.subsetToValue(automaton, newSubset);
          if (!visited[newSubsetValue]) {
            fromWhereSubsetVal[newSubsetValue] = subsetValue;
            fromWhereTransition[newSubsetValue] = trans;
            queue[end] = newSubsetValue;
            end++;
            visited[newSubsetValue] = true;
          }
        }
      }
    }

    throw new WordNotFoundException();
  }

  public static ArrayList<Integer> findWeighted(DFA automaton, InverseDFA inverseAutomaton, boolean[] subset,
                                                Rational[] rationalWeights, boolean[] selectedStates) throws WordNotFoundException {
    int N = automaton.getN();
    int K = automaton.getK();
    BigInteger[] weights = AlgebraicModule.rationalArrayByCommonDenominator(rationalWeights);
    if (N == 0)
      throw new WordNotFoundException();

    boolean[] visited = new boolean[2 << automaton.getN()];
    int[] fromWhereSubsetVal = new int[visited.length];
    int[] fromWhereTransition = new int[visited.length];
    Arrays.fill(visited, false);
    Arrays.fill(fromWhereSubsetVal, -1);
    Arrays.fill(fromWhereTransition, -1);

    int[] queue = new int[visited.length];
    int start = 0;
    int end = 0;
    int subsetValue = Helper.subsetToValue(automaton, subset);
    queue[end] = subsetValue;
    end++;
    visited[subsetValue] = true;

    while (start < end) {
      subsetValue = queue[start];
      start++;

      if (checkWeightCondition(automaton, subsetValue, selectedStates, weights)) {
        ArrayList<Integer> transitions = new ArrayList<>();
        while (fromWhereSubsetVal[subsetValue] != -1) {
          transitions.add(fromWhereTransition[subsetValue]);
          subsetValue = fromWhereSubsetVal[subsetValue];
        }

        Collections.reverse(transitions);
        return transitions;
      } else {
        // subset = Helper.valueToSubset(automaton, subsetValue);
        for (int trans = 0; trans < K; trans++) {
          boolean[] newSubset = new boolean[N];
          for (int i = 0; i < N; i++) {
            if ((subsetValue >> (N - 1 - i)) % 2 == 1) {
              int[] subset2 = inverseAutomaton.getMatrix()[i][trans];
              for (int j = 0; j < subset2.length; j++)
                newSubset[subset2[j]] = true;
            }
          }

          int newSubsetValue = Helper.subsetToValue(automaton, newSubset);
          if (!visited[newSubsetValue]) {
            fromWhereSubsetVal[newSubsetValue] = subsetValue;
            fromWhereTransition[newSubsetValue] = trans;
            queue[end] = newSubsetValue;
            end++;
            visited[newSubsetValue] = true;
          }
        }
      }
    }

    throw new WordNotFoundException();
  }

}
