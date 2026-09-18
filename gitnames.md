# Buenas Prácticas para Nombrar Branches, Commits y PRs en Git: Consistencia y Claridad

Las buenas prácticas para nombrar *branches* y *commits* en Git se centran en la **consistencia**, la **claridad** y la **trazabilidad**, utilizando prefijos estructurados y convenciones descriptivas.

---

## 1. Buenas Prácticas para Nombrar Branches

El nombre de una rama debe indicar su propósito y, a menudo, vincularse con un ticket de gestión de proyectos. La estructura estándar recomienda usar minúsculas y separadores como barras diagonales (`/`) o guiones (`-`).

### Estructura Standard
```text
tipo/<identificador>-<descripcion-corta>
```
* El ID del ticket debe ser el protagonista para que herramientas como Jira o GitHub Issues lo rastreen sin errores.
* **Ejemplo:** `feature/TSDS-5-cambiar-contrasena`

### Prefijos Comunes
* `feature/` o `feat/`: Para nuevas funcionalidades.
* `bugfix/` o `fix/`: Para correcciones de errores.
* `hotfix/`: Para parches críticos en producción.
* `release/`: Para preparar nuevas versiones (ej. `release/1.2.0`).
* `refactor/`: Para reestructuración de código sin cambiar funcionalidad.
* `docs/`: Solo cambios en documentación.

### Reglas Clave
* Usar **singular** y **minúsculas** siempre.
* Incluir el número de ticket (`JIRA`, `GitHub Issue`, etc.) para trazabilidad automática.
* Evitar nombres genéricos como `test` o `update`.
* Mantener el nombre corto (idealmente menos de 30-50 caracteres).
* **Ejemplo correcto:** `feature/JIRA-123-add-login`

---

## 2. Buenas Prácticas para Mensajes de Commits

Los mensajes de commit deben ser **concisos, imperativos y descriptivos**, explicando qué se hizo y por qué, no solo el qué.

### Estructura (Conventional Commits)
```text
tipo(scope): descripción
```
* **Ejemplo con Ticket:** `feat(TSDS-22): implementar login de usuario`

### Tipos de Commit
* `feat`: Nueva funcionalidad.
* `fix`: Corrección de errores.
* `docs`: Cambios en la documentación.
* `style`: Formato, espacios en blanco, punto y coma faltantes, etc. (sin cambios en código de lógica).
* `refactor`: Reestructuración de código que no corrige errores ni añade características.
* `test`: Añadir o corregir pruebas existentes.
* `chore`: Mantenimiento, actualización de dependencias, tareas auxiliares.

### Reglas Clave
* Usar el modo **imperativo** (ej. *"añadir"* o *"implementar"*, no *"añadido"* ni *"añade"*).
* La primera línea debe ser breve (menos de 50-72 caracteres).
* Si es necesario, añadir un cuerpo explicando el motivo del cambio o detalles técnicos.
* Separar la línea del asunto del cuerpo con una línea en blanco.

#### Ejemplo de un buen commit
```text
feat(TSDS-24): implementar algoritmo de predicciones

Implementa la lógica para sugerir predicciones en base a historial.
```

---

## 3. Pull Requests (PR)

El título debe ser claro y el cuerpo debe explicar detalladamente el **qué** y el **por qué**.

### Título
Debe seguir la misma estructura que un commit profesional o usarse entre corchetes:
* `[Tipo] Título corto` (ej. `[Feature] Implementar sistema de puntos`)
* `TSDS-24: Sistema de puntuación para el Prode`

### Estructura de la Descripción (Template)
```markdown
## ¿Qué se hizo?
Se implementó la lógica de negocio para la resolución de puntuación definida en el ticket TSDS-24.

## ¿Cómo se puede probar?
1. Ejecutar los tests unitarios con `npm test`.
2. Probar la API en la ruta `/api/v1/prode/points` enviando un payload de prueba.

## Relación con tickets
Closes #45 / TSDS-24
```

#### Ejemplo completo de PR
> **Título:** `TSDS-24: Sistema de puntuación para el Prode`  
> **Descripción:**  
> Este PR resuelve la lógica de negocio definida en el ticket TSDS-24. Se agregaron tests unitarios para cubrir los casos de empate.