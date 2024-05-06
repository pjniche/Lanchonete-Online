/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchonete.controllers;

import lanchonete.dao.DaoBebida;
import lanchonete.dao.DaoCliente;
import lanchonete.dao.DaoLanche;
import lanchonete.dao.DaoPedido;
import lanchonete.helpers.ValidadorCookie;
import lanchonete.model.Bebida;
import lanchonete.model.Cliente;
import lanchonete.model.Lanche;
import lanchonete.model.Pedido;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kener_000
 */
public class Comprar extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    private boolean validarCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        ValidadorCookie validar = new ValidadorCookie();
        return validar.validar(cookies);
    }

    private String lerJsonDaRequisicao(BufferedReader br) throws IOException {
        return br.readLine();
    }

    private void processarJson(String json, HttpServletResponse response) throws ServletException, IOException {
        byte[] bytes = json.getBytes(ISO_8859_1); 
        String jsonStr = new String(bytes, UTF_8);            
        JSONObject dados = new JSONObject(jsonStr);
        // ... restante do processamento do JSON ...
    }

    private Cliente buscarCliente(int id) {
        DaoCliente clienteDao = new DaoCliente(); 
        return clienteDao.pesquisaPorID(String.valueOf(id));
    }

    private void processarLanchesEBebidas(JSONObject dados, List<Lanche> lanches, List<Bebida> bebidas) {
        Iterator<String> keys = dados.keys();
        while (keys.hasNext()) {
            String nome = keys.next();
            if (!nome.equals("id")) {
                // ... processamento de lanches e bebidas ...
            }
        }
    }

    private void salvarPedido(Pedido pedido, DaoPedido pedidoDao) {
        pedidoDao.salvar(pedido);
        pedido = pedidoDao.pesquisaPorData(pedido);
        pedido.setCliente(cliente);
    }

    private void vincularLanchesEBebidasAoPedido(Pedido pedido, List<Lanche> lanches, List<Bebida> bebidas, DaoPedido pedidoDao) {
        for (int i = 0; i < lanches.size(); i++) {
            pedidoDao.vincularLanche(pedido, lanches.get(i));
        }
        for (int i = 0; i < bebidas.size(); i++) {
            pedidoDao.vincularBebida(pedido, bebidas.get(i));
        }
    }

    private void retornarResposta(HttpServletResponse response, String mensagem) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.println(mensagem);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
    String json = lerJsonDaRequisicao(br);

    boolean resultado = validarCookie(request);
        if (resultado) {
            processarJson(json, response);
            // ... restante do processamento ...
            Cliente cliente = buscarCliente(dados.getInt("id"));
            List<Lanche> lanches = new ArrayList<>();
            List<Bebida> bebidas = new ArrayList<>();
            processarLanchesEBebidas(dados, lanches, bebidas);
            Pedido pedido = new Pedido();
            salvarPedido(pedido, new DaoPedido());
            vincularLanchesEBebidasAoPedido(pedido, lanches, bebidas, new DaoPedido());
            retornarResposta(response, "Pedido Salvo com Sucesso!");
        } else {
            retornarResposta(response, "erro");
        }
    }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
