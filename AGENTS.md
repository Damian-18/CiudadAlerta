# Instrucciones para agentes de Ciudad Alerta

Estas instrucciones aplican a todo el repositorio. Su propósito es que los colaboradores puedan continuar el trabajo y conservar los cambios de los demás. Las instrucciones explícitas del usuario para la tarea actual tienen prioridad.

## 1. Qué leer antes de trabajar

1. **AGENTS.md**: reglas de colaboración y entrega.
2. **app/Specs_CiudadAlerta.md**: fuente de requisitos, arquitectura, reglas de negocio y contratos. Leerlo completo al incorporarse por primera vez; en tareas posteriores, revisar las secciones afectadas.
3. **SEGUIMIENTO.md**: resumen del estado, decisiones técnicas, validaciones y notas de continuidad. Empezar por el resumen y las entradas recientes.
4. **task.md**: pendientes, identificadores, criterios de cierre y distinción entre código, pruebas y acciones externas.
5. Instrucciones adicionales `AGENTS.md`, si existen dentro de las carpetas que se van a modificar, y el código y las pruebas relacionados con la tarea.

No asumir que un resumen histórico de “completado” significa que ya no hay pendientes. La revisión de task.md se realizó después del cierre inicial de SEGUIMIENTO.md y documenta casos todavía abiertos. Comparar siempre las actualizaciones más recientes con el código actual.

La especificación define qué debe cumplir la aplicación; task.md indica qué falta revisar o resolver; SEGUIMIENTO.md registra qué se hizo y con qué evidencia. Un documento de avance no cambia por sí mismo los requisitos.

## 2. Antes de editar

- Identificar la tarea solicitada y su ID, por ejemplo C1, C2, D1 o V1. No ejecutar automáticamente todos los pendientes.
- Revisar `git status --short --branch`, `git diff`, `git diff --cached` y los commits recientes.
- Distinguir los cambios ya existentes de los propios. Un archivo modificado o sin seguimiento puede pertenecer al usuario o a su compañero: no borrarlo, añadirlo al commit ni reformatearlo por conveniencia.
- Confirmar el comportamiento actual y leer las pruebas relevantes. Los problemas detectados solo por inspección deben distinguirse de los reproducidos.
- Registrar bajo la tarea en task.md el responsable indicado por el usuario, rama, estado, archivos previstos y próximo paso. Si no se conoce el responsable, usar la rama como identificador temporal; no inventar nombres.
- Consultar si hay trabajo en curso sobre los mismos archivos. Si existe solapamiento, continuar con las partes independientes y coordinar la parte compartida antes de editarla.

## 3. Cómo trabajar sin sobrescribir al otro

### Separar el trabajo

- Usar una rama propia por tarea, por ejemplo `fix/C1-importacion-fotos` o `fix/C2-accesos-demo`.
- Cada colaborador debe utilizar su propio clon o worktree. Dos agentes en el mismo checkout comparten archivos e índice aunque tengan tareas distintas.
- No cambiar de rama, hacer checkout, merge, rebase, pull ni stash dentro de un checkout que otro agente esté usando. Preparar un worktree separado cuando sea necesario.
- Antes de sincronizar con la rama base, comprobar el estado del árbol. Consultar la rama base real del repositorio; no asumir que se llama main.
- La asignación escrita en task.md es un acuerdo de coordinación, no un bloqueo automático. Si cada compañero tiene su propia rama, la anotación no será visible al otro hasta compartirla. No asumir que anotar una tarea localmente la reserva.

### Limitar los cambios

- Modificar únicamente los archivos necesarios para la tarea.
- Evitar cambios masivos de formato, renombrados, actualizaciones de dependencias y limpieza de archivos ajenos sin relación con el objetivo.
- Antes de guardar un archivo compartido, volver a leerlo y comprobar si cambió desde la lectura inicial. Aplicar cambios puntuales sobre la versión actual, sin reemplazarlo por una copia antigua.
- No usar `git reset --hard`, `git clean`, restauraciones que descarten trabajo, borrados de ramas ajenas ni push forzado para resolver diferencias.
- No descartar cambios ajenos mediante `git restore`, `git checkout --`, stash o sobrescritura de archivos.
- Al resolver conflictos, conservar la intención de ambas tareas y revisar cada bloque. No aceptar todo “ours” o todo “theirs” automáticamente. Si hay decisiones incompatibles, pedir una decisión concreta al usuario.
- Realizar commits, push y merges solo dentro de lo solicitado por el usuario. Cuando se solicite un commit, revisar y añadir archivos concretos; evitar `git add .` si puede incluir trabajo ajeno.
- Tras integrar cambios de otra rama, repetir las comprobaciones afectadas. Una prueba aprobada antes de la integración no acredita el resultado combinado.

## 4. Reglas técnicas del proyecto

- Paquete principal: `com.example.proyectofinal`.
- Mantener la inyección manual: Application y AppContainer proporcionan dependencias; MainActivity presenta AppNavigation; AppViewModelProvider.Factory crea los ViewModels; los repositorios acceden a Room y Retrofit.
- No agregar Hilt, Dagger, Koin, Firebase ni servicios de pago.
- Mantener Retrofit, Gson y OkHttp con MockNetworkInterceptor autónomo. No sustituir el mock por un servidor real salvo que el usuario cambie explícitamente el alcance.
- Conservar las cuatro entidades y sus DAO de Room: reportes, borradores, multimedia y notificaciones.
- Usar operaciones suspend, Flow y StateFlow según los contratos; no bloquear el hilo principal.
- Respetar las validaciones de descripción, ubicación y observación, y la transición PENDIENTE → EN_PROCESO → RESUELTO. Exigir evidencia al resolver.
- No dejar implementaciones incompletas, TODO como sustituto de una función, código truncado ni éxitos simulados ante errores.
- No cambiar reglas, firmas, tablas o dependencias sin evaluar sus consumidores. Si se modifica el esquema de Room con datos existentes, atender la migración.
- Consultar las decisiones de compatibilidad en SEGUIMIENTO.md antes de cambiar Gradle, Kotlin, Room o SDK. No revertir ajustes que ya resolvieron un error sin comprobar la alternativa.
- No incluir credenciales personales, claves de firma, rutas locales nuevas ni archivos generados de compilación en los commits.

## 5. Cómo actualizar task.md

Mantener los IDs existentes para que las referencias sigan siendo útiles. Actualizar solo la tarea trabajada; no marcar tareas de otro colaborador ni reescribir todo el documento.

Usar los estados: **pendiente**, **en curso**, **bloqueado**, **implementado pendiente de validar** y **completado**. “Bloqueado” debe explicar la causa y el dato o acción necesarios para continuar.

Bajo el ID correspondiente, mantener una nota breve como esta:

```text
Responsable:
Rama:
Estado:
Última actualización:
Archivos afectados:
Resultado o evidencia:
Validación pendiente:
Siguiente paso:
```

- Marcar una casilla como completada únicamente cuando se cumpla su criterio de cierre y se registre la evidencia.
- Si el código está escrito pero faltan pruebas, indicar “implementado pendiente de validar”.
- Para tareas de prueba, anotar dispositivo o emulador, API, pasos y resultado observado.
- Si aparece un problema adicional, agregarlo con un ID nuevo y evidencia, sin convertir mejoras opcionales en requisitos obligatorios.
- Separar una limitación externa de un fallo de código. Indicar qué se necesita, sin contratar servicios ni ampliar el alcance por cuenta propia.
- Antes de guardar, releer task.md para conservar actualizaciones de otros colaboradores.

## 6. Cómo actualizar SEGUIMIENTO.md

Actualizarlo al cerrar una tarea, antes de entregar trabajo incompleto o cuando aparezca un bloqueo relevante.

1. Mantener un resumen inicial breve y vigente: estado real, tareas abiertas y siguiente paso. Corregir afirmaciones generales de cierre si hay pendientes.
2. Añadir una entrada por tarea, sin eliminar el historial de otros colaboradores.
3. Registrar cambios concretos, motivo, archivos, comandos ejecutados y resultados.
4. Incluir limitaciones y verificaciones no realizadas. No presentar pruebas históricas como si se hubieran repetido.
5. Dejar una instrucción de continuidad que permita retomar el trabajo sin leer todo el archivo.

Formato sugerido:

```text
Fecha y tarea:
Responsable y rama:
Qué cambió y por qué:
Archivos:
Verificación ejecutada y resultado:
Pendientes o bloqueos:
Próximo paso:
Commit o PR: si existe
```

Los documentos de coordinación también pueden tener conflictos de Git. Integrar las entradas de ambos colaboradores y reconciliar los estados; no sustituir el documento entero por una versión de una sola rama.

## 7. Verificación

Ejecutar las comprobaciones adecuadas al cambio. No es necesario compilar la app por una modificación exclusivamente documental.

Desde la raíz, en PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Con un dispositivo o emulador conectado, cuando el cambio afecte Room, navegación o interacción Android:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

En macOS/Linux usar `./gradlew` con las mismas tareas. Usar el JDK y SDK disponibles en cada equipo; no copiar rutas personales a archivos compartidos.

- Revisar el resultado y el código de salida, no solo que el comando haya comenzado.
- Añadir pruebas de regresión cuando se corrija comportamiento que las pruebas actuales no cubren.
- Una compilación correcta no sustituye una prueba funcional.
- Si no se puede ejecutar una comprobación, indicar el motivo y dejarla pendiente.
- Distinguir imágenes simuladas de fotografías reales, emulador de dispositivo físico y pruebas parciales de cobertura completa.
- Tras cambiar nuevamente código ya probado, repetir las verificaciones afectadas.

## 8. Entrega o relevo

Antes de entregar:

- Revisar `git diff --check`, el diff final y `git status --short`.
- Confirmar que los cambios corresponden a la tarea y no incluyen archivos ajenos.
- Actualizar task.md y SEGUIMIENTO.md con el estado real.
- Informar al usuario qué cambió, qué se verificó y qué falta.
- Si queda trabajo sin terminar, dejar archivos afectados, estado actual y siguiente acción concreta. No marcarlo como completado.
- No afirmar que algo está compartido, publicado o integrado si solo existe en la rama local.
