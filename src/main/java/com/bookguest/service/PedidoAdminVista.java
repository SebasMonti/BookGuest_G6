package com.bookguest.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PedidoAdminVista {

    private final Long idPedido;
    private final String numeroOrden;
    private final Long idCliente;
    private final String nombreCliente;
    private final String emailCliente;
    private final String libros;
    private final List<PedidoProductoVista> productos;
    private final int cantidadProductos;
    private final BigDecimal precioUnitario;
    private final boolean variosProductos;
    private final int cantidad;
    private final BigDecimal total;
    private final LocalDate fechaEntregaEstimada;
    private final String estado;
    private final String estadoCodigo;
}
