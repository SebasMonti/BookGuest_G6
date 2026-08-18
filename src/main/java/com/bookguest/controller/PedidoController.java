package com.bookguest.controller;

import com.bookguest.service.PedidoAdminVista;
import com.bookguest.service.PedidoService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final MessageSource messageSource;

    public PedidoController(PedidoService pedidoService,
            MessageSource messageSource) {
        this.pedidoService = pedidoService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String pedidos(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "false") boolean mostrarFiltros,
            Model model) {

        cargarModelo(model, page, busqueda, estado, mostrarFiltros, "lista", null);
        return "admin/pedidos";
    }

    @GetMapping("/seleccionar")
    public String seleccionar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModelo(model, page, busqueda, estado, false, "seleccionar", null);
        return "admin/pedidos";
    }

    @GetMapping("/modificar")
    public String modificar(@RequestParam Long idPedido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            Model model) {

        cargarModelo(model, page, busqueda, estado, false, "modificar", idPedido);
        return "admin/pedidos";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long idPedido,
            @RequestParam String nuevoEstado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            pedidoService.actualizarEstado(idPedido, nuevoEstado);

            redirectAttributes.addAttribute("idPedido", idPedido);
            redirectAttributes.addAttribute("page", page);

            if (busqueda != null && !busqueda.isBlank()) {
                redirectAttributes.addAttribute("busqueda", busqueda);
            }

            if (estado != null && !estado.isBlank()) {
                redirectAttributes.addAttribute("estado", estado);
            }

            return "redirect:/admin/pedidos/confirmacion";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
            redirectAttributes.addAttribute("idPedido", idPedido);
            redirectAttributes.addAttribute("page", page);

            if (busqueda != null && !busqueda.isBlank()) {
                redirectAttributes.addAttribute("busqueda", busqueda);
            }

            if (estado != null && !estado.isBlank()) {
                redirectAttributes.addAttribute("estado", estado);
            }

            return "redirect:/admin/pedidos/modificar";
        }
    }

    @GetMapping("/confirmacion")
    public String confirmacion(@RequestParam Long idPedido,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            Model model) {

        int pagina = page == null ? pedidoService.getPaginaDePedido(idPedido) : Math.max(page, 0);
        cargarModelo(model, pagina, busqueda, estado, false, "confirmacion", idPedido);
        return "admin/pedidos";
    }

    private void cargarModelo(Model model,
            int page,
            String busqueda,
            String estado,
            boolean mostrarFiltros,
            String modo,
            Long idPedido) {

        Page<PedidoAdminVista> paginaPedidos = pedidoService
                .getPedidosAdministracion(page, busqueda, estado);

        model.addAttribute("paginaPedidos", paginaPedidos);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("mostrarFiltros", mostrarFiltros);
        model.addAttribute("modo", modo);
        model.addAttribute("totalPedidos", pedidoService.getTotalPedidos());
        model.addAttribute("pedidosRecibidos", pedidoService.getPedidosRecibidosUltimosSieteDias());
        model.addAttribute("pedidosEntregados", pedidoService.getPedidosEntregadosUltimosSieteDias());
        model.addAttribute("pedidosEnEntrega", pedidoService.getPedidosEnEntrega());

        if (idPedido != null) {
            model.addAttribute("pedidoSeleccionado", pedidoService.getPedidoAdministracion(idPedido));
        }
    }

    private String getMensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, clave, locale);
    }
}
