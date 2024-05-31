/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.lanchonete;

import org.example.lanchonete.dao.DaoBebida;
import org.example.lanchonete.dao.DaoCliente;
import org.example.lanchonete.dao.DaoLanche;
import org.example.lanchonete.dao.DaoPedido;
import org.example.lanchonete.model.Bebida;
import org.example.lanchonete.model.Cliente;
import org.example.lanchonete.model.Lanche;
import org.example.lanchonete.model.Pedido;
import org.junit.Test;
import org.json.JSONObject;
import static org.mockito.Mockito.*;

/**
 *
 * @author paulo
 */

public class ComprarTest {

    @Test
    public void testValidarPedido() {
        // Criação dos mocks
        DaoCliente daoClienteMock = mock(DaoCliente.class);
        DaoLanche daoLancheMock = mock(DaoLanche.class);
        DaoBebida daoBebidaMock = mock(DaoBebida.class);
        DaoPedido daoPedidoMock = mock(DaoPedido.class);
        Cliente clienteMock = mock(Cliente.class);
        Lanche lancheMock = mock(Lanche.class);
        Bebida bebidaMock = mock(Bebida.class);
        Pedido pedidoMock = mock(Pedido.class);

        // Definindo o comportamento dos mocks
        when(daoClienteMock.pesquisaPorID(anyString())).thenReturn(clienteMock);
        when(daoLancheMock.pesquisaPorNome(anyString())).thenReturn(lancheMock);
        when(daoBebidaMock.pesquisaPorNome(anyString())).thenReturn(bebidaMock);
        when(daoPedidoMock.pesquisaPorData(any())).thenReturn(pedidoMock);

        // Instância da classe a ser testada
        Comprar comprar = new Comprar();

        // Substituir os Daos na classe Comprar pelos mocks
        // ...

        // Dados do pedido em formato JSON
        JSONObject dados = new JSONObject("{\n" +
"    \"id\": 1,\n" +
"    \"Hamburguer\": [\"Hamburguer\", \"lanche\", 2],\n" +
"    \"Coca-Cola\": [\"Coca-Cola\", \"bebida\", 1]\n" +
"}"); // Substitua por um JSON válido

        // Execução do método a ser testado
        comprar.validarPedido(dados);

        // Verificações
        verify(daoClienteMock, times(1)).pesquisaPorID(anyString());
        verify(daoLancheMock, atLeastOnce()).pesquisaPorNome(anyString());
        verify(daoBebidaMock, atLeastOnce()).pesquisaPorNome(anyString());
        verify(daoPedidoMock, times(1)).salvar(any());
        verify(daoPedidoMock, times(1)).pesquisaPorData(any());
        verify(daoPedidoMock, atLeastOnce()).vincularLanche(any(), any());
        verify(daoPedidoMock, atLeastOnce()).vincularBebida(any(), any());
    }
}
