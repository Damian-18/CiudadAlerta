# Guía de Estudio Exahustiva: Ciudad Alerta

Esta guía técnica está diseñada para auditar y estudiar el código fuente **real y actual** del proyecto `Ciudad Alerta`. Su propósito es prepararte para defender y explicar cada línea de código, configuración y decisión de arquitectura de la versión en producción, conectada a Supabase.

---

## 1. Mapa Completo del Proyecto (Carpetas y Archivos)

El proyecto sigue una estructura limpia, separando las capas de Dominio, Datos, Inyección de Dependencias y UI (Presentación). A continuación, el árbol real de los archivos:

```text
app/src/main/
├── AndroidManifest.xml (Configuración principal, define CiudadAlertaApplication y permisos de INTERNET)
├── java/com/example/proyectofinal/
│   ├── CiudadAlertaApplication.kt (Clase Application, instancia el AppContainer al arrancar)
│   ├── MainActivity.kt (Actividad única, calcula el WindowSizeClass e infla Jetpack Compose)
│   ├── di/
│   │   ├── AppContainer.kt (Contenedor manual de dependencias: Room, Retrofit, SharedPreferences, Repositorios)
│   │   └── AppViewModelProvider.kt (Factory que provee los ViewModels inyectándoles los repositorios)
│   ├── model/
│   │   └── DomainModels.kt (Enums de Roles, Departamentos, KPIs, UserSession y validaciones estáticas)
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt (Base de datos Room SQLite 'ciudad_alerta_db' v1)
│   │   │   ├── entity/
│   │   │   │   ├── DraftEntity.kt (Tabla 'borradores', caché local del ciudadano)
│   │   │   │   ├── MultimediaEntity.kt (Tabla 'multimedia', rutas de fotos locales o remotas)
│   │   │   │   ├── NotificationEntity.kt (Tabla 'notificaciones', historial de estado)
│   │   │   │   └── ReportEntity.kt (Tabla 'reportes', caché offline de los incidentes de Supabase)
│   │   │   └── dao/
│   │   │       ├── DraftDao.kt (CRUD para borradores locales)
│   │   │       ├── MultimediaDao.kt (Consultas reactivas de fotos por reporte)
│   │   │       ├── NotificationDao.kt (Gestión de notificaciones leídas/no leídas)
│   │   │       └── ReportDao.kt (Sincronización y consultas reactivas con Flow para la UI)
│   │   ├── remote/
│   │   │   ├── CiudadAlertaApiService.kt (Interfaz Retrofit con los endpoints de Auth, Storage y PostgREST)
│   │   │   ├── SupabaseAuthInterceptor.kt (Interceptor de OkHttp para inyectar apikey y token Bearer)
│   │   │   └── dto/
│   │   │       ├── AuthDto.kt (Data classes de login, registro y perfiles para GoTrue)
│   │   │       └── NetworkDtos.kt (Data classes con @SerializedName snake_case y mapeo toEntity/toDto)
│   │   └── repository/
│   │       ├── AuthRepository.kt (Gestiona login/registro en Supabase, guarda sesión en SharedPreferences)
│   │       ├── DraftRepository.kt (Expone Flow y métodos de Room para operar borradores)
│   │       ├── MultimediaRepository.kt (Expone Flow para cargar imágenes del reporte)
│   │       ├── NetworkResult.kt (Helper `resultOf` y extensión `requireBody` para respuestas Retrofit)
│   │       ├── NotificationRepository.kt (Manejo de bandeja de notificaciones)
│   │       └── ReportRepository.kt (Orquesta la sincronización remota: sube fotos a Storage, envía/actualiza en Postgres y persiste en Room)
│   └── ui/
│       ├── AppNavigation.kt (NavHost principal, maneja rutas condicionales por rol y menú adaptativo)
│       ├── Components.kt (Componentes UI reutilizables: Page, AdaptiveLayout, PhotoField con PickVisualMedia)
│       ├── auth/
│       │   ├── AuthViewModel.kt (StateFlow con `session` y llamadas a AuthRepository)
│       │   ├── LoginScreen.kt (Formulario de inicio de sesión)
│       │   ├── RecoveryScreen.kt (Recuperación de contraseña con Supabase)
│       │   └── RegisterScreen.kt (Formulario de registro ciudadano)
│       ├── reportes/
│       │   ├── ReportesViewModel.kt (Orquesta estado de reportes, borradores y notificaciones)
│       │   ├── BorradoresScreen.kt (Listado de borradores no subidos)
│       │   ├── CapturaIncidenciaScreen.kt (Formulario para crear reporte / guardar borrador)
│       │   ├── DetalleReporteScreen.kt (Expediente completo con foto inicial y de resolución)
│       │   ├── ListadoReportesScreen.kt (Dashboard ciudadano con toggle "Mis reportes / Todos")
│       │   └── NotificacionesScreen.kt (Bandeja de avisos de estado)
│       ├── admin/
│       │   ├── AdminViewModel.kt (Calcula KPIs y orquesta el cambio de estado de reportes)
│       │   ├── AdminBandejaScreen.kt (Listado filtrable de todos los incidentes)
│       │   ├── AdminDashboardScreen.kt (Tarjetas de KPIs por departamento)
│       │   └── AdminResolverScreen.kt (Cambio de estado con observacion_admin y evidencia obligatoria)
│       └── theme/ (Color, Theme y Type para Material Design 3)
└── res/
    ├── values/strings.xml (Textos base en Español)
    └── values-en/strings.xml (Textos traducidos al Inglés para i18n automático)
```

---

## 2. Configuración del Proyecto y Dependencias

### 2.1 Dependencias Clave (en `app/build.gradle.kts` y `libs.versions.toml`)
*   **Jetpack Compose & Navigation:** (`navigation-compose`, `activity-compose`, `lifecycle-viewmodel-compose`). Permiten UI declarativa y navegación sin Fragmentos.
*   **WindowSizeClass:** (`material3-window-size-class`). API para detectar si la pantalla es de teléfono (Compact) o tablet (Medium/Expanded) para el `AdaptiveLayout`.
*   **Room:** (`room-runtime`, `room-ktx`, `room-compiler` vía KAPT). Base de datos local (SQLite).
*   **Red (Supabase REST):** Retrofit2 (`retrofit`, `converter-gson`), OkHttp3 (`okhttp`, `logging-interceptor`). Sustituyen cualquier SDK monolítico.
*   **Coil:** (`coil-compose`). Usado en el componente `AsyncImage` para descargar e inflar las fotografías de evidencia desde URIs locales o URLs HTTP públicas de Supabase Storage de manera transparente y en hilos secundarios.

### 2.2 Seguridad y Endpoints (`local.properties` a `BuildConfig`)
1.  **El problema:** Subir credenciales de Base de Datos al repositorio en GitHub es un fallo grave de seguridad.
2.  **La solución:** Las credenciales reales residen exclusivamente en la computadora del desarrollador dentro del archivo `local.properties` (que es ignorado por `.gitignore`):
    ```properties
    SUPABASE_URL=https://<PROYECTO>.supabase.co
    SUPABASE_ANON_KEY=sb_publishable_...
    ```
3.  **La inyección en compilación:** El bloque `defaultConfig` en `app/build.gradle.kts` usa la clase `java.util.Properties` para leer ese archivo y generar constantes seguras en código a través de `buildConfigField`. Esto expone `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_ANON_KEY` en Kotlin sin exponerlas en GitHub.

---

## 3. Arquitectura e Inyección de Dependencias Manual

El proyecto prescinde de librerías como Hilt o Dagger, utilizando Inyección de Dependencias de tipo **"Vanilla" (Manual)** para maximizar el control y reducir los tiempos de compilación.

**El Flujo Exacto:**
1.  **Arranque:** El OS lee el `AndroidManifest.xml` que define a `CiudadAlertaApplication`.
2.  **Application:** `CiudadAlertaApplication.kt` sobrescribe el `onCreate()` instanciando el `AppContainer`.
3.  **Contenedor Central:** En `AppContainer.kt`, las dependencias se construyen "bajo demanda" (con `by lazy`):
    *   Genera `AppDatabase.getDatabase(context)` (Room).
    *   Genera `SharedPreferences` para "auth_prefs".
    *   Genera `SupabaseAuthInterceptor` y lo adjunta a un `OkHttpClient`.
    *   Genera `Retrofit.Builder` apuntando a `BuildConfig.SUPABASE_URL` y construye la `CiudadAlertaApiService`.
    *   Finalmente, construye `ReportRepository`, `AuthRepository`, etc., pasándoles las instancias anteriores.
4.  **UI:** En `MainActivity.kt`, se llama a `AppNavigation(AppViewModelProvider.Factory)`.
5.  **ViewModel Factory:** En `AppViewModelProvider.kt`, los métodos `initializer` inyectan los repositorios del `AppContainer` directo a los constructores de `AuthViewModel`, `ReportesViewModel` y `AdminViewModel`.

---

## 4. Capa de Dominio y Reglas de Negocio

El archivo `model/DomainModels.kt` centraliza la lógica pura que no depende de la UI ni de Room:

*   **Roles y Departamentos:** `UserRole` (`CIUDADANO`, `ADMIN`). `Department` mapea el código interno (ej. `"BACHES"`) a etiquetas legibles ("Baches", "Departamento de Obras Públicas").
*   **ReportStatus:** Restringe la máquina de estados. La función `canTransition(from, to)` dicta que solo se permite flujo lineal: `PENDIENTE -> EN_PROCESO` y `EN_PROCESO -> RESUELTO`.
*   **ReportValidator (El Árbitro):** Contiene lógica estática que dicta si un dato es válido o retorna el texto del error. Reglas reales aplicadas en el código:
    *   `descripcion`: Mínimo 20 y Máximo 1000 caracteres.
    *   `ubicacion`: Mínimo 10 y Máximo 200 caracteres.
    *   `observacionAdmin`: Obligatoria para cambiar el estado, de 5 a 500 caracteres.

---

## 5. Capa de Datos Local (SQLite con Room)

Room opera como la caché offline (SSOT local). Definido en `AppDatabase.kt`.

| Tabla (`@Entity`) | Clave Primaria | Columnas / Propósito en el proyecto |
| :--- | :--- | :--- |
| `reportes` | `folio` (String) | Es el clon local de Postgres. Contiene `autorEmail`, `departamento`, `estado`, URLs de evidencia, observaciones, etc. Permite que la app liste reportes sin esperar a la red. |
| `borradores` | `id` (Auto Int) | Información temporal del `CIUDADANO` que **no** sube a Supabase. Se destruye con la función `clearUserDrafts(email)` en el logout. |
| `multimedia` | `id` (Auto Int) | Vincula con `reporteFolio`. Registra si la URL apunta a una evidencia `EVIDENCIA_INICIAL` o de un admin (`EVIDENCIA_RESUELTO`). |
| `notificaciones`| `id` (Auto Int) | Actúa como historial de estados. Guarda `titulo`, `mensaje` y si está `leida`. Se vincula al reporte. |

*Nota sobre Room en el proyecto:* Los DAOs retornan `Flow<List<T>>` para listas reactivas (la UI se repinta sola si la DB cambia) y `suspend` para operaciones directas (insert, update, delete).

---

## 6. Capa de Datos Remota (Retrofit2 + OkHttp3 + Supabase)

Todo el backend se consume directamente con HTTP. No existen SDKs propietarios.

### Autenticación en Red (`SupabaseAuthInterceptor.kt`)
Clase clave. Intercepta todas las peticiones salientes e inyecta:
*   `apikey: BuildConfig.SUPABASE_ANON_KEY` (Obligatorio en Supabase).
*   `Authorization: Bearer <token>` (Usa el accessToken del usuario logueado. Si no hay, inyecta la ANON_KEY).
*   `Prefer: return=representation` (Al hacer `POST/PATCH` a la BD, PostgREST retorna la fila insertada en JSON en lugar de vacío. Solo se inyecta en `/rest/v1/`).

### Los Endpoints Reales (`CiudadAlertaApiService.kt`)
| Método y Verbo | Ruta (`@POST`, `@GET`, etc) | Qué hace en la Nube Supabase |
| :--- | :--- | :--- |
| `login()` | `POST auth/v1/token?grant_type=password` | Genera JWT vía GoTrue. Retorna access y refresh tokens. |
| `register()` | `POST auth/v1/signup` | Registra email/password en Supabase Auth y dispara un trigger en PostgreSQL que creará el perfil. |
| `obtenerPerfil()` | `GET rest/v1/perfiles?id=eq.{id}` | PostgREST: Obtiene el `rol` real y `nombre` del usuario en Postgres, dado su UID. |
| `listarReportes()`| `GET rest/v1/reportes?order=fecha_creacion.desc` | PostgREST: Descarga todos los reportes (en caché se hidratará Room). |
| `agregarReporte()`| `POST rest/v1/reportes` | PostgREST: Inserta una fila en la tabla de reportes pública. |
| `actualizarEstado()`| `PATCH rest/v1/reportes?folio=eq.{folio}`| PostgREST: Actualiza columnas (ej. `estado`, `observacion_admin`) y retorna la fila actualizada. |
| `agregarHistorial()`| `POST rest/v1/historial_reportes` | PostgREST: Inserta rastro de auditoría con `estado_anterior`, `estado_nuevo` y `actor_email`. |
| `subirEvidencia()`| `POST storage/v1/object/evidencias/{rutaArchivo}` | Storage API: Recibe bytes RAW (`RequestBody`) en Content-Type `image/jpeg` y retorna metadatos. |

### DTOs y Mapeo (*snake_case* a *camelCase*)
En `NetworkDtos.kt` y `AuthDto.kt`, se usan los tags `@SerializedName("columna_real_postgres")`. 
*   **Null-Safety Patcheado:** En el código actual, campos de la BD que pueden ser nulos (como `foto_inicial_url` en `ReportDto`, o `nombre` en `PerfilDto`) se anotan explícitamente como `String?` en Kotlin. Si Supabase omite un campo o devuelve null, Kotlin no arrojará excepción por variables nulas. 
*   Para pasar a entidad local, existen los convertidores `.toEntity()` que complementan atributos que en PostgREST pueden no existir.

---

## 7. Capa de Repositorios (El Puente Room-Supabase)

El **Patrón Repository** oculta a la UI de dónde provienen los datos. El mejor ejemplo es `ReportRepository.kt`:

1.  **Sincronización Transaccional (`syncWithRemote`):** Descarga el JSON de `listarReportes()`. Abre un `database.withTransaction`. Compara folios de Postgres contra los folios de Room. Si un folio de Room ya no está en Postgres, lo borra en cascada (junto con su multimedia y notificación locales). Si existe, lo inserta o actualiza (`persist`).
2.  **Operaciones Duales (`createReport`, `updateReportStatusByAdmin`):** 
    *   Valida reglas de dominio (`ReportValidator`).
    *   Llama al `uploadImageIfNeeded(...)` (Si hay foto, genera un Body y sube al Storage).
    *   Hace el `POST/PATCH` a Retrofit (`apiService`).
    *   Con la respuesta 200 OK del backend, guarda los datos en Room y genera la notificación local. Todo bajo un `mutex.withLock` para evitar choques en hilos.

---

## 8. Capa de Presentación: ViewModels, StateFlow y Corrutinas

La arquitectura sigue el patrón **MVI / MVVM Unidireccional** usando `StateFlow`.
Cada pantalla observa una data class de Estado y el ViewModel expone métodos (Eventos) para mutarla.

*   `AuthViewModel`: Expone `AuthUiState`. Ejecuta corrutinas (`viewModelScope.launch`) llamando a `AuthRepository.login(...)`. Actualiza el estado con el `UserSession`.
*   `ReportesViewModel`: Escucha activamente a Room. Su función `bind()` hace un `combine` de tres Flujos de base de datos (`reports`, `drafts`, `notifications`). Tiene la propiedad computada `filteredReports` que toma los datos crudos y aplica el filtro si seleccionaste un departamento o si diste clic en "Mis Reportes". Llama a `syncRemoteReports()`.
*   `AdminViewModel`: Su clase `AdminUiState` contiene lógica auto-calculada muy potente (`pendientes`, `enProceso`, `resueltos` y la lista de `DepartmentKpi`). Cuando el administrador da clic en resolver, el VM invoca la función asíncrona del repo para intentar el `PATCH` remoto.

---

## 9. Pantallas Compose, UX Adaptativa e Idiomas (i18n)

### 9.1 Adaptabilidad (`WindowSizeClass` y `AdaptiveLayout`)
*   En `MainActivity.kt`, se calcula el tamaño del dispositivo del usuario: `calculateWindowSizeClass(this)`.
*   Esa variable viaja al `AppNavigation`, que delega al componente envoltorio `AdaptiveLayout` (en `Components.kt`).
*   **Regla de interfaz:** Si el ancho es `WindowWidthSizeClass.Compact` (teléfono), pinta un `NavigationBar` en la zona inferior de la pantalla. Si es tablet/desktop (cualquier otra cosa), pinta un `NavigationRail` del lado izquierdo y encierra el contenido en un `Row`.

### 9.2 Soporte Multi-idioma (i18n)
La interfaz carece por completo de textos duros ("hardcodeados"). 
En su lugar, todas las pantallas usan `stringResource(R.string.mi_clave)`.
*   Directorio Base: `app/src/main/res/values/strings.xml` (Textos en Español por omisión).
*   Directorio Inglés: `app/src/main/res/values-en/strings.xml`. (Android inyectará estos textos instantáneamente si el teléfono del usuario cambia su idioma global a Inglés).

### 9.3 Tabla de Navegación (`AppNavigation.kt`)
| Ruta / Argumentos | UI Screen Asociada | Reglas / Restricciones |
| :--- | :--- | :--- |
| `login`, `register`, `recovery` | `LoginScreen`, `RegisterScreen`, etc. | Público. Si hay sesión se salta esta ruta mediante `startDestination`. |
| `listado_reportes` | `ListadoReportesScreen` | Menú principal del CIUDADANO. Filtros, botón flotante de agregar. |
| `captura_incidencia?folio={folio}&draftId={id}`| `CapturaIncidenciaScreen` | Formulario reutilizable (Ciudadano). Si recibe folio, edita el remoto. Si recibe id, retoma un borrador local. |
| `detalle_reporte/{folio}` | `DetalleReporteScreen` | Compartido por ambos roles. Muestra toda la data y fotos del expediente. |
| `admin_dashboard` | `AdminDashboardScreen` | Menú del ADMIN. Visualiza los KPIs calculados. |
| `admin_bandeja` | `AdminBandejaScreen` | Bandeja con todos los incidentes para el ADMIN. |
| `admin_resolver/{folio}` | `AdminResolverScreen` | ADMIN: Pide seleccionar estado `RESUELTO`, redactar observación y foto. |

---

## 10. Recorrido Paso a Paso de 5 Flujos Principales (Traza de Código)

**1. Inicio de Sesión y Perfil (Login)**
`LoginScreen` (usuario presiona Login) -> `AuthViewModel.login(email, pass)` -> Lanza corrutina -> `AuthRepository.login()` -> Retrofit POST a `auth/v1/token` -> Obtiene `access_token` y se lo da a `SupabaseAuthInterceptor` -> Retrofit GET a `rest/v1/perfiles?id=eq...` -> Lee el rol -> Guarda en `SharedPreferences` y retorna `UserSession` -> VM emite el StateFlow actualizado -> UI reacciona y `AppNavigation` salta al Dashboard según su rol.

**2. Guardar un Borrador (Caché Offline)**
`CapturaIncidenciaScreen` (llena datos, no envía, guarda borrador) -> `ReportesViewModel.guardarBorrador()` -> `DraftRepository.saveDraft(DraftEntity(...))` -> Room `draftDao.insertDraft()` -> La base SQLite se actualiza -> El `Flow` general en el ViewModel emite de nuevo -> UI muestra (`Borradores (1)`).

**3. Enviar Incidente con Fotografía Real a Supabase**
`CapturaIncidenciaScreen` (selecciona foto en URI de caché local, da "Enviar") -> `ReportesViewModel.crearOEditarReporte()` -> `ReportRepository.createReport()` -> Valida reglas (20 a 1000 char) -> Entra a función privada `uploadImageIfNeeded(localUri)`: lee bytes del disco, POST Retrofit a `storage/v1/object/evidencias/UUID.jpg`. Si triunfa, retorna URL HTTP pública. -> Inserta ReportDto con la URL HTTP al POST `rest/v1/reportes` -> Response 201 de Supabase con `representation` -> Traduce JSON a Entity y hace Insert en SQLite Room y Notificación local -> UI finaliza y regresa al listado.

**4. Actualización del Dashboard (Listado/Sincronización)**
App inicia `ReportesViewModel` -> Hace `combine` a flujos locales de Room -> Emite lista (UI se pinta instantánea por la caché) -> Corrutina llama `syncRemoteReports()` -> `ReportRepository.syncWithRemote()` -> Retrofit GET `rest/v1/reportes` descarga JSON Array de Supabase -> Abre `withTransaction` en SQLite -> Elimina folios que ya no existen, inserta/actualiza los vivos -> Los observers de Room re-emiten la data -> La UI se refresca sin fricción.

**5. Administrador Resolviendo un Caso (Evidencia e Historial)**
`AdminResolverScreen` (cambia a RESUELTO, anexa foto final) -> `AdminViewModel.resolverOCambiarEstado()` -> `ReportRepository.updateReportStatusByAdmin()` -> Verifica restricción `evidenciaResueltoUri != null` -> `uploadImageIfNeeded(evidencia)` para la nube -> PATCH a Retrofit `rest/v1/reportes` (actualiza estado/observación/foto) -> Dispara POST a Retrofit `rest/v1/historial_reportes` (traza de estado_anterior a nuevo) -> Response 200 de PostgREST -> Insert/Update en Room local con la Notificación tipo "Estado cambiado" -> UI cierra.

---

## 11. Guía Rápida de Defensa Técnica (Preguntas Frecuentes)

**P1: ¿Por qué inyectaron dependencias manualmente (AppContainer) en lugar de Hilt o Dagger?**
R: Porque es un requerimiento técnico estricto de la rúbrica/arquitectura solicitada para garantizar el entendimiento profundo del ciclo de vida. Dagger/Hilt añaden mucha sobrecarga en tiempo de compilación y código generado. Al instanciar a través del patrón Container en el `Application` (Singleton global) y usar `ViewModelFactory`, mantenemos total transparencia en cómo Retrofit o Room entran al ViewModel.

**P2: ¿Cómo evitan bloquear la interfaz (ANR) si hacen llamadas a base de datos y red?**
R: Todo se ejecuta mediante "Corrutinas" (Kotlin Coroutines) en bloques `suspend` y bajo `viewModelScope.launch`. OkHttp y Retrofit están diseñados para mandar la tarea de I/O a un hilo secundario (Dispatcher.IO). En Room, cualquier consulta marcada con `suspend` o expuesta como `Flow` ya maneja el hilo internamente.

**P3: ¿Qué pasa si falla la red al intentar subir un reporte con foto?**
R: En `ReportRepository`, la subida de foto al Storage tiene verificación (`if(response.isSuccessful)`). Si falla la API, lanzo una `IllegalStateException` explícita que rompe el proceso (deteniendo el flujo) y propaga el error (vía `Result.failure`) hacia el ViewModel, el cual muta el estado de UI copiando la excepción a la variable `message`. La UI dibuja el `Feedback` de error. El registro **no** se guarda falsamente en SQLite ni se envía a PostgreSQL corrupto. 

**P4: El Dashboard del Administrador no tiene base de datos local propia, ¿cómo calcula los indicadores (KPIs)?**
R: En `AdminUiState` (dentro de `AdminViewModel.kt`), la lista cruda de entidades descargadas desde Room/Supabase se somete a programación funcional reactiva. Cada variable (`resueltos`, `porcentajeAtencion`, `departments`) emplea métodos puros como `reports.count { it.estado == "..." }` para auto-computar las métricas de forma barata y determinística antes de entregar el objeto a la UI (Jetpack Compose).

**P5: ¿Cómo garantizan que la foto se dibuje si provino de internet en otro dispositivo?**
R: Se utiliza la librería `Coil` mediante su composable `AsyncImage`. En `ReportRepository`, me aseguro de que el string guardado en Postgres sea la URL pública 100% resuelta hacia el bucket. Al abrir `DetalleReporteScreen`, le paso esa URL a `AsyncImage`, el cual tiene su propio caché en disco y red, dibujándola asíncronamente de forma transparente.

**P6: ¿Cómo lograron la adaptabilidad de la UI a Tablets y Multi-Idioma?**
R: Se evalúa el tamaño físico del hardware con `calculateWindowSizeClass(this)` en el MainActivity. Se la pasamos al componente `AdaptiveLayout`. Si el OS determina un ancho Compact (menor a 600dp), inflamos `NavigationBar`; si es mayor, `NavigationRail`. Todo el sistema de textos invoca dinámicamente `stringResource(R.string.x)`, lo que le dice al OS de Android que vaya y escoja los valores de `res/values-en/` o `res/values/` dependiendo exclusivamente de las configuraciones idiomáticas nativas del usuario.

---
**Documento generado a partir del estado absoluto del código (v2026 Supabase). Listo para revisión.**