package pedroleonez.jsfff.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import pedroleonez.jsfff.model.Tarefa;
import pedroleonez.jsfff.repository.TarefaRepository;

import java.io.Serializable;

public class TarefaService implements Serializable {

    @Inject
    private TarefaRepository repository;

    @Inject
    private EntityManager em;

    public void salvar(Tarefa tarefa) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            repository.salvar(tarefa);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
