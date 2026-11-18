# VitalLog — Plan de implementación (9 hitos)

Plan incremental derivado de `CLAUDE.md` (fuente de verdad) y de `docs/Proyect.pdf`. Al terminar cada hito la app **compila y pasa sus pruebas JVM**. Claude no usa emulador ni dispositivo: la demo en dispositivo la hace el usuario (bloques "Verificación manual").

**Estado:** Hitos 1–9 completos. El rediseño visual "Sobremesa" (Hitos 7–9, ver `docs/DESIGN_DIRECTION.md`) quedó aplicado en todas las pantallas. La estructura de paquetes vigente está en el código (`app/src/main/java/com/davidrdz/vitallog/`).

## Decisiones globales (conflictos resueltos con CLAUDE.md)

| Tema | Decisión |
|---|---|
| Presión sistólica mínima | **60 mmHg** (CLAUDE.md §4.2), no 50. |
| Modelo de datos | `GlucoseReading`, `BloodPressureReading`, `Medication`, `DoseLog`, `Appointment`. Persistencia con **Room**. |
| Retroalimentación al guardar | Versión básica (banner + tono) desde el Hito 3, para cumplir §3.4 desde el primer guardado. El Hito 6 la pule y agrega el historial. |
| Citas médicas | Incluidas (entidad `Appointment`, recordatorio con el mismo modal). CLAUDE.md §4 y §4.3 ya actualizados. |
| Dial rotativo | **Descartado**: con temblor es menos preciso que los botones +/−. Solo botones +/− con aceleración. |

## Convenciones transversales

- **Hito terminado:**
  1. `./gradlew assembleDebug test lint` en verde.
  2. `python3 .claude/skills/gerontological-ui-auditor/scripts/audit_ui.py app/src/main/res app/src/main/java` sin `ERROR`.
  3. `contrast.py` ≥ 7:1 para cada par de colores nuevo.
  4. Revisión estática de escala de fuente (sin alturas fijas en contenedores de texto, `ScrollView` donde haga falta, sin `maxLines`/`ellipsize` que corten texto).
  5. Sin commit de Claude: el usuario revisa el diff y commitea.
- Pruebas instrumentadas (`androidTest/`): se escriben pero Claude **no las ejecuta**; se reportan como "escritas, no ejecutadas".
- Dependencias nuevas solo vía `gradle/libs.versions.toml`.
- Textos en `strings.xml`, dimensiones en `dimens.xml`, colores en `colors.xml`; nada literal en layouts ni en Java.
- Inyección manual: `VitalLogApp` crea y expone los repositorios; los ViewModels los reciben con `ViewModelProvider.Factory`.
- La lógica de dominio (`analytics/`, alarmas, formateo de horas, resúmenes) es **Java puro** para probarla con JUnit en la JVM.
- Las pruebas replican la estructura de paquetes: `src/test` (JUnit 4, JVM) y `src/androidTest` (Room en memoria, Espresso).

---

## Hitos completados (resumen)

### Hito 1 — Maquetado y sistema de diseño ✅
Tema claro fijo (`values-night` eliminado), tokens semánticos de color validados con `contrast.py`, dimensiones base (`touch_target` 64dp, botones de acción 120dp, texto ≥ 20sp, valores 56sp), iconos siempre con texto, dashboard plano con 3 botones y `BaseScreenActivity` con botón "Volver" visible. Prueba instrumentada: `DashboardNavigationTest`.

### Hito 2 — Control numérico visual reutilizable ✅
`NumericStepperView` (botones −/+ de 80dp, valor ≥ 56sp, sin teclado) con `StepperModel`, `AccelerationPolicy` (paso 1 → 5 → 10 a las 10 y 20 repeticiones) y `RepeatTouchListener` (400 ms inicial, repite cada 100 ms). Rangos en `VitalRanges` (se crearon aquí porque los límites del control salen de ellos): glucosa 20–600, sistólica 60–260, diastólica 30–160, presión de pulso mínima 10; valores típicos 100 / 120 / 80. Accesibilidad: live region, acciones de TalkBack y conserva el valor al rotar.

### Hito 3 — Persistencia offline + retroalimentación básica ✅
Room v1 (5 entidades, 5 DAOs, `exportSchema` con `app/schemas/` versionado), sin `allowMainThreadQueries` ni `fallbackToDestructiveMigration`; escrituras en un `ExecutorService` compartido. Repositorios con `save(entity, callback)` que entrega `SaveResult` en el hilo principal. Banner (3 s) + tono con `ToneGenerator`. Agenda con listas y formularios de medicamento y cita (nombre por teclado, el resto con steppers); borrar pide confirmación ("Sí, borrar" / "No, conservar"). `SpokenTimeFormatter` (es-MX). Privacidad: `allowBackup="false"` y base de datos excluida de `cloud-backup` y `device-transfer`.

### Hito 4 — Gestor analítico de validación ✅
`ClinicalRangeValidator` (Java puro) devuelve `ValidationError` (códigos, sin texto de UI); `VitalsRepository` valida **antes** de insertar y devuelve `SaveResult.rejected(error)`. `ReadingClassifier` es informativo, no diagnóstico:
- Glucosa: < 70 baja; 70–180 en rango; 181–249 alta; ≥ 250 muy alta.
- Presión: < 90/60 baja; < 120/80 normal; 120–129 con < 80 elevada; ≥ 130 o ≥ 80 alta; ≥ 180 o ≥ 120 crítica.

**Decisiones:**
- El aviso crítico se muestra **en vivo** bajo el control (`ReadingStatusView`), con texto + icono + color; no hay diálogo extra, así que guardar sigue siendo un toque. Un valor posible nunca se bloquea.
- El aviso en lenguaje llano solo aparece en CRITICAL; glucosa < 70 se muestra sin aviso adicional.
- El control de presión **no** limita la sistólica a diastólica + 10 en vivo, para poder capturar exactamente lo que marca el aparato (90/85 → error + tono grave).
- La presión se clasifica por el número más preocupante (150/55 → alta). "Elevada" solo aplica a presión.

### Hito 5 — Alarmas y modal a pantalla completa ✅
`NextTriggerCalculator` (java.time, `ZoneId` inyectado; aviso de cita = `scheduledAt − 120 min`), `ReminderPolicy` (posponer 10 min), `AlarmScheduler` (`setAlarmClock`), `ReminderReceiver` / `ReminderNotifier` (canal de alarma, `CATEGORY_ALARM`, Full-Screen Intent), `BootReceiver` (reinicio, cambio de hora/zona, actualización), `FullScreenAlarmActivity` con **exactamente 2 botones** de 96dp ("Ya lo tomé" / "Recordar más tarde"; cita: "Entendido" / "Recordar más tarde"), `AlarmPermissionChecker` + `PermissionsGuideActivity` (un permiso por pantalla). Manifiesto: receivers `exported="false"` salvo `BootReceiver`. Pruebas JVM: `NextTriggerCalculatorTest`, `ReminderRequestCodesTest`, `ReminderPolicyTest`. Instrumentada (escrita, no ejecutada): `FullScreenAlarmActivityTest`.

**Decisiones:**
- Sin permiso de alarmas exactas el respaldo es `setAndAllowWhileIdle` (aproximada), porque `setExactAndAllowWhileIdle` exige el mismo permiso que `setAlarmClock`. `BootReceiver` también escucha `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` y reprograma como exacto al concederse.
- Los repositorios programan/cancelan la alarma en la misma tarea de fondo que guarda/borra, para que base de datos y alarmas no se desincronicen.
- "Ya lo tomé" desde la notificación abre el modal (PendingIntent de Activity), que registra la toma con banner + tono (§3.4). `ReminderActionReceiver` solo maneja "Recordar más tarde" y "Entendido", que no guardan nada.
- Botón Atrás cierra el modal pero deja la notificación activa y no registra nada.
- `FullScreenAlarmActivityTest` usa el singleton de `VitalLogApp`, no una base en memoria; cada prueba trabaja solo con el medicamento que inserta.

### Hito 6 — Retroalimentación multimodal pulida + historial simple ✅
`FeedbackBannerView` entra con escala (banner 0.9 → 1 y check con rebote, sin transparencia), visible 3 s, se muestra sin animar si `ValueAnimator.areAnimatorsEnabled()` es falso y anuncia el mensaje con `announceForAccessibility`. `FeedbackTonePlayer` usa `SoundPool` (`USAGE_ASSISTANCE_SONIFICATION`) con `res/raw/feedback_success.ogg` (Do5 → Sol5) y `feedback_error.ogg` (La3), sintetizados para el proyecto; si un sonido no cargó, suena `ToneGenerator`. Dashboard con 4.º botón "Ver mi historial" (`action_history_*`, 10.2:1). `HistoryActivity` (solo lectura): botones "Últimos 7 días" / "Últimos 30 días", resumen y lista agrupada por día. Java puro: `HistoryPeriod`, `HistoryItem` (fila unificada + `groupByDay`), `HistorySummary`, `HistorySummaryCalculator`; `SpokenTimeFormatter.formatDayHeading`. Pruebas JVM: `HistorySummaryCalculatorTest`, `FeedbackDurationsTest`, `SpokenTimeFormatterTest` ampliado. Instrumentadas (escritas, no ejecutadas): `HistoryActivityTest` y el botón nuevo en `DashboardNavigationTest`.

**Decisiones:**
- "Últimos N días" = N días de calendario contando hoy (desde las 00:00 de hace N − 1 días). El ViewModel observa una vez 30 días y el cambio de periodo filtra en memoria.
- Lista de lo más reciente a lo más antiguo, con encabezado "Hoy, martes 17 de noviembre".
- Resumen de presión: promedio como par (128/82) y rango de cada número por separado; el promedio redondea x.5 hacia arriba.
- Consultas nuevas en los DAOs (`observeSince`, `observeTakenSince` con `JOIN` a `medications` → POJO `TakenDose`) sin tocar el esquema; las tomas de medicamentos borrados siguen saliendo.
- Reutilización: `ReadingCategoryStyle` (icono/color/texto de categoría, antes privado en `ReadingStatusView`) y `ChoiceButtonStyle` (antes en `MedicationFormActivity`).
- El mensaje del banner dejó de ser región "live" para que TalkBack no lo lea dos veces. `announceForAccessibility` está en desuso desde API 36 pero funciona; se usa porque es lo que pide el plan y repite mensajes idénticos.
- Las filas no son tocables; `screenReaderFocusable` hace que TalkBack lea cada tarjeta completa.

---

## Riesgos y notas

- **Full-Screen Intent en API 34+**: Google lo restringe a apps de llamadas y alarmas. Si no se concede, la notificación aparece como heads-up con sonido de alarma y la pantalla de permisos lleva a los ajustes.
- **Profundidad de navegación**: los formularios de medicamento y cita son un paso de la tarea que siempre regresa a la agenda; no cuentan como sección nueva.
- **Ahorro de batería agresivo de fabricantes**: fuera de alcance; solo se usa `setAlarmClock`.
- **Pendientes visuales fuera del Hito 9** (no tocan layouts):
  - El icono de la app y el icono pequeño de la notificación siguen siendo los de la plantilla de Android (androide verde).
  - El campo de nombre es de una sola línea, así que con fuente 2.0× un nombre largo se desplaza
    horizontalmente mientras se escribe. Hacerlo multilínea cambia la tecla Enter del teclado.
  - En horizontal con fuente 2.0×, los 2 botones fijos del modal dejan poco espacio a la información.
- **Fuera de alcance**: exportar a PDF, borrar lecturas desde el historial, red o sincronización (prohibidas por CLAUDE.md) y el dial rotativo.

---

## Rediseño visual "Sobremesa" (Hitos 7–9)

Fuente de la apariencia: `docs/DESIGN_DIRECTION.md` (tokens, ratios, formas, wireframes). No cambia
la lógica, la navegación, los textos ni el modelo de datos, salvo lo indicado. Cada hito se cierra
con la definición de "Hito terminado" de arriba. Las capturas a 1.0×, 1.3× y 2.0× las revisa el usuario en su dispositivo.

### Hito 7 — Tokens visuales y tema ✅
Paleta "Sobremesa" aplicada **solo con recursos** (`colors.xml`, `dimens.xml`, `styles.xml`,
`themes.xml`); sin cambios en layouts ni en Java. Toda la app ya usa fondo blanco cálido (#FFFAF3),
tinta café (#2A1E14), arena (#F3ECE2) y cacao (#5A3A22). Los botones de glucosa y presión usan cobalto
y ciruela con radio de 28dp, la alarma radio de 20dp y Subir/Bajar forma de píldora (40dp).
El número del stepper pasó a 60sp `sans-serif-black`. Los 30 pares de color pasan `contrast.py`
(mínimo: `status_ok`/`surface_success`, 7.31).

**Decisiones (desvíos del borrador):**
- **Alias temporales en lugar de borrar tokens:** el hito prohibía tocar layouts y Java, así que
  `surface_neutral` → `surface_sand`, `action_glucose_fg` → `vital_glucose`, `action_pressure_fg`
  → `vital_pressure` y `action_agenda_fg` → `brand_primary` (cacao; los medicamentos dejan el verde
  que chocaba con `status_ok`). Se borraron `action_*_bg` y `action_history_*` (solo los usaban estilos).
  Los alias se eliminan en los Hitos 8–9, al actualizar `HistoryAdapter`, `NumericStepperView`,
  `item_medication` e `item_appointment`.
- **Recursos que solo usa el Hito 8 se crean en el Hito 8:** `Greeting`, `text_greeting`,
  `primary_action_height`, `secondary_action_height`, `icon_plate_*`, `greeting_icon_size` y los
  estilos `Stepper.Glucose/Pressure/Neutral`. Crearlos antes sería código muerto (§6) y avisos `UnusedResources`.
- **Transición del dashboard:** `Action.Agenda` y `Action.History` ya son arena + cacao (radio 16), pero siguen
  midiendo 120dp porque `activity_main.xml` fija `minHeight`; la altura de 88dp llega con el Hito 8.
- **Stepper en el límite (transitorio):** hoy el estado normal (arena + borde cacao + signo) y el de límite
  (arena + borde tinta, sin signo, "Máximo"/"Mínimo") solo se distinguen por el texto, el signo y el
  borde. Con `stepperAccent` (Hito 8), el estado normal tendrá el color del signo vital y el contraste volverá a ser claro.
- **Interlineado de 1.25 en el cuerpo:** `lineSpacingMultiplier` no se aplica desde `textAppearance`;
  se ajusta en los layouts de los Hitos 8–9.
- **Tema:** solo cambian los valores de los tokens (`colorPrimary` = cacao, `colorPrimaryContainer`
  = arena, `colorSurface` = blanco cálido). Los botones M3 ya tienen elevación 0, así que no hizo falta nada más.

**Verificación:** `assembleDebug test lint` en verde (134 pruebas JVM, lint con 0 errores y los
mismos 7 avisos previos); `audit_ui.py` con 0 ERROR (2 WARN de `EditText` en nombre de
medicamento y médico, ya justificados). Revisión estática de escala: sin `maxLines`, `ellipsize`
ni alturas fijas en textos; los steppers van a todo lo ancho y en vertical, así que 60sp cabe a 2.0×.
**Verificación manual (usuario):** recorrer todas las pantallas a 1.0× y 2.0×: fondo cálido, sin texto
gris claro, sin botones que parezcan deshabilitados y con "Máximo"/"Mínimo" distinguible en los steppers.

### Hito 8 — Dashboard con saludo, captura y stepper ✅
Etapa A (dashboard) aprobada con capturas del usuario; etapa B (captura) entregada.
- **Dashboard:**
  - Saludo según la hora con pictograma (sol naciente, sol o luna) y la fecha en palabras. Usa
    `DayPeriod` (Java puro, 8 pruebas), `DayPeriodStyle` y `SpokenTimeFormatter.formatFullDate`.
    Se recalcula en `onResume`, y TalkBack lo lee de una vez.
  - Glucosa y presión: botones `Primary.*` de 136dp, radio 28 y plato `ic_plate_*` de 64dp con `iconTint` @null.
  - Agenda e historial: botones `Secondary.*` de 88dp, arena + cacao.
  - Se eliminan `Action.*`, `action_button_height` e `icon_action_size`. IDs y textos existentes intactos.
- **Captura:**
  - `app:stepperAccent` (`StepperAccent`: neutral, glucose, pressure). `NumericStepperView.renderButton`
    solo cambia los colores; en el límite el botón pasa a `background` + `outline_strong`.
  - Plato de 48dp junto al título con `BaseScreenActivity.getScreenIconRes()`.
  - Interlineado de 1.25 en el texto de ayuda y en el aviso de la lectura.
  - Se elimina el alias `surface_neutral`.
- **Pruebas:** `DayPeriodTest` y `fullDate_isCapitalizedWithoutRelativeDay` (143 JVM en verde).
  `DashboardNavigationTest.greeting_showsPeriodAndTodayDate`: escrita y compilada, no ejecutada.

**Decisiones:**
- El usuario aprobó dos cambios de Java fuera de "solo apariencia": el saludo (lógica pura nueva) y
  `stepperAccent`. El icono del título usa la misma vía (hook de presentación en `BaseScreenActivity`).
- El plato bajó de 72 a 64dp y el inicio usa margen lateral de 16dp, para dejar ancho al texto a 2.0×.
- **Peso 900 sin `sans-serif-black`:** en el dispositivo del usuario `sans-serif-black` cayó al peso
  normal. Se usa `sans-serif` + `textStyle=bold` + `textFontWeight=900` (API 28+).
- **Límite del stepper en blanco cálido** (no arena): con el acento neutral, arena se confundía con el estado normal.

**Verificación manual (usuario):** capturas de glucosa y presión a 1.0× y 2.0×: valor por defecto, "Máximo"/"Mínimo", aviso crítico y error de validación.

### Hito 9 — Resto de pantallas y cierre ✅
Coherencia visual en todas las pantallas, en 3 etapas aprobadas por el usuario (A: modal de alarma;
B: agenda, formularios y diálogo; C: historial y permisos) y una pasada final de consistencia.
- **Modal de alarma:** sigue con **exactamente 2 botones** de ≥ 96dp y radio 20, ahora **fijos al pie**
  (fuera del `ScrollView`) para que se vean sin desplazar aunque la fuente esté a 2.0×. Arriba van el
  plato (`bg_plate_brand`, arena con borde cacao, 128dp, icono de 88dp), el aviso, el nombre en
  `TextAppearance.VitalLog.AlarmName` (34sp, peso 900, se parte con guion) y la dosis y la hora,
  todo alineado al inicio.
- **Agenda:** el icono pasa de cada tarjeta al encabezado de su sección ("Mis medicamentos",
  "Mis citas con el médico"), para dejar todo el ancho al nombre. Entre los dos grupos hay
  `spacing_section` (40dp). Las tarjetas siguen en `bg_outlined`.
- **Formularios:** campos en `surface_field` (#FFFFFF) con borde tinta de 3dp que, al escribir,
  pasa a cacao de 5dp (`bg_text_field`). Entre el nombre y los números hay 40dp.
- **Diálogo de borrado:** `ThemeOverlay.VitalLog.Dialog` (blanco cálido sólido, radio 20, sin
  cambios en Java). "No, conservar" (`Button.Keep`: arena, cacao, 26sp) pesa más que "Sí, borrar"
  (`Button.Delete.Confirm`: blanco cálido, borde rojo, 22sp). La opción segura sigue primero y a 24dp.
- **Historial:** cada subtítulo del resumen lleva su icono (`vital_glucose`, `vital_pressure`,
  `brand_primary`). `HistoryAdapter` usa los tokens `vital_*` y `brand_primary`, y se **borraron los
  alias** `action_*_fg` del Hito 7.
- **Guía de permisos:** el pictograma va en el mismo plato que el modal (96dp, icono de 64dp), alineado al inicio.
- **Pasada de consistencia:**
  - Botones grandes consolidados en `Button.Large` → `Save` y `General` → `Add`,
    `OpenSettings`, `Keep` y `Secondary` (antes había 4 copias de "arena + cacao + 88dp").
  - Tipografía con negrita 700 en lugar de `sans-serif-medium`.
  - `letterSpacing = 0` en el cuerpo.
  - `elevationOverlayEnabled = false` en el tema.
  - `touch_target_min` documentado con `tools:ignore`.
  - Sin literales de color ni de tamaño en layouts ni en Java.
- **Pruebas:** `FullScreenAlarmActivityTest.takenButton_createsDoseLog` cambia `scrollTo()` por
  `check(isDisplayed())` + `click()`, porque el botón ya no está en un `ScrollView` (escrita y
  compilada, no ejecutada). IDs, textos y lógica sin cambios.

**Decisiones:**
- **Negrita 700 en vez de medium** (aprobada por el usuario). Causa encontrada al cerrar el hito: los padres
  `TextAppearance.Material3.*` traen `app:fontFamily="sans-serif"`, que en los `TextView` de AppCompat
  manda sobre `android:fontFamily`, así que `sans-serif-medium` nunca se aplicó. Ahora cada estilo fija
  `fontFamily` y `android:fontFamily`, y pide el peso con `textStyle=bold` + `textFontWeight`.
- **"Borrar" sigue en rojo** (aprobado por el usuario): es una excepción documentada a "sin rojo en acciones",
  igual que el verde de "Guardar"/"Ya lo tomé".
- **Tinte de elevación apagado en todo el tema:** "Sí, borrar" se veía más oscuro que el fondo del
  diálogo. La causa probable es el tinte de elevación de Material 3, porque su relleno coincide con
  `colorSurface` y la vista se infla con el tema de la Activity. No se confirmó en dispositivo.

**Verificación:**
- `./gradlew assembleDebug test lint assembleDebugAndroidTest` en verde: 143 pruebas JVM; lint con
  0 errores y 6 avisos, todos de versiones de dependencias o de Gradle.
- `audit_ui.py` con 0 ERROR y 2 WARN (`EditText` de nombre y médico, justificados).
- 27 pares de color con `contrast.py` ≥ 7:1, todos oscuro sobre claro; el mínimo sigue siendo
  `status_ok`/`surface_success`, 7.31.
- Revisión estática de escala en los 19 layouts: ningún texto con altura fija, `maxLines` ni `ellipsize`,
  y todas las pantallas dentro de un `ScrollView`.

**Verificación manual (usuario):** etapas A y B revisadas con capturas a 1.0× y 2.0×; la etapa C se
revisó en el dispositivo sin capturas. Falta comprobar en el dispositivo la negrita 700 y que el
diálogo ya no tiña "Sí, borrar".
