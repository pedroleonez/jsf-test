package pedroleonez.jsfff.repository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pedroleonez.jsfff.model.Tarefa;
import java.io.Serializable;
import java.util.List;

public class TarefaRepository implements Serializable {

    @Inject
    private EntityManager em;

    public void salvar(Tarefa tarefa) {
        if (tarefa.getId() == null) {
            em.persist(tarefa);
        } else {
            em.merge(tarefa);
        }
    }

    public List<Tarefa> buscarTodas() {
        return em.createQuery("SELECT t FROM Tarefa t", Tarefa.class).getResultList();
    }

    public Tarefa buscarPorId(Long id) {
        return em.find(Tarefa.class, id);
    }

    public void excluir(Long id) {
        Tarefa t = buscarPorId(id);
        if (t != null) em.remove(t);
    }
}
