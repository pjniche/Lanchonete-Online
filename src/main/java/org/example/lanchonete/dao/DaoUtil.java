/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.example.lanchonete.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DaoUtil {
    
    public Connection conecta(){
        try{
            String url = "jdbc:postgresql://localhost:5432/postgres";
            String usuario = "postgres";
            String senha = "pjniche";
            return DriverManager.getConnection(url, usuario, senha);
            
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        
    }
    
}
