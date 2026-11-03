# Pendientes de Ciudad Alerta

Fecha de revisión: 2026-11-02.
Proyecto: C:\Users\Damian\AndroidStudioProjects\CiudadAlerta
Fuente de requisitos: app/Specs_CiudadAlerta.md y las instrucciones originales del usuario.

## Resumen para continuar

Las cinco fases y las once pantallas están implementadas. No se identificaron módulos principales ausentes. Sin embargo, la afirmación anterior de SEGUIMIENTO.md de que no queda ningún pendiente es demasiado amplia: quedan dos casos de funcionamiento por corregir, una discrepancia de configuración por resolver y comprobaciones de aceptación pendientes.

Esta revisión fue de lectura del código y de los resultados existentes. No se modificó código, no se ejecutaron pruebas nuevas y no se reprodujeron los casos descritos abajo. Solo se creó este documento. Las correcciones identificadas por inspección deben confirmarse con sus pruebas de regresión al implementarlas.

## 1. Pendientes de código

### C1. Coordinar la importación de una foto con el guardado del formulario

- [ ] Comunicar el estado de importación de PhotoField al formulario y evitar enviar/guardar mientras la copia está en curso.
- Evidencia: ui/Components.kt mantiene un estado busy interno mientras copia la imagen. CapturaIncidenciaScreen.kt y AdminResolverScreen.kt habilitan sus botones usando únicamente state.busy del ViewModel.
- Consecuencia: tras elegir una foto, el ciudadano puede guardar antes de que onChange publique la nueva URI. Si ya había una foto, también se puede enviar la anterior mientras se importa su reemplazo. En resolución sin foto previa, la validación sí impide resolver sin evidencia, pero no cubre el reemplazo de una foto.
- Relación con requisitos: captura de fotografía, guardado de borradores y evidencia de caso resuelto.
- Criterio de cierre: al seleccionar una imagen con copia lenta, el formulario espera el resultado; ante error, lo informa; el registro enviado contiene la foto finalmente seleccionada.
- Tipo: código y prueba de regresión. No depende de servicios externos.

### C2. Conservar la utilidad de los accesos rápidos Demo Ciudadano y Demo Admin

Responsable: fix/C2-accesos-demo (identificador temporal).
Rama: fix/C2-accesos-demo, clon separado de origin/master.
Estado: en curso.
Última actualización: 2026-11-03.
Archivos previstos: MockNetworkInterceptor.kt, LoginScreen.kt, RecoveryScreen.kt, DemoAccessTest.kt y documentos de seguimiento.
Siguiente paso: reservar las cuentas demo con acceso fijo, comprobar recuperación y rehidratación de cuentas; conservar recuperación para cuentas registradas.
Coordinación: el usuario informa de otro colaborador en main; esta rama no modifica ni integra su trabajo.

- [ ] Definir e implementar un comportamiento consistente entre recuperación de contraseña y acceso demo.
- Evidencia: AuthViewModel.kt usa siempre Demo1234 en loginDemoCiudadano/loginDemoAdmin. MockNetworkInterceptor.kt permite cambiar y persistir la contraseña de esas mismas cuentas mediante recovery.
- Consecuencia: después de cambiar la contraseña demo, el botón rápido correspondiente deja de autenticar; el formulario normal con la contraseña nueva sí funciona.
- Relación con requisitos: botones de acceso rápido para revisión presencial y restablecimiento de contraseña.
- Criterio de cierre: probar ambos botones después de usar recuperación y después de reiniciar el proceso. Si se reservan las cuentas demo para acceso fijo, explicarlo en la UI y mantener recuperación para las cuentas registradas.
- Tipo: código y prueba de regresión. No requiere correo real ni servidor.

## 2. Discrepancia de configuración frente al documento

### D1. Resolver compileSdk 37 frente a compileSdk 36 de la especificación

- [ ] Confirmar si se admite la adaptación documentada o alinear la configuración con el SDK 36.
- Evidencia: sección 3.2 de Specs_CiudadAlerta.md indica compileSdk = 36. app/build.gradle.kts usa release(37), con targetSdk = 36 y minSdk = 26.
- Esto no es un fallo de compilación: la versión actual compiló. Es una diferencia respecto al contrato escrito.
- Si se exige cumplimiento literal: disponer del SDK 36, revisar los requisitos mínimos de las dependencias actuales, ajustar las versiones necesarias y volver a compilar/probar. No cambiar solo el número sin verificar compatibilidad.
- Tipo: configuración/código; instalación de un SDK gratuito si no está disponible. La aceptación de la diferencia es una decisión del responsable o evaluador, no una funcionalidad por programar.
- Room 2.8.3 y com.android.legacy-kapt son adaptaciones de compatibilidad ya documentadas y compiladas. No volver a Room 2.6.1 sin resolver su incompatibilidad observada con Kotlin 2.2.

## 3. Verificaciones de aceptación pendientes

Estos puntos no significan que la función no exista. Falta evidencia de que funcione en esos escenarios.

### V1. Selector del sistema con fotografías reales

- [ ] Elegir una fotografía real mediante PickVisualMedia para un incidente y para la resolución administrativa.
- [ ] Comprobar cancelación, error de lectura y sustitución de una fotografía.
- [ ] Guardar un borrador con foto, reiniciar el proceso y comprobar que la imagen sigue accesible; enviarlo y consultar el detalle.
- Evidencia actual: la prueba Compose usa imágenes simuladas locales; no automatiza el selector del sistema.
- Tipo: prueba manual o automatización adicional. Se necesita una imagen en un dispositivo/emulador; no requiere cuenta de nube ni pago.

### V2. Compatibilidad en el rango Android declarado

- [ ] Ejecutar los flujos principales al menos en API 26 y API 36.
- Evidencia actual: las pruebas instrumentadas registradas se ejecutaron en Pixel_10_Pro con API 37.
- Tipo: validación en emuladores gratuitos o dispositivos disponibles. Solo requiere cambios de código si se encuentra un fallo.
- Una prueba en teléfono físico es recomendable antes de la entrega, pero no es un requisito explícito adicional de implementación.

### V3. Completar la matriz de aceptación de UI y persistencia

- [ ] Verificar desde la UI registro, login normal y recuperación.
- [ ] Verificar combinación de filtros, Todos/Mis Reportes, ausencia de acciones de edición para reportes ajenos y estado vacío.
- [ ] Verificar lectura de avisos, borradores de distintos usuarios y limpieza de borradores al salir.
- [ ] Verificar reinicio completo del proceso después de crear, modificar, resolver y eliminar un reporte: no debe perderse un cambio ni reaparecer un eliminado.
- [ ] Comprobar rotación/recreación de formularios, especialmente al importar una fotografía.
- Evidencia actual: hay pruebas de API y repositorios para parte de estos comportamientos y una prueba Compose del flujo principal; eso no acredita todos los escenarios de UI ni un reinicio real del proceso.
- Tipo: pruebas. No es autorización para agregar funcionalidades nuevas.

## 4. Mejoras menores opcionales, no requisitos faltantes

- [ ] Si se amplía la fidelidad de multimedia, almacenar MIME y tamaño reales: ReportRepository.persist usa los valores predeterminados image/jpeg y 102400 bytes, incluso para imágenes de otros formatos. Los valores predeterminados proceden del contrato de la entidad y no impiden mostrar las fotos.
- [ ] Si se desea pulir el proyecto, revisar las 26 advertencias de lint. El resultado existente es 0 errores; no todas requieren cambios ni conviene actualizar dependencias indiscriminadamente.
- [ ] Si se prepara distribución fuera de la revisión de desarrollo, generar una versión release firmada. Requiere definir propietario y custodia de la clave; no es necesario para el APK debug solicitado.

## 5. Lo externo y lo que NO falta para esta especificación

No hace falta contratar hosting, crear Firebase, pagar servicios ni conectar una API real: el mock autónomo es un requisito explícito.

Tampoco están exigidos por este alcance:
- Envío real de correos de recuperación.
- Sincronización entre distintos teléfonos o usuarios en un servidor compartido.
- Notificaciones push.
- Publicación en Google Play.
- Captura directa con cámara: se pidió PickVisualMedia y foto de prueba, y ambos están implementados.

Si se solicita cualquiera de esas capacidades en el futuro, será una ampliación del alcance; algunas exigirán infraestructura, cuentas o decisiones externas, además de código. No deben presentarse como pendientes obligatorios actuales.

No hay que separar ReportDto y StatusUpdateDto en archivos distintos para cumplir Specs_CiudadAlerta.md: el documento exige NetworkDtos.kt y ahí están. AuthDto.kt también existe.

No se requieren nuevas pantallas CRUD independientes para multimedia/notificaciones: los contratos detallados establecen su administración desde los flujos de reportes, evidencias y avisos, y sus DAOs ya contienen las operaciones correspondientes.

## 6. Evidencia existente y orden sugerido

Resultados anteriores, no ejecutados de nuevo en esta revisión:
- assembleDebug: correcto, APK debug disponible.
- Pruebas JVM: 5 aprobadas; 4 de negocio/API y 1 prueba base de plantilla.
- Pruebas instrumentadas registradas: 3 aprobadas; UI Compose, repositorios/Room y contexto de aplicación.
- lintDebug: 0 errores y 26 advertencias.

Orden para un agente posterior:
1. Reproducir y corregir C1 y C2.
2. Resolver D1 con el criterio de entrega aplicable.
3. Ejecutar V1–V3 y registrar resultados reales, separando fallos de casos no probados.
4. Actualizar SEGUIMIENTO.md después de ejecutar ese trabajo; no marcar como completadas las tareas de este archivo solo por compilar.

No se implementó ninguno de estos pendientes durante esta revisión.

