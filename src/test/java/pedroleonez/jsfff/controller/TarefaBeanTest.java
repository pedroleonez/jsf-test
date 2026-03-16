package pedroleonez.jsfff.controller;

import jakarta.el.ELContext;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pedroleonez.jsfff.exception.ValidationException;
import pedroleonez.jsfff.model.Prioridade;
import pedroleonez.jsfff.model.Tarefa;
import pedroleonez.jsfff.service.TarefaService;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TarefaBeanTest {

    private TestFacesContext facesContext;
    private TarefaBean bean;
    private FakeTarefaService service;

    @BeforeEach
    void setUp() throws Exception {
        facesContext = new TestFacesContext();
        bean = new TarefaBean();
        service = new FakeTarefaService();
        inject(bean, "service", service);
    }

    @AfterEach
    void tearDown() {
        facesContext.release();
    }

    @Test
    void initDeveCarregarListaDeTarefas() {
        service.tarefasParaListar = List.of(criarTarefa("A"), criarTarefa("B"));

        bean.init();

        assertEquals(2, bean.getTarefas().size());
    }

    @Test
    void salvarDeveResetarTarefaAtualizarListaEMostrarMensagemDeSucesso() {
        Tarefa original = criarTarefa("Salvar");
        service.tarefasParaListar = List.of(original);
        bean.setTarefa(original);

        bean.salvar();

        assertEquals(original, service.ultimaTarefaSalva);
        assertNotSame(original, bean.getTarefa());
        assertEquals(1, bean.getTarefas().size());
        assertUltimaMensagem(FacesMessage.SEVERITY_INFO, "Sucesso!", "Tarefa salva com exito.");
    }

    @Test
    void salvarDeveMostrarMensagemDeErroQuandoServiceLancarExcecao() {
        service.excecaoSalvar = new ValidationException("Dados invalidos");
        bean.setTarefa(criarTarefa("Com erro"));

        bean.salvar();

        assertUltimaMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Dados invalidos");
    }

    @Test
    void buscarDeveAplicarFiltrosEInformarQuandoNaoHouverResultados() {
        bean.setFiltroId(7L);
        bean.setFiltroTexto("infra");
        bean.setFiltroResponsavel("Ana");
        bean.setFiltroConcluida(true);
        service.resultadoFiltros = List.of();

        bean.buscar();

        assertEquals(7L, service.ultimoFiltroId);
        assertEquals("infra", service.ultimoFiltroTexto);
        assertEquals("Ana", service.ultimoFiltroResponsavel);
        assertEquals(true, service.ultimoFiltroConcluida);
        assertTrue(bean.getTarefas().isEmpty());
        assertUltimaMensagem(FacesMessage.SEVERITY_INFO, "Informacao", "Nenhuma tarefa encontrada para os criterios.");
    }

    @Test
    void concluirDeveMostrarErroQuandoServiceFalhar() {
        Tarefa tarefa = criarTarefa("Concluir");
        tarefa.setId(10L);
        service.excecaoConcluir = new ValidationException("Ja concluida");

        bean.concluir(tarefa);

        assertEquals(tarefa, service.ultimaTarefaConcluida);
        assertUltimaMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Ja concluida");
    }

    @Test
    void getListaResponsaveisDeveRemoverDuplicadosEVazios() {
        List<Tarefa> lista = new ArrayList<>();
        lista.add(criarTarefaComResponsavel("T1", "Ana"));
        lista.add(criarTarefaComResponsavel("T2", ""));
        lista.add(criarTarefaComResponsavel("T3", null));
        lista.add(criarTarefaComResponsavel("T4", "Ana"));
        lista.add(criarTarefaComResponsavel("T5", "Bruno"));

        bean.setTarefas(lista);

        List<String> responsaveis = bean.getListaResponsaveis();

        assertEquals(2, responsaveis.size());
        assertTrue(responsaveis.contains("Ana"));
        assertTrue(responsaveis.contains("Bruno"));
    }

    private void assertUltimaMensagem(FacesMessage.Severity severity, String summary, String detail) {
        FacesMessage mensagem = facesContext.getLastMessage();
        assertNotNull(mensagem);
        assertEquals(severity, mensagem.getSeverity());
        assertEquals(summary, mensagem.getSummary());
        assertEquals(detail, mensagem.getDetail());
    }

    private static Tarefa criarTarefa(String titulo) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(titulo);
        tarefa.setDescricao("Descricao " + titulo);
        tarefa.setResponsavel("Ana");
        tarefa.setDeadline(LocalDate.now().plusDays(1));
        tarefa.setPrioridade(Prioridade.MEDIA);
        return tarefa;
    }

    private static Tarefa criarTarefaComResponsavel(String titulo, String responsavel) {
        Tarefa tarefa = criarTarefa(titulo);
        tarefa.setResponsavel(responsavel);
        return tarefa;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class FakeTarefaService extends TarefaService {

        private Tarefa ultimaTarefaSalva;
        private Tarefa ultimaTarefaConcluida;
        private ValidationException excecaoSalvar;
        private ValidationException excecaoConcluir;

        private List<Tarefa> tarefasParaListar = List.of();
        private List<Tarefa> resultadoFiltros = List.of();

        private Long ultimoFiltroId;
        private String ultimoFiltroTexto;
        private String ultimoFiltroResponsavel;
        private Boolean ultimoFiltroConcluida;

        @Override
        public void salvar(Tarefa tarefa) {
            ultimaTarefaSalva = tarefa;
            if (excecaoSalvar != null) {
                throw excecaoSalvar;
            }
        }

        @Override
        public List<Tarefa> listarTodas() {
            return tarefasParaListar;
        }

        @Override
        public List<Tarefa> listarComFiltros(Long id, String texto, String responsavel, Boolean concluida) {
            ultimoFiltroId = id;
            ultimoFiltroTexto = texto;
            ultimoFiltroResponsavel = responsavel;
            ultimoFiltroConcluida = concluida;
            return resultadoFiltros;
        }

        @Override
        public void concluir(Tarefa tarefa) {
            ultimaTarefaConcluida = tarefa;
            if (excecaoConcluir != null) {
                throw excecaoConcluir;
            }
        }
    }

    private static class TestFacesContext extends FacesContext {

        private final List<FacesMessage> messages = new ArrayList<>();

        TestFacesContext() {
            setCurrentInstance(this);
        }

        FacesMessage getLastMessage() {
            if (messages.isEmpty()) {
                return null;
            }
            return messages.get(messages.size() - 1);
        }

        @Override
        public void addMessage(String clientId, FacesMessage message) {
            messages.add(message);
        }

        @Override
        public Application getApplication() {
            return null;
        }

        @Override
        public Iterator<String> getClientIdsWithMessages() {
            return List.<String>of().iterator();
        }

        @Override
        public ExternalContext getExternalContext() {
            return null;
        }

        @Override
        public FacesMessage.Severity getMaximumSeverity() {
            return messages.stream().map(FacesMessage::getSeverity).findFirst().orElse(null);
        }

        @Override
        public Iterator<FacesMessage> getMessages() {
            return messages.iterator();
        }

        @Override
        public Iterator<FacesMessage> getMessages(String clientId) {
            return messages.iterator();
        }

        @Override
        public RenderKit getRenderKit() {
            return null;
        }

        @Override
        public boolean getRenderResponse() {
            return false;
        }

        @Override
        public boolean getResponseComplete() {
            return false;
        }

        @Override
        public ResponseStream getResponseStream() {
            return null;
        }

        @Override
        public void setResponseStream(ResponseStream responseStream) {
            // No-op in test context.
        }

        @Override
        public ResponseWriter getResponseWriter() {
            return null;
        }

        @Override
        public void setResponseWriter(ResponseWriter responseWriter) {
            // No-op in test context.
        }

        @Override
        public UIViewRoot getViewRoot() {
            return null;
        }

        @Override
        public void setViewRoot(UIViewRoot root) {
            // No-op in test context.
        }

        @Override
        public void release() {
            setCurrentInstance(null);
            messages.clear();
        }

        @Override
        public void renderResponse() {
            // No-op in test context.
        }

        @Override
        public void responseComplete() {
            // No-op in test context.
        }

        @Override
        public ELContext getELContext() {
            return null;
        }

        @Override
        public PartialViewContext getPartialViewContext() {
            return null;
        }

        @Override
        public Lifecycle getLifecycle() {
            return null;
        }

        @Override
        public boolean isPostback() {
            return false;
        }

        @Override
        public boolean isValidationFailed() {
            return false;
        }

        @Override
        public void validationFailed() {
            // No-op in test context.
        }
    }
}
