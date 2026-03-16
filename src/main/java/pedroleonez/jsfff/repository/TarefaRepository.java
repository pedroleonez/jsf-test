package pedroleonez.jsfff.repository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pedroleonez.jsfff.model.Tarefa;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de acesso a dados que encapsula operações JPA da entidade Tarefa.
 */
public class TarefaRepository implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    private EntityManager em;

    public void salvar(Tarefa tarefa) {
        // Persist para novos registros e merge para edições de tarefas já existentes.
        if (tarefa.getId() == null) {
            em.persist(tarefa);
        } else {
            em.merge(tarefa);
        }
    }

    public List<Tarefa> buscarTodas() {
        return em.createQuery("SELECT t FROM Tarefa t ORDER BY t.id DESC", Tarefa.class)
                .getResultList();
    }

    /**
     * Busca avançada com filtros dinâmicos usando Criteria API
     */
    public List<Tarefa> buscarComFiltros(Long id, String texto, String responsavel, Boolean concluida) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tarefa> cq = cb.createQuery(Tarefa.class);
        Root<Tarefa> tarefa = cq.from(Tarefa.class);

        // Cada filtro preenchido adiciona um predicado; campos vazios são ignorados.
        List<Predicate> predicates = new ArrayList<>();

        // Filtro por Número (ID)
        if (id != null) {
            predicates.add(cb.equal(tarefa.get("id"), id));
        }

        // Filtro por Título ou Descrição (Prioridade ao título na lógica de OR)
        if (texto != null && !texto.trim().isEmpty()) {
            String likeText = "%" + texto.toLowerCase() + "%";
            Predicate titleLike = cb.like(cb.lower(tarefa.get("titulo")), likeText);
            Predicate descLike = cb.like(cb.lower(tarefa.get("descricao")), likeText);
            // Um único texto pode combinar tanto com o título quanto com a descrição.
            predicates.add(cb.or(titleLike, descLike));
        }

        // Filtro por Responsável
        if (responsavel != null && !responsavel.trim().isEmpty()) {
            predicates.add(cb.equal(tarefa.get("responsavel"), responsavel));
        }

        // Filtro por Situação (Concluída ou Em Andamento)
        if (concluida != null) {
            predicates.add(cb.equal(tarefa.get("concluida"), concluida));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(tarefa.get("id"))); // Mantém as mais novas no topo

        TypedQuery<Tarefa> query = em.createQuery(cq);
        return query.getResultList();
    }

    public Tarefa buscarPorId(Long id) {
        return em.find(Tarefa.class, id);
    }

    public void excluir(Long id) {
        Tarefa t = buscarPorId(id);
        if (t != null) {
            // O merge reanexa a entidade quando ela não está mais sendo gerenciada pelo contexto atual.
            em.remove(em.contains(t) ? t : em.merge(t));
        }
    }
}