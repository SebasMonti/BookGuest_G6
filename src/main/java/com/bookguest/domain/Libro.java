package com.bookguest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "libro")
public class Libro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long idLibro;

    @NotBlank(message = "El título es obligatorio.")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria.")
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "1.00", message = "El precio debe ser mayor a cero.")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Min(value = 0, message = "Las existencias no pueden ser negativas.")
    @Column(nullable = false)
    private int existencias;

    @NotBlank(message = "El ISBN es obligatorio.")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @NotBlank(message = "La ubicación física es obligatoria.")
    @Column(name = "ubicacion_fisica", nullable = false, length = 80)
    private String ubicacionFisica;

    @Column(name = "ruta_imagen", length = 1000)
    private String rutaImagen;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_editorial")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Editorial editorial;
}
