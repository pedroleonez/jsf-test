package pedroleonez.jsfff.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pedroleonez.jsfff.model.Tarefa;
import pedroleonez.jsfff.model.Prioridade;
import pedroleonez.jsfff.service.TarefaService;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TarefaBean implements Serializable {

    @Inject
    private TarefaService service;

    private Tarefa tarefa = new Tarefa();
    private List<Tarefa> tarefas;

    @PostConstruct
    public void init() {
        atualizarLista();
    }

    public void salvar() {
        service.salvar(tarefa);
        tarefa = new Tarefa(); // Limpa o formulário
        atualizarLista();
    }

    public void concluir(Tarefa t) {
        service.concluir(t);
        atualizarLista();
    }

    public void remover(Tarefa t) {
        service.remover(t);
        atualizarLista();
    }

    private void atualizarLista() {
        tarefas = service.listarTodas();
    }

    // Getters e Setters
    public Tarefa getTarefa() { return tarefa; }
    public void setTarefa(Tarefa tarefa) { this.tarefa = tarefa; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public Prioridade[] getPrioridades() { return Prioridade.values(); }
}
