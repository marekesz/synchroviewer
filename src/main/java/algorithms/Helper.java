
package algorithms;

import models.Automaton;


public abstract class Helper {

    public static int subsetToValue(Automaton automaton, int[] subset) {
        int value = 0;
        for (int i = 0; i < subset.length; i++)
            value = 2 * value + subset[i];

        return value;
    }


}
