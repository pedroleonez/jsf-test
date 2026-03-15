package pedroleonez.jsfff.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pedroleonez.jsfff.model.Tarefa;
import pedroleonez.jsfff.model.Prioridade;
import pedroleonez.jsfff.service.TarefaService;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class TarefaBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    private TarefaService service;

    // Objeto para cadastro/edição
    private Tarefa tarefa = new Tarefa();

    // Lista principal e lista para o PrimeFaces gerenciar internamente
    private List<Tarefa> tarefas;
    private List<Tarefa> tarefasFiltradas;

    // Campos de filtro para a busca avançada
    private Long filtroId;
    private String filtroTexto; // Título ou Descrição
    private String filtroResponsavel;
    private Boolean filtroConcluida;

    @PostConstruct
    public void init() {
        atualizarLista();
    }

    public void salvar() {
        try {
            service.salvar(tarefa);
            adicionarMensagem("Sucesso!", "Tarefa salva com êxito.");
            tarefa = new Tarefa();
            atualizarLista();
        } catch (Exception e) {
            adicionarMensagem("Erro", "Falha ao salvar tarefa: " + e.getMessage());
        }
    }

    /**
     * Método acionado pelo botão "Buscar Tarefas"
     * Aqui você pode optar por filtrar via Java (Stream) para performance em listas pequenas
     * ou passar os parâmetros para o Service/Repository buscar no SQL (Recomendado para o teste).
     */
    public void buscar() {
        // Opção robusta: Chamar o serviço passando os filtros
        // Para fins de teste rápido, você pode filtrar a lista atual ou recarregar do banco:
        this.tarefas = service.listarComFiltros(filtroId, filtroTexto, filtroResponsavel, filtroConcluida);

        if (tarefas.isEmpty()) {
            adicionarMensagem("Informação", "Nenhuma tarefa encontrada para os critérios.");
        }
    }

    public void prepararEdicao(Tarefa t) {
        this.tarefa = t;
    }

    public void concluir(Tarefa t) {
        try {
            service.concluir(t);
            adicionarMensagem("Concluída", "Tarefa marcada como concluída.");
            atualizarLista();
        } catch (Exception e) {
            adicionarMensagem("Erro", "Não foi possível concluir a tarefa.");
        }
    }

    public void remover(Tarefa t) {
        try {
            service.remover(t);
            adicionarMensagem("Removida", "Tarefa excluída do sistema.");
            atualizarLista();
        } catch (Exception e) {
            adicionarMensagem("Erro", "Erro ao remover: " + e.getMessage());
        }
    }

    private void atualizarLista() {
        this.tarefas = service.listarTodas();
    }

    private void adicionarMensagem(String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, resumo, detalhe));
    }

    public LocalDate getDataAtual() {
        return LocalDate.now();
    }

    // Getters e Setters para os Filtros
    public Long getFiltroId() { return filtroId; }
    public void setFiltroId(Long filtroId) { this.filtroId = filtroId; }

    public String getFiltroTexto() { return filtroTexto; }
    public void setFiltroTexto(String filtroTexto) { this.filtroTexto = filtroTexto; }

    public String getFiltroResponsavel() { return filtroResponsavel; }
    public void setFiltroResponsavel(String filtroResponsavel) { this.filtroResponsavel = filtroResponsavel; }

    public Boolean getFiltroConcluida() { return filtroConcluida; }
    public void setFiltroConcluida(Boolean filtroConcluida) { this.filtroConcluida = filtroConcluida; }

    // Getters e Setters padrões
    public Tarefa getTarefa() { return tarefa; }
    public void setTarefa(Tarefa tarefa) { this.tarefa = tarefa; }

    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }

    public List<Tarefa> getTarefasFiltradas() { return tarefasFiltradas; }
    public void setTarefasFiltradas(List<Tarefa> tarefasFiltradas) { this.tarefasFiltradas = tarefasFiltradas; }

    public Prioridade[] getPrioridades() { return Prioridade.values(); }

    // Método auxiliar para o Select de Responsáveis (evita duplicados na lista de filtro)
    public List<String> getListaResponsaveis() {
        return tarefas.stream()
                .map(Tarefa::getResponsavel)
                .filter(r -> r != null && !r.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}