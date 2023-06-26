package models;

public abstract class AbstractNFA {

  public abstract int getK();

  public abstract int getN();

  public abstract int[] getTransitions(int i, int k);

}
