package pedroleonez.jsfff.exception;

public class ApplicationException extends RuntimeException {

    private final String userMessage;

    public ApplicationException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    public ApplicationException(String technicalMessage, String userMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
