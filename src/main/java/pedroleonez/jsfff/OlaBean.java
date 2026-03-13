package pedroleonez.jsfff;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

@Named
@RequestScoped
public class OlaBean {
    private String mensagem = "Ambiente JSF configurado com sucesso!";
    // Getter
    public String getMensagem() { return mensagem; }
}