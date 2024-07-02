package org.example.lanchonete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SeleniumComprarTest {
    private SeleniumComprarTestPageObject realizarPedido;

    @BeforeEach
    public void beforeEach() {
        this.realizarPedido = new SeleniumComprarTestPageObject();
        this.realizarPedido.setUp();
    }

    //@AfterEach
    public void afterEach() {
        realizarPedido.tearDown();
    }

    @Test
    public void comprarLanche() {
        realizarPedido.realizarClickHome();
        realizarPedido.realizarClickLanche();
        realizarPedido.realizarClickBebida();
        realizarPedido.realizarClickCarrinho();
        realizarPedido.loginUsuario();
        realizarPedido.pedidoCheckout();
    }
}
