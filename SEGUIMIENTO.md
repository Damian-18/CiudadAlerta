# Ciudad Alerta — continuidad
Fuente funcional: app/Specs_CiudadAlerta.md (leída completa). Paquete: com.example.proyectofinal.
## Estado rápido
**COMPLETADO Y VERIFICADO — 2026-11-02.** Implementación directa en este proyecto.
No hay fases de implementación pendientes. El siguiente agente puede comenzar por este resumen; la bitácora inferior documenta los checkpoints anteriores, no tareas todavía abiertas.

### Resultado final comprobado
- `assembleDebug`: correcto. APK: `app/build/outputs/apk/debug/app-debug.apk`.
- `testDebugUnitTest`: 5 pruebas aprobadas (4 de negocio/API + prueba base).
- `connectedDebugAndroidTest`: 3 pruebas aprobadas en Pixel_10_Pro, Android API 37 (Compose, repositorios/Room y contexto de app).
- `lintDebug`: 0 errores, 26 advertencias no bloqueantes (versiones nuevas disponibles, target 36 solicitado, recomendación KSP, recursos de colores heredados y sugerencia KTX).
- Sin TODO, NotImplementedError ni dependencias Firebase/Hilt/Dagger/Koin en el código de la app.

### Cómo continuar en menos de un minuto
1. Abrir este proyecto en Android Studio y sincronizar Gradle.
2. Ejecutar MainActivity. Usar Demo Ciudadano o Demo Admin.
3. Antes de modificar contratos consultar la sección pertinente de `app/Specs_CiudadAlerta.md`; no fue modificada.
4. Repetir `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` después de cambios.
5. Con emulador conectado, ejecutar `gradlew.bat :app:connectedDebugAndroidTest`.
6. Si JAVA_HOME no está configurado: usar `C:\Program Files\Android\Android Studio\jbr` (JDK incluido en Android Studio).

### Mapa útil para mantenimiento
- `model/DomainModels.kt`: enums, límites de validación y transiciones.
- `data/local/`: cuatro entidades y DAOs; `AppDatabase` precarga mediante callback; `DemoData` contiene tres incidentes.
- `data/remote/`: servicio Retrofit, DTOs y mock HTTP en memoria. `AppContainer` rehidrata el mock desde Room al arrancar.
- `data/repository/`: transacciones, sincronización, CRUD, autenticación demo y propagación de errores.
- `di/`: contenedor manual y Factory; `CiudadAlertaApplication` registrado en Manifest.
- `ui/AppNavigation.kt`: once destinos, con rutas protegidas por rol. Tres ViewModels mantienen StateFlow.
- `ui/Components.kt`: formularios compartidos y PickVisualMedia; copia de fotos a filesDir/evidence.
- `src/test/.../DomainAndApiTest.kt`, `src/androidTest/.../RepositoryIntegrationTest.kt` y `DemoFlowTest.kt`: verificaciones funcionales.

### Alcance y notas importantes
- Backend y autenticación son demostraciones locales sin servidor ni envío de correos. Registro y restablecimiento funcionan mediante Retrofit contra el mock; cuentas demo persistidas en preferencias privadas.
- Accesos iniciales: ciudadano@comunidad.mx / Demo1234 y admin@ciudadalerta.mx / Demo1234. Si se cambia una contraseña mediante recuperación, usar la nueva en el formulario de login.
- Room conserva reportes, evidencias y avisos. Al cerrar sesión se borran únicamente los borradores del usuario, como exige la especificación.
- Las fotos de prueba son ilustraciones PNG locales, identificadas en pantalla como simuladas. La prueba automatizada usa esas fotos; la selección de una foto real en el selector del sistema no se automatizó.
- Historial de cambios guardado en notificaciones por folio; la UI permite marcar avisos como leídos conservando el historial.
- CompileSdk 37 instalado; targetSdk 36 y minSdk 26. Room 2.8.3 corrige la incompatibilidad comprobada de Room 2.6.1 con Kotlin 2.2.
- No se probó en dispositivo físico ni se generó versión de distribución firmada; se entrega APK debug.

## Plan
- [x] Fase 1: dependencias y dominio.
- [x] Fase 2: cuatro entidades, DAOs CRUD y Room precargado.
- [x] Fase 3: DTOs, Retrofit y servidor simulado autónomo.
- [x] Fase 4: repositorios, DI manual y Application.
- [x] Fase 5: ViewModels, once pantallas y navegación.
- [x] Validación: assembleDebug, pruebas unitarias, lint y revisión funcional disponible.
## Reglas de continuidad
Mantener DI manual; no Firebase/Hilt/Dagger/Koin. No dejar TODO ni funciones vacías.
Registrar comandos, resultados y pendientes aquí. No confundir compilación con prueba en dispositivo.
## Decisiones
- La especificación prevalece sobre los nombres de DTOs del mensaje: NetworkDtos.kt agrupa ReportDto y StatusUpdateDto; AuthDto.kt amplía el contrato de autenticación.
- Se conserva compileSdk 37 ya instalado y AGP 9.3.3 del proyecto; targetSdk se ajusta a 36 según la especificación.
- KAPT compatible con Kotlin integrado: com.android.legacy-kapt (misma versión AGP). Referencia: https://developer.android.com/build/migrate-to-built-in-kotlin
- Fotos demo locales para permitir revisión sin internet.

### Fase 1 implementada
Dependencias agregadas y validadores extraídos del contrato. Compilación de etapa pendiente de ejecutar.

### Fase 2 implementada
Cuatro entidades y DAOs completos; callback síncrono precarga reportes, multimedia y notificaciones. DemoData es compartido con el futuro mock; no depende de red. Consultas por ID y registro de historial agregados.
Fase 1: compileDebugKotlin correcto. Fase 2: Room 2.6.1 rechaza metadata Kotlin 2.2; se actualiza Room a 2.8.3 conservando los contratos. Se repite compilación.

### Fase 3 implementada
Retrofit y DTOs, mock JSON con verbos GET/POST/PUT/PATCH/DELETE y autenticación local. Valida transiciones y devuelve errores HTTP ante solicitudes inválidas; peticiones válidas 200/201. Al iniciar proceso se rehidratará desde Room para evitar revertir cambios o resucitar eliminados.

### Fase 4 implementada
Repositorios con errores HTTP explícitos, cancelación propagada y transacciones Room; sincronización no duplica multimedia. DI manual y Application registrados. Factory se conecta junto con ViewModels para no introducir referencias huérfanas. Mock se hidrata desde Room en hilo de OkHttp; cuentas demo persistidas localmente, no autenticación de producción.

### Fase 5 — ViewModels
Auth, Reportes y Admin con StateFlow, operaciones suspend y manejo de errores; restricciones de autor/rol y limpieza de borradores al salir. Factory conectada. Pendiente completar UI, navegación y pruebas.

### Fase 5 — UI implementada
Once pantallas conectadas, navegación separada por rol, filtros, CRUD, borradores, notificaciones, evidencia e historial. Selector PickVisualMedia copia las imágenes al almacenamiento privado para que sobrevivan al reinicio. KPI de atención = resueltos / total. Pendiente compilar y ejecutar pruebas de integración.

### Verificación preparada
Pruebas unitarias de límites, matriz de estados, verbos HTTP, registro/login/recuperación y rehidratación vacía. Prueba instrumentada de repositorios/Room, aislamiento de borradores, evidencias e historial. Prueba Compose del flujo ciudadano y administrador. Fotos simuladas PNG incluidas como assets locales.

### Resultados antes de cierre
Fases 1, 2, 3 y 4 compiladas individualmente. assembleDebug + testDebugUnitTest + lintDebug + assembleDebugAndroidTest: BUILD SUCCESSFUL. 5 pruebas JVM, 0 fallos. connectedDebugAndroidTest en Pixel_10_Pro API 37: 3 pruebas, 0 fallos. Últimos ajustes: catálogo centralizado, eliminación de TODO de plantilla, cancelación de carga de foto y prueba UI repetible que solo modifica su propio reporte. Se repiten las comprobaciones afectadas.

## Cierre
Última ejecución conjunta: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest` — BUILD SUCCESSFUL, 22 s, 87 tareas (23 ejecutadas, 64 actualizadas). La prueba Compose final crea y elimina su propio reporte; comprueba PENDIENTE -> EN_PROCESO -> RESUELTO, evidencia e historial. No quedan bloqueos.
