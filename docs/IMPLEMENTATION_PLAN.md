# VitalLog — Plan de implementación (6 hitos)

Plan incremental derivado de `CLAUDE.md` (fuente de verdad) y de `docs/Proyect.pdf`. Al terminar cada hito la app **compila y pasa sus pruebas JVM**. Claude no usa emulador ni dispositivo: la demo en dispositivo la hace el usuario (bloques "Verificación manual").

**Estado:** Hitos 1–6 completos (plan terminado). La estructura de paquetes vigente está en el código (`app/src/main/java/com/davidrdz/vitallog/`).

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
