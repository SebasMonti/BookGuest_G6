# BookGuest Online

Sistema de gestión de inventario y e-commerce desarrollado para la PYME "El paraíso de las páginas". 
Permite la navegación del catálogo de libros, gestión automatizada de stock en tiempo real y administración de pedidos.

## Stack Tecnológico
* **Backend:** Java 17+, Spring Boot (Spring Web, Spring Data JPA, Spring Security).
* **Frontend:** Thymeleaf / HTML5, CSS3, Bootstrap 5.
* **Base de Datos:** MySQL.
* **Control de Versiones:** Git / GitHub.

## Integrantes del Equipo (Colaboradores)
* Sebastián Montiel Rojas
* María Fernanda Hernández Moya
* Danny Antonio Fonseca García
* Nahum Esteban Ramírez Fuentes

## Acuerdo Básico de Trabajo por Ramas (Git Workflow)

Para mantener la estabilidad del código e integrar funcionalidades de manera ordenada, el equipo seguirá un modelo basado en Feature Branching:

### Estructura de Ramas
1. **`main`**: Rama de producción. Contiene únicamente código estable, testeado y funcional. **Queda estrictamente prohibido hacer push directo a esta rama.**
2. **`develop`**: Rama principal de integración. Todo el código nuevo se une aquí para pruebas conjuntas antes de pasar a `main`.
3. **`feature/<nombre-funcionalidad>`**: Ramas temporales creadas a partir de `develop` para trabajar en historias de usuario específicas. (Ejemplo: `feature/catalogo-libros`, `feature/login-roles`).
4. **`bugfix/<nombre-error>`**: Ramas temporales para solucionar errores encontrados en `develop`.

### Flujo de Trabajo (Paso a Paso)
1. Antes de iniciar una tarea, actualizar localmente: `git checkout develop` -> `git pull origin develop`.
2. Crear la rama de la funcionalidad: `git checkout -b feature/nombre-tarea`.
3. Realizar commits atómicos y descriptivos: `git commit -m "feat: agrega entidad libro y repositorio JPA"`.
4. Subir la rama al repositorio: `git push origin feature/nombre-tarea`.
5. Crear un **Pull Request (PR)** en GitHub apuntando hacia `develop`.
6. El PR debe ser revisado y aprobado por al menos **un integrante distinto** al autor antes de hacer el merge.
