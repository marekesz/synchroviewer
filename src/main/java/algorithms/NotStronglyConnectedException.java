package algorithms;

public class NotStronglyConnectedException extends Exception {

    @Override
    public String getMessage() {
        return "Not strongly connected";
    }

    private static final long serialVersionUID = 1L;
}
