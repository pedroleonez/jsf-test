package pedroleonez.jsfff.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Entidade principal da aplicação. Representa uma tarefa com validações declarativas
 * usadas tanto pela persistência quanto pela camada web.
 */
@Entity
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotBlank(message = "O responsável é obrigatório")
    private String responsavel;

    @NotNull(message = "O deadline é obrigatório")
    @FutureOrPresent(message = "A data não pode ser no passado")
    private LocalDate deadline;

    @NotNull(message = "A prioridade é obrigatória")
    @Enumerated(EnumType.STRING)
    // Enum salvo como texto para manter legibilidade e evitar dependência da ordem ordinal.
    private Prioridade prioridade;

    // Novas tarefas começam abertas e só mudam de estado pela ação de concluir.
    private boolean concluida = false;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getResponsavel() {
        return responsavel;
    }
    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDate getDeadline() {
        return deadline;
    }
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }
    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public boolean isConcluida() {
        return concluida;
    }
    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
}

