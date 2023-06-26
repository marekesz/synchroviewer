package algorithms;

import main.Config;
import models.DFA;

import java.util.AbstractMap;
import java.util.Stack;

public abstract class Primitivity {

  public static boolean refineToCongruence(DFA automaton, int[] input) {
    Stack<AbstractMap.SimpleEntry<Integer, Integer>> stack = new Stack<>();
    int N = automaton.getN();
    int K = automaton.getK();
    boolean ret = false;
    for (int i = 0; i < N; i++) {
        for (int j = i + 1; j < N; j++) {
          if (input[i] == input[j])
            stack.push(new AbstractMap.SimpleEntry<>(i, j));
        }
    }
    int[] output = new int[N];
    for (int i = 0; i < N; i++) {
      output[i] = i;
    }

    while(!stack.empty()){
      AbstractMap.SimpleEntry<Integer, Integer> pair = stack.pop();
      int v1 = pair.getKey();
      int v2 = pair.getValue();
      if (output[v1] != output[v2]) {
        int[] l1 = getListOfStates(output, output[v1]);
        int[] l2 = getListOfStates(output, output[v2]);
        for (int i = 0; i < l1.length; i++) {
          for (int j = 0; j < l2.length; j++) {
            for (int k = 0; k < K; k++) {
              int a = automaton.getTransitions(v1, k)[0];
              int b = automaton.getTransitions(v2, k)[0];
              if( output[a] != output[b]) {
                stack.push(new AbstractMap.SimpleEntry<>(a, b));
              }
            }

            merge(output,l1,l2);
          }
        }
        if (input[v1] != input[v2]) {
          ret = true;
        }
      }
    }
    for (int i = 0; i < N; i++) {
      input[i] = output[i];
    }
    return ret;
  }

  public static void merge(int[] classes,int[] l1,int[] l2){
      for (int i = 0; i < l2.length; i++) {
        classes[l2[i]] = classes[l1[0]];
      }
  }

  public static int[] getListOfStates(int[] classes, int c){
    int counter = 0;
    for (int i = 0; i < classes.length; i++) {
        if(classes[i] == c)
            counter++;
    }
    int[] out = new int[counter];
    counter = 0;
    for (int i = 0; i < classes.length; i++) {
      if(classes[i] == c) {
        out[counter] = i;
        counter++;
      }
    }
    return out;
  }


  public static boolean isOneClass(int[] arr) {
    for (int i = 1; i < arr.length; i++) {
      if (arr[i] != arr[0])
        return false;
    }
    return true;
  }

  public static boolean isPrimitive(DFA automaton) {
    int N = automaton.getN();
    for (int i = 0; i < N; i++)
      for (int j = i + 1; j < N; j++) {
        int[] arr = new int[N];
        for (int n = 0; n < N; n++)
          arr[n] = n;
        arr[j] = i;
        refineToCongruence(automaton, arr);
        if (!isOneClass(arr)) return false;
      }
    return true;
  }

  public static boolean isCongruence(DFA dfa, boolean[][] selectedStates) {
    int N = dfa.getN();
    Helper.UnionFind unionFind = new Helper.UnionFind(N);// Find equivalence closure
    for (int k = 0; k < Config.STATES_COLORS.length; k++) {

      int state = -1;
      for (int j = 0; j < N; j++) {
        if (selectedStates[k][j]) {
          if (state == -1)
            state = j;
          int t1 = unionFind.find(state);
          int t2 = unionFind.find(j);
          unionFind.union(t1, t2);
        }
      }
    }
    int[] arr = new int[N];
    for (int i = 0; i < N; i++) {
      arr[i] = unionFind.find(i);
    }
    return !refineToCongruence(dfa, arr);
  }



}
