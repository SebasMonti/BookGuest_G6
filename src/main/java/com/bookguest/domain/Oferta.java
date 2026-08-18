package com.bookguest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "oferta")
public class Oferta implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId ZONA_COSTA_RICA = ZoneId.of("America/Costa_Rica");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Long idOferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Libro libro;

    @Column(nullable = false, length = 150)
    private String descripcion;

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Transient
    public BigDecimal getPrecioOferta() {
        if (libro == null || libro.getPrecio() == null || porcentajeDescuento == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal factorDescuento = porcentajeDescuento
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        return libro.getPrecio()
                .multiply(BigDecimal.ONE.subtract(factorDescuento))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transient
    public long getFechaFinEpochMillis() {
        if (fechaFin == null) {
            return 0L;
        }

        return fechaFin.plusDays(1)
                .atStartOfDay(ZONA_COSTA_RICA)
                .toInstant()
                .toEpochMilli();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now(ZONA_COSTA_RICA);
        fechaCreacion = ahora;
        fechaModificacion = ahora;
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now(ZONA_COSTA_RICA);
    }
}
