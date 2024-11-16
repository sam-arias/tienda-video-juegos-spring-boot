package com.videogame_store.videogame_store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VideojuegoController {

    @Autowired
    private VideoJuegoRepository videojuegoRepository;

    List<VideoJuego> carrito = new ArrayList<VideoJuego>();

    @GetMapping("/administrador")
    public String mostrarVideojuegos(Model model) {
        model.addAttribute("videojuegosFisicos", videojuegoRepository.findByFormato("Fisico"));
        model.addAttribute("videojuegosDigitales", videojuegoRepository.findByFormato("Digital"));
        model.addAttribute("videojuegoFisico", new VideoJuegoFisico());
        model.addAttribute("videojuegoDigital", new VideoJuegoDigital());
        return "administrador";
    }

    @GetMapping("/inicio")
    public String mostrarVideojuegosTienda(Model model) {
        model.addAttribute("videojuegosFisicos", videojuegoRepository.findByFormato("Fisico"));
        model.addAttribute("videojuegosDigitales", videojuegoRepository.findByFormato("Digital"));
        return "videojuego"; // This should match the template name
    }

    @GetMapping("/videojuego/nuevo/fisico")
    public String NuevoVideoJuegoFisico(Model model) {
        model.addAttribute("videojuegoFisico", new VideoJuegoFisico());
        return "crear_videojuego_fisico";
    }

    @PostMapping("/videojuego/nuevo/fisico")
    public String guardarVideoJuegoFisico(@Validated @ModelAttribute("videojuegoFisico") VideoJuegoFisico videojuego,
            BindingResult result) {
        if (result.hasErrors()) {
            return "crear_videojuego_fisico";
        }
        videojuegoRepository.save(videojuego);
        return "redirect:/administrador";
    }

    @GetMapping("/videojuego/nuevo/digital")
    public String NuevoVideoJuegoDigital(Model model) {
        model.addAttribute("videojuegoDigital", new VideoJuegoDigital());
        return "crear_videojuego_digital";
    }

    @PostMapping("/videojuego/nuevo/digital")
    public String guardarVideoJuegoFisico(@Validated @ModelAttribute("videojuegoDigital") VideoJuegoDigital videojuego,
            BindingResult result) {
        if (result.hasErrors()) {
            return "crear_videojuego_digital";
        }
        videojuegoRepository.save(videojuego);
        return "redirect:/administrador";
    }

    @GetMapping("/videojuego/editar/fisico/{id}")
    public String editarVideoJuegoFisico(@PathVariable("id") Long id, Model model) {
        VideoJuegoFisico videojuegoFisico = (VideoJuegoFisico) videojuegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid videojuego Id:" + id));
        model.addAttribute("videojuegoFisico", videojuegoFisico);
        return "editar_videojuego_fisico";
    }

    @PostMapping("/videojuego/editar/fisico/{id}")
    public String actualizarVideoJuegoFisico(@PathVariable("id") Long id,
            @Validated @ModelAttribute("videojuegoFisico") VideoJuegoFisico videojuego, BindingResult result) {
        if (result.hasErrors()) {
            videojuego.setId(id);
            return "editar_videojuego_fisico";
        }
        videojuegoRepository.save(videojuego);
        return "redirect:/administrador";
    }

    @GetMapping("/videojuego/editar/digital/{id}")
    public String editarVideoJuegoDigital(@PathVariable("id") Long id, Model model) {
        VideoJuegoDigital videojuegoDigital = (VideoJuegoDigital) videojuegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid videojuego Id:" + id));
        model.addAttribute("videojuegoDigital", videojuegoDigital);
        return "editar_videojuego_digital";
    }

    @PostMapping("/videojuego/editar/digital/{id}")
    public String actualizarVideoJuegoDigital(@PathVariable("id") Long id,
            @Validated @ModelAttribute("videojuegoDigital") VideoJuegoDigital videojuego, BindingResult result) {
        if (result.hasErrors()) {
            videojuego.setId(id);
            return "editar_videojuego_digital";
        }
        videojuegoRepository.save(videojuego);
        return "redirect:/administrador";
    }

    @GetMapping("/videojuego/eliminar/{id}")
    public String eliminarVideoJuego(@PathVariable Long id) {
        videojuegoRepository.deleteById(id);
        return "redirect:/administrador";
    }

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(@RequestParam Long id, @RequestParam String tipo, HttpSession session) {
        // Obtener el videojuego por ID
        VideoJuego videojuego = videojuegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid videojuego Id:" + id));

        // Agregar el videojuego al carrito (guardado en la sesión)
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new Carrito();
        }
        carrito.agregarProducto(videojuego);
        session.setAttribute("carrito", carrito);

        return "redirect:/carrito";
    }

    @GetMapping("/carrito")
    public String mostrarCarrito(Model model, HttpSession session) {
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new Carrito();
        }
        model.addAttribute("carrito", carrito);
        return "carrito";
    }

    // Aqui esta el error, no perimite culminar la compra si no cumple la condicion
    // de las tarjetas, sale un error
    @PostMapping("/carrito/comprar")
    public String comprar(HttpSession session, Model model, @RequestParam String metodoPago,
            @RequestParam String direccion, @RequestParam int cantidad) {
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        if (carrito != null && !carrito.getProductos().isEmpty()) {
            double totalCompra = carrito.getProductos().stream()
                    .mapToDouble(v -> v.getPrecio() * cantidad)
                    .sum();
    
            // Validar método de pago
            String mensajeError = validarMetodoPago(metodoPago, totalCompra);
            if (mensajeError != null) {
                model.addAttribute("mensaje", mensajeError);
                model.addAttribute("carrito", carrito);
                return "carrito";
            }
    
            // Validación de dirección de envío
            if (direccion == null || direccion.trim().isEmpty()) {
                model.addAttribute("mensaje", "Por favor, ingrese su dirección de envío.");
                model.addAttribute("carrito", carrito);
                return "carrito";
            }
    
            // Procesar la compra y actualizar la base de datos
            boolean todosFisicos = true; // Asumir que todos son físicos inicialmente
    
            for (VideoJuego videojuego : carrito.getProductos()) {
                if (videojuego.getDisponibles() >= cantidad) {
                    videojuego.setDisponibles(videojuego.getDisponibles() - cantidad);
                    videojuegoRepository.save(videojuego); // Actualizar inventario
                } else {
                    model.addAttribute("mensaje",
                            "No hay suficiente stock para el videojuego " + videojuego.getTitulo());
                    model.addAttribute("carrito", carrito);
                    return "carrito";
                }
    
                // Verificar si hay algún videojuego digital
                if (!videojuego.isFisico()) {
                    todosFisicos = false; // Hay al menos un videojuego digital
                }
            }
    
            // Limpiar carrito y finalizar compra
            carrito.limpiarCarrito();
            session.removeAttribute("carrito");
    
            // Mensaje según el tipo de videojuego
            if (todosFisicos) {
                model.addAttribute("mensaje",
                        "¡Gracias por tu compra! El envío se realizará en los próximos 5 días hábiles a la dirección: "
                                + direccion);
            } else {
                model.addAttribute("mensaje", "¡Gracias por tu compra! Disfruta de tus videojuego.");
            }
        } else {
            model.addAttribute("mensaje", "No hay productos en el carrito.");
        }
        return "confirmacion_compra";
    }
    
    private String validarMetodoPago(String metodoPago, double totalCompra) {
        switch (metodoPago) {
            case "efectivo":
                if (totalCompra > 150000) {
                    return "El monto de la compra no puede superar los $150,000 en efectivo. Por favor, seleccione otro método de pago.";
                }
                break;
            case "debito":
                // No hay restricciones para la tarjeta de débito
                break;
            case "creditoA":
                if (totalCompra > 100000) {
                    return "El monto de la compra supera el cupo de $100,000 de la Tarjeta A. Por favor, seleccione otro método de pago.";
                }
                break;
            case "creditoB":
                if (totalCompra > 300000) {
                    return "El monto de la compra supera el cupo de $300,000 de la Tarjeta B. Por favor, seleccione otro método de pago.";
                }
                break;
            case "creditoC":
                if (totalCompra > 500000) {
                    return "El monto de la compra supera el cupo de $500,000 de la Tarjeta C. Por favor, seleccione otro método de pago.";
                }
                break;
            default:
                return "Método de pago no reconocido. Por favor, seleccione un método de pago válido.";
        }
        return null; // No hay errores
    }

    @PostMapping("/carrito/eliminar")
    public String eliminarDelCarrito(@RequestParam Long id, HttpSession session) {
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        if (carrito != null) {
            carrito.eliminarProducto(id);
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

}
