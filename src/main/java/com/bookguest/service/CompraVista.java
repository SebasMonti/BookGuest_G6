package com.bookguest.service;

import com.bookguest.domain.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompraVista {

    private final Long idPedido;
    private final String numeroOrden;
    private final String nombreCliente;
    private final String emailCliente;
    private final String telefono;
    private final String direccionEntrega;
    private final MetodoPago metodoPago;
    private final LocalDateTime fecha;
    private final List<CompraProductoVista> productos;
    private final BigDecimal subtotal;
    private final BigDecimal total;
}
