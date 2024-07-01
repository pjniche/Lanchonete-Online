package org.example.lanchonete;

import org.example.lanchonete.controller.Comprar;
import org.example.lanchonete.dao.*;
import org.example.lanchonete.helper.ValidadorCookie;
import org.example.lanchonete.model.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ComprarTest {

    @Mock
    private DaoCliente clienteDao;
    @Mock
    private DaoLanche lancheDao;
    @Mock
    private DaoBebida bebidaDao;
    @Mock
    private DaoPedido pedidoDao;
    @Mock
    private ValidadorCookie validadorCookie;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private Comprar comprar;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        comprar = new Comprar(clienteDao, lancheDao, bebidaDao, pedidoDao, validadorCookie);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testProcessRequestWithValidCookieAndData() throws Exception {
        // Arrange
        Cookie[] cookies = {new Cookie("session", "valid")};
        when(request.getCookies()).thenReturn(cookies);
        when(validadorCookie.validar(cookies)).thenReturn(true);

        JSONObject dados = new JSONObject();
        dados.put("id", 1);
        dados.put("hamburguer", new JSONObject().put("tipo", "lanche").put("quantidade", 2));
        dados.put("refrigerante", new JSONObject().put("tipo", "bebida").put("quantidade", 1));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(dados.toString().getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamWrapper(inputStream));

        Cliente cliente = new Cliente();
        when(clienteDao.pesquisaPorID("1")).thenReturn(cliente);

        Lanche lanche = new Lanche();
        lanche.setValor_venda(10.0);
        when(lancheDao.pesquisaPorNome("hamburguer")).thenReturn(lanche);

        Bebida bebida = new Bebida();
        bebida.setValor_venda(5.0);
        when(bebidaDao.pesquisaPorNome("refrigerante")).thenReturn(bebida);

        Pedido pedido = new Pedido();
        when(pedidoDao.pesquisaPorData(any())).thenReturn(pedido);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        comprar.setProcessRequest(request, response);

        // Assert
        verify(pedidoDao).salvar(any(Pedido.class));
        verify(pedidoDao).vincularLanche(eq(pedido), any(Lanche.class));
        verify(pedidoDao).vincularBebida(eq(pedido), any(Bebida.class));
        assert(stringWriter.toString().contains("Pedido Salvo com Sucesso!"));
    }

    @Test
    void testProcessRequestWithInvalidCookie() throws Exception {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        comprar.setProcessRequest(request, response);

        // Assert
        assert(stringWriter.toString().contains("erro"));
    }

    @Test
    void testProcessRequestWithEmptyJson() throws Exception {
        // Arrange
        Cookie[] cookies = {new Cookie("session", "valid")};
        when(request.getCookies()).thenReturn(cookies);
        when(validadorCookie.validar(cookies)).thenReturn(true);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamWrapper(inputStream));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        comprar.setProcessRequest(request, response);

        // Assert
        assert(stringWriter.toString().contains("erro"));
    }

    @Test
    void testValidarCookie_ComCookiesValidos() {
        Cookie[] cookies = new Cookie[]{new Cookie("session", "valid")};
        when(request.getCookies()).thenReturn(cookies);
        when(validadorCookie.validar(cookies)).thenReturn(true);

        assertTrue(comprar.setValidarCookie(request));
        verify(validadorCookie).validar(cookies);
    }

    @Test
    void testValidarCookie_ComCookiesInvalidos() {
        Cookie[] cookies = new Cookie[]{new Cookie("session", "invalid")};
        when(request.getCookies()).thenReturn(cookies);
        when(validadorCookie.validar(cookies)).thenReturn(false);

        assertFalse(comprar.setValidarCookie(request));
        verify(validadorCookie).validar(cookies);
    }

    @Test
    void testValidarCookie_SemCookies() {
        when(request.getCookies()).thenReturn(null);
        when(validadorCookie.validar(null)).thenReturn(false);

        assertFalse(comprar.setValidarCookie(request));
        verify(validadorCookie).validar(null);
    }

    @Test
    void testLerJsonComConteudoValido() throws IOException, JSONException {
        JSONObject dados = new JSONObject();
        dados.put("id", 1);
        dados.put("hamburguer", new JSONObject().put("tipo", "lanche").put("quantidade", 2));
        dados.put("refrigerante", new JSONObject().put("tipo", "bebida").put("quantidade", 1));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(dados.toString().getBytes());
        when(request.getInputStream()).thenReturn(new ServletInputStreamWrapper(inputStream));

        String result = comprar.setLerJson(request);

        assertEquals(dados.toString(), result);
    }

    @Test
    void testLerJsonComInputStreamNulo() throws IOException {
        when(request.getInputStream()).thenReturn(null);

        String result = comprar.setLerJson(request);

        assertEquals("", result);
    }

    @Test
    void testProcessarItens_WithLancheAndBebida() throws JSONException {
        // Arrange
        JSONObject dados = new JSONObject();
        dados.put("id", 1);
        dados.put("hamburguer", new JSONObject().put("tipo", "lanche").put("quantidade", 2));
        dados.put("refrigerante", new JSONObject().put("tipo", "bebida").put("quantidade", 2));

        List<Lanche> lanches = new ArrayList<>();
        List<Bebida> bebidas = new ArrayList<>();

        Lanche lanche = mock(Lanche.class);
        when(lanche.getValor_venda()).thenReturn(10.0);
        when(lancheDao.pesquisaPorNome("hamburguer")).thenReturn(lanche);

        Bebida bebida = mock(Bebida.class);
        when(bebida.getValor_venda()).thenReturn(5.0);
        when(bebidaDao.pesquisaPorNome("refrigerante")).thenReturn(bebida);

        // Act
        double valorTotal = comprar.setProcessarItens(dados, lanches, bebidas);

        // Assert
        assertEquals(30.0, valorTotal, 0.01);
        assertEquals(1, lanches.size());
        assertEquals(1, bebidas.size());
        verify(lanche).setQuantidade(2);
        verify(bebida).setQuantidade(2);
    }

    @Test
    void testProcessarItens_EmptyData() throws JSONException {
        // Arrange
        JSONObject dados = new JSONObject();
        dados.put("id", 1);
        List<Lanche> lanches = new ArrayList<>();
        List<Bebida> bebidas = new ArrayList<>();

        // Act
        double valorTotal = comprar.setProcessarItens(dados, lanches, bebidas);

        // Assert
        assertEquals(0.0, valorTotal, 0.01);
        assertTrue(lanches.isEmpty());
        assertTrue(bebidas.isEmpty());
    }

    @Test
    void testProcessarItens_InvalidItemType() throws JSONException {
        // Arrange
        JSONObject dados = new JSONObject();
        dados.put("id", 1);
        JSONObject invalidItem = new JSONObject();
        invalidItem.put("tipo", "invalid");
        invalidItem.put("quantidade", 1);
        dados.put("InvalidItem", invalidItem);

        List<Lanche> lanches = new ArrayList<>();
        List<Bebida> bebidas = new ArrayList<>();

        // Act
        double valorTotal = comprar.setProcessarItens(dados, lanches, bebidas);

        // Assert
        assertEquals(0.0, valorTotal, 0.01);
        assertTrue(lanches.isEmpty());
        assertTrue(bebidas.isEmpty());
    }

    @Test
    void testProcessarLanche_Success() {
        // Arrange
        String nomeLanche = "X-Tudo";
        int quantidade = 2;
        Lanche lanche = new Lanche();
        lanche.setNome(nomeLanche);
        lanche.setValor_venda(5.0);

        when(lancheDao.pesquisaPorNome(nomeLanche)).thenReturn(lanche);

        // Act
        Lanche resultado = comprar.setProcessarLanche(nomeLanche, quantidade);

        // Assert
        assertNotNull(resultado);
        assertEquals(nomeLanche, resultado.getNome());
        assertEquals(quantidade, resultado.getQuantidade());
        assertEquals(5.0, resultado.getValor_venda());
        verify(lancheDao, times(1)).pesquisaPorNome(nomeLanche);
    }

    @Test
    void testProcessarLanche_LancheNaoEncontrado() {
        // Arrange
        String nomeLanche = "Lanche Inexistente";
        int quantidade = 1;

        when(lancheDao.pesquisaPorNome(nomeLanche)).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            comprar.setProcessarLanche(nomeLanche, quantidade);
        });
        verify(lancheDao, times(1)).pesquisaPorNome(nomeLanche);
    }

    @Test
    void testProcessarLanche_QuantidadeZero() {
        // Arrange
        String nomeLanche = "Misto Quente";
        int quantidade = 0;
        Lanche lanche = new Lanche();
        lanche.setNome(nomeLanche);
        lanche.setValor_venda(2.0);

        when(lancheDao.pesquisaPorNome(nomeLanche)).thenReturn(lanche);

        // Act
        Lanche resultado = comprar.setProcessarLanche(nomeLanche, quantidade);

        // Assert
        assertNotNull(resultado);
        assertEquals(nomeLanche, resultado.getNome());
        assertEquals(0, resultado.getQuantidade());
        assertEquals(2.0, resultado.getValor_venda());
        verify(lancheDao, times(1)).pesquisaPorNome(nomeLanche);
    }

    @Test
    void testProcessarBebida_Success() {
        // Arrange
        String nomeBebida = "Coca-Cola";
        int quantidade = 2;
        Bebida bebida = new Bebida();
        bebida.setNome(nomeBebida);
        bebida.setValor_venda(5.0);

        when(bebidaDao.pesquisaPorNome(nomeBebida)).thenReturn(bebida);

        // Act
        Bebida resultado = comprar.setProcessarBebida(nomeBebida, quantidade);

        // Assert
        assertNotNull(resultado);
        assertEquals(nomeBebida, resultado.getNome());
        assertEquals(quantidade, resultado.getQuantidade());
        assertEquals(5.0, resultado.getValor_venda());
        verify(bebidaDao, times(1)).pesquisaPorNome(nomeBebida);
    }

    @Test
    void testProcessarBebida_BebidaNaoEncontrada() {
        // Arrange
        String nomeBebida = "Bebida Inexistente";
        int quantidade = 1;

        when(bebidaDao.pesquisaPorNome(nomeBebida)).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            comprar.setProcessarBebida(nomeBebida, quantidade);
        });
        verify(bebidaDao, times(1)).pesquisaPorNome(nomeBebida);
    }

    @Test
    void testProcessarBebida_QuantidadeZero() {
        // Arrange
        String nomeBebida = "Água";
        int quantidade = 0;
        Bebida bebida = new Bebida();
        bebida.setNome(nomeBebida);
        bebida.setValor_venda(2.0);

        when(bebidaDao.pesquisaPorNome(nomeBebida)).thenReturn(bebida);

        // Act
        Bebida resultado = comprar.setProcessarBebida(nomeBebida, quantidade);

        // Assert
        assertNotNull(resultado);
        assertEquals(nomeBebida, resultado.getNome());
        assertEquals(0, resultado.getQuantidade());
        assertEquals(2.0, resultado.getValor_venda());
        verify(bebidaDao, times(1)).pesquisaPorNome(nomeBebida);
    }

    /*
    @Test
    void testCriarESalvarPedido_Success() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId_cliente(1);
        cliente.setNome("Paulo");

        double valorTotal = 50.0;

        Pedido expectedPedido = new Pedido();
        expectedPedido.setCliente(cliente);
        expectedPedido.setValor_total(valorTotal);

        when(pedidoDao.salvar(any(Pedido.class))).thenReturn(true);
        when(pedidoDao.pesquisaPorData(any(Pedido.class))).thenReturn(expectedPedido);

        // Act
        Pedido result = comprar.setCriarESalvarPedido(cliente, valorTotal);

        // Assert
        assertNotNull(result);
        assertEquals(cliente, result.getCliente());
        assertEquals(valorTotal, result.getValor_total());
        assertNotNull(result.getData_pedido());

        verify(pedidoDao, times(1)).salvar(any(Pedido.class));
        verify(pedidoDao, times(1)).pesquisaPorData(any(Pedido.class));
    }

    @Test
    void testCriarESalvarPedido_NullCliente() {
        // Arrange
        double valorTotal = 50.0;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> comprar.setCriarESalvarPedido(null, valorTotal));
    }

    @Test
    void testCriarESalvarPedido_NegativeValorTotal() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId_cliente(1);
        cliente.setNome("Paulo");

        double valorTotal = -10.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comprar.setCriarESalvarPedido(cliente, valorTotal));
    }
    */

    @Test
    void testVincularItensPedidoComLanchesEBebidas() {
        Pedido pedido = new Pedido();
        Lanche lanche1 = new Lanche();
        Lanche lanche2 = new Lanche();
        Bebida bebida1 = new Bebida();
        Bebida bebida2 = new Bebida();

        List<Lanche> lanches = Arrays.asList(lanche1, lanche2);
        List<Bebida> bebidas = Arrays.asList(bebida1, bebida2);

        comprar.setVincularItensPedido(pedido, lanches, bebidas);

        verify(pedidoDao, times(2)).vincularLanche(eq(pedido), any(Lanche.class));
        verify(pedidoDao, times(2)).vincularBebida(eq(pedido), any(Bebida.class));
    }

    @Test
    void testVincularItensPedidoSemLanches() {
        Pedido pedido = new Pedido();
        Bebida bebida = new Bebida();

        List<Lanche> lanches = Collections.emptyList();
        List<Bebida> bebidas = Collections.singletonList(bebida);

        comprar.setVincularItensPedido(pedido, lanches, bebidas);

        verify(pedidoDao, never()).vincularLanche(any(), any());
        verify(pedidoDao, times(1)).vincularBebida(eq(pedido), eq(bebida));
    }

    @Test
    void testVincularItensPedidoSemBebidas() {
        Pedido pedido = new Pedido();
        Lanche lanche = new Lanche();

        List<Lanche> lanches = Collections.singletonList(lanche);
        List<Bebida> bebidas = Collections.emptyList();

        comprar.setVincularItensPedido(pedido, lanches, bebidas);

        verify(pedidoDao, times(1)).vincularLanche(eq(pedido), eq(lanche));
        verify(pedidoDao, never()).vincularBebida(any(), any());
    }

    @Test
    void testVincularItensPedidoSemItens() {
        Pedido pedido = new Pedido();

        List<Lanche> lanches = Collections.emptyList();
        List<Bebida> bebidas = Collections.emptyList();

        comprar.setVincularItensPedido(pedido, lanches, bebidas);

        verify(pedidoDao, never()).vincularLanche(any(), any());
        verify(pedidoDao, never()).vincularBebida(any(), any());
    }

    @Test
    void testResponderComSucesso() throws IOException {
        // Act
        comprar.setResponderComSucesso(response);

        // Assert
        verify(response, times(1)).getWriter();
        assert(stringWriter.toString().contains("Pedido Salvo com Sucesso!"));
    }

    @Test
    void testResponderComSucessoHandlesIOException() throws IOException {
        // Arrange
        when(response.getWriter()).thenThrow(new IOException("Simulated IO error"));

        // Act & Assert
        assertThrows(IOException.class, () -> comprar.setResponderComSucesso(response));
    }

    @Test
    void testResponderComErro() throws IOException {
        // Arrange
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        comprar.setResponderComErro(response);

        // Assert
        verify(response, times(1)).getWriter();
        assert(stringWriter.toString().contains("erro"));
    }

    @Test
    void testResponderComErroIOException() throws IOException {
        // Arrange
        when(response.getWriter()).thenThrow(new IOException("Simulated IO error"));

        // Act & Assert
        try {
            comprar.setResponderComErro(response);
        } catch (IOException e) {
            assertEquals("Simulated IO error", e.getMessage());
        }

        verify(response, times(1)).getWriter();
    }

    // Helper class to wrap ByteArrayInputStream as ServletInputStream
    private static class ServletInputStreamWrapper extends jakarta.servlet.ServletInputStream {
        private final InputStream inputStream;

        public ServletInputStreamWrapper(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {
        }
    }
}