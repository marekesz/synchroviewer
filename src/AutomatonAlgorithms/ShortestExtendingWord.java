
package AutomatonAlgorithms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import AutomatonModels.AbstractNFA;
import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;

public abstract class ShortestExtendingWord {

    private static BigInteger weightedSumOfSubset(int subset[], BigInteger[] weights) {
        BigInteger result = new BigInteger("0");
        for (int i = 0; i < subset.length; i++)
            result = result.add(new BigInteger(Integer.toString(subset[i])).multiply(weights[i]));
        return result;
    }

    public static boolean checkWeightCondition(Automaton automaton, int subsetValue, int selectedStates[],
            BigInteger[] weights) {
        int[] subset = Helper.valueToSubset(automaton, subsetValue);
        // System.out.println("for weights: ");
        // AlgebraicModule.printArray(weights);
        // System.out.println("for subset: ");
        // AlgebraicModule.printArray(subset);
        // System.out.println(
        // weightedSumOfSubset(subset, weights).toString() + " > " +
        // weightedSumOfSubset(selectedStates, weights));
        return weightedSumOfSubset(subset, weights).compareTo(weightedSumOfSubset(selectedStates, weights)) > 0;
    }

    public static ArrayList<Integer> find(Automaton automaton, InverseAutomaton inverseAutomaton, int[] subset,
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
                subset = Helper.valueToSubset(automaton, subsetValue);
                for (int trans = 0; trans < K; trans++) {
                    int[] newSubset = new int[N];
                    for (int i = 0; i < subset.length; i++) {
                        if (subset[i] == 1) {
                            int[] subset2 = inverseAutomaton.getMatrix()[i][trans];
                            for (int j = 0; j < subset2.length; j++)
                                newSubset[subset2[j]] = 1;
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

    public static ArrayList<Integer> findWeighted(Automaton automaton, InverseAutomaton inverseAutomaton, int[] subset,
            Rational[] rationalWeights, int[] selectedStates) throws WordNotFoundException {
        int N = automaton.getN();
        int K = automaton.getK();
        // AlgebraicModule.printArray(rationalWeights);
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
                subset = Helper.valueToSubset(automaton, subsetValue);
                for (int trans = 0; trans < K; trans++) {
                    int[] newSubset = new int[N];
                    for (int i = 0; i < subset.length; i++) {
                        if (subset[i] == 1) {
                            int[] subset2 = inverseAutomaton.getMatrix()[i][trans];
                            for (int j = 0; j < subset2.length; j++)
                                newSubset[subset2[j]] = 1;
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
