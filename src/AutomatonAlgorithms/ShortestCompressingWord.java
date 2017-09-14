
package AutomatonAlgorithms;

import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class ShortestCompressingWord
{
  
    public static ArrayList<Integer> find(Automaton automaton, InverseAutomaton inverseAutomaton, int[] subset) throws WordNotFoundException
    {
        int N = automaton.getN();
        int K = automaton.getK();
        
        boolean[] visited = new boolean[N*(N-1)];
        int[] fromWherePair = new int[N*(N-1)];
        int[] fromWhereTransition = new int[N*(N-1)];
        Arrays.fill(visited, false);
        Arrays.fill(fromWherePair, -1);
        Arrays.fill(fromWhereTransition, -1);
        
        int[] queue = new int[N*(N-1)/2];
        Arrays.fill(queue, -1);
        int start = 0;
        int end = 0;
        
        for (int n = 0; n < N; n++)
        {
            for (int k = 0; k < K; k++)
            {   
                int[] states = inverseAutomaton.getMatrix()[n][k];
                for (int i1 = 0; i1 < states.length; i1++)
                {
                    for (int i2 = 0; i2 < states.length; i2++)
                    {
                        int a = states[i1];
                        int b = states[i2];
                        if (a >= b)
                            continue;
                        
                        if (!visited[a*N + b]) 
                        {
                            if (subset[a] == 1 && subset[b] == 1)
                            {
                                ArrayList<Integer> transitions = new ArrayList<>();
                                transitions.add(k);
                                return transitions;
                            }

                            visited[a*N + b] = true;
                            fromWhereTransition[a*N + b] = k;
                            queue[end] = a*N + b;
                            end++;
                        }
                    }
                }
            }
        }
        
        while (start < end) 
        {
            int q = queue[start] / N;
            int p = queue[start] % N;
            start++;
            
            for (int k = 0; k < K; k++)
            {   
                int[] states1 = inverseAutomaton.getMatrix()[q][k];
                int[] states2 = inverseAutomaton.getMatrix()[p][k];

                for (int i1 = 0; i1 < states1.length; i1++)
                {
                    for (int i2 = 0; i2 < states2.length; i2++)
                    {   
                        int a = states1[i1];
                        int b = states2[i2];
                        if (a > b)
                        {
                            a = states2[i2];
                            b = states1[i1];
                        }

                        if (visited[a*N + b])
                            continue;

                        visited[a*N + b] = true;
                        fromWherePair[a*N + b] = q*N + p;
                        fromWhereTransition[a*N + b] = k;
                        queue[end] = a*N + b;
                        end++;

                        if (subset[a] == 1 && subset[b] == 1)
                        {
                            int pair = a*N + b;
                            ArrayList<Integer> transitions = new ArrayList<>();
                            while (true)
                            {
                                transitions.add(fromWhereTransition[pair]);
                                pair = fromWherePair[pair];

                                if (pair == -1)
                                    break;
                            }

                            return transitions;
                        }
                    }
                }
            }
        }

        throw new WordNotFoundException();
    }
}
