package pedroleonez.jsfff.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pedroleonez.jsfff.model.Prioridade;
import pedroleonez.jsfff.model.Tarefa;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TarefaRepositoryH2Test {

    private static EntityManagerFactory emf;

    private EntityManager em;
    private TarefaRepository repository;

    @BeforeAll
    static void initFactory() {
        emf = Persistence.createEntityManagerFactory("tarefasPUTest");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        em = emf.createEntityManager();
        repository = new TarefaRepository();
        inject(repository, "em", em);

        em.getTransaction().begin();
        em.createQuery("DELETE FROM Tarefa").executeUpdate();
        em.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @Test
    void deveListarTodasOrdenadasPorIdDesc() {
        persistir(criarTarefa("Primeira", "Descricao 1", "Ana", false));
        Tarefa segunda = persistir(criarTarefa("Segunda", "Descricao 2", "Bruno", false));

        List<Tarefa> tarefas = repository.buscarTodas();

        assertEquals(2, tarefas.size());
        assertEquals(segunda.getId(), tarefas.get(0).getId());
    }

    @Test
    void deveFiltrarPorTextoNoTituloOuDescricao() {
        persistir(criarTarefa("Estudar JSF", "Camada web", "Ana", false));
        persistir(criarTarefa("Revisar API", "Ajustar filtro dinâmico", "Carlos", false));

        List<Tarefa> tarefas = repository.buscarComFiltros(null, "filtro", null, null);

        assertEquals(1, tarefas.size());
        assertEquals("Revisar API", tarefas.get(0).getTitulo());
    }

    @Test
    void deveFiltrarPorResponsavelESituacao() {
        persistir(criarTarefa("T1", "D1", "Ana", false));
        persistir(criarTarefa("T2", "D2", "Ana", true));
        persistir(criarTarefa("T3", "D3", "Bruno", true));

        List<Tarefa> tarefas = repository.buscarComFiltros(null, null, "Ana", true);

        assertEquals(1, tarefas.size());
        assertEquals("T2", tarefas.get(0).getTitulo());
        assertTrue(tarefas.get(0).isConcluida());
    }

    @Test
    void deveExcluirPorId() {
        Tarefa tarefa = persistir(criarTarefa("Excluir", "Remover item", "Dani", false));
        assertNotNull(repository.buscarPorId(tarefa.getId()));

        em.getTransaction().begin();
        repository.excluir(tarefa.getId());
        em.getTransaction().commit();

        assertNull(repository.buscarPorId(tarefa.getId()));
    }

    private Tarefa persistir(Tarefa tarefa) {
        em.getTransaction().begin();
        repository.salvar(tarefa);
        em.getTransaction().commit();
        return tarefa;
    }

    private Tarefa criarTarefa(String titulo, String descricao, String responsavel, boolean concluida) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(descricao);
        tarefa.setResponsavel(responsavel);
        tarefa.setDeadline(LocalDate.now().plusDays(2));
        tarefa.setPrioridade(Prioridade.MEDIA);
        tarefa.setConcluida(concluida);
        return tarefa;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
