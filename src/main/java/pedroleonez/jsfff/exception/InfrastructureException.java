package pedroleonez.jsfff.exception;

public class InfrastructureException extends ApplicationException {

    public InfrastructureException(String technicalMessage, String userMessage, Throwable cause) {
        super(technicalMessage, userMessage, cause);
    }
}
