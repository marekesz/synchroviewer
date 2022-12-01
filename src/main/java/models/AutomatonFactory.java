package models;

import java.util.function.Function;



public abstract class AutomatonFactory {

    public static Function<Integer, Automaton> getCernySeries() {
        return n -> {
            StringBuilder s = new StringBuilder();
            s.append("2 " + n + " ");
            for (int i = 0; i < n - 1; i++) {
                s.append((i + 1) + " " + i + " ");
            }
            s.append("0 0");
            return new Automaton(s.toString());
        };
    }

}
