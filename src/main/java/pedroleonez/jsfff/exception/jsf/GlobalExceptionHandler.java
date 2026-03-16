package pedroleonez.jsfff.exception.jsf;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import pedroleonez.jsfff.exception.ApplicationException;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intercepta exceções não tratadas no ciclo JSF e converte falhas técnicas ou de negócio
 * em mensagens amigáveis para a interface.
 */
public class GlobalExceptionHandler extends ExceptionHandlerWrapper {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());

    private final ExceptionHandler wrapped;

    public GlobalExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> unhandledEvents = getUnhandledExceptionQueuedEvents().iterator();

        while (unhandledEvents.hasNext()) {
            ExceptionQueuedEvent event = unhandledEvents.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable rootCause = resolveRootCause(context.getException());

            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                LOGGER.log(Level.SEVERE, "Erro nao tratado na camada JSF", rootCause);

                String userMessage = "Ocorreu um erro inesperado. Tente novamente em instantes.";
                if (rootCause instanceof ApplicationException applicationException) {
                    userMessage = applicationException.getUserMessage();
                }

                facesContext.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Erro",
                        userMessage
                ));
                // Mantém a mensagem disponível após o redirect para a mesma view.
                facesContext.getExternalContext().getFlash().setKeepMessages(true);

                String viewId = facesContext.getViewRoot() != null
                        ? facesContext.getViewRoot().getViewId()
                        : "/index.xhtml";
                facesContext.getApplication().getNavigationHandler()
                        .handleNavigation(facesContext, null, viewId + "?faces-redirect=true");
                facesContext.renderResponse();
            }

            unhandledEvents.remove();
        }

        getWrapped().handle();
    }

    private Throwable resolveRootCause(Throwable throwable) {
        // Caminha até a causa mais profunda para logar e exibir a mensagem mais útil.
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
