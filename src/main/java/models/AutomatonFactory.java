package models;

import java.util.function.Function;


public abstract class AutomatonFactory {

  public static Function<Integer, DFA> getCycle() {
    return n -> {
      StringBuilder s = new StringBuilder();
      s.append("1 ").append(n).append(" ");
      for (int i = 0; i < n - 1; i++) {
        s.append(i + 1).append(" ");
      }
      s.append("0");
      return new DFA(s.toString());
    };
  }

  public static Function<Integer, DFA> getCerny() {
    return n -> {
      StringBuilder s = new StringBuilder();
      s.append("2 ").append(n).append(" ");
      for (int i = 0; i < n - 1; i++) {
        s.append(i + 1).append(" ").append(i).append(" ");
      }
      s.append("0 0");
      return new DFA(s.toString());
    };
  }

  public static Function<Integer, DFA> getSlowlySynchronizingWithSink() {
    return n -> {
      StringBuilder s = new StringBuilder();
      s.append(n-1).append(" ").append(n).append(" ");
      for (int i = 0; i < n; i++) {
        if (i == 0) {
          for (int k = 0; k < n-1; k++) s.append("0").append(" ");
        } else {
          for (int k = 0; k < n-1; k++) {
            if (k + 1 == i) s.append(i - 1);
            else if (k == i) s.append(i+1); else
              s.append(i);
            s.append(" ");
          }
        }
      }
      return new DFA(s.toString().strip());
    };
  }
}
