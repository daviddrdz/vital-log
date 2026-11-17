# VitalLog — Plan de implementación (6 hitos)

Plan incremental derivado de `CLAUDE.md` (fuente de verdad) y de `docs/Proyect.pdf`. Al terminar cada hito la app **compila y se puede demostrar en un emulador Android**.

## Decisiones tomadas (conflictos resueltos con CLAUDE.md)

| Tema | Decisión |
|---|---|
| Presión sistólica mínima | **60 mmHg** (CLAUDE.md §4.2), no 50. |
| Modelo de datos | Entidades de CLAUDE.md: `GlucoseReading`, `BloodPressureReading`, `Medication`, `DoseLog` (no `VitalRecord`/`MedicationSchedule`). Persistencia con **Room** (no `SQLiteOpenHelper`). |
| Retroalimentación al guardar | Versión básica (banner + tono) **desde el Hito 3**, para cumplir §3.4 desde el primer guardado. El Hito 6 la pule y agrega el historial. |
| Citas médicas | **Incluidas**: entidad `Appointment` y recordatorio con el mismo modal ("Entendido" / "Recordar más tarde"). Requiere actualizar CLAUDE.md §4 y §4.3 (Hito 3). |
| Dial rotativo | **Descartado**: con temblor es menos preciso que los botones +/−. Solo se usan botones +/− con aceleración. |

---

## 0. Convenciones transversales (aplican a todos los hitos)

- **Criterio de "hito terminado"**:
  1. `./gradlew assembleDebug test lint` en verde.
  2. `python3 .claude/skills/gerontological-ui-auditor/scripts/audit_ui.py app/src/main/res app/src/main/java` sin `ERROR`.
  3. `contrast.py` para cada par de colores (texto/fondo) nuevo, con resultado ≥ 7:1.
  4. Demo manual en el emulador (API 34+) con escala de fuente 1.0× y 2.0×.
  5. Un commit por hito.
- Dependencias nuevas solo vía `gradle/libs.versions.toml`.
- Textos en `strings.xml`, dimensiones en `dimens.xml`, colores en `colors.xml`. Nada literal en layouts ni en Java.
- Inyección manual de dependencias: `VitalLogApp` (Application) crea y expone los repositorios; los ViewModels los reciben con un `ViewModelProvider.Factory`.
- La lógica de dominio (`analytics/`, cálculos de alarmas, formateo de horas, resúmenes) es **Java puro** para poder probarla con JUnit en la JVM.

---

## 1. Estructura de paquetes Java

El árbol final se llena de forma incremental. Entre corchetes va el hito en que se crea cada archivo.

```
com.davidrdz.vitallog
├── VitalLogApp.java                      [H3] Application: repositorios; canales de notificación [H5]
├── analytics/                            Java puro, sin imports de Android
│   ├── VitalRange.java                   [H2] min, max, valor típico, unidad
│   ├── VitalRanges.java                  [H2] GLUCOSE 20–600, SYSTOLIC 60–260, DIASTOLIC 30–160, MIN_PULSE_PRESSURE 10
│   ├── ClinicalRangeValidator.java       [H4]
│   ├── ValidationResult.java             [H4] ok / error (código de motivo, sin texto de UI)
│   ├── ValidationError.java              [H4] enum de motivos de rechazo
│   ├── ReadingCategory.java              [H4] LOW, NORMAL, ELEVATED, HIGH, CRITICAL
│   └── ReadingClassifier.java            [H4] clasificación informativa, no diagnóstica
├── data/
│   ├── db/
│   │   ├── AppDatabase.java              [H3] singleton, ExecutorService compartido, exportSchema = true
│   │   ├── GlucoseReadingDao.java        [H3]
│   │   ├── BloodPressureReadingDao.java  [H3]
│   │   ├── MedicationDao.java            [H3]
│   │   ├── DoseLogDao.java               [H3]
│   │   └── AppointmentDao.java           [H3]
│   ├── entity/
│   │   ├── GlucoseReading.java           [H3] id, valueMgDl, takenAt
│   │   ├── BloodPressureReading.java     [H3] id, systolic, diastolic, takenAt
│   │   ├── Medication.java               [H3] id, name, doseAmount, doseUnit, hour, minute, active
│   │   ├── DoseLog.java                  [H3] id, medicationId (FK), scheduledAt, takenAt
│   │   └── Appointment.java              [H3] id, doctorOrPlace, scheduledAt, active
│   └── repository/
│       ├── VitalsRepository.java         [H3] guarda/observa glucosa y presión; valida desde [H4]
│       ├── MedicationRepository.java     [H3] medicamentos + DoseLog
│       ├── AppointmentRepository.java    [H3]
│       └── SaveResult.java               [H3] éxito con la entidad guardada / rechazo con ValidationError
├── alarms/
│   ├── NextTriggerCalculator.java        [H5] Java puro (java.time): próximo disparo
│   ├── ReminderRequestCodes.java         [H5] Java puro: códigos de PendingIntent únicos
│   ├── ReminderPolicy.java               [H5] SNOOZE_MINUTES = 10, APPOINTMENT_LEAD_MINUTES = 120
│   ├── AlarmScheduler.java               [H5] setAlarmClock / setExactAndAllowWhileIdle; cancelar, posponer, reprogramar todo
│   ├── ReminderReceiver.java             [H5] BroadcastReceiver → ReminderNotifier
│   ├── ReminderNotifier.java             [H5] canal de alarma, CATEGORY_ALARM, Full-Screen Intent, acciones
│   ├── ReminderActionReceiver.java       [H5] acciones desde la notificación ("Ya lo tomé" / "Recordar más tarde")
│   ├── BootReceiver.java                 [H5] BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED, MY_PACKAGE_REPLACED
│   └── AlarmPermissionChecker.java       [H5] canScheduleExactAlarms, canUseFullScreenIntent, POST_NOTIFICATIONS
├── feedback/
│   ├── FeedbackBannerView.java           [H3] banner grande con icono + texto (visible 3 s)
│   ├── FeedbackTonePlayer.java           [H3] ToneGenerator → SoundPool con sonido agradable [H6]
│   ├── SaveFeedback.java                 [H3] fachada: showSuccess(msg); showError(msg) desde [H4]
│   └── FeedbackDurations.java            [H3] BANNER_VISIBLE_MS = 3000
└── ui/
    ├── common/
    │   ├── BaseScreenActivity.java       [H1] insets + botón visible "Volver"
    │   ├── Event.java                    [H3] evento de un solo consumo para LiveData
    │   ├── SpokenTimeFormatter.java      [H3] Java puro: "8:30 de la mañana", "lunes 16 de noviembre"
    │   └── ViewModelFactory.java         [H3]
    ├── home/
    │   └── MainActivity.java             [H1] dashboard plano (se mueve desde el paquete raíz)
    ├── widget/
    │   ├── StepperModel.java             [H2] Java puro: valor, límites, clamp, incremento
    │   ├── AccelerationPolicy.java       [H2] Java puro: paso 1 → 5 → 10 según repeticiones
    │   ├── RepeatTouchListener.java      [H2] pulsación continua (retraso inicial + repetición)
    │   └── NumericStepperView.java       [H2] vista compuesta: − / valor ≥ 56sp + unidad / +
    ├── glucose/
    │   ├── GlucoseEntryActivity.java     [H1 pantalla base → H2 control → H3 guardado]
    │   └── GlucoseEntryViewModel.java    [H3]
    ├── pressure/
    │   ├── PressureEntryActivity.java    [H1 → H2 → H3]
    │   └── PressureEntryViewModel.java   [H3]
    ├── agenda/                           "Mis medicamentos y citas"
    │   ├── AgendaActivity.java           [H1 pantalla base → H3 listas]
    │   ├── AgendaViewModel.java          [H3]
    │   ├── MedicationFormActivity.java   [H3]
    │   ├── AppointmentFormActivity.java  [H3]
    │   ├── MedicationAdapter.java        [H3]
    │   └── AppointmentAdapter.java       [H3]
    ├── alarm/
    │   ├── FullScreenAlarmActivity.java  [H5]
    │   └── AlarmViewModel.java           [H5]
    ├── permissions/
    │   └── PermissionsGuideActivity.java [H5] un permiso por pantalla, botón "Abrir ajustes"
    └── history/
        ├── HistoryActivity.java          [H6]
        ├── HistoryViewModel.java         [H6]
        ├── HistoryAdapter.java           [H6]
        ├── HistoryItem.java              [H6] fila unificada (azúcar / presión / toma)
        └── HistorySummaryCalculator.java [H6] Java puro: promedios, mín/máx, conteo por categoría
```

Las pruebas siguen la misma estructura de paquetes:
- `app/src/test/java/com/davidrdz/vitallog/...`: pruebas JVM (JUnit 4).
- `app/src/androidTest/java/com/davidrdz/vitallog/...`: pruebas instrumentadas (Room en memoria, Espresso).

---

## 2. Hitos

### Hito 1 — Maquetado y sistema de diseño

**Entregables**
- `themes.xml`: tema claro fijo `Theme.Material3.Light.NoActionBar`. Se **borra** `values-night/themes.xml`. Los colores del tema se mapean a `colors.xml`. Estilos `Widget.VitalLog.Button.Action` y `TextAppearance.VitalLog.Title/Body/Value` (`sans-serif` / `sans-serif-medium`).
- `colors.xml`: tokens semánticos (`text_primary` ≈ #1A1A1A, `background` #FFFFFF, `surface_action` claro sólido, `outline_strong`, `status_ok`/`status_warning`/`status_danger` oscuros). Cada par se valida con `contrast.py` (≥ 7:1).
- `dimens.xml`: `touch_target_min` 60dp, `touch_target` 64dp, `action_button_height` 120dp, `alarm_button_height` 96dp, `spacing_touch` 12dp, `screen_padding` 24dp, `text_body` 20sp, `text_button` 22sp, `text_title` 28sp, `text_value` 56sp.
- `strings.xml`: textos del dashboard y del botón "Volver".
- Iconos vectoriales: gota (glucosa), corazón (presión), pastilla/calendario (agenda) y flecha (volver). Siempre acompañados de texto.
- `activity_main.xml`: `ScrollView` con el título "¿Qué quieres hacer hoy?" y 3 botones a todo lo ancho (≥ 120dp de alto, separados ≥ 24dp): **"Registrar glucosa"**, **"Registrar presión arterial"** y **"Mis medicamentos y citas"**.
- `BaseScreenActivity` y las 3 Activities destino, con título y botón "Volver" (se completan en los hitos siguientes). `MainActivity` pasa a `ui/home/` y se actualiza el manifiesto.

**Pruebas**
- No hay lógica de negocio en este hito. La comprobación automática es `audit_ui.py` + `contrast.py`.
- Instrumentada `DashboardNavigationTest` (Espresso): los 3 botones son visibles, miden ≥ 64dp y abren su pantalla; "Volver" regresa al dashboard.
- Se eliminan `ExampleUnitTest` y `ExampleInstrumentedTest`.

**Demo**: el dashboard se ve bien con escala de fuente 1.0× y 2.0×, sin texto cortado. Cada botón abre su pantalla y "Volver" regresa.

---

### Hito 2 — Control numérico visual reutilizable

**Entregables**
- `analytics/VitalRange` y `VitalRanges`, con las constantes de CLAUDE.md §4.2 y valores típicos (glucosa 100, sistólica 120, diastólica 80). Se crean aquí porque los límites del control salen de estas constantes.
- `StepperModel` (Java puro): valor actual, `increment(step)`, `decrement(step)`, clamp a [min, max], `isAtMin()` / `isAtMax()`.
- `AccelerationPolicy` (Java puro): paso 1 en las primeras 10 repeticiones, 5 en las 10 siguientes y 10 a partir de ahí. Todas las cifras son constantes con nombre.
- `RepeatTouchListener`: al mantener presionado, espera 400 ms y luego repite cada 100 ms. Se reinicia al soltar.
- `NumericStepperView` (layout `view_numeric_stepper.xml`):
  - Botones − / + de 80dp y valor central ≥ 56sp, con la unidad debajo y un rótulo arriba ("Azúcar en la sangre").
  - Se configura desde Java con `bind(VitalRanges.GLUCOSE)`; no hay números en el XML.
  - Al llegar a un límite, el botón muestra el texto "Máximo" o "Mínimo" en color sólido (sin transparencia).
  - Accesibilidad: `accessibilityLiveRegion` para que TalkBack lea el valor, acciones de accesibilidad para subir y bajar, y `contentDescription` en los dos botones.
  - Conserva el valor al rotar la pantalla.
- Pantalla de glucosa (1 control) y pantalla de presión (2 controles: "Presión alta (número de arriba)" y "Presión baja (número de abajo)"). Todavía sin botón de guardar.

**Pruebas JVM**
- `StepperModelTest`: nunca pasa de min ni de max; el valor inicial está dentro del rango; un valor fuera de rango se ajusta al límite.
- `AccelerationPolicyTest`: el paso cambia en las repeticiones 10 y 20, y vuelve a 1 al reiniciar.
- `VitalRangesTest`: en cada rango, min < típico < max; el rango de la sistólica es compatible con el de la diastólica más la presión de pulso mínima.

**Demo**: subir y bajar con toques sueltos y manteniendo presionado (se nota la aceleración). La glucosa no baja de 20 ni sube de 600. El teclado nunca aparece.

---

### Hito 3 — Persistencia local offline + retroalimentación básica

**Entregables**
- `libs.versions.toml`: `room-runtime`, `room-compiler` (con `annotationProcessor`), `lifecycle-viewmodel`, `lifecycle-livedata`; para pruebas, `room-testing` y `androidx-test-core`. Se configura `room.schemaLocation` → `app/schemas/` y esa carpeta se versiona en git para poder escribir migraciones explícitas más adelante.
- 5 entidades y 5 DAOs (esquema versión 1). `DoseLog.medicationId` lleva clave foránea e índice. Las lecturas devuelven `LiveData`: lista ordenada por fecha descendente y última lectura (se usa como valor inicial del control).
- `AppDatabase` singleton, sin `allowMainThreadQueries()` ni `fallbackToDestructiveMigration()`. Expone un `ExecutorService` compartido para las escrituras.
- Repositorios con `save(entity, callback)` asíncrono que entrega un `SaveResult` en el hilo principal.
- ViewModels de captura: cargan la última lectura como valor inicial y exponen `LiveData<Event<SaveResult>>`.
- Botones "Guardar azúcar" y "Guardar presión" (≥ 64dp).
- `feedback/` básico: `FeedbackBannerView` (check grande + "Guardado: azúcar 120 mg/dL, hoy 8:30 de la mañana", visible 3 s) + tono con `ToneGenerator`. Así se cumple §3.4 desde el primer guardado.
- Agenda ("Mis medicamentos y citas"): lista de medicamentos y lista de citas, con los botones "Agregar medicamento" y "Agregar cita", que abren sus formularios.
  - Medicamento: el nombre se escribe con el teclado (no es un signo vital, así que §3.1 no aplica). Dosis con `NumericStepperView` + botones grandes de unidad (tableta, cápsula, ml, unidades). Hora con un stepper de hora y otro de minutos de 5 en 5, mostrando "8:30 de la mañana".
  - Cita: médico o lugar (texto), día (stepper "dentro de N días" con la fecha escrita) y hora.
  - Borrar un medicamento o una cita pide confirmación con los botones "Sí, borrar" / "No, conservar".
- `SpokenTimeFormatter` (Locale es-MX).
- Privacidad: `android:allowBackup="false"`; `backup_rules.xml` y `data_extraction_rules.xml` excluyen la base de datos tanto de `cloud-backup` como de `device-transfer`.
- Actualizar CLAUDE.md §4 (entidad `Appointment`) y §4.3 (botones del modal de cita).
- **Limitación conocida hasta el Hito 4**: el control ya impide valores fuera de rango, pero todavía se podría guardar una sistólica ≤ diastólica.

**Pruebas JVM**
- `VitalsRepositoryTest` y `MedicationRepositoryTest`, con DAOs falsos y un `Executor` síncrono: guardar llama al DAO y devuelve éxito.
- `SpokenTimeFormatterTest`: medianoche, mediodía, 8:05, "hoy" / "ayer" / día de la semana.

**Pruebas instrumentadas** (Room en memoria)
- `GlucoseReadingDaoTest` y `BloodPressureReadingDaoTest`: orden descendente y última lectura.
- `MedicationDaoTest` y `DoseLogDaoTest`: clave foránea y tomas por día.
- `AppointmentDaoTest`: solo devuelve citas futuras y activas.

**Demo**: guardar una glucosa → banner y tono. Cerrar y abrir la app → el control arranca en el último valor. Dar de alta un medicamento y una cita y verlos en la agenda. En modo avión todo funciona igual.

---

### Hito 4 — Gestor analítico de validación

**Entregables**
- `ClinicalRangeValidator` (Java puro, usa `VitalRanges`): `validateGlucose(int)` y `validateBloodPressure(int systolic, int diastolic)` devuelven un `ValidationResult` con un `ValidationError`: `GLUCOSE_TOO_LOW`, `GLUCOSE_TOO_HIGH`, `SYSTOLIC_TOO_LOW`, `SYSTOLIC_TOO_HIGH`, `DIASTOLIC_TOO_LOW`, `DIASTOLIC_TOO_HIGH`, `SYSTOLIC_NOT_ABOVE_DIASTOLIC`, `PULSE_PRESSURE_TOO_NARROW`.
- `VitalsRepository` valida **antes** de insertar: si la validación falla, no llama al DAO y devuelve `SaveResult.rejected(error)`.
- `ReadingClassifier` (informativo, no diagnóstico). Los umbrales van en constantes y se documentan como orientativos:
  - Glucosa: < 70 baja; 70–180 en rango; 181–249 alta; ≥ 250 muy alta.
  - Presión: < 90/60 baja; < 120/80 normal; 120–129 con < 80 elevada; ≥ 130 o ≥ 80 alta; ≥ 180 o ≥ 120 crítica.
- Avisos que no frustran al usuario:
  - Mientras ajusta el valor, debajo del control aparece la categoría con texto + icono + color ("Azúcar alta"), nunca solo color.
  - En rango crítico se muestra un aviso en lenguaje llano antes de guardar ("Este número es muy alto. Si te sientes mal, llama a tu médico."). El usuario **puede guardar igual**: un valor posible nunca se bloquea.
- Error de validación (p. ej. el número de arriba ≤ el de abajo): `SaveFeedback.showError` con banner + un tono grave distinto y el mensaje "El número de arriba debe ser mayor que el de abajo. Revísalos." Como apoyo, el control de presión puede limitar la sistólica a ≥ diastólica + 10 en vivo.
- La conversión de `ValidationError` a texto de `strings.xml` se hace en la capa de UI, para que el validador no dependa de Android.

**Pruebas JVM**
- `ClinicalRangeValidatorTest`:
  - Glucosa: 19 ✗ / 20 ✓ / 600 ✓ / 601 ✗.
  - Sistólica: 59 ✗ / 60 ✓ / 260 ✓ / 261 ✗.
  - Diastólica: 29 ✗ / 30 ✓ / 160 ✓ / 161 ✗.
  - Sistólica = diastólica ✗; sistólica < diastólica ✗.
  - Presión de pulso: 9 ✗ / 10 ✓.
- `ReadingClassifierTest`: cada frontera de categoría, probada por ambos lados.
- `VitalsRepositoryTest` ampliado: una lectura inválida nunca llega al DAO y devuelve el motivo correcto.

**Demo**: con la presión en 90/85 → error visual + tono grave, y no se guarda. Con glucosa 320 → aviso "muy alta", se guarda y se confirma.

---

### Hito 5 — Orquestador de alarmas y modal a pantalla completa

**Entregables**
- Manifiesto:
  - Permisos `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK` y `VIBRATE`.
  - Receivers con `exported="false"`, salvo `BootReceiver`, que necesita sus filtros del sistema.
  - `FullScreenAlarmActivity` con `showWhenLocked` y `turnScreenOn` (atributos + `setShowWhenLocked(true)` / `setTurnScreenOn(true)` en código), `launchMode="singleTask"` y `excludeFromRecents`.
- `NextTriggerCalculator` (java.time, con `ZoneId` inyectado): calcula la próxima toma diaria de un medicamento y el aviso de una cita (`scheduledAt − 120 min`; si ese momento ya pasó, no se programa).
- `AlarmScheduler`:
  - Usa `setAlarmClock` (exacto y visible en el reloj del sistema), con `setExactAndAllowWhileIdle` como respaldo.
  - Al guardar, editar o borrar un medicamento o una cita, programa o cancela su alarma.
  - Cuando se dispara una alarma diaria, programa la del día siguiente.
  - "Recordar más tarde" programa una alarma única a +10 min, con su propio código, sin alterar la diaria.
- `ReminderReceiver` → `ReminderNotifier`: canal de alarma de importancia alta (sonido `USAGE_ALARM` + vibración), `CATEGORY_ALARM`, `setFullScreenIntent` y notificación persistente con las mismas 2 acciones del modal.
- `FullScreenAlarmActivity` (`activity_full_screen_alarm.xml`): fondo claro sólido a pantalla completa, pictograma universal grande (pastilla o calendario, 160dp), nombre ≥ 32sp, dosis y hora. **Exactamente 2 botones** apilados a todo lo ancho, de ≥ 96dp de alto y separados ≥ 24dp:
  - Medicamento: **"Ya lo tomé"** registra un `DoseLog`, muestra `SaveFeedback` y cierra; **"Recordar más tarde"** pospone 10 min y cierra.
  - Cita: **"Entendido"** cierra; **"Recordar más tarde"** pospone 10 min.
  - Botón Atrás del sistema: cierra la pantalla, pero la notificación sigue activa con sus 2 acciones (no se registra nada).
- `BootReceiver`: con `goAsync()` y el executor, reprograma todas las alarmas activas tras un reinicio, un cambio de hora o de zona horaria, o una actualización de la app.
- `AlarmPermissionChecker` + `PermissionsGuideActivity`: al entrar a la agenda o al guardar el primer recordatorio, si falta algún permiso (notificaciones en API 33+, alarmas exactas, pantalla completa en API 34+), se muestra una pantalla por permiso con una explicación en lenguaje llano y el botón "Abrir ajustes".

**Pruebas JVM**
- `NextTriggerCalculatorTest`:
  - Hora futura → hoy; hora pasada → mañana; hora igual a ahora → mañana.
  - Cruce de medianoche.
  - Cambio de horario de verano (con `America/New_York`).
  - Aviso de una cita que ya pasó → no se programa.
- `ReminderRequestCodesTest`: los códigos de medicamento, cita y posposición no chocan para un mismo id.
- `ReminderPolicyTest`: la posposición es de exactamente 10 min.

**Pruebas instrumentadas**
- `FullScreenAlarmActivityTest`: se muestran exactamente 2 botones; "Ya lo tomé" crea un `DoseLog` en la base de datos en memoria.

**Demo** (emulador API 34+):
1. Crear un medicamento para dentro de 2 min.
2. Bloquear la pantalla (`adb shell input keyevent KEYCODE_SLEEP`) y esperar: la pantalla se enciende con el modal.
3. Tocar "Recordar más tarde": el modal vuelve 10 min después.
4. Tocar "Ya lo tomé": banner + tono.
5. Reiniciar el emulador y comprobar con `adb shell dumpsys alarm | grep vitallog` que la alarma sigue programada.

---

### Hito 6 — Retroalimentación multimodal pulida + historial simple

**Entregables**
- `FeedbackBannerView`: animación de entrada (escala + aparición del check, sin transparencia sobre el texto final), visible 3 s, `announceForAccessibility` con el mismo mensaje. Si el sistema tiene "quitar animaciones" activado, el banner aparece sin animación.
- `FeedbackTonePlayer` con `SoundPool`:
  - Sonido de éxito agradable (dos notas ascendentes) y sonido de error neutro (una nota grave).
  - Archivos `.ogg` cortos en `res/raw/`, sintetizados para el proyecto (sin problemas de licencia), con `AudioAttributes.USAGE_ASSISTANCE_SONIFICATION`.
  - Si `SoundPool` no carga, se usa `ToneGenerator` como respaldo.
- 4.º botón en el dashboard: **"Ver mi historial"** (la navegación sigue siendo plana).
- `HistoryActivity`, pensada para enseñarla al médico:
  - Dos botones grandes de periodo: "Últimos 7 días" / "Últimos 30 días" (son botones de filtro, no pestañas).
  - Arriba, un resumen: promedio, mínimo y máximo de azúcar y de presión, y número de tomas registradas.
  - Abajo, una lista cronológica agrupada por día ("Lunes 16 de noviembre"), con filas de ≥ 20sp: hora, tipo en palabras, valor + unidad y categoría en texto + icono.
  - Solo lectura y sin gestos.
- `HistorySummaryCalculator` (Java puro) que calcula el resumen a partir de listas de entidades.

**Pruebas JVM**
- `HistorySummaryCalculatorTest`: lista vacía → "sin datos"; promedio redondeado; mínimo y máximo; filtros de 7 y 30 días en sus fronteras.
- `SpokenTimeFormatterTest` ampliado: encabezados por día.
- `FeedbackDurationsTest`: el banner dura ≥ 2 500 ms.

**Pruebas instrumentadas**
- `HistoryActivityTest`: con datos de prueba cargados, cada fila muestra el valor, la unidad y la categoría en texto.

**Demo**: guardar una lectura → animación + sonido agradable. Provocar un error → sonido distinto. Abrir "Ver mi historial" con 30 días de datos y mostrarlo como se le mostraría al médico.

---

## 3. Riesgos y notas

- **Full-Screen Intent en API 34+**: Google restringe este permiso a apps de llamadas y alarmas. Si no se concede, la notificación aparece como heads-up con sonido de alarma, y la pantalla de permisos lleva a los ajustes.
- **Profundidad de navegación**: los formularios para agregar un medicamento o una cita son un paso de la tarea que siempre regresa a la agenda (con "Volver" o al guardar). No cuentan como una sección de navegación nueva.
- **Fabricantes con ahorro de batería agresivo**: queda fuera de alcance; solo se usa `setAlarmClock`.
- **Fuera de alcance**: exportar a PDF, borrar lecturas desde el historial, red o sincronización (prohibidas por CLAUDE.md) y el dial rotativo.
