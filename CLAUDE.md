# CLAUDE.md — VitalLog

Fuente de verdad del proyecto. Toda decisión de código, UI o arquitectura debe respetar este documento. Si una petición lo contradice, se señala el conflicto antes de implementar.

## 1. Contexto del producto

**VitalLog** es una app Android nativa para **adultos mayores con diabetes tipo 2 e hipertensión**. Permite registrar glucosa y presión arterial, y recordar la toma de medicamentos. El diseño se rige por **accesibilidad gerontológica**: baja agudeza visual, temblor o menor precisión motora, menor familiaridad con interfaces complejas.

Principio rector: **si un adulto mayor puede equivocarse con un control, el control está mal diseñado.**

## 2. Stack técnico

| Aspecto | Decisión |
|---|---|
| Lenguaje | **Java** (sin Kotlin en código de la app). Java 11 (`compileOptions`). |
| IDE / Build | Android Studio, Gradle Kotlin DSL (`*.gradle.kts`), catálogo de versiones en `gradle/libs.versions.toml`. |
| SDK | `minSdk 26`, `targetSdk 37`, paquete `com.davidrdz.vitallog`. |
| UI | Vistas XML + AppCompat + Material Components. Sin Compose. |
| Persistencia | **Room** sobre SQLite, offline-first. Anotaciones con `annotationProcessor` (no kapt/ksp). |
| Concurrencia | `ExecutorService` para escrituras/lecturas; `LiveData` para observar datos en UI. |
| Alarmas | `AlarmManager.setExactAndAllowWhileIdle()` / `setAlarmClock()` + notificación con **Full-Screen Intent**. |
| Red | **Ninguna.** Sin servidores externos, sin analítica, sin HL7/FHIR, sin permiso `INTERNET`. |

Dependencias nuevas se agregan siempre vía `libs.versions.toml`, nunca con coordenadas literales en `app/build.gradle.kts`.

## 3. Reglas estrictas de UI/UX (cero excepciones)

### 3.1 Captura de datos sin teclado
- **PROHIBIDO** usar `EditText` (o cualquier control que abra el teclado virtual) para capturar signos vitales.
- Usar **exclusivamente** controles numéricos visuales:
  - Botones **+ / −** de gran tamaño con el valor centrado en tipografía grande, y/o
  - Diales rotativos / `NumberPicker` estilizado con valores grandes.
- Mantener pulsado +/− acelera el incremento (p. ej. 1 → 5 → 10) para evitar cientos de toques.
- Cada control inicia en un **valor por defecto razonable** (último valor registrado o un valor típico), no en 0.
- Los límites del control coinciden con los rangos del Gestor Analítico (§4.2): es imposible seleccionar un valor fuera de rango.

### 3.2 Navegación 100% plana
- **Sin** menús hamburguesa, `NavigationDrawer`, menús de desbordamiento (⋮), submenús ni pestañas anidadas.
- Pantalla de inicio con botones grandes que llevan directamente a cada acción (máximo 1 nivel de profundidad).
- Toda pantalla secundaria tiene un botón visible **"Volver"** con texto, además del gesto del sistema.
- Sin gestos ocultos (swipe para borrar, long-press para opciones). Toda acción es un botón visible con texto.

### 3.3 Tipografía, contraste y tamaño táctil
- Fuente **sans-serif** del sistema (`sans-serif` / `sans-serif-medium`).
- Tamaños mínimos: **cuerpo 18–20sp**, **títulos ≥ 24sp**, valores numéricos de captura ≥ 48sp.
- Siempre `sp` para texto (respeta la escala de fuente del sistema); el layout debe funcionar con escala 1.3×–2.0×.
- Contraste **WCAG AAA: ≥ 7:1** para todo texto e iconos informativos, **oscuro sobre fondo claro sólido** (tema claro fijo, sin `DayNight`), sin transparencias ni degradados detrás de texto. Colores definidos en `res/values/colors.xml`; no usar colores literales en layouts.
- No transmitir información solo con color (acompañar con texto o icono).
- Áreas táctiles **mínimo 60dp × 60dp, ideal ≥ 64dp**, con separación ≥ 12dp entre objetivos.

### 3.6 Auditoría obligatoria
Al crear o modificar layouts, estilos, temas, drawables o vistas Java de UI, aplicar la skill `.claude/skills/gerontological-ui-auditor/` (script `audit_ui.py` + revisión manual) antes de dar la pantalla por terminada.
- Iconos siempre acompañados de etiqueta de texto; todo control con `contentDescription` para TalkBack.

### 3.4 Retroalimentación obligatoria al guardar
Todo guardado exitoso dispara **ambos**:
1. **Visual:** banner/animación clara a pantalla (p. ej. check grande + "Guardado correctamente"), visible ≥ 2–3 s. No usar `Toast` como único feedback.
2. **Auditivo:** tono corto mediante `ToneGenerator` o `SoundPool`.

Los errores de validación también tienen feedback visual + auditivo (tono distinto) con un mensaje en lenguaje llano, sin jerga técnica.

### 3.5 Lenguaje
- Textos de UI en **español**, en `res/values/strings.xml` (nunca literales en código o XML).
- Frases cortas, en segunda persona, sin abreviaturas ambiguas (escribir "Presión arterial", no "PA").

## 4. Arquitectura de módulos

Patrón por capas: **UI (Activity/Fragment + ViewModel) → Repositorio → (Gestor Analítico) → Room**.

```
com.davidrdz.vitallog
├── data/            # Capa de Persistencia Local
│   ├── db/          #   AppDatabase, DAOs
│   ├── entity/      #   GlucoseReading, BloodPressureReading, Medication, DoseLog
│   └── repository/  #   Repositorios (único punto de acceso a datos desde UI)
├── analytics/       # Gestor de Procesamiento Analítico (validación de rangos)
├── alarms/          # Orquestador de Tareas y Alarmas
├── ui/              # Activities, ViewModels, controles numéricos personalizados
└── feedback/        # Retroalimentación visual/auditiva reutilizable
```

### 4.1 Capa de Persistencia Local (Room)
- Una sola `AppDatabase` (singleton). Entidades con timestamp (`long` epoch millis).
- **Nunca** `allowMainThreadQueries()`. Escrituras en un `ExecutorService` compartido; lecturas observables vía `LiveData`.
- Las migraciones de esquema se escriben explícitamente; no usar `fallbackToDestructiveMigration()` (perdería datos clínicos del usuario).
- La UI nunca accede a DAOs directamente: siempre a través del repositorio.

### 4.2 Gestor de Procesamiento Analítico
Valida **antes de persistir**; el repositorio rechaza cualquier lectura que no pase la validación. Lógica en Java puro (sin dependencias de Android) para poder probarla con JUnit.

Rangos biológicamente posibles (límites de captura):

| Medida | Mínimo | Máximo | Regla adicional |
|---|---|---|---|
| Glucosa (mg/dL) | 20 | 600 | — |
| Presión sistólica (mmHg) | 60 | 260 | sistólica > diastólica |
| Presión diastólica (mmHg) | 30 | 160 | diferencia (presión de pulso) ≥ 10 |

- Valores fuera de rango → rechazo con mensaje claro, no se guardan.
- Opcionalmente clasifica lecturas válidas (normal / elevada / alta) para mostrarlas con texto + color; esta clasificación es informativa, no diagnóstica.
- Los rangos viven en constantes de una sola clase (p. ej. `VitalRanges`) y los reutilizan tanto el validador como los controles de UI.

### 4.3 Orquestador de Tareas y Alarmas
- Programa recordatorios con `AlarmManager` **exacto** (`setExactAndAllowWhileIdle` o `setAlarmClock`).
- Al dispararse: `BroadcastReceiver` → notificación de alta prioridad (categoría `CATEGORY_ALARM`) con **Full-Screen Intent** → `Activity` modal que enciende y desbloquea la pantalla (`setShowWhenLocked(true)`, `setTurnScreenOn(true)`).
- La Activity modal muestra el medicamento/dosis y **exactamente 2 botones binarios** grandes:
  - **"Ya lo tomé"** → registra la toma (`DoseLog`) con feedback visual + auditivo y cierra.
  - **"Recordar más tarde"** → reprograma la alarma (posponer, p. ej. 10 min) y cierra.
- Reprogramar todas las alarmas tras reinicio (`RECEIVE_BOOT_COMPLETED`) y cambio de hora/zona.
- Permisos requeridos: `SCHEDULE_EXACT_ALARM` (o `USE_EXACT_ALARM`), `USE_FULL_SCREEN_INTENT`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`. Verificar en tiempo de ejecución `AlarmManager.canScheduleExactAlarms()` y `NotificationManager.canUseFullScreenIntent()` (API 34+), guiando al usuario con una pantalla simple si faltan.

## 5. Comandos

```bash
./gradlew assembleDebug          # Compilar APK de depuración
./gradlew installDebug           # Instalar en dispositivo/emulador conectado
./gradlew test                   # Pruebas unitarias (JVM)
./gradlew connectedAndroidTest   # Pruebas instrumentadas (requiere dispositivo)
./gradlew lint                   # Análisis estático de Android Lint
./gradlew clean                  # Limpiar artefactos de compilación
```

Antes de dar una tarea por terminada: `./gradlew assembleDebug` y `./gradlew test` deben pasar.

## 6. Convenciones de código limpio (Java)

- **Nombres:** clases `PascalCase`, métodos/variables `camelCase`, constantes `UPPER_SNAKE_CASE`. Identificadores en inglés; textos de UI en español vía recursos.
- **Layouts/recursos:** `activity_*.xml`, `item_*.xml`, `view_*.xml`; IDs `btn_*`, `txt_*`, etc.; dimensiones en `dimens.xml` (p. ej. `touch_target_min = 64dp`).
- Clases pequeñas con una sola responsabilidad; métodos cortos; sin lógica de negocio en Activities.
- Inmutabilidad por defecto (`final` en campos y parámetros cuando aplique).
- Sin números mágicos: rangos, duraciones de posposición y tamaños en constantes o recursos.
- Manejo explícito de errores; nunca capturar `Exception` genérica en silencio.
- Nada de código muerto, `TODO` sin contexto ni `Log.d` con datos de salud en builds de release.
- **Pruebas:** toda regla del Gestor Analítico tiene pruebas JUnit en `app/src/test/` (límites inferiores, superiores, sistólica ≤ diastólica, etc.).
- **Privacidad:** los datos de salud nunca salen del dispositivo. Revisar `backup_rules.xml` / `data_extraction_rules.xml` al añadir la base de datos.
