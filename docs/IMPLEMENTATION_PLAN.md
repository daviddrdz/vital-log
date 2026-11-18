# VitalLog — Plan de implementación (9 hitos)

Plan incremental derivado de `CLAUDE.md` (fuente de verdad) y de `docs/Proyect.pdf`. Al terminar cada hito la app **compila y pasa sus pruebas JVM**. Claude no usa emulador ni dispositivo: la demo en dispositivo la hace el usuario (bloques "Verificación manual").

**Estado:** Hitos 1–7 completos. Hitos 8–9 (rediseño visual "Sobremesa", ver `docs/DESIGN_DIRECTION.md`) pendientes. La estructura de paquetes vigente está en el código (`app/src/main/java/com/davidrdz/vitallog/`).

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

### Hito 8 — Dashboard con saludo, captura y stepper
**Objetivo:** jerarquía de dos niveles en el inicio, el saludo según la hora y las pantallas de captura con el acento de cada signo vital.

- **Java puro:** `ui/home/DayPeriod` (enum MORNING / AFTERNOON / NIGHT con `from(LocalTime)`, límites
  5:00 / 12:00 / 19:00). En `SpokenTimeFormatter`, un método `formatFullDate(epochMillis)` para
  "Martes 17 de noviembre" (sin "Hoy,"; reutiliza el `formatDate` interno).
- **Dashboard (`activity_main.xml`, `MainActivity`):**
  - Encabezado de saludo: pictograma + `txt_greeting` + `txt_today_date`. Se calcula en
    `onResume`, para que esté al día si la app queda abierta.
  - Dos acciones primarias: vista `view_primary_action.xml` con un `MaterialCardView` clicable de
    radio 28 que contiene el plato y el texto. Lleva `contentDescription`, mínimo de 136dp y se
    conservan los IDs `btn_register_glucose` / `btn_register_pressure`.
  - Dos acciones secundarias: `MaterialButton` con estilo `Widget.VitalLog.Button.Secondary` de 88dp, conservando `btn_agenda` / `btn_history`.
  - Strings nuevas: `greeting_morning`, `greeting_afternoon` y `greeting_night`.
  - Drawables nuevos: `ic_greeting_morning`, `ic_greeting_afternoon`, `ic_greeting_night` y `bg_icon_plate`.
- **Captura (`activity_glucose_entry.xml`, `activity_pressure_entry.xml`):** icono de 40dp del
  signo vital junto al título y steppers con el acento de la pantalla.
- **`NumericStepperView`:** atributo `stepperAccent` (glucose / pressure / neutral) en `attrs.xml`.
  El estado de límite sigue siendo `surface_sand` + `outline_strong` + "Máximo"/"Mínimo".
  Los steppers de hora (`view_time_steppers`) usan el acento neutral.
- **Pruebas JVM:** `DayPeriodTest` (4:59, 5:00, 11:59, 12:00, 18:59, 19:00, medianoche) y un caso de
  `formatFullDate` en `SpokenTimeFormatterTest`. Instrumentada (escrita, no ejecutada):
  `DashboardNavigationTest` sigue pasando por IDs y se agrega una verificación de que el saludo es visible.
- **Escala de fuente:** revisión estática de `view_primary_action` (texto `wrap_content`, plato centrado, sin `maxLines`).

### Hito 9 — Resto de pantallas y cierre
**Objetivo:** coherencia visual en todas las pantallas y auditoría final.

- **Modal de alarma (`activity_full_screen_alarm.xml`):** siguen siendo **exactamente 2 botones** de ≥ 96dp,
  ahora con radio de 20dp. "Ya lo tomé"/"Entendido" en verde de éxito; "Recordar más tarde" en arena + cacao.
  El pictograma del medicamento o de la cita va en cacao.
- **Agenda, formularios, historial y permisos:** tarjetas `item_*` en blanco cálido con borde
  `outline_strong` y radio 16. En el historial, los iconos de glucosa y presión usan `vital_*` y los de
  medicamento, cacao (antes verde, que chocaba con `status_ok`). Los campos de texto van en blanco con borde tinta.
- **Banner de feedback y diálogo de borrado:** solo cambian tokens; las animaciones, la duración y los tonos se quedan como están.
- Eliminar los estilos `Action.*` y los dimens o drawables que queden sin uso (sin código muerto).
- **Cierre:** lista de comprobación final de la skill auditora, con la tabla regla → estado; revisión
  estática de escala de todas las pantallas; actualizar "Estado actual" en CLAUDE.md y el resumen de los Hitos 7–9 aquí.
- **Verificación manual (usuario):** flujo completo a 1.0×, 1.3× y 2.0× (registrar glucosa y presión,
  agregar un medicamento, disparar un recordatorio, ver el historial).
