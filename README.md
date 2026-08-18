# 📚 BookGuest

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Docker](https://img.shields.io/badge/Docker-Compatible-2496ED)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7)
![Aiven](https://img.shields.io/badge/Database-Aiven-FF355E)

BookGuest es una aplicación web para la gestión y venta de libros, desarrollada como proyecto universitario.

El sistema incluye una tienda virtual para clientes y un panel administrativo para gestionar usuarios, inventario, productos, pedidos y ofertas.

## Características principales

### Cliente

- Registro, inicio y cierre de sesión.
- Configuración de la cuenta.
- Carga de fotografía de perfil.
- Catálogo de libros.
- Búsqueda por título.
- Filtrado por categorías.
- Visualización de detalles de los productos.
- Lista de favoritos.
- Carrito de compras.
- Modificación de cantidades en el carrito.
- Generación de órdenes con formato `ORD-####`.
- Consulta de pedidos y sus estados.
- Métodos de pago simulados:
  - Visa.
  - Mastercard.
  - Pago en efectivo contra entrega.
- Interfaz disponible en español e inglés.

> La aplicación no procesa pagos bancarios reales. Los métodos de pago forman parte de la simulación académica del proceso de compra.

### Administrador

- Dashboard administrativo.
- Gestión del inventario.
- Control de precios unitarios, existencias y disponibilidad.
- Registro y modificación de productos.
- Gestión de usuarios.
- Asignación de roles de cliente y administrador.
- Gestión de pedidos.
- Cambio de estado de los pedidos:
  - Abierto.
  - En entrega.
  - Entregado.
  - Cancelado.
- Devolución automática del stock al cancelar un pedido.
- Cálculo de compras por usuario, excluyendo pedidos cancelados.
- Creación y administración de ofertas.
- Configuración del descuento y vigencia de las ofertas.
- Configuración de la cuenta administrativa.
- Interfaz disponible en español e inglés.

## Tecnologías utilizadas

| Área | Tecnología |
|---|---|
| Lenguaje principal | Java 21 |
| Framework backend | Spring Boot 4.1.0 |
| Arquitectura | MVC |
| Seguridad | Spring Security |
| Persistencia | Spring Data JPA |
| ORM | Hibernate |
| Validación | Spring Validation / Jakarta Bean Validation |
| Motor de plantillas | Thymeleaf |
| Frontend | HTML5, CSS3 y JavaScript |
| Diseño responsive | Bootstrap 5.3.8 |
| Iconos | Font Awesome 7.2.0 |
| Base de datos | MySQL 8.x |
| Almacenamiento de imágenes | Firebase Storage |
| Internacionalización | Spring MessageSource e i18n |
| Gestión de dependencias | Maven |
| Contenedores | Docker |
| Despliegue de la aplicación | Render |
| Base de datos en producción | Aiven for MySQL |
| IDE utilizado | Apache NetBeans 29 |
| Utilidades de desarrollo | Lombok y Spring Boot DevTools |

## Arquitectura del proyecto

BookGuest utiliza una arquitectura MVC con renderizado de vistas desde el servidor mediante Thymeleaf.

```text
src/main/java/com/bookguest/
├── controller/                 # Controladores web
├── domain/                     # Entidades y modelos
├── repository/                 # Acceso a la base de datos
├── service/                    # Lógica de negocio
├── BookguestApplication.java
├── InternationalizationConfig.java
├── ProjectConfig.java
└── StorageConfig.java

src/main/resources/
├── static/                     # CSS, JavaScript e imágenes
├── templates/
│   ├── admin/                  # Vistas del administrador
│   ├── cliente/                # Vistas del cliente
│   └── general/                # Vistas compartidas
├── application.properties
├── creaTablas.sql
├── messages.properties
├── messages_es.properties
└── messages_en.properties

├── Dockerfile
├── pom.xml
├── .env.example
└── DESPLIEGUE.md
```

## Requisitos

Antes de ejecutar el proyecto se necesita:

- Java Development Kit 21.
- Maven 3.9 o superior.
- MySQL 8.x o una instancia de MySQL en Aiven.
- Git.
- Docker, opcional para ejecutar el proyecto mediante contenedores.
- Cuenta de Firebase, únicamente si se utilizará el almacenamiento remoto de imágenes.

## Clonar el repositorio

```powershell
git clone https://github.com/SebasMonti/BookGuest_G6.git
cd BookGuest_G6
```

## Configuración de la base de datos

El archivo final para crear la estructura y los datos iniciales se encuentra en:

```text
src/main/resources/creaTablas.sql
```

### Importante

El script elimina y vuelve a crear las tablas del sistema. Debe ejecutarse únicamente sobre una base de datos nueva, vacía o cuyos datos puedan descartarse.

Puede ejecutarse desde MySQL Workbench o mediante la consola:

```powershell
cmd /c "mysql -h localhost -P 3306 -u usuario_bookguest -p bookguest < src\main\resources\creaTablas.sql"
```

El script no crea automáticamente la base de datos ni el usuario de MySQL. Estos deben existir antes de ejecutarlo.

## Variables de entorno

El proyecto utiliza variables de entorno para evitar almacenar credenciales en el repositorio.

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_HOST` | Servidor de MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `bookguest` |
| `DB_USERNAME` | Usuario de MySQL | `usuario_bookguest` |
| `DB_PASSWORD` | Contraseña de MySQL | `contraseña_segura` |
| `DB_SSL_MODE` | Modo SSL de MySQL | `DISABLED` o `REQUIRED` |
| `FIREBASE_BUCKET_NAME` | Bucket de Firebase Storage | `proyecto.appspot.com` |
| `FIREBASE_STORAGE_PATH` | Carpeta utilizada dentro del bucket | `bookguest` |
| `FIREBASE_CREDENTIALS_BASE64` | Credenciales de Firebase codificadas en Base64 | Valor codificado |
| `FIREBASE_CREDENTIALS_PATH` | Ruta alternativa al archivo de credenciales | `firebase-admin.json` |
| `PORT` | Puerto de ejecución | `91` o el asignado por Render |
| `THYMELEAF_CACHE` | Caché de vistas Thymeleaf | `false` local, `true` producción |
| `JPA_SHOW_SQL` | Mostrar consultas SQL | `false` |
| `JPA_FORMAT_SQL` | Formatear consultas SQL | `false` |

Ejemplo de configuración local en PowerShell:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="bookguest"
$env:DB_USERNAME="usuario_bookguest"
$env:DB_PASSWORD="CAMBIAR_CONTRASEÑA"
$env:DB_SSL_MODE="DISABLED"
$env:PORT="91"
$env:THYMELEAF_CACHE="false"
```

No se deben subir al repositorio:

- Contraseñas reales.
- Archivos `.env`.
- Archivos JSON de Firebase.
- Claves privadas.
- Credenciales de Aiven.

## Ejecutar localmente con Maven

Compilar el proyecto:

```powershell
mvn clean package -DskipTests
```

Ejecutar desde Maven:

```powershell
mvn spring-boot:run
```

También puede ejecutarse el archivo JAR generado:

```powershell
java -jar target/bookguest-1.jar
```

La aplicación estará disponible en:

```text
http://localhost:91/login
```

## Verificación de compilación

Para comprobar que el proyecto compila correctamente:

```powershell
mvn -B clean verify
```

El proyecto no incluye una suite de pruebas automatizadas, debido a que fue desarrollado con fines académicos.

## Ejecutar con Docker

Construir la imagen:

```powershell
docker build -t bookguest .
```

Ejecutar el contenedor:

```powershell
docker run --rm -p 10000:10000 --env-file .env bookguest
```

La imagen utiliza una construcción de varias etapas con:

- Maven 3.9.11.
- Eclipse Temurin JDK 21.
- Eclipse Temurin JRE 21.

## Despliegue en Aiven

1. Crear un servicio de MySQL en Aiven.
2. Crear o seleccionar la base de datos del proyecto.
3. Ejecutar `src/main/resources/creaTablas.sql`.
4. Copiar los datos de conexión suministrados por Aiven.
5. Configurar las variables:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
DB_SSL_MODE=REQUIRED
```

## Despliegue en Render

1. Subir el proyecto a GitHub.
2. Crear un nuevo Web Service en Render.
3. Conectar el repositorio de BookGuest.
4. Seleccionar Docker como entorno de ejecución.
5. Configurar las variables de entorno de MySQL, Firebase y Thymeleaf.
6. Configurar `/login` como ruta de comprobación de estado.
7. Realizar el despliegue.

Render asignará automáticamente la variable `PORT`.

Para producción se recomienda:

```text
THYMELEAF_CACHE=true
DB_SSL_MODE=REQUIRED
JPA_SHOW_SQL=false
JPA_FORMAT_SQL=false
```

Puede consultarse información adicional en el archivo:

```text
DESPLIEGUE.md
```

## Configuración de Firebase

Firebase Storage se utiliza para almacenar imágenes de usuarios y productos.

Las credenciales pueden proporcionarse mediante:

- `FIREBASE_CREDENTIALS_BASE64`, recomendado para Render.
- `FIREBASE_CREDENTIALS_PATH`, útil durante el desarrollo local.

Para convertir las credenciales JSON a Base64 desde PowerShell:

```powershell
[Convert]::ToBase64String(
    [IO.File]::ReadAllBytes("firebase-admin.json")
)
```

El archivo JSON original no debe subirse a GitHub.

## Internacionalización

La aplicación se encuentra disponible en:

- Español de Costa Rica, idioma predeterminado.
- Inglés.

Los textos traducidos se administran mediante:

```text
messages.properties
messages_es.properties
messages_en.properties
```

La preferencia del idioma se mantiene durante la sesión del usuario.

## Roles del sistema

### Cliente

```text
ROLE_CLIENTE
```

Permite acceder a la tienda, favoritos, carrito, pedidos y configuración de la cuenta.

### Administrador

```text
ROLE_ADMIN
```

Permite acceder al dashboard y a los módulos administrativos.

## Cuentas de demostración

Las siguientes cuentas son únicamente para fines académicos y demostrativos:

### Administrador

```text
Correo: admin@bookguest.com
Contraseña: Admin123.
```

### Cliente

```text
Correo: cliente@bookguest.com
Contraseña: Cliente123.
```

Estas contraseñas deben cambiarse si el proyecto se publica en un entorno accesible al público.

## Seguridad

El proyecto implementa:

- Autenticación mediante Spring Security.
- Autorización basada en roles.
- Contraseñas almacenadas con BCrypt.
- Validación de formularios.
- Sesiones de usuario.
- Rutas separadas para clientes y administradores.
- Credenciales externas mediante variables de entorno.
- Conexión SSL con MySQL en producción.

## Flujo de ramas

El flujo de trabajo recomendado utiliza:

```text
main
└── develop
    ├── feature/backend-database
    ├── feature/admin-modules
    ├── feature/client-store
    └── feature/deployment-documentation
```

Cada integrante desarrolla en su propia rama y envía sus cambios hacia `develop`. Una vez validada la integración, se realiza un Pull Request de `develop` hacia `main`.

## Integrantes

- Sebastián Montiel
- María Fernanda Hernández Moya
- Danny Antonio Fonseca García
- Nahum Esteban Ramírez Fuentes

## Repositorio

[BookGuest_G6](https://github.com/SebasMonti/BookGuest_G6)

## Uso académico

BookGuest fue desarrollado como proyecto universitario para aplicar conocimientos de programación web, bases de datos, seguridad, arquitectura MVC, despliegue en la nube y trabajo colaborativo con Git.

El proyecto no representa una tienda comercial real y no procesa transacciones bancarias reales.

---

Desarrollado por el Grupo 6 — BookGuest.
