package pedroleonez.jsfff.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import pedroleonez.jsfff.exception.InfrastructureException;
import pedroleonez.jsfff.exception.ResourceNotFoundException;
import pedroleonez.jsfff.exception.ValidationException;
import pedroleonez.jsfff.model.Tarefa;
import pedroleonez.jsfff.repository.TarefaRepository;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Camada de negócio responsável por validar operações e controlar transações antes
 * de delegar a persistência ao repositório.
 */
public class TarefaService implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(TarefaService.class.getName());

    @Inject
    private TarefaRepository repository;

    @Inject
    private EntityManager em;

    public void salvar(Tarefa tarefa) {
        if (tarefa == null) {
            throw new ValidationException("Nenhuma tarefa foi informada para salvar.");
        }

        // A transação fica concentrada no serviço para manter a regra de negócio consistente.
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            repository.salvar(tarefa);
            tx.commit();
        } catch (PersistenceException | IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.log(Level.SEVERE, "Erro tecnico ao salvar tarefa", e);
            throw new InfrastructureException(
                    "Erro tecnico ao salvar tarefa",
                    "Nao foi possivel salvar a tarefa no momento.",
                    e
            );
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public List<Tarefa> listarTodas() {
        try {
            return repository.buscarTodas();
        } catch (PersistenceException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Erro tecnico ao listar tarefas", e);
            throw new InfrastructureException(
                    "Erro tecnico ao listar tarefas",
                    "Nao foi possivel carregar a lista de tarefas.",
                    e
            );
        }
    }

    /**
     * Método para busca avançada com filtros dinâmicos
     */
    public List<Tarefa> listarComFiltros(Long id, String texto, String responsavel, Boolean concluida) {
        // O repositório monta a consulta dinamicamente de acordo com os filtros preenchidos.
        try {
            return repository.buscarComFiltros(id, texto, responsavel, concluida);
        } catch (PersistenceException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Erro tecnico ao consultar tarefas com filtros", e);
            throw new InfrastructureException(
                    "Erro tecnico ao consultar tarefas com filtros",
                    "Nao foi possivel buscar as tarefas com os filtros informados.",
                    e
            );
        }
    }

    public void remover(Tarefa tarefa) {
        if (tarefa == null || tarefa.getId() == null) {
            throw new ValidationException("Selecione uma tarefa valida para remover.");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            validarTarefaExistente(tarefa.getId());
            repository.excluir(tarefa.getId());
            tx.commit();
        } catch (PersistenceException | IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.log(Level.SEVERE, "Erro tecnico ao remover tarefa", e);
            throw new InfrastructureException(
                    "Erro tecnico ao remover tarefa",
                    "Nao foi possivel remover a tarefa no momento.",
                    e
            );
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public void concluir(Tarefa tarefa) {
        if (tarefa == null || tarefa.getId() == null) {
            throw new ValidationException("Selecione uma tarefa valida para concluir.");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Tarefa t = repository.buscarPorId(tarefa.getId());
            if (t == null) {
                throw new ResourceNotFoundException("A tarefa selecionada nao foi encontrada.");
            }
            // Evita retrabalho e mantém a ação idempotente do ponto de vista funcional.
            if (t.isConcluida()) {
                throw new ValidationException("A tarefa selecionada ja esta concluida.");
            }

            t.setConcluida(true);
            repository.salvar(t);
            tx.commit();
        } catch (PersistenceException | IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.log(Level.SEVERE, "Erro tecnico ao concluir tarefa", e);
            throw new InfrastructureException(
                    "Erro tecnico ao concluir tarefa",
                    "Nao foi possivel concluir a tarefa no momento.",
                    e
            );
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    private void validarTarefaExistente(Long id) {
        // Garante que a exclusão só ocorra para registros ainda existentes no banco.
        if (repository.buscarPorId(id) == null) {
            throw new ResourceNotFoundException("A tarefa selecionada nao existe mais.");
        }
    }
}