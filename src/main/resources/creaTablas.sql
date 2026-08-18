/*
  BookGuest - esquema final MySQL 8.0

  Compatible con MySQL local y Aiven for MySQL.

  IMPORTANTE:
  - Ejecute este archivo conectado a la base de datos de destino.
  - El script reinicia las tablas de BookGuest y elimina sus datos anteriores.
  - No crea bases de datos ni usuarios MySQL, porque Aiven administra esos
    recursos. La aplicación toma la conexión desde variables de entorno.

  Cuentas iniciales:
  - Administrador: admin@bookguest.com / Admin123.
  - Cliente:       cliente@bookguest.com / Cliente123.
*/

SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

-- Tablas actuales y tablas históricas que ya no utiliza la aplicación.
DROP TABLE IF EXISTS carrito_detalle;
DROP TABLE IF EXISTS favorito;
DROP TABLE IF EXISTS pedido_detalle;
DROP TABLE IF EXISTS oferta;
DROP TABLE IF EXISTS carrito;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS inventario;
DROP TABLE IF EXISTS libro;
DROP TABLE IF EXISTS usuario_rol;
DROP TABLE IF EXISTS ruta;
DROP TABLE IF EXISTS constante;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS autor;
DROP TABLE IF EXISTS editorial;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE rol (
  id_rol BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL,
  PRIMARY KEY (id_rol),
  UNIQUE KEY uk_rol_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
  id_usuario BIGINT NOT NULL AUTO_INCREMENT,
  password VARCHAR(512) NOT NULL,
  nombre VARCHAR(50) NOT NULL,
  apellidos VARCHAR(75) NOT NULL,
  email VARCHAR(75) NOT NULL,
  telefono VARCHAR(25) NULL,
  direccion VARCHAR(255) NULL,
  ruta_imagen VARCHAR(1024) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uk_usuario_email (email),
  INDEX idx_usuario_activo (activo),
  INDEX idx_usuario_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario_rol (
  id_usuario BIGINT NOT NULL,
  id_rol BIGINT NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  INDEX idx_usuario_rol_rol (id_rol),
  CONSTRAINT fk_usuario_rol_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_usuario_rol_rol
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categoria (
  id_categoria BIGINT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(120) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_categoria),
  UNIQUE KEY uk_categoria_descripcion (descripcion),
  INDEX idx_categoria_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE autor (
  id_autor BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_autor),
  UNIQUE KEY uk_autor_nombre (nombre),
  INDEX idx_autor_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE editorial (
  id_editorial BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_editorial),
  UNIQUE KEY uk_editorial_nombre (nombre),
  INDEX idx_editorial_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE libro (
  id_libro BIGINT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(150) NOT NULL,
  descripcion VARCHAR(1000) NOT NULL,
  precio DECIMAL(12,2) NOT NULL,
  existencias INT NOT NULL DEFAULT 0,
  isbn VARCHAR(20) NOT NULL,
  ubicacion_fisica VARCHAR(80) NOT NULL,
  ruta_imagen VARCHAR(1000) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  id_categoria BIGINT NOT NULL,
  id_autor BIGINT NOT NULL,
  id_editorial BIGINT NOT NULL,
  PRIMARY KEY (id_libro),
  UNIQUE KEY uk_libro_isbn (isbn),
  INDEX idx_libro_titulo (titulo),
  INDEX idx_libro_activo_existencias (activo, existencias),
  INDEX idx_libro_categoria (id_categoria),
  INDEX idx_libro_autor (id_autor),
  INDEX idx_libro_editorial (id_editorial),
  CONSTRAINT chk_libro_precio CHECK (precio > 0),
  CONSTRAINT chk_libro_existencias CHECK (existencias >= 0),
  CONSTRAINT fk_libro_categoria
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
  CONSTRAINT fk_libro_autor
    FOREIGN KEY (id_autor) REFERENCES autor(id_autor) ON DELETE RESTRICT,
  CONSTRAINT fk_libro_editorial
    FOREIGN KEY (id_editorial) REFERENCES editorial(id_editorial) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE oferta (
  id_oferta BIGINT NOT NULL AUTO_INCREMENT,
  id_libro BIGINT NOT NULL,
  descripcion VARCHAR(150) NOT NULL,
  porcentaje_descuento DECIMAL(5,2) NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_oferta),
  INDEX idx_oferta_libro (id_libro),
  INDEX idx_oferta_vigencia (activo, fecha_inicio, fecha_fin),
  CONSTRAINT chk_oferta_descuento
    CHECK (porcentaje_descuento > 0 AND porcentaje_descuento < 100),
  CONSTRAINT chk_oferta_fechas CHECK (fecha_fin >= fecha_inicio),
  CONSTRAINT fk_oferta_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE favorito (
  id_favorito BIGINT NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT NOT NULL,
  id_libro BIGINT NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_favorito),
  UNIQUE KEY uk_favorito_usuario_libro (id_usuario, id_libro),
  INDEX idx_favorito_usuario_fecha (id_usuario, fecha_creacion),
  INDEX idx_favorito_libro (id_libro),
  CONSTRAINT fk_favorito_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_favorito_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE carrito (
  id_carrito BIGINT NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_carrito),
  INDEX idx_carrito_usuario_activo (id_usuario, activo),
  CONSTRAINT fk_carrito_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE carrito_detalle (
  id_carrito_detalle BIGINT NOT NULL AUTO_INCREMENT,
  id_carrito BIGINT NOT NULL,
  id_libro BIGINT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_carrito_detalle),
  UNIQUE KEY uk_carrito_detalle_libro (id_carrito, id_libro),
  INDEX idx_carrito_detalle_libro (id_libro),
  CONSTRAINT chk_carrito_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_carrito_detalle_precio CHECK (precio_unitario >= 0),
  CONSTRAINT fk_carrito_detalle_carrito
    FOREIGN KEY (id_carrito) REFERENCES carrito(id_carrito) ON DELETE CASCADE,
  CONSTRAINT fk_carrito_detalle_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pedido (
  id_pedido BIGINT NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT NOT NULL,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(12,2) NOT NULL,
  impuesto DECIMAL(12,2) NOT NULL DEFAULT 0,
  total DECIMAL(12,2) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
  metodo_pago VARCHAR(20) NOT NULL DEFAULT 'Tarjeta',
  direccion_envio VARCHAR(255) NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_pedido),
  INDEX idx_pedido_usuario_fecha (id_usuario, fecha),
  INDEX idx_pedido_estado (estado),
  INDEX idx_pedido_fecha_modificacion (fecha_modificacion),
  CONSTRAINT chk_pedido_subtotal CHECK (subtotal >= 0),
  CONSTRAINT chk_pedido_impuesto CHECK (impuesto >= 0),
  CONSTRAINT chk_pedido_total CHECK (total > 0),
  CONSTRAINT chk_pedido_estado
    CHECK (estado IN ('Pendiente', 'Pagado', 'Enviado', 'Entregado', 'Cancelado')),
  CONSTRAINT chk_pedido_metodo_pago
    CHECK (metodo_pago IN ('Tarjeta', 'Sinpe', 'Transferencia', 'Efectivo')),
  CONSTRAINT fk_pedido_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pedido_detalle (
  id_pedido_detalle BIGINT NOT NULL AUTO_INCREMENT,
  id_pedido BIGINT NOT NULL,
  id_libro BIGINT NOT NULL,
  precio_historico DECIMAL(12,2) NOT NULL,
  cantidad INT NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_pedido_detalle),
  INDEX idx_pedido_detalle_pedido (id_pedido),
  INDEX idx_pedido_detalle_libro (id_libro),
  CONSTRAINT chk_pedido_detalle_precio CHECK (precio_historico >= 0),
  CONSTRAINT chk_pedido_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_pedido_detalle_subtotal CHECK (subtotal >= 0),
  CONSTRAINT fk_pedido_detalle_pedido
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE,
  CONSTRAINT fk_pedido_detalle_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos mínimos para iniciar y demostrar todas las funciones.
INSERT INTO rol (id_rol, nombre) VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_CLIENTE');

-- Las contraseñas están codificadas con BCrypt.
INSERT INTO usuario
  (id_usuario, password, nombre, apellidos, email, telefono, direccion, ruta_imagen, activo)
VALUES
  (1, '{bcrypt}$2a$10$QUOIIpvNyp95eL847CJAiOzybgF6W7dX9uwlp.lKNhTi0QWwtXlIm',
   'Administrador', 'BookGuest', 'admin@bookguest.com', '8888-0001',
   'San José centro, San José, Costa Rica', NULL, TRUE),
  (2, '{bcrypt}$2a$10$ih0anDVfrJluGhqR0ODjz.Jt1pxGru.biyRw4xmUKciDI3IQZ0kJq',
   'Cliente', 'BookGuest', 'cliente@bookguest.com', '8888-0002',
   'Heredia centro, Heredia, Costa Rica', NULL, TRUE);

INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
  (1, 1),
  (2, 2);

INSERT INTO categoria (id_categoria, descripcion, activo) VALUES
  (1, 'Novela', TRUE),
  (2, 'Fantasía', TRUE),
  (3, 'Ciencia ficción', TRUE),
  (4, 'Desarrollo personal', TRUE),
  (5, 'Tecnología', TRUE),
  (6, 'Novela corta', TRUE),
  (7, 'Literatura Infantil y Juvenil', TRUE),
  (8, 'Ficción', TRUE);

INSERT INTO autor (id_autor, nombre, activo) VALUES
  (1, 'Gabriel García Márquez', TRUE),
  (2, 'J. R. R. Tolkien', TRUE),
  (3, 'George Orwell', TRUE),
  (4, 'Isaac Asimov', TRUE),
  (5, 'Robert C. Martin', TRUE),
  (6, 'James Clear', TRUE),
  (7, 'Antoine de Saint-Exupéry', TRUE),
  (8, 'Lara Rios', TRUE),
  (9, 'Fernando Contreras Castro', TRUE);

INSERT INTO editorial (id_editorial, nombre, activo) VALUES
  (1, 'Editorial Sudamericana', TRUE),
  (2, 'Minotauro', TRUE),
  (3, 'Debolsillo', TRUE),
  (4, 'Penguin Random House', TRUE),
  (5, 'Prentice Hall', TRUE),
  (6, 'Planeta', TRUE),
  (7, 'Salamandra', TRUE),
  (8, 'Editorial Costa Rica', TRUE),
  (9, 'Desconocido', TRUE);

INSERT INTO libro
  (id_libro, titulo, descripcion, precio, existencias, isbn, ubicacion_fisica,
   ruta_imagen, activo, id_categoria, id_autor, id_editorial)
VALUES
  (1, 'Cien años de soledad', 'Novela emblemática del realismo mágico latinoamericano.',
   12500.00, 12, '9780307474728', 'Estante A1',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2Fb7d3e716-1a05-4dd7-8ad4-0adf3d511635.jpg?alt=media&token=f4792b2c-088b-4182-8a91-8e77734d7b81', TRUE, 1, 1, 1),
  (2, 'El Hobbit', 'Historia fantástica sobre el viaje de Bilbo Bolsón.',
   9800.00, 15, '9780547928227', 'Estante B1',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2Fcd0f51ef-018c-4e89-b38c-31e0b9bebffa.jpg?alt=media&token=0a05d895-09bb-45ad-984c-861b2e33df0d', TRUE, 2, 2, 2),
  (3, '1984', 'Novela distópica sobre vigilancia, poder y control social.',
   7800.00, 17, '9780451524935', 'Estante A2',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F48313994-39dd-4ecc-a3ee-12adb377212a.jpg?alt=media&token=6f5195eb-fe0b-4421-a987-cc53e8203342', TRUE, 1, 3, 3),
  (4, 'Fundación', 'Obra clásica de ciencia ficción sobre historia, imperio y predicción social.',
   11200.00, 9, '9780553293357', 'Estante C1',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F49709c89-599c-43d1-bda6-86c0efc1e7fd.jpg?alt=media&token=4239ec21-3a46-4cba-8c06-880241b040bb', TRUE, 3, 4, 4),
  (5, 'Clean Code', 'Libro técnico sobre principios para escribir código limpio y mantenible.',
   32000.00, 8, '9780132350884', 'Estante T1',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F0942e2ab-1fde-40b4-a5ed-36ee210959e3.jpg?alt=media&token=d13890f9-35d3-43b3-be2f-ce983c8a16f3', TRUE, 5, 5, 5),
  (6, 'Hábitos Atómicos', 'Libro sobre construcción de hábitos, mejora continua y productividad.',
   14500.00, 18, '9780735211292', 'Estante D1',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F00a86c7c-6e2f-4066-b399-fda62d6e8209.jpg?alt=media&token=3e0c9bfe-131b-46dc-bf94-dc613a365280', TRUE, 4, 6, 6),
  (7, 'Arquitectura Limpia', 'Libro técnico sobre arquitectura de software y separación de responsabilidades.',
   35500.00, 6, '9780134494166', 'Estante T2',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2Fec822384-93ab-4b64-b3a8-de4d3494a643.jpg?alt=media&token=55ae402a-3515-480d-b5c4-864a84ce3227', TRUE, 5, 5, 5),
  (8, 'El Señor de los Anillos', 'Obra de fantasía épica centrada en la Tierra Media.',
   22500.00, 9, '9780618640157', 'Estante B2',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2Fa05289bf-7056-4b6f-b704-3ab8dad94a10.webp?alt=media&token=dbeaa992-5321-44c7-b35b-4f09de385ac6', TRUE, 2, 2, 2),
  (9, 'El Principito', 'Relato poético sobre la amistad, el amor y la mirada de la infancia.',
   8700.00, 5, '9788418174193', 'Estante C2',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F3adeb103-b458-4c59-825a-e65b3183a4a1.jpg?alt=media&token=a8b331c3-5618-4bf1-8b36-532066d7d32f', TRUE, 6, 7, 7),
  (10, 'Los cuentos de mi alcancía', 'Colección de relatos para lectores infantiles y juveniles.',
   9500.00, 5, '9789930549223', 'Estante K1',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2Fe540245f-2870-40fe-b2f5-e6ec4a1931d1.jpg?alt=media&token=0db60748-bd6d-4cc1-8c04-78fc1726d7f3', TRUE, 7, 8, 8),
  (11, '1984', 'Edición alternativa de la novela distópica de George Orwell.',
   9000.00, 6, '9791387810658', 'Estante A3',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F7231e643-d8be-4140-980c-9624921658fb.jpg?alt=media&token=22d673a5-2a95-40da-b2bc-5976c5601576', TRUE, 1, 3, 2),
  (12, 'Única mirando al mar', 'Novela costarricense sobre exclusión social, dignidad y supervivencia.',
   6500.00, 15, '9789930519837', 'Estante A5',
   'https://firebasestorage.googleapis.com/v0/b/bookguest-36f8e.firebasestorage.app/o/bookguest%2Flibros%2F5af88c77-6eed-459b-a78d-cfca6ff3c68e.jpg?alt=media&token=497f9ae1-a9c2-45c1-918a-2a064671506b', TRUE, 8, 9, 8);

-- Fecha de Costa Rica independiente de la zona horaria del servidor MySQL/Aiven.
SET @fecha_hoy_cr = DATE(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 6 HOUR));

-- Ofertas vigentes durante siete días desde la ejecución del script.
INSERT INTO oferta
  (id_oferta, id_libro, descripcion, porcentaje_descuento, fecha_inicio, fecha_fin, activo)
VALUES
  (1, 3, 'Oferta especial en novelas clásicas', 10.00, @fecha_hoy_cr, DATE_ADD(@fecha_hoy_cr, INTERVAL 7 DAY), TRUE),
  (2, 6, 'Promoción de desarrollo personal', 15.00, @fecha_hoy_cr, DATE_ADD(@fecha_hoy_cr, INTERVAL 7 DAY), TRUE),
  (3, 5, 'Descuento en libros técnicos', 8.00, @fecha_hoy_cr, DATE_ADD(@fecha_hoy_cr, INTERVAL 7 DAY), TRUE),
  (4, 9, 'Oferta de la semana', 12.00, @fecha_hoy_cr, DATE_ADD(@fecha_hoy_cr, INTERVAL 7 DAY), TRUE);

-- Comprobación rápida del resultado de la instalación.
SELECT
  (SELECT COUNT(*) FROM rol) AS roles,
  (SELECT COUNT(*) FROM usuario) AS usuarios,
  (SELECT COUNT(*) FROM categoria) AS categorias,
  (SELECT COUNT(*) FROM libro) AS libros,
  (SELECT COUNT(*) FROM oferta WHERE activo = TRUE) AS ofertas_activas;
