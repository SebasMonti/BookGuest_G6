package com.bookguest.controller;

import com.bookguest.service.LibroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final LibroService libroService;

    public AdminController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("totalProductos", libroService.getTotalLibrosActivos());
        model.addAttribute("totalCategorias", libroService.getTotalCategorias());
        model.addAttribute("totalExistencias", libroService.getTotalExistenciasActivas());
        model.addAttribute("valorInventario", libroService.getValorTotalInventario());
        model.addAttribute("totalSinStock", libroService.getTotalSinStock());
        model.addAttribute("totalPocasUnidades", libroService.getTotalPocasUnidades());

        model.addAttribute("ultimosLibros", libroService.getUltimosLibrosAgregados());
        model.addAttribute("librosBajoStock", libroService.getLibrosBajoStock());
        model.addAttribute("librosSinStock", libroService.getLibrosSinStockListado());

        return "admin/dashboard";
    }

    @GetMapping("/admin/configuracion")
    public String configuracion() {
        return "admin/configuracion";
    }

    @GetMapping("/admin/usuarios")
    public String usuarios() {
        return "admin/usuarios";
    }

    @GetMapping("/admin/pedidos")
    public String pedidos() {
        return "admin/pedidos";
    }

    @GetMapping("/admin/productos")
    public String productos() {
        return "admin/productos";
    }
}
