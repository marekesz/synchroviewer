package AutomatonAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import AutomatonModels.Automaton;


public abstract class ShortestResetWord
{

    public static ArrayList<Integer> find(Automaton automaton, int[] subset) throws WordNotFoundException
    {
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
        
        while (start < end)
        {
            subsetValue = queue[start];
            start++;
            
            if (Integer.bitCount(subsetValue) == 1) // singleton is a power of two
            {
                ArrayList<Integer> transitions = new ArrayList<>();
                while (fromWhereSubsetVal[subsetValue] != -1)
                {
                    transitions.add(fromWhereTransition[subsetValue]);
                    subsetValue = fromWhereSubsetVal[subsetValue];
                }
                
                Collections.reverse(transitions);
                return transitions;
            }
            else
            {
                subset = Helper.valueToSubset(automaton, subsetValue);
                for (int trans = 0; trans < automaton.getK(); trans++)
                {
                    int[] newSubset = new int[automaton.getN()];
                    for (int i = 0; i < subset.length; i++)
                    {
                        if (subset[i] == 1)
                            newSubset[automaton.getMatrix()[i][trans]] = 1;
                    }
                    
                    int newSubsetValue = Helper.subsetToValue(automaton, newSubset);
                    if (!visited[newSubsetValue])
                    {
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
