package algorithms;

import models.DFA;


public abstract class Helper {
  public static int subsetToValue(DFA automaton, boolean[] subset) {
    int value = 0;
    for (int i = 0; i < subset.length; i++)
      value = 2 * value + (subset[i] ? 1 : 0);
    return value;
  }

  public static class UnionFind {
    public int[] id;
    int[] count;

    public UnionFind(int N) {
      id = new int[N];
      count = new int[N];
      for (int i = 0; i < N; i = i + 1) {
        id[i] = i;
        count[i] = i;
      }
    }

    public int find(int v) {
      if (id[v] == v) return v;
      int u = find(id[v]);
      id[v] = u;
      return u;
    }

    public boolean union(int v1, int v2) {
      int u1 = find(v1);
      int u2 = find(v2);
      if (u1 == u2) return false;
      if (count[u1] >= count[u2]) {
        id[u1] = u2;
        count[u2] += count[u1];
      } else {
        id[u2] = u1;
        count[u1] += count[u2];
      }
      return true;
    }

  }


}
