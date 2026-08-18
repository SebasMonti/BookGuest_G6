package com.bookguest.controller;

import com.bookguest.domain.Libro;
import com.bookguest.domain.Oferta;
import com.bookguest.service.OfertaService;
import com.bookguest.service.ProductoAdminVista;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
public class OfertaController {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ISO_LOCAL_DATE;

    private final OfertaService ofertaService;
    private final MessageSource messageSource;

    public OfertaController(OfertaService ofertaService,
            MessageSource messageSource) {
        this.ofertaService = ofertaService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String productos(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            Model model) {

        cargarModelo(model, page, busqueda, "lista", null);
        return "admin/productos";
    }

    @GetMapping("/oferta")
    public String configurarOferta(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            RedirectAttributes redirectAttributes,
            Locale locale,
            Model model) {

        Libro libro = ofertaService.getLibroActivo(idLibro);

        if (libro == null) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    getMensaje("admin.productos.error.productoInactivo", locale)
            );
            return "redirect:/admin/productos";
        }

        cargarModelo(model, page, busqueda, "oferta", idLibro);
        return "admin/productos";
    }

    @PostMapping("/oferta/guardar")
    public String guardarOferta(@RequestParam Long idLibro,
            @RequestParam String descripcion,
            @RequestParam BigDecimal porcentajeDescuento,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            ofertaService.guardarOferta(
                    idLibro,
                    descripcion,
                    porcentajeDescuento,
                    fechaInicio,
                    fechaFin
            );
            redirectAttributes.addFlashAttribute(
                    "mensajeOk",
                    getMensaje("admin.productos.ofertaGuardada", locale)
            );
            agregarParametrosListado(redirectAttributes, page, busqueda);
            return "redirect:/admin/productos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    getMensaje(e.getMessage(), locale)
            );
            redirectAttributes.addAttribute("idLibro", idLibro);
            agregarParametrosListado(redirectAttributes, page, busqueda);
            return "redirect:/admin/productos/oferta";
        }
    }

    @PostMapping("/oferta/finalizar")
    public String finalizarOferta(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            ofertaService.finalizarOferta(idLibro);
            redirectAttributes.addFlashAttribute(
                    "mensajeOk",
                    getMensaje("admin.productos.ofertaFinalizada", locale)
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    getMensaje(e.getMessage(), locale)
            );
        }

        agregarParametrosListado(redirectAttributes, page, busqueda);
        return "redirect:/admin/productos";
    }

    private void cargarModelo(Model model,
            int page,
            String busqueda,
            String modo,
            Long idLibro) {

        Page<ProductoAdminVista> paginaProductos
                = ofertaService.getProductosAdministracion(page, busqueda);

        model.addAttribute("paginaProductos", paginaProductos);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("modo", modo);

        if (idLibro != null) {
            Libro libro = ofertaService.getLibroActivo(idLibro);
            Oferta oferta = ofertaService.getOfertaConfigurada(idLibro);
            LocalDate ahora = ofertaService.hoyCostaRica();

            model.addAttribute("libroSeleccionado", libro);
            model.addAttribute("ofertaSeleccionada", oferta);
            model.addAttribute(
                    "fechaInicioFormulario",
                    (oferta != null && oferta.getFechaInicio() != null
                            ? oferta.getFechaInicio() : ahora).format(FORMATO_FECHA)
            );
            model.addAttribute(
                    "fechaFinFormulario",
                    (oferta != null && oferta.getFechaFin() != null
                            && oferta.getFechaFin().isAfter(ahora)
                            ? oferta.getFechaFin() : ahora.plusDays(7)).format(FORMATO_FECHA)
            );
        }
    }

    private void agregarParametrosListado(RedirectAttributes redirectAttributes,
            int page,
            String busqueda) {

        redirectAttributes.addAttribute("page", Math.max(page, 0));

        if (busqueda != null && !busqueda.isBlank()) {
            redirectAttributes.addAttribute("busqueda", busqueda.trim());
        }
    }

    private String getMensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, clave, locale);
    }
}
