package com.bookguest.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PedidoClienteVista {

    private final Long idPedido;
    private final String numeroOrden;
    private final LocalDateTime fecha;
    private final List<CompraProductoVista> productos;
    private final int cantidadTotal;
    private final BigDecimal total;
    private final String metodoPago;
    private final String direccionEntrega;
    private final String estado;
    private final String estadoCodigo;
}
