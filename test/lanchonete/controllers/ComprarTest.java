/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lanchonete.controllers;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import lanchonete.dao.DaoLanche;
import lanchonete.dao.DaoBebida;
import lanchonete.model.Lanche;
import lanchonete.model.Bebida;

/**
 *
 * @author paulo
 */
@RunWith(MockitoJUnitRunner.class)
public class ComprarTest {

    @Mock
    private JSONObject dadosMock;

    @Mock
    private DaoLanche lancheDaoMock;

    @Mock
    private DaoBebida bebidaDaoMock;

    @InjectMocks
    private Comprar comprar;

    @Test
    public void testProcessarLanchesEBebidas() {
        // Configurar dados simulados
        when(dadosMock.keys()).thenReturn(new Iterator<String>() {
            private boolean hasNext = true;
            public boolean hasNext() { return hasNext; }
            public String next() {
                hasNext = false;
                return "lancheNome";
            }
        });
        when(dadosMock.getJSONArray("lancheNome")).thenReturn(new JSONArray().put("lancheNome").put("lanche").put(2));

        // Configurar comportamento simulado para DaoLanche
        Lanche lancheSimulado = new Lanche();
        lancheSimulado.setValor_venda(10.0);
        when(lancheDaoMock.pesquisaPorNome("lancheNome")).thenReturn(lancheSimulado);

        // Chamar método a ser testado
        List<Lanche> lanches = new ArrayList<>();
        List<Bebida> bebidas = new ArrayList<>();
        comprar.processarLanchesEBebidas(dadosMock, lanches, bebidas);

        // Verificar se o método setQuantidade foi chamado para o lanche simulado
        verify(lancheSimulado).setQuantidade(2);

        // Verificar se o lanche foi adicionado à lista de lanches
        assertEquals(1, lanches.size());
        assertEquals(lancheSimulado, lanches.get(0));

        // Verificar se o valor total foi atualizado corretamente
        assertEquals(20.0, comprar.valorTotal, 0.01);
    }
}