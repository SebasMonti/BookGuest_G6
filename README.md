# 📚 BookGuest - 

## 🎯 Descripción del Proyecto

**BookGuest** es una aplicación web desarrollada con **Spring Boot** que funciona como una plataforma completa de e-commerce para la venta y administración de libros. El sistema implementa un modelo de negocio con dos roles principales: administrador y cliente, con funcionalidades completas de catálogo, carrito de compras, gestión de pedidos e integración con Firebase Storage para la carga de imágenes.

Este proyecto fue desarrollado como trabajo final del curso de **Desarrollo Web** por el **Grupo 6**.

---

## 👥 Equipo de Desarrollo

- **Sebastián Montiel** - Desarrollador
- **María Fernanda Hernández Moya** - Desarrolladora
- **Danny Antonio Fonseca García** - Desarrollador
- **Nahum Esteban Ramírez Fuentes** - Desarrollador

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
| --- | --- | --- |
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 4.1.0 | Framework web |
| **Spring Security** | 4.1.0 | Autenticación y autorización |
| **Spring Data JPA** | 4.1.0 | Acceso a datos |
| **Thymeleaf** | 4.1.0 | Motor de plantillas |
| **MySQL** | Latest | Base de datos |
| **Bootstrap** | 5.3.8 | Framework CSS |
| **jQuery** | 4.0.0 | Librería JavaScript |
| **Font Awesome** | 7.2.0 | Iconos |
| **Firebase Admin SDK** | 9.9.0 | Gestión de almacenamiento en la nube |
| **Lombok** | Latest | Reducción de código boilerplate |
| **Maven** | Latest | Gestor de dependencias |

---

## 🚀 Características Principales

### 📱 Módulo Público
- ✅ Página de inicio atractiva
- ✅ Autenticación (login/logout)
- ✅ Registro de nuevos clientes
- ✅ Recuperación y restablecimiento de contraseña
- ✅ Manejo personalizado de errores

### 👤 Módulo Cliente
- 📖 Exploración completa del catálogo de libros
- 🔍 Búsqueda y filtrado avanzado
- 📄 Visualización detallada de cada libro
- 🎁 Acceso a ofertas y promociones
- 🛒 Gestión integral del carrito de compras
- 📦 Visualización y seguimiento de pedidos
- 👤 Gestión del perfil de usuario

### 🔐 Módulo Administrador
- 📊 Dashboard administrativo con estadísticas
- 📚 Gestión completa de libros (CRUD)
- 👥 Administración de usuarios
- 📋 Gestión de pedidos
- 🏪 Gestión de inventario
- 🎁 Administración de ofertas
- ⚙️ Configuración general del sistema
- 👨‍🏫 Gestión de autores, categorías y editoriales

### 🔒 Seguridad
- 🔐 Autenticación con Spring Security
- 📧 Login mediante correo y contraseña
- 👮 Control de acceso basado en roles (RBAC)
- 🛡️ Protección de rutas según permisos
- ↩️ Redirección automática según rol del usuario

### 🌍 Internacionalización
- 🇪🇸 Soporte para español
- 🇺🇸 Soporte para inglés
- 🇲🇽 Configuración regional

---

## 📁 Estructura del Proyecto

```javascript
BookGuest_G6/
├── src/
│   └── main/
│       ├── java/com/bookguest/
│       │   ├── controller/          # Controladores MVC
│       │   │   ├── AdminController.java
│       │   │   ├── ClienteController.java
│       │   │   ├── LoginController.java
│       │   │   └── ...
│       │   ├── service/             # Lógica de negocio
│       │   │   ├── LibroService.java
│       │   │   ├── UsuarioService.java
│       │   │   ├── CarritoService.java
│       │   │   └── ...
│       │   ├── repository/          # Acceso a datos (Spring Data JPA)
│       │   │   ├── LibroRepository.java
│       │   │   ├── UsuarioRepository.java
│       │   │   └── ...
│       │   ├── domain/              # Entidades JPA
│       │   │   ├── Usuario.java
│       │   │   ├── Libro.java
│       │   │   ├── Pedido.java
│       │   │   └── ...
│       │   ├── BookguestApplication.java    # Clase principal
│       │   ├── ProjectConfig.java           # Configuración
│       │   └── StorageConfig.java           # Config de Firebase
│       └── resources/
│           ├── templates/           # Vistas Thymeleaf
│           │   ├── admin/           # Plantillas administrativas
│           │   ├── cliente/         # Plantillas de cliente
│           │   └── general/         # Plantillas públicas
│           ├── firebase/            # Credenciales Firebase
│           ├── application.properties
│           ├── creaTablas.sql       # Script de inicialización BD
│           ├── messages.properties
│           ├── messages_es.properties
│           └── messages_en.properties
├── pom.xml                          # Dependencias Maven
└── README.md
```

---

## ⚙️ Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- ✅ **Java JDK 21** o superior → [Descargar](https://www.oracle.com/java/technologies/downloads/)
- ✅ **Maven 3.9+** → [Descargar](https://maven.apache.org/download.cgi)
- ✅ **MySQL Server 8.0+** → [Descargar](https://www.mysql.com/downloads/)
- ✅ **IDE compatible**: NetBeans, IntelliJ IDEA, Eclipse o VS Code
- ✅ **Cuenta Firebase** con Storage configurado (opcional, pero recomendado)

---

## 🗄️ Configuración de Base de Datos

### 1. Crear la base de datos

Ejecutar el script SQL que se encuentra en:

```javascript
src/main/resources/creaTablas.sql
```

Este script crea automáticamente:
- Base de datos `bookguest`
- Usuario de aplicación
- Todas las tablas necesarias
- Datos iniciales (usuarios, categorías, libros, etc.)
- Roles de seguridad

**Instrucciones:**

```bash
mysql -u root -p < src/main/resources/creaTablas.sql
```

O importar el archivo SQL directamente desde MySQL Workbench o tu cliente favorito.

### 2. Configurar conexión a la base de datos

Editar el archivo:

```javascript
src/main/resources/application.properties
```

Asegúrate de que tengas:

```properties
# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/bookguest?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=usuario_bookguest
spring.datasource.password=tu_contraseña

# Dialecto JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Puerto de la aplicación
server.port=91
```

---

## 🔑 Usuarios de Prueba

El script `creaTablas.sql` proporciona usuarios iniciales para probar la aplicación:

### Administrador
```javascript
Correo:     admin@bookguest.com
Contraseña: (revisar en creaTablas.sql)
Rol:        ROLE_ADMIN
```

### Cliente
```javascript
Correo:     cliente@bookguest.com
Contraseña: (revisar en creaTablas.sql)
Rol:        ROLE_CLIENTE
```

### Cliente Adicional
```javascript
Correo:     maria@bookguest.com
Contraseña: Cliente123.
Rol:        ROLE_CLIENTE
```

---

## 🔥 Configuración de Firebase Storage (Opcional)

Si deseas habilitar la carga de imágenes con Firebase:

### 1. Obtener credenciales de Firebase

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Crear un nuevo proyecto o seleccionar uno existente
3. Descargar el archivo JSON de credenciales (`bookguest-36f8e-firebase-adminsdk-...json`)

### 2. Colocar el archivo de credenciales

Guardar el archivo en:

```javascript
src/main/resources/firebase/
```

### 3. Configurar en application.properties

```properties
# Firebase Storage
firebase.bucket.name=bookguest-36f8e.firebasestorage.app
firebase.storage.path=bookguest
firebase.json.path=firebase
firebase.json.file=bookguest-36f8e-firebase-adminsdk-fbsvc-c4bbaa9be2.json
```

⚠️ **Nota de Seguridad:** No subir archivos de credenciales privadas a repositorios públicos. Usar variables de entorno en producción.

---

## 🚀 Ejecución del Proyecto

### Opción 1: Desde el IDE

1. Clonar el repositorio:
```bash
   git clone https://github.com/SebasMonti/BookGuest_G6.git
```

2. Abrir el proyecto en tu IDE favorito (NetBeans, IntelliJ, Eclipse)

3. Verificar que MySQL esté corriendo:
```bash
   mysql -u root -p
```

4. Ejecutar el script `creaTablas.sql`

5. Ejecutar la clase principal desde el IDE:
    - Buscar: `BookguestApplication.java`
    - Click derecho → Run

6. Acceder a la aplicación:
```javascript
   http://localhost:91
```

### Opción 2: Desde la terminal con Maven

```bash
# Navegar al directorio del proyecto
cd BookGuest_G6

# Ejecutar la aplicación
mvn spring-boot:run
```

Luego acceder a: `http://localhost:91`

### Opción 3: Generar archivo JAR ejecutable

```bash
# Compilar y empaquetar
mvn clean package

# Ejecutar el JAR
java -jar target/bookguest-1.jar
```

La aplicación estará disponible en: `http://localhost:91`

---

## 📍 Rutas Principales

### Rutas Públicas
| Ruta | Descripción |
| --- | --- |
| `/` | Página de inicio |
| `/login` | Formulario de inicio de sesión |
| `/registro` | Formulario de registro |
| `/restablecer` | Recuperación de contraseña |
| `/error` | Página de error personalizada |

### Rutas de Cliente
| Ruta | Descripción |
| --- | --- |
| `/cliente/inicio` | Dashboard del cliente |
| `/cliente/libros` | Catálogo de libros |
| `/cliente/libro/{idLibro}` | Detalle de un libro |
| `/cliente/ofertas` | Ofertas especiales |
| `/cliente/carrito` | Carrito de compras |
| `/cliente/pedidos` | Mis pedidos |
| `/cliente/perfil` | Perfil de usuario |

### Rutas de Administrador
| Ruta | Descripción |
| --- | --- |
| `/admin/dashboard` | Dashboard administrativo |
| `/admin/libros` | Gestión de libros |
| `/admin/usuarios` | Gestión de usuarios |
| `/admin/pedidos` | Gestión de pedidos |
| `/admin/inventario` | Gestión de inventario |
| `/admin/ofertas` | Gestión de ofertas |
| `/admin/configuracion` | Configuración general |

---

## 🗂️ Modelo de Datos

El sistema utiliza las siguientes entidades principales:

- **Usuario** - Información de usuarios del sistema
- **Rol** - Definición de roles (Admin, Cliente)
- **Libro** - Catálogo de libros disponibles
- **Categoría** - Categorización de libros
- **Autor** - Información de autores
- **Editorial** - Información de editoriales
- **Oferta** - Promociones y descuentos
- **Carrito** - Carrito de compras de usuario
- **CarritoDetalle** - Items dentro del carrito
- **Pedido** - Órdenes de compra
- **PedidoDetalle** - Detalles de cada pedido
- **Inventario** - Stock disponible de libros

---

## 🔐 Sistema de Seguridad

### Roles

```javascript
ROLE_ADMIN    → Acceso a funciones administrativas
ROLE_CLIENTE  → Acceso a funciones de cliente
```

### Mecanismos de Protección

- ✅ Autenticación mediante correo y contraseña
- ✅ Contraseñas hasheadas con bcrypt
- ✅ Sesiones seguras
- ✅ CSRF protection
- ✅ XSS prevention
- ✅ SQL injection prevention (mediante JPA)
- ✅ Control de acceso basado en roles (RBAC)

---

## 🌐 Internacionalización (i18n)

La aplicación soporta múltiples idiomas mediante archivos de propiedades:

- `messages.properties` - Idioma por defecto
- `messages_es.properties` - Español
- `messages_en.properties` - Inglés

Esto permite separar la lógica de texto de las vistas y facilita el mantenimiento y traducción.

---

## 📊 Compilación y Build

### Compilar el proyecto

```bash
mvn clean install
```

### Generar archivo ejecutable

```bash
mvn clean package
```

El archivo `.jar` se generará en la carpeta `target/`

### Ejecutar tests

```bash
mvn test
```

---

## 📝 Notas Importantes

- ⚠️ Las credenciales de Firebase no deben subirse a repositorios públicos
- ⚠️ Cambiar las contraseñas de usuarios de prueba en producción
- ⚠️ Configurar variables de entorno para datos sensibles
- ⚠️ Usar HTTPS en producción
- ⚠️ Configurar adecuadamente los permisos de CORS si es necesario

---

## 🤝 Contribuciones

Este es un proyecto académico. Para sugerencias o mejoras, por favor contactar a los miembros del equipo.

---

## 📄 Licencia

Este proyecto es propiedad del Grupo 6 de Desarrollo Web.

---

## 📞 Contacto y Soporte

Para preguntas o soporte técnico, contactar con los desarrolladores del Grupo 6.

---

**Última actualización:** Julio 2026

**Estado:** ✅ Proyecto completado y funcional