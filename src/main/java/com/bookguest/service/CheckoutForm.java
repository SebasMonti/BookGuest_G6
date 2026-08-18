package com.bookguest.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutForm {

    @NotBlank(message = "{checkout.validacion.nombreRequerido}")
    @Size(max = 125, message = "{checkout.validacion.nombreLargo}")
    private String nombre;

    @NotBlank(message = "{checkout.validacion.direccionRequerida}")
    @Size(max = 160, message = "{checkout.validacion.direccionLarga}")
    private String direccion;

    @Size(max = 100, message = "{checkout.validacion.direccionSecundariaLarga}")
    private String direccionSecundaria;

    @NotBlank(message = "{checkout.validacion.ciudadRequerida}")
    @Size(max = 80, message = "{checkout.validacion.ciudadLarga}")
    private String ciudad;

    @NotBlank(message = "{checkout.validacion.telefonoRequerido}")
    @Pattern(regexp = "^(?:\\+?506[ -]?)?[2678]\\d{3}[ -]?\\d{4}$",
            message = "{checkout.validacion.telefonoInvalido}")
    private String telefono;

    @NotBlank(message = "{checkout.validacion.metodoRequerido}")
    private String metodoPago;

    private boolean guardarInformacion;
}
