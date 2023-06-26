package algorithms;

public class WordNotFoundException extends Exception {

  @Override
  public String getMessage() {
    return "Word not found";
  }

  private static final long serialVersionUID = 1L;
}
