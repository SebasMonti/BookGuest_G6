package com.bookguest.controller;

import com.bookguest.service.PedidoClienteService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PedidoClienteController {

    private final PedidoClienteService pedidoClienteService;

    public PedidoClienteController(PedidoClienteService pedidoClienteService) {
        this.pedidoClienteService = pedidoClienteService;
    }

    @GetMapping("/cliente/pedidos")
    public String pedidos(Principal principal, Model model) {
        model.addAttribute(
                "pedidosCliente",
                pedidoClienteService.getPedidosCliente(principal.getName())
        );
        return "cliente/pedidos";
    }
}
