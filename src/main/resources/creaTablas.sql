/*
  Script de creación de base de datos para BookGuest
  Proyecto Final Desarrollo Web

  Este script crea el esquema, tablas, usuarios,
  roles, rutas, constantes y datos iniciales.
*/

-- -----------------------------------------------------
-- Sección de administración
-- Ejecutar una vez en entorno de desarrollo
-- -----------------------------------------------------

DROP DATABASE IF EXISTS bookguest;
DROP USER IF EXISTS 'usuario_bookguest'@'localhost';
DROP USER IF EXISTS 'usuario_bookguest'@'%';
DROP USER IF EXISTS 'usuario_reportes'@'localhost';
DROP USER IF EXISTS 'usuario_reportes'@'%';

CREATE DATABASE bookguest
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER 'usuario_bookguest'@'%' IDENTIFIED BY 'BookGuest_2026';
CREATE USER 'usuario_reportes'@'%' IDENTIFIED BY 'BookGuest_Reportes_2026';

GRANT SELECT, INSERT, UPDATE, DELETE ON bookguest.* TO 'usuario_bookguest'@'%';
GRANT SELECT ON bookguest.* TO 'usuario_reportes'@'%';

FLUSH PRIVILEGES;

USE bookguest;

-- -----------------------------------------------------
-- Sección de creación de tablas
-- -----------------------------------------------------

-- Tabla de roles
CREATE TABLE rol (
  id_rol INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_rol),
  UNIQUE (nombre)
) ENGINE = InnoDB;

-- Tabla de usuarios
CREATE TABLE usuario (
  id_usuario INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(80) NOT NULL,
  email VARCHAR(120) NOT NULL,
  password VARCHAR(512) NOT NULL,
  telefono VARCHAR(25) NULL,
  direccion VARCHAR(255) NULL,
  ruta_imagen VARCHAR(1024) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario),
  UNIQUE (email),
  CHECK (email REGEXP '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
  INDEX ndx_usuario_email (email)
) ENGINE = InnoDB;

-- Tabla intermedia usuario / rol
CREATE TABLE usuario_rol (
  id_usuario INT NOT NULL,
  id_rol INT NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario, id_rol),
  CONSTRAINT fk_usuario_rol_usuario
    FOREIGN KEY (id_usuario)
    REFERENCES usuario(id_usuario),
  CONSTRAINT fk_usuario_rol_rol
    FOREIGN KEY (id_rol)
    REFERENCES rol(id_rol)
) ENGINE = InnoDB;

-- Tabla de categorías de libros
CREATE TABLE categoria (
  id_categoria INT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(80) NOT NULL,
  ruta_imagen VARCHAR(1024) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_categoria),
  UNIQUE (descripcion),
  INDEX ndx_categoria_descripcion (descripcion)
) ENGINE = InnoDB;

-- Tabla de autores
CREATE TABLE autor (
  id_autor INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  biografia TEXT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_autor),
  UNIQUE (nombre),
  INDEX ndx_autor_nombre (nombre)
) ENGINE = InnoDB;

-- Tabla de editoriales
CREATE TABLE editorial (
  id_editorial INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  pais VARCHAR(80) NULL,
  sitio_web VARCHAR(255) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_editorial),
  UNIQUE (nombre),
  INDEX ndx_editorial_nombre (nombre)
) ENGINE = InnoDB;

-- Tabla de libros
CREATE TABLE libro (
  id_libro INT NOT NULL AUTO_INCREMENT,
  id_categoria INT NOT NULL,
  id_autor INT NOT NULL,
  id_editorial INT NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  isbn VARCHAR(20) NOT NULL,
  descripcion TEXT NOT NULL,
  precio DECIMAL(12,2) NOT NULL CHECK (precio > 0),
  existencias INT UNSIGNED NOT NULL DEFAULT 0 CHECK (existencias >= 0),
  ubicacion_fisica VARCHAR(150) NOT NULL,
  ruta_imagen VARCHAR(1024) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_publicacion DATE NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_libro),
  UNIQUE (isbn),
  INDEX ndx_libro_titulo (titulo),
  INDEX ndx_libro_categoria (id_categoria),
  INDEX ndx_libro_autor (id_autor),
  INDEX ndx_libro_editorial (id_editorial),
  CONSTRAINT fk_libro_categoria
    FOREIGN KEY (id_categoria)
    REFERENCES categoria(id_categoria),
  CONSTRAINT fk_libro_autor
    FOREIGN KEY (id_autor)
    REFERENCES autor(id_autor),
  CONSTRAINT fk_libro_editorial
    FOREIGN KEY (id_editorial)
    REFERENCES editorial(id_editorial)
) ENGINE = InnoDB;

-- Tabla de ofertas
CREATE TABLE oferta (
  id_oferta INT NOT NULL AUTO_INCREMENT,
  id_libro INT NOT NULL,
  descripcion VARCHAR(150) NOT NULL,
  porcentaje_descuento DECIMAL(5,2) NOT NULL CHECK (porcentaje_descuento > 0 AND porcentaje_descuento <= 100),
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_oferta),
  INDEX ndx_oferta_libro (id_libro),
  CONSTRAINT fk_oferta_libro
    FOREIGN KEY (id_libro)
    REFERENCES libro(id_libro),
  CHECK (fecha_fin >= fecha_inicio)
) ENGINE = InnoDB;

-- Tabla de carritos
CREATE TABLE carrito (
  id_carrito INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  estado ENUM('Activo', 'Comprado', 'Cancelado') NOT NULL DEFAULT 'Activo',
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_carrito),
  INDEX ndx_carrito_usuario (id_usuario),
  CONSTRAINT fk_carrito_usuario
    FOREIGN KEY (id_usuario)
    REFERENCES usuario(id_usuario)
) ENGINE = InnoDB;

-- Tabla de detalle del carrito
CREATE TABLE carrito_detalle (
  id_carrito_detalle INT NOT NULL AUTO_INCREMENT,
  id_carrito INT NOT NULL,
  id_libro INT NOT NULL,
  cantidad INT UNSIGNED NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_carrito_detalle),
  UNIQUE (id_carrito, id_libro),
  INDEX ndx_carrito_detalle_carrito (id_carrito),
  INDEX ndx_carrito_detalle_libro (id_libro),
  CONSTRAINT fk_carrito_detalle_carrito
    FOREIGN KEY (id_carrito)
    REFERENCES carrito(id_carrito),
  CONSTRAINT fk_carrito_detalle_libro
    FOREIGN KEY (id_libro)
    REFERENCES libro(id_libro)
) ENGINE = InnoDB;

-- Tabla transaccional principal: pedidos
CREATE TABLE pedido (
  id_pedido INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
  impuesto DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (impuesto >= 0),
  total DECIMAL(12,2) NOT NULL CHECK (total > 0),
  estado ENUM('Pendiente', 'Pagado', 'Enviado', 'Entregado', 'Cancelado') NOT NULL DEFAULT 'Pendiente',
  metodo_pago ENUM('Tarjeta', 'Sinpe', 'Transferencia', 'Efectivo') NOT NULL DEFAULT 'Tarjeta',
  direccion_envio VARCHAR(255) NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_pedido),
  INDEX ndx_pedido_usuario (id_usuario),
  CONSTRAINT fk_pedido_usuario
    FOREIGN KEY (id_usuario)
    REFERENCES usuario(id_usuario)
) ENGINE = InnoDB;

-- Tabla transaccional de detalle de pedidos
CREATE TABLE pedido_detalle (
  id_pedido_detalle INT NOT NULL AUTO_INCREMENT,
  id_pedido INT NOT NULL,
  id_libro INT NOT NULL,
  precio_historico DECIMAL(12,2) NOT NULL CHECK (precio_historico >= 0),
  cantidad INT UNSIGNED NOT NULL CHECK (cantidad > 0),
  subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_pedido_detalle),
  INDEX ndx_pedido_detalle_pedido (id_pedido),
  INDEX ndx_pedido_detalle_libro (id_libro),
  CONSTRAINT fk_pedido_detalle_pedido
    FOREIGN KEY (id_pedido)
    REFERENCES pedido(id_pedido),
  CONSTRAINT fk_pedido_detalle_libro
    FOREIGN KEY (id_libro)
    REFERENCES libro(id_libro)
) ENGINE = InnoDB;

-- Tabla de inventario
CREATE TABLE inventario (
  id_inventario INT NOT NULL AUTO_INCREMENT,
  id_libro INT NOT NULL,
  tipo_movimiento ENUM('Entrada', 'Salida', 'Ajuste') NOT NULL,
  cantidad INT NOT NULL,
  descripcion VARCHAR(255) NULL,
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_inventario),
  INDEX ndx_inventario_libro (id_libro),
  CONSTRAINT fk_inventario_libro
    FOREIGN KEY (id_libro)
    REFERENCES libro(id_libro)
) ENGINE = InnoDB;

-- Tabla de rutas
CREATE TABLE ruta (
  id_ruta INT NOT NULL AUTO_INCREMENT,
  ruta VARCHAR(255) NOT NULL,
  id_rol INT NULL,
  requiere_rol BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_ruta),
  CHECK (id_rol IS NOT NULL OR requiere_rol = FALSE),
  CONSTRAINT fk_ruta_rol
    FOREIGN KEY (id_rol)
    REFERENCES rol(id_rol)
) ENGINE = InnoDB;

-- Tabla de constantes
CREATE TABLE constante (
  id_constante INT NOT NULL AUTO_INCREMENT,
  atributo VARCHAR(50) NOT NULL,
  valor VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_constante),
  UNIQUE (atributo)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Sección de inserción de datos
-- -----------------------------------------------------

-- Roles
INSERT INTO rol (id_rol, nombre) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_CLIENTE');

-- Usuarios
-- Contraseñas temporales con {noop}, válidas para desarrollo:
-- admin@bookguest.com   / Admin123.
-- cliente@bookguest.com / Cliente123.
INSERT INTO usuario
(id_usuario, nombre, email, password, telefono, direccion, ruta_imagen, activo)
VALUES
(1, 'Administrador BookGuest', 'admin@bookguest.com', '{noop}Admin123.', '8888-0001', 'San José, Costa Rica', NULL, TRUE),
(2, 'Cliente BookGuest', 'cliente@bookguest.com', '{noop}Cliente123.', '8888-0002', 'Heredia, Costa Rica', NULL, TRUE),
(3, 'María Lectura', 'maria@bookguest.com', '{noop}Cliente123.', '8888-0003', 'Alajuela, Costa Rica', NULL, TRUE);

-- Relación usuarios / roles
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(1, 1),
(2, 2),
(3, 2);

-- Categorías
INSERT INTO categoria (id_categoria, descripcion, ruta_imagen, activo) VALUES
(1, 'Novela', 'https://images.unsplash.com/photo-1512820790803-83ca734da794', TRUE),
(2, 'Fantasía', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f', TRUE),
(3, 'Ciencia ficción', 'https://images.unsplash.com/photo-1532012197267-da84d127e765', TRUE),
(4, 'Desarrollo personal', 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d', TRUE),
(5, 'Tecnología', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3', TRUE);

-- Autores
INSERT INTO autor (id_autor, nombre, biografia, activo) VALUES
(1, 'Gabriel García Márquez', 'Autor reconocido por obras de literatura latinoamericana.', TRUE),
(2, 'J. R. R. Tolkien', 'Autor de literatura fantástica.', TRUE),
(3, 'George Orwell', 'Autor de obras de crítica social y política.', TRUE),
(4, 'Isaac Asimov', 'Autor de ciencia ficción y divulgación científica.', TRUE),
(5, 'Robert C. Martin', 'Autor técnico especializado en buenas prácticas de programación.', TRUE),
(6, 'James Clear', 'Autor de libros de hábitos y productividad.', TRUE);

-- Editoriales
INSERT INTO editorial (id_editorial, nombre, pais, sitio_web, activo) VALUES
(1, 'Editorial Sudamericana', 'Argentina', NULL, TRUE),
(2, 'Minotauro', 'España', NULL, TRUE),
(3, 'Debolsillo', 'España', NULL, TRUE),
(4, 'Penguin Random House', 'Estados Unidos', NULL, TRUE),
(5, 'Prentice Hall', 'Estados Unidos', NULL, TRUE),
(6, 'Planeta', 'España', NULL, TRUE);

-- Libros
INSERT INTO libro
(id_libro, id_categoria, id_autor, id_editorial, titulo, isbn, descripcion, precio, existencias, ubicacion_fisica, ruta_imagen, activo, fecha_publicacion)
VALUES
(1, 1, 1, 1, 'Cien años de soledad', '9780307474728',
 'Novela emblemática del realismo mágico latinoamericano.', 12500.00, 12,
 'Estante A1 - Literatura latinoamericana',
 'https://images.unsplash.com/photo-1544947950-fa07a98d237f', TRUE, '1967-05-30'),

(2, 2, 2, 2, 'El Hobbit', '9780547928227',
 'Historia fantástica sobre el viaje de Bilbo Bolsón.', 9800.00, 15,
 'Estante B1 - Fantasía',
 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e', TRUE, '1937-09-21'),

(3, 1, 3, 3, '1984', '9780451524935',
 'Novela distópica sobre vigilancia, poder y control social.', 8700.00, 20,
 'Estante A2 - Novela clásica',
 'https://images.unsplash.com/photo-1516979187457-637abb4f9353', TRUE, '1949-06-08'),

(4, 3, 4, 4, 'Fundación', '9780553293357',
 'Obra clásica de ciencia ficción sobre historia, imperio y predicción social.', 11200.00, 10,
 'Estante C1 - Ciencia ficción',
 'https://images.unsplash.com/photo-1526243741027-444d633d7365', TRUE, '1951-01-01'),

(5, 5, 5, 5, 'Clean Code', '9780132350884',
 'Libro técnico sobre principios para escribir código limpio y mantenible.', 32000.00, 8,
 'Estante T1 - Programación',
 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4', TRUE, '2008-08-01'),

(6, 4, 6, 6, 'Hábitos Atómicos', '9780735211292',
 'Libro sobre construcción de hábitos, mejora continua y productividad.', 14500.00, 18,
 'Estante D1 - Desarrollo personal',
 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da', TRUE, '2018-10-16'),

(7, 5, 5, 5, 'Arquitectura Limpia', '9780134494166',
 'Libro técnico sobre arquitectura de software y separación de responsabilidades.', 35500.00, 6,
 'Estante T2 - Ingeniería de software',
 'https://images.unsplash.com/photo-1519389950473-47ba0277781c', TRUE, '2017-09-20'),

(8, 2, 2, 2, 'El Señor de los Anillos', '9780618640157',
 'Obra de fantasía épica centrada en la Tierra Media.', 22500.00, 9,
 'Estante B2 - Fantasía épica',
 'https://images.unsplash.com/photo-1519682337058-a94d519337bc', TRUE, '1954-07-29');

-- Ofertas
INSERT INTO oferta
(id_oferta, id_libro, descripcion, porcentaje_descuento, fecha_inicio, fecha_fin, activo)
VALUES
(1, 3, 'Oferta especial en novelas clásicas', 10.00, '2026-01-01', '2026-12-31', TRUE),
(2, 6, 'Promoción de desarrollo personal', 15.00, '2026-01-01', '2026-12-31', TRUE),
(3, 5, 'Descuento en libros técnicos', 8.00, '2026-01-01', '2026-12-31', TRUE);

-- Carritos
INSERT INTO carrito (id_carrito, id_usuario, estado) VALUES
(1, 2, 'Activo'),
(2, 3, 'Activo');

-- Detalle de carritos
INSERT INTO carrito_detalle
(id_carrito_detalle, id_carrito, id_libro, cantidad, precio_unitario)
VALUES
(1, 1, 2, 1, 9800.00),
(2, 1, 6, 1, 14500.00),
(3, 2, 3, 1, 8700.00);

-- Pedidos
INSERT INTO pedido
(id_pedido, id_usuario, fecha, subtotal, impuesto, total, estado, metodo_pago, direccion_envio)
VALUES
(1, 2, '2026-02-10 10:30:00', 22300.00, 2899.00, 25199.00, 'Pagado', 'Tarjeta', 'Heredia, Costa Rica'),
(2, 3, '2026-02-12 15:45:00', 8700.00, 1131.00, 9831.00, 'Entregado', 'Sinpe', 'Alajuela, Costa Rica');

-- Detalle de pedidos
INSERT INTO pedido_detalle
(id_pedido_detalle, id_pedido, id_libro, precio_historico, cantidad, subtotal)
VALUES
(1, 1, 2, 9800.00, 1, 9800.00),
(2, 1, 6, 12500.00, 1, 12500.00),
(3, 2, 3, 8700.00, 1, 8700.00);

-- Movimientos de inventario
INSERT INTO inventario
(id_inventario, id_libro, tipo_movimiento, cantidad, descripcion)
VALUES
(1, 1, 'Entrada', 12, 'Carga inicial de inventario'),
(2, 2, 'Entrada', 15, 'Carga inicial de inventario'),
(3, 3, 'Entrada', 20, 'Carga inicial de inventario'),
(4, 4, 'Entrada', 10, 'Carga inicial de inventario'),
(5, 5, 'Entrada', 8, 'Carga inicial de inventario'),
(6, 6, 'Entrada', 18, 'Carga inicial de inventario'),
(7, 2, 'Salida', 1, 'Venta registrada en pedido 1'),
(8, 6, 'Salida', 1, 'Venta registrada en pedido 1'),
(9, 3, 'Salida', 1, 'Venta registrada en pedido 2');

-- Rutas protegidas para administrador
INSERT INTO ruta (ruta, id_rol, requiere_rol) VALUES
('/admin/**', 1, TRUE),
('/admin/dashboard', 1, TRUE),
('/admin/inventario', 1, TRUE),
('/admin/usuarios', 1, TRUE),
('/admin/pedidos', 1, TRUE),
('/admin/productos', 1, TRUE),
('/admin/configuracion', 1, TRUE);

-- Rutas protegidas para cliente
INSERT INTO ruta (ruta, id_rol, requiere_rol) VALUES
('/cliente/**', 2, TRUE),
('/cliente/inicio', 2, TRUE),
('/cliente/libros', 2, TRUE),
('/cliente/ofertas', 2, TRUE),
('/cliente/carrito', 2, TRUE),
('/cliente/perfil', 2, TRUE),
('/cliente/pedidos', 2, TRUE);

-- Rutas públicas
INSERT INTO ruta (ruta, requiere_rol) VALUES
('/', FALSE),
('/index', FALSE),
('/login', FALSE),
('/registro', FALSE),
('/registro/confirmacion', FALSE),
('/restablecer', FALSE),
('/restablecer/confirmacion', FALSE),
('/logout', FALSE),
('/error', FALSE),
('/webjars/**', FALSE);

-- Constantes de la aplicación
INSERT INTO constante (atributo, valor) VALUES
('dominio', 'localhost'),
('servidor.http', 'http://localhost:91'),
('app.nombre', 'BookGuest'),
('app.descripcion', 'Sistema web para venta y administración de libros'),
('moneda', 'CRC'),
('impuesto.ventas', '13'),
('correo.soporte', 'soporte@bookguest.com'),
('firebase.storage.path', 'bookguest');

USE bookguest;

DROP TABLE IF EXISTS carrito_detalle;
DROP TABLE IF EXISTS carrito;

CREATE TABLE carrito (
    id_carrito BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_carrito),

    INDEX ndx_carrito_usuario (id_usuario),

    CONSTRAINT fk_carrito_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
) ENGINE = InnoDB;

CREATE TABLE carrito_detalle (
    id_carrito_detalle BIGINT NOT NULL AUTO_INCREMENT,
    id_carrito BIGINT NOT NULL,
    id_libro INT NOT NULL,
    cantidad INT UNSIGNED NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(12,2) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_carrito_detalle),

    UNIQUE KEY uk_carrito_libro (id_carrito, id_libro),

    INDEX ndx_carrito_detalle_carrito (id_carrito),
    INDEX ndx_carrito_detalle_libro (id_libro),

    CONSTRAINT fk_carrito_detalle_carrito
        FOREIGN KEY (id_carrito)
        REFERENCES carrito(id_carrito),

    CONSTRAINT fk_carrito_detalle_libro
        FOREIGN KEY (id_libro)
        REFERENCES libro(id_libro)
) ENGINE = InnoDB;


--Modificación de la tabla usuario

USE bookguest

ALTER TABLE usuario 
ADD COLUMN apellidos VARCHAR(75) NOT NULL DEFAULT 'Pendiente' AFTER nombre;

ALTER TABLE usuario 
ADD COLUMN telefono VARCHAR(25) NULL AFTER email;

ALTER TABLE usuario 
ADD COLUMN direccion VARCHAR(255) NULL AFTER telefono;

ALTER TABLE usuario 
ADD COLUMN ruta_imagen VARCHAR(1024) NULL AFTER direccion;

ALTER TABLE usuario 
ADD COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE usuario 
ADD COLUMN fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
