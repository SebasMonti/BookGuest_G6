package com.bookguest.controller;

import com.bookguest.domain.Libro;
import com.bookguest.service.LibroService;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventario")
public class InventarioController {

    private final LibroService libroService;

    public InventarioController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public String inventario(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "false") boolean mostrarFiltros,
            Model model) {
        
        cargarModeloInventario(model, page, estado, mostrarFiltros, "lista", null);
        return "admin/inventario";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModeloInventario(model, page, estado, false, "nuevo", null);
        return "admin/inventario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String titulo,
            @RequestParam String autor,
            @RequestParam String precio,
            @RequestParam String editorial,
            @RequestParam String isbn,
            @RequestParam String ubicacionFisica,
            @RequestParam String categoria,
            @RequestParam(defaultValue = "0") int existencias,
            @RequestParam("imagenFile") MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) throws IOException {

        try {
            Libro libro = libroService.crearDesdeInventario(
                    titulo,
                    autor,
                    precio,
                    editorial,
                    isbn,
                    ubicacionFisica,
                    categoria,
                    existencias,
                    imagenFile
            );

            return "redirect:/admin/inventario/confirmacion?idLibro=" + libro.getIdLibro();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/inventario/nuevo";
        }
    }

    @GetMapping("/confirmacion")
    public String confirmacion(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModeloInventario(model, page, estado, false, "confirmacionAgregar", idLibro);
        return "admin/inventario";
    }

    @GetMapping("/seleccionar")
    public String seleccionar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModeloInventario(model, page, estado, false, "seleccionar", null);
        return "admin/inventario";
    }

    @GetMapping("/modificar")
    public String modificar(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModeloInventario(model, page, estado, false, "modificar", idLibro);
        return "admin/inventario";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long idLibro,
            @RequestParam String titulo,
            @RequestParam String autor,
            @RequestParam String precio,
            @RequestParam String editorial,
            @RequestParam String isbn,
            @RequestParam String ubicacionFisica,
            @RequestParam String categoria,
            @RequestParam(defaultValue = "0") int existencias,
            @RequestParam("imagenFile") MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) throws IOException {

        try {
            Libro libro = libroService.actualizarDesdeInventario(
                    idLibro,
                    titulo,
                    autor,
                    precio,
                    editorial,
                    isbn,
                    ubicacionFisica,
                    categoria,
                    existencias,
                    imagenFile
            );

            if (libro == null) {
                return "redirect:/admin/inventario";
            }

            return "redirect:/admin/inventario/modificado?idLibro=" + libro.getIdLibro();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/inventario/modificar?idLibro=" + idLibro;
        }
    }

    @GetMapping("/modificado")
    public String modificado(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModeloInventario(model, page, estado, false, "confirmacionModificar", idLibro);
        return "admin/inventario";
    }

    private void cargarModeloInventario(Model model,
            int page,
            String estado,
            boolean mostrarFiltros,
            String modo,
            Long idLibro) {

        Page<Libro> paginaLibros = libroService.getLibrosInventario(estado, page);

        model.addAttribute("paginaLibros", paginaLibros);
        model.addAttribute("estado", estado);
        model.addAttribute("mostrarFiltros", mostrarFiltros);
        model.addAttribute("modo", modo);
        model.addAttribute("totalCategorias", libroService.getTotalCategorias());
        model.addAttribute("totalProductos", libroService.getTotalLibrosActivos());
        model.addAttribute("totalSinStock", libroService.getTotalSinStock());
        model.addAttribute("totalPocasUnidades", libroService.getTotalPocasUnidades());

        if (idLibro != null) {
            model.addAttribute("libroSeleccionado", libroService.getLibro(idLibro));
        }
    }
}
