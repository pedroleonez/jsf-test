package pedroleonez.jsfff.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pedroleonez.jsfff.exception.InfrastructureException;
import pedroleonez.jsfff.exception.ResourceNotFoundException;
import pedroleonez.jsfff.exception.ValidationException;
import pedroleonez.jsfff.model.Tarefa;
import pedroleonez.jsfff.repository.TarefaRepository;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository repository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    private TarefaService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new TarefaService();
        inject(service, "repository", repository);
        inject(service, "em", entityManager);
        lenient().when(entityManager.getTransaction()).thenReturn(transaction);
    }

    @Test
    void deveLancarValidationExceptionAoSalvarTarefaNula() {
        assertThrows(ValidationException.class, () -> service.salvar(null));
        verify(entityManager, never()).getTransaction();
    }

    @Test
    void deveTraduzirErroTecnicoAoSalvar() {
        Tarefa tarefa = new Tarefa();
        when(transaction.isActive()).thenReturn(true);
        doThrow(new PersistenceException("falha de banco")).when(repository).salvar(tarefa);

        assertThrows(InfrastructureException.class, () -> service.salvar(tarefa));

        verify(transaction).begin();
        verify(transaction).rollback();
    }

    @Test
    void deveLancarResourceNotFoundAoConcluirTarefaInexistente() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(10L);
        when(repository.buscarPorId(10L)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> service.concluir(tarefa));

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void deveLancarValidationExceptionAoConcluirTarefaJaConcluida() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(20L);

        Tarefa existente = new Tarefa();
        existente.setId(20L);
        existente.setConcluida(true);

        when(repository.buscarPorId(20L)).thenReturn(existente);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(ValidationException.class, () -> service.concluir(tarefa));

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(repository, never()).salvar(existente);
    }

    @Test
    void deveConcluirTarefaComSucesso() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(30L);

        Tarefa existente = new Tarefa();
        existente.setId(30L);
        existente.setConcluida(false);

        when(repository.buscarPorId(30L)).thenReturn(existente);

        service.concluir(tarefa);

        verify(transaction).begin();
        verify(repository).salvar(existente);
        verify(transaction).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void deveLancarValidationExceptionAoRemoverSemId() {
        Tarefa tarefa = new Tarefa();

        assertThrows(ValidationException.class, () -> service.remover(tarefa));
        verify(entityManager, never()).getTransaction();
    }

    @Test
    void deveLancarResourceNotFoundAoRemoverTarefaInexistente() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(99L);
        when(repository.buscarPorId(99L)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> service.remover(tarefa));

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(repository, never()).excluir(99L);
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
