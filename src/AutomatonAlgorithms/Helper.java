
package AutomatonAlgorithms;

import AutomatonModels.Automaton;


public abstract class Helper
{
    
    public static int subsetToValue(Automaton automaton, int[] subset)
    {
        int value = 0;
        for (int i = 0; i < subset.length; i++)
            value = 2 * value + subset[i];
        
        return value;
    }
    
    public static int[] valueToSubset(Automaton automaton, int value)
    {
        int[] subset = new int[automaton.getN()];
        for (int i = 0; i < subset.length; i++)
        {
            subset[subset.length - 1 - i] = (int) (value % 2);
            value /= 2;
            if (value == 0)
                break;
        }
        
        return subset;
    }
}
