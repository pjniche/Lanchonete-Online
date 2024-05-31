/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.example.lanchonete.controller;

import org.example.lanchonete.dao.DaoIngrediente;
import org.example.lanchonete.dao.DaoLanche;
import org.example.lanchonete.helper.ValidadorCookie;
import org.example.lanchonete.model.Ingrediente;
import org.example.lanchonete.model.Lanche;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Iterator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kener_000
 */
public class SalvarLancheCliente extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        
        ////////Validar Cookie
        boolean resultado = false;
        
        try{
        Cookie[] cookies = request.getCookies();
        ValidadorCookie validar = new ValidadorCookie();
        
        resultado = validar.validar(cookies);
        }catch(java.lang.NullPointerException e){}
        //////////////
        
        if ((br != null) && resultado) {
            json = br.readLine();
            byte[] bytes = json.getBytes(ISO_8859_1); 
            String jsonStr = new String(bytes, UTF_8);            
            JSONObject dados = new JSONObject(jsonStr);
            JSONObject ingredientes = dados.getJSONObject("ingredientes");
            
            double precoDoLanche = 0.00;
            
            Lanche lanche = new Lanche();
            
            lanche.setNome(dados.getString("nome"));
            lanche.setDescricao(dados.getString("descricao"));
            
            
            DaoLanche lancheDao = new DaoLanche();
            DaoIngrediente ingredienteDao = new DaoIngrediente();
            
            Iterator<String> keys = ingredientes.keys();
            
            while(keys.hasNext()) {
                
                String key = keys.next(); 
                Ingrediente ingredienteLanche = new Ingrediente();
                ingredienteLanche.setNome(key);
                
                Ingrediente ingredienteComID = ingredienteDao.pesquisaPorNome(ingredienteLanche);
                precoDoLanche += ingredienteComID.getValor_venda() * Double.valueOf(ingredientes.getInt(key));
            }
            
            
            lanche.setValor_venda(precoDoLanche);
            lancheDao.salvarCliente(lanche);
            
            Lanche lancheComID = lancheDao.pesquisaPorNome(lanche);
            
            while(keys.hasNext()) {
                
                String key = keys.next(); 
                Ingrediente ingredienteLanche = new Ingrediente();
                ingredienteLanche.setNome(key);
                
                Ingrediente ingredienteComID = ingredienteDao.pesquisaPorNome(ingredienteLanche);
                ingredienteComID.setQuantidade(ingredientes.getInt(key));
                lancheDao.vincularIngrediente(lancheComID, ingredienteComID);
            }
            
            try (PrintWriter out = response.getWriter()) {
            out.println("../carrinho/carrinho.html?nome="+String.valueOf(lancheComID.getNome())+"&preco="+String.valueOf(lancheComID.getValor_venda()));
            }
        } else {
            try (PrintWriter out = response.getWriter()) {
            out.println("erro");
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
        try {
            processRequest(request, response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
        try {
            processRequest(request, response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
