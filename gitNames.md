Buenas Prácticas para Nombrar Branches, Commits y Pull Requests

Las buenas prácticas para nombrar branches, commits y Pull Requests (PR) en Git se basan en tres principios fundamentales:

Consistencia: utilizar una convención común en todo el equipo.

Claridad: que el nombre permita entender rápidamente el propósito del cambio.

Trazabilidad: vincular los cambios con su ticket correspondiente en Jira, GitHub Issues u otra herramienta de gestión.

🌿 Branches

El nombre de una branch debe indicar claramente su propósito y, cuando corresponda, estar vinculado a un ticket de gestión de proyectos.

Estructura recomendada
tipo/<identificador>-<descripcion-corta>


Por ejemplo:

feature/TSDS-5-cambiar-contrasena


Importante: el identificador del ticket debe tener un lugar destacado en el nombre de la branch para facilitar su trazabilidad e integración con herramientas como Jira.

Prefijos comunes
Prefijo	Uso
feature/	Nueva funcionalidad
bugfix/	Corrección de errores
fix/	Corrección de errores
hotfix/	Corrección crítica en producción
release/	Preparación de una nueva versión
refactor/	Reestructuración del código sin modificar su funcionalidad
docs/	Cambios exclusivamente relacionados con documentación
Reglas recomendadas

Utilizar minúsculas.

Utilizar nombres en singular cuando corresponda.

Incluir siempre el identificador del ticket.

Utilizar / para separar el tipo de branch del identificador.

Utilizar - para separar las palabras de la descripción.

Evitar nombres genéricos como test, update, changes o new-feature.

Mantener el nombre lo más corto posible, idealmente entre 30 y 50 caracteres.

La descripción debe ser breve pero suficientemente clara.

✅ Ejemplos
feature/TSDS-123-add-login
bugfix/TSDS-145-fix-user-validation
refactor/TSDS-167-update-auth-service
docs/TSDS-180-update-api-docs
hotfix/TSDS-192-fix-payment-error
release/1.2.0

📝 Commits

Los mensajes de commit deben ser concisos, claros y descriptivos. Deben permitir entender qué cambio se realizó y, cuando sea necesario, por qué se realizó.

Estructura recomendada

Se recomienda utilizar una convención basada en Conventional Commits:

tipo(scope): descripción


Cuando el commit está relacionado con un ticket:

tipo(TSDS-ID): descripción


Por ejemplo:

feat(TSDS-22): implementar login de usuario

Tipos de commit
Tipo	Uso
feat	Nueva funcionalidad
fix	Corrección de un error
docs	Cambios en documentación
style	Cambios de formato sin modificar la lógica
refactor	Reestructuración del código sin cambiar su comportamiento
test	Incorporación o modificación de pruebas
chore	Tareas de mantenimiento
Reglas recomendadas

Utilizar el modo imperativo.

Mantener la primera línea breve, idealmente entre 50 y 72 caracteres.

Describir claramente el cambio realizado.

Incluir el identificador del ticket cuando corresponda.

Evitar mensajes genéricos como:

update

fix

changes

final

test

Si el cambio requiere contexto adicional, utilizar un cuerpo de commit.

Commit simple
feat(TSDS-22): implementar login de usuario

Commit con cuerpo
feat(TSDS-24): implementar algoritmo de predicciones

Implementa la lógica para sugerir predicciones en base al
historial de resultados del usuario.


El cuerpo debe estar separado del asunto mediante una línea en blanco.

🔀 Pull Requests

El título de un Pull Request debe permitir identificar rápidamente qué cambio contiene y, preferentemente, a qué ticket está asociado.

Estructura recomendada

Se pueden utilizar formatos como:

<TSDS-ID>: descripción corta


o:

[Tipo] Descripción corta


Por ejemplo:

TSDS-24: implementar sistema de puntuación para el Prode


o:

[Feature] Implementar sistema de puntuación

Descripción del PR

La descripción debe proporcionar el contexto necesario para que otra persona pueda revisar y probar el cambio.

Se recomienda incluir:

¿Qué se hizo?

¿Por qué se hizo?

¿Cómo se puede probar?

¿Qué ticket está relacionado?

Información adicional relevante para el reviewer.

Plantilla recomendada
## Descripción

<!-- Explicar brevemente qué cambios se realizaron y por qué. -->

## Cambios realizados

- Cambio 1
- Cambio 2
- Cambio 3

## ¿Cómo probarlo?

1. Ejecutar ...
2. Acceder a ...
3. Verificar que ...

## Ticket relacionado

Closes #45

Ejemplo

Título:

TSDS-24: implementar sistema de puntuación para el Prode


Descripción:

## Descripción

Este PR implementa la lógica de negocio definida en el ticket TSDS-24.

## Cambios realizados

- Se implementó el cálculo de puntuaciones.
- Se agregaron tests unitarios.
- Se cubrieron los casos de empate.

## ¿Cómo probarlo?

1. Ejecutar la suite de tests.
2. Crear una predicción.
3. Verificar el cálculo de la puntuación.

## Ticket relacionado

Closes #45

📌 Resumen de convenciones
Elemento	Convención	Ejemplo
Branch	tipo/TICKET-descripcion	feature/TSDS-123-add-login
Commit	tipo(TICKET): descripción	feat(TSDS-123): agregar login
PR	TICKET: descripción	TSDS-123: implementar login
Objetivo

Una buena convención de nombres permite que cualquier integrante del equipo pueda identificar rápidamente:

qué se está modificando → por qué se modifica → qué ticket lo originó → dónde revisar el cambio.

La consistencia en estas convenciones facilita además la revisión de código, la trazabilidad de cambios y el mantenimiento del historial del proyecto.
