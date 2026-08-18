package com.bookguest.service;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompraProductoVista {

    private final String titulo;
    private final String rutaImagen;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;
}
