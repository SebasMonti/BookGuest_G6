package com.bookguest.service;

import com.bookguest.domain.Libro;
import com.bookguest.domain.Oferta;

public record ProductoAdminVista(
        Libro libro,
        Oferta oferta,
        String estadoOferta) {
}
