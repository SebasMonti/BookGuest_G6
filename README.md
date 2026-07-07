# BookGuest

BookGuest es una aplicación web desarrollada con Spring Boot para la venta y administración de libros. El sistema permite manejar usuarios, roles, catálogo de libros, carrito de compras, pedidos, inventario, ofertas y carga de imágenes mediante Firebase Storage.

## Descripción del proyecto

El proyecto está orientado a una librería en línea, donde existen dos perfiles principales:

* Administrador: gestiona inventario, productos, usuarios, pedidos y configuración general.
* Cliente: puede registrarse, iniciar sesión, consultar libros, ver ofertas, administrar su carrito y revisar sus pedidos.

La aplicación utiliza una arquitectura por capas, separando controladores, servicios, repositorios y entidades de dominio.

## Tecnologías utilizadas

* Java 21
* Spring Boot 4.1.0
* Spring MVC
* Spring Data JPA
* Spring Security
* Thymeleaf
* MySQL
* Maven
* Bootstrap 5.3.8
* Font Awesome
* jQuery
* Firebase Admin SDK
* Firebase Storage
* Lombok

## Funcionalidades principales

### Módulo público

* Página de inicio.
* Inicio de sesión.
* Registro de clientes.
* Recuperación o restablecimiento de acceso.
* Manejo de errores personalizados.

### Módulo de cliente

* Página de inicio para clientes.
* Consulta de libros disponibles.
* Búsqueda de libros.
* Visualización del detalle de cada libro.
* Consulta de ofertas.
* Administración del carrito de compras.
* Actualización de cantidades en el carrito.
* Eliminación de productos del carrito.
* Consulta de pedidos.
* Consulta de perfil.

### Módulo de administrador

* Dashboard administrativo.
* Gestión de inventario.
* Registro de nuevos libros.
* Modificación de libros existentes.
* Administración de usuarios.
* Administración de pedidos.
* Administración de productos.
* Configuración general.

### Seguridad

* Autenticación mediante Spring Security.
* Inicio de sesión usando correo electrónico y contraseña.
* Control de acceso por roles.
* Rutas protegidas para administradores.
* Rutas protegidas para clientes.
* Redirección automática según el rol del usuario.

### Internacionalización

El proyecto utiliza archivos de mensajes para manejar textos visibles en la aplicación:

* `messages.properties`
* `messages_es.properties`
* `messages_en.properties`

Esto permite separar los textos de las vistas y facilitar la traducción o mantenimiento del sistema.

## Estructura general del proyecto

```text
BookGuest/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/bookguest/
│       │       ├── controller/
│       │       ├── domain/
│       │       ├── repository/
│       │       ├── service/
│       │       ├── BookguestApplication.java
│       │       ├── ProjectConfig.java
│       │       └── StorageConfig.java
│       └── resources/
│           ├── templates/
│           │   ├── admin/
│           │   ├── cliente/
│           │   └── general/
│           ├── firebase/
│           ├── application.properties
│           ├── creaTablas.sql
│           ├── messages.properties
│           ├── messages_es.properties
│           └── messages_en.properties
├── pom.xml
└── README.md
```

## Requisitos previos

Antes de ejecutar el proyecto, se debe tener instalado:

* Java JDK 21 o superior.
* Maven.
* MySQL Server.
* NetBeans, IntelliJ IDEA, Eclipse o cualquier IDE compatible con Maven.
* Una cuenta o configuración válida de Firebase Storage, si se desea usar la carga de imágenes.

## Configuración de base de datos

El proyecto utiliza MySQL como motor de base de datos.

El archivo SQL principal se encuentra en:

```text
src/main/resources/creaTablas.sql
```

Este script crea:

* Base de datos `bookguest`.
* Usuario de aplicación.
* Usuario de reportes.
* Tablas principales del sistema.
* Roles.
* Usuarios iniciales.
* Categorías.
* Autores.
* Editoriales.
* Libros.
* Ofertas.
* Carritos.
* Pedidos.
* Inventario.
* Rutas protegidas.
* Constantes de configuración.

Para cargar la base de datos, ejecutar el script `creaTablas.sql` en MySQL.

## Configuración de conexión

La conexión se define en el archivo:

```text
src/main/resources/application.properties
```

Configuración principal:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookguest?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=usuario_bookguest
spring.datasource.password=BookGuest_2026
```

Puerto de ejecución:

```properties
server.port=91
```

Por defecto, la aplicación se ejecuta en:

```text
http://localhost:91
```

## Usuarios de prueba

El script de base de datos incluye usuarios iniciales para pruebas.

### Administrador

```text
Correo: admin@bookguest.com
Rol: ROLE_ADMIN
```

### Cliente

```text
Correo: cliente@bookguest.com
Rol: ROLE_CLIENTE
```

### Cliente adicional

```text
Correo: maria@bookguest.com
Contraseña: Cliente123.
Rol: ROLE_CLIENTE
```

## Configuración de Firebase Storage

El proyecto incluye integración con Firebase Storage para la carga de imágenes.

Configuración en `application.properties`:

```properties
firebase.bucket.name=bookguest-36f8e.firebasestorage.app
firebase.storage.path=bookguest
firebase.json.path=firebase
firebase.json.file=bookguest-36f8e-firebase-adminsdk-fbsvc-c4bbaa9be2.json
```

El archivo de credenciales debe ubicarse en:

```text
src/main/resources/firebase/
```

Nota importante: por seguridad, no se recomienda subir archivos de credenciales privadas de Firebase a repositorios públicos. En un entorno real, estas credenciales deberían manejarse mediante variables de entorno, secretos del servidor o configuración externa.

## Ejecución del proyecto

### Opción 1: Ejecutar desde el IDE

1. Clonar el repositorio.
2. Abrir el proyecto en NetBeans, IntelliJ IDEA o Eclipse.
3. Verificar que MySQL esté activo.
4. Ejecutar el script `creaTablas.sql`.
5. Revisar la configuración de `application.properties`.
6. Ejecutar la clase principal:

```text
BookguestApplication.java
```

7. Abrir el navegador en:

```text
http://localhost:91
```

### Opción 2: Ejecutar con Maven

Desde la raíz del proyecto, ejecutar:

```bash
mvn spring-boot:run
```

Luego abrir:

```text
http://localhost:91
```

## Compilación del proyecto

Para compilar el proyecto:

```bash
mvn clean install
```

Para generar el archivo ejecutable:

```bash
mvn clean package
```

El archivo `.jar` se generará en la carpeta:

```text
target/
```

## Rutas principales

### Rutas públicas

```text
/
 /index
 /login
 /registro
 /registro/confirmacion
 /restablecer
 /restablecer/confirmacion
 /error
```

### Rutas de cliente

```text
/cliente/inicio
/cliente/libros
/cliente/libro/{idLibro}
/cliente/ofertas
/cliente/carrito
/cliente/pedidos
/cliente/perfil
```

### Rutas de administrador

```text
/admin/dashboard
/admin/inventario
/admin/usuarios
/admin/pedidos
/admin/productos
/admin/configuracion
```

## Modelo de datos principal

El sistema utiliza las siguientes entidades principales:

* Usuario
* Rol
* Categoria
* Autor
* Editorial
* Libro
* Oferta
* Carrito
* CarritoDetalle
* Pedido
* PedidoDetalle
* Inventario

Estas entidades se gestionan mediante Spring Data JPA y repositorios propios para cada módulo.

## Seguridad y roles

El sistema define dos roles principales:

```text
ROLE_ADMIN
ROLE_CLIENTE
```

Las rutas administrativas solo pueden ser accedidas por usuarios con rol de administrador.

Las rutas de cliente solo pueden ser accedidas por usuarios con rol de cliente.

La autenticación se realiza mediante formulario personalizado en `/login`.

## Internacionalización

Los textos visibles de la aplicación se administran mediante archivos de mensajes. Esto evita quemar textos directamente en las vistas y facilita el soporte para varios idiomas.

Archivos utilizados:

*

Estas entidades se gestionan mediante Spring Data JPA y repositorios propios para cada módulo.

## Seguridad y roles

El sistema define dos roles principales:

```text
ROLE_ADMIN
ROLE_CLIENTE
```

Las rutas administrativas solo pueden ser accedidas por usuarios con rol de administrador.

Las rutas de cliente solo pueden ser accedidas por usuarios con rol de cliente.

La autenticación se realiza mediante formulario personalizado en `/login`.

## Internacionalización

Los textos visibles de la aplicación se administran mediante archivos de mensajes. Esto evita quemar textos directamente en las vistas y facilita el soporte para varios idiomas.

Archivos utilizados:

```text
messages.properties
messages_es.properties
messages_en.properties
```

## Estado del proyecto

Proyecto desarrollado como aplicación web académica para la gestión de una librería en línea. Incluye funcionalidades de catálogo, carrito, pedidos, inventario, roles, seguridad, internacionalización y carga de imágenes.

## Autor

Grupo 6 - Desarrollo Web_K

## Licencia

Proyecto desarrollado con fines académicos.
