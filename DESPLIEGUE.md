# Instalación y despliegue de BookGuest

## 1. Preparar MySQL

El archivo `src/main/resources/creaTablas.sql` es el instalador final de la
base de datos. Debe ejecutarse estando conectado a una base de datos vacía o
a la base de BookGuest que se desea reiniciar.

> Advertencia: el script elimina y vuelve a crear todas las tablas de
> BookGuest. No debe ejecutarse sobre una base cuyos datos se quieran
> conservar.

El script no crea la base de datos ni cuentas de MySQL. Esto permite usar el
mismo archivo tanto en MySQL local como en Aiven, donde la base y el usuario
son administrados por el servicio.

Al terminar quedan disponibles estas cuentas de demostración:

- Administrador: `admin@bookguest.com` / `Admin123.`
- Cliente: `cliente@bookguest.com` / `Cliente123.`

## 2. Variables de conexión

La aplicación utiliza estas variables de entorno:

| Variable | Uso | Valor local predeterminado |
| --- | --- | --- |
| `DB_HOST` | Servidor MySQL | `localhost` |
| `DB_PORT` | Puerto MySQL | `3306` |
| `DB_NAME` | Base de datos | `bookguest` |
| `DB_USERNAME` | Usuario MySQL | `usuario_bookguest` |
| `DB_PASSWORD` | Contraseña MySQL | `BookGuest_2026` |
| `DB_SSL_MODE` | SSL de MySQL | `DISABLED` |
| `PORT` | Puerto HTTP | `91` |

Para Aiven se deben copiar los datos de conexión entregados por el servicio
y establecer `DB_SSL_MODE=REQUIRED`. La aplicación usa `PORT` cuando Render
lo proporciona automáticamente.

La cadena JDBC resultante usa SSL obligatorio, igual que el ejemplo oficial
de Aiven para Java:
https://aiven.io/docs/products/mysql/howto/connect-with-java

## 3. Firebase Storage

Para desarrollo local se puede conservar el archivo JSON únicamente en
`src/main/resources/firebase`. Los JSON de credenciales están excluidos de
Git para evitar su publicación accidental.

En Render se recomienda establecer:

- `FIREBASE_BUCKET_NAME`
- `FIREBASE_STORAGE_PATH` (opcional; el valor predeterminado es `bookguest`)
- `FIREBASE_CREDENTIALS_BASE64`, con el contenido completo del JSON
  codificado en Base64

Como alternativa, `FIREBASE_CREDENTIALS_PATH` puede apuntar a un archivo de
credenciales disponible en el servidor.

## 4. Compilar y ejecutar localmente

```text
mvn clean package -DskipTests
java -jar target/bookguest-1.jar
```

Para producción también se puede definir `THYMELEAF_CACHE=true`.

## 5. Publicar en Render

Render ejecuta esta aplicación mediante el `Dockerfile` incluido en la raíz.
El contenedor compila con Maven y Java 21 y luego ejecuta solamente el JAR con
el runtime de Java 21.

Al crear el servicio se debe seleccionar el runtime Docker y configurar las
variables de MySQL/Aiven y Firebase indicadas anteriormente. No se debe
configurar manualmente `PORT`: Render la proporciona al servicio web y Spring
Boot la toma desde `application.properties`.

La ruta recomendada para el health check es `/login`. Render espera que el
servicio escuche el puerto indicado por `PORT`; la configuración actual de
Spring Boot ya cumple este requisito:
https://render.com/docs/web-services#port-binding

## 6. Revisión de seguridad antes de publicar Git

El historial anterior del repositorio contiene un JSON antiguo de una cuenta
de servicio de Firebase. El archivo ya fue retirado del proyecto y está
ignorado, pero borrarlo del último commit no revoca una clave que ya apareció
en el historial. Antes de hacer público el despliegue se debe:

1. Eliminar o deshabilitar esa clave antigua en Google Cloud/Firebase.
2. Crear una clave nueva exclusivamente para Render.
3. Guardarla solo como `FIREBASE_CREDENTIALS_BASE64` en las variables secretas
   de Render; nunca añadir el JSON nuevo a Git.

Antes del commit final se recomienda ejecutar:

```text
git add -A
git status --short
git diff --cached --check
git commit -m "Preparar BookGuest para despliegue"
git push origin develop
```

El archivo `.env.example` sirve únicamente como guía. Los valores reales deben
permanecer en variables de entorno locales, Aiven y Render.
