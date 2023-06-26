package models;

import main.Config;
import main.ProgramState;

import java.util.ArrayList;

public abstract class MergeAutomation {

  public static DFA merge(int[] arr, DFA a) {
    StringBuilder ret;
    ArrayList<Integer> S = new ArrayList<>();
    int[] Sp = new int[a.getN()];
    for (int i = 0; i < arr.length; i++) {
      if (i == arr[i])
        S.add(i);
    }
    for (int i = 0; i < S.size(); i++) {
      Sp[S.get(i)] = i;
    }

    ret = new StringBuilder(a.getK() + " " + S.size());

    int[][] matrix = a.getMatrix();

    for (int i = 0; i < S.size(); i++) {
      for (int j = 0; j < a.getK(); j++) {
        ret.append(" ").append(Sp[arr[matrix[S.get(i)][j]]]);
      }
    }

    return new DFA(ret.toString().trim());
  }

  public static void loadColor(int[] arr, ProgramState a, boolean[][] ssbc) {
    ArrayList<Integer> S = new ArrayList<>();
    int[] Sp = new int[arr.length];
    for (int i = 0; i < arr.length; i++) {
      if (i == arr[i])
        S.add(i);
    }
    for (int i = 0; i < S.size(); i++) {
      Sp[S.get(i)] = i;
    }

    for (int i = 0; i < arr.length; i++) {
      for (int j = 0; j < Config.STATES_COLORS.length; j++) {
        if (ssbc[j][i])
          a.selectState(Sp[arr[i]], j);
      }
    }

  }
}
