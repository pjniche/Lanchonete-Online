package org.example.lanchonete.controller;

import org.example.lanchonete.dao.DaoBebida;
import org.example.lanchonete.dao.DaoCliente;
import org.example.lanchonete.dao.DaoLanche;
import org.example.lanchonete.dao.DaoPedido;
import org.example.lanchonete.helper.ValidadorCookie;
import org.example.lanchonete.model.Bebida;
import org.example.lanchonete.model.Cliente;
import org.example.lanchonete.model.Lanche;
import org.example.lanchonete.model.Pedido;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

public class Comprar extends HttpServlet {

    private final DaoCliente clienteDao;
    private final DaoLanche lancheDao;
    private final DaoBebida bebidaDao;
    private final DaoPedido pedidoDao;
    private final ValidadorCookie validadorCookie;

    public Comprar(DaoCliente clienteDao, DaoLanche lancheDao, DaoBebida bebidaDao, DaoPedido pedidoDao, ValidadorCookie validadorCookie) {
        this.clienteDao = clienteDao;
        this.lancheDao = lancheDao;
        this.bebidaDao = bebidaDao;
        this.pedidoDao = pedidoDao;
        this.validadorCookie = validadorCookie;
    }

    public void setProcessRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        this.processRequest(request, response);
    }

    public boolean setValidarCookie(HttpServletRequest request) {
        return this.validarCookie(request);
    }

    public String setLerJson(HttpServletRequest request) throws IOException {
        return this.lerJson(request);
    }

    public double setProcessarItens(JSONObject dados, List<Lanche> lanches, List<Bebida> bebidas) throws JSONException {
        return this.processarItens(dados, lanches, bebidas);
    }

    public Lanche setProcessarLanche(String nome, int quantidade) {
        return this.processarLanche(nome, quantidade);
    }

    public Bebida setProcessarBebida(String nome, int quantidade) {
        return this.processarBebida(nome, quantidade);
    }

    public Pedido setCriarESalvarPedido(Cliente cliente, double valorTotal) {
        return this.criarESalvarPedido(cliente, valorTotal);
    }

    public void setVincularItensPedido(Pedido pedido, List<Lanche> lanches, List<Bebida> bebidas) {
        this.vincularItensPedido(pedido, lanches, bebidas);
    }

    public void setResponderComSucesso(HttpServletResponse response) throws IOException {
        this.responderComSucesso(response);
    }

    public void setResponderComErro(HttpServletResponse response) throws IOException {
        this.responderComErro(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, JSONException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!validarCookie(request)) {
            responderComErro(response);
            return;
        }

        String jsonStr = lerJson(request);
        if (jsonStr.isEmpty()) {
            responderComErro(response);
            return;
        }

        JSONObject dados = new JSONObject(jsonStr);
        Cliente cliente = clienteDao.pesquisaPorID(String.valueOf(dados.getInt("id")));

        List<Lanche> lanches = new ArrayList<>();
        List<Bebida> bebidas = new ArrayList<>();
        double valorTotal = processarItens(dados, lanches, bebidas);

        Pedido pedido = criarESalvarPedido(cliente, valorTotal);
        vincularItensPedido(pedido, lanches, bebidas);

        responderComSucesso(response);
    }

    private boolean validarCookie(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            return validadorCookie.validar(cookies);
        } catch (NullPointerException e) {
            return false;
        }
    }

    private String lerJson(HttpServletRequest request) throws IOException {
        if (request == null || request.getInputStream() == null) {
            return "";
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.ISO_8859_1))) {
            String json = br.readLine();
            if (json == null) {
                return "";
            }
            return new String(json.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
    }

    private double processarItens(JSONObject dados, List<Lanche> lanches, List<Bebida> bebidas) throws JSONException {
        double valorTotal = 0.0;
        for (Iterator it = dados.keys(); it.hasNext(); ) {
            String nome = (String) it.next();
            if (!nome.equals("id")) {
                JSONObject item = dados.getJSONObject(nome);
                String tipo = item.getString("tipo");
                int quantidade = item.getInt("quantidade");

                if ("lanche".equals(tipo)) {
                    Lanche lanche = processarLanche(nome, quantidade);
                    valorTotal += lanche.getValor_venda() * quantidade;
                    lanches.add(lanche);
                } else if ("bebida".equals(tipo)) {
                    Bebida bebida = processarBebida(nome, quantidade);
                    valorTotal += bebida.getValor_venda() * quantidade;
                    bebidas.add(bebida);
                }
            }
        }
        return valorTotal;
    }

    private Lanche processarLanche(String nome, int quantidade) {
        Lanche lanche = lancheDao.pesquisaPorNome(nome);
        lanche.setQuantidade(quantidade);
        return lanche;
    }

    private Bebida processarBebida(String nome, int quantidade) {
        Bebida bebida = bebidaDao.pesquisaPorNome(nome);
        bebida.setQuantidade(quantidade);
        return bebida;
    }

    private Pedido criarESalvarPedido(Cliente cliente, double valorTotal) {
        Pedido pedido = new Pedido();
        pedido.setData_pedido(Instant.now().toString());
        pedido.setCliente(cliente);
        pedido.setValor_total(valorTotal);
        pedidoDao.salvar(pedido);
        return pedidoDao.pesquisaPorData(pedido);
    }

    private void vincularItensPedido(Pedido pedido, List<Lanche> lanches, List<Bebida> bebidas) {
        for (Lanche lanche : lanches) {
            pedidoDao.vincularLanche(pedido, lanche);
        }
        for (Bebida bebida : bebidas) {
            pedidoDao.vincularBebida(pedido, bebida);
        }
    }

    private void responderComSucesso(HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.println("Pedido Salvo com Sucesso!");
        }
    }

    private void responderComErro(HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.println("erro");
        }
    }
}