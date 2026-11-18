# VitalLog — Dirección visual: "Sobremesa"
Define la APARIENCIA. Lo inviolable está en CLAUDE.md §3; si chocan, gana §3 (o la regla más estricta
de la skill `gerontological-ui-auditor`). Se implementa en los Hitos 7–9 de `docs/IMPLEMENTATION_PLAN.md`.

## Intención
La mesa de la cocina a media mañana: luz cálida, cosas de casa, alguien que te saluda. Calma y
confianza, no "aparato médico". Lo de todos los días está arriba y es grande; lo demás queda a un toque, sin esconderse.

**Lo memorable (una sola cosa):** el saludo según la hora, con pictograma y la fecha en palabras.
Sirve para algo: ubica a la persona en el día. Todo lo demás es tranquilo y disciplinado.

## Libertad de diseño
| Se puede cambiar | NO se puede cambiar |
|---|---|
| Matiz de colores (hue) y paleta de marca | Contraste ≥ 7:1, oscuro sobre claro sólido, tema claro fijo |
| Jerarquía entre acciones (tamaño, orden, peso) | Táctiles ≥ 60dp, separación ≥ 12dp, +/− en vez de teclado |
| Forma (radios, grosor de borde), iconografía | Navegación plana, botón "Volver" con texto |
| Pesos y escala tipográfica (sp) | Cuerpo ≥ 18sp, valores ≥ 48sp, escala 1.3×–2.0× sin cortes |
| Ilustración / pictogramas | Sin gradientes, alfa ni animaciones decorativas; info nunca solo por color |

## Decisiones y choques resueltos
- **Tipografía del sistema** (`sans-serif` en pesos normal, negrita 700 y 900), por §3.3. Los pesos se piden
  con `textStyle=bold` + `textFontWeight` (700 o 900), **no** con `sans-serif-medium` ni `sans-serif-black`.
  En el dispositivo del usuario `sans-serif-black` cayó al peso normal (Hito 8, etapa A), y el medium nunca
  se aplicó (Hito 9): los padres `TextAppearance.Material3.*` traen `app:fontFamily="sans-serif"`, que en
  AppCompat manda sobre `android:fontFamily`. Cada estilo fija `fontFamily` y `android:fontFamily`.
  Se descartó empaquetar Atkinson Hyperlegible: el usuario prefirió conservar §3.3 y la fuente que eligió en su teléfono.
- **Los colores de acción evitan verde, ámbar y rojo**, que son exclusivos de los estados
  (`status_ok`, `status_warning`, `status_danger`). Por eso la glucosa va en cobalto y la presión en
  ciruela, no en miel ni rosa. La calidez sale del fondo, de la tinta café y del arena.
  **Dos excepciones, siempre con icono y texto:** confirmar en verde ("Guardar …", "Ya lo tomé"/"Entendido")
  y borrar en rojo ("Borrar", "Sí, borrar"), porque es la única acción destructiva (aprobado en el Hito 9).
- **Sin cliché de "crema + serif + terracota":** el fondo #FFFAF3 es casi blanco, la letra es sans y no hay terracota.
- **Sin sombras, degradados ni transparencias:** elevación 0; las superficies se separan con borde sólido.
- **Nada de "claro sobre oscuro":** ningún botón relleno oscuro. La jerarquía sale del tamaño, el radio, el relleno contra contorno y el matiz.
- **Alternativas descartadas:** A "Sereno" (un solo petróleo; demasiado parecida a lo actual, sigue
  fría) y C "Pastillero" (tapas de color por sección; reintroducía 4 colores).

## Tokens de color
Los nombres son semánticos. Se **conservan** los nombres existentes cuando el rol no cambia (solo
cambia el hex) para no tocar código que ya los usa; los `action_*` se reemplazan por `vital_*` y `surface_sand`.

| Token (`colors.xml`) | Hex | Rol | Par probado | Ratio |
|---|---|---|---|---|
| `background` | #FFFAF3 | fondo de pantalla (blanco cálido) | — | — |
| `text_primary` | #2A1E14 | texto principal (café casi negro) | sobre `background` | 15.62 |
| `text_secondary` | #4A3B2E | texto secundario, unidades | sobre `background` / `surface_sand` | 10.34 / 9.15 |
| `outline_strong` | #2A1E14 | bordes neutros, campos de texto | sobre `background` | 15.62 |
| `brand_primary` | #5A3A22 | cacao: borde e icono de acciones generales (Agregar, Abrir ajustes, Recordar más tarde, opción elegida) | sobre `surface_sand` / `background` | 8.68 / 9.80 |
| `brand_primary_container` | #F3ECE2 | = `surface_sand` (alias) | — | — |
| `surface_sand` | #F3ECE2 | relleno de botones secundarios y "Volver" | `text_primary` encima | 13.84 |
| `vital_glucose` | #1E3A8A | cobalto: icono, borde y plato de glucosa | sobre `vital_glucose_container` / `background` | 8.46 / 9.97 |
| `vital_glucose_container` | #E2E8FA | relleno de la acción y los steppers de glucosa | `text_primary` / `text_secondary` encima | 13.26 / 8.77 |
| `vital_pressure` | #6B1E5A | ciruela: icono, borde y plato de presión | sobre `vital_pressure_container` / `background` | 8.78 / 10.32 |
| `vital_pressure_container` | #F4E4F0 | relleno de la acción y los steppers de presión | `text_primary` / `text_secondary` encima | 13.29 / 8.79 |
| `status_ok` | #0B5A23 | sin cambio | sobre `background` / `surface_success` | 8.09 / 7.31 |
| `status_warning` | #6B3E00 | sin cambio | sobre `background` | 8.74 |
| `status_danger` | #8A0F0F | sin cambio | sobre `background` / `surface_error` | 9.38 / 8.30 |
| `surface_success` | #E4F3E8 | sin cambio | `text_primary` encima | 14.13 |
| `surface_error` | #FDE8E8 | sin cambio | `text_primary` encima | 13.82 |
| `surface_neutral` | → se elimina | lo sustituye `surface_sand` | — | — |

| `surface_field` | #FFFFFF | relleno de los campos de texto (Hito 9) | `text_primary` / `text_secondary` / `brand_primary` encima | 16.23 / 10.74 / 10.18 |

Los campos de texto (nombre del medicamento, médico o lugar) van en `surface_field` con borde
`outline_strong` de 3dp. Mientras se escribe, el borde pasa a `brand_primary` de 5dp (`bg_text_field`):
el foco se nota por el grosor, no solo por el color. Los alias `action_*_fg` y `surface_neutral` ya no existen.

El margen más estrecho es `status_ok`/`surface_success` (7.31). No se toca ese par sin volver a medirlo.

## Tipografía (solo fuentes del sistema, siempre en sp)
| Estilo | Familia | Tamaño | Uso |
|---|---|---|---|
| `TextAppearance.VitalLog.Greeting` | `sans-serif`, peso 900 (negrita en API 26–27) | 32sp | saludo del dashboard |
| `TextAppearance.VitalLog.Title` | `sans-serif`, negrita 700 | 28sp | título de pantalla y de sección |
| `TextAppearance.VitalLog.Label` | `sans-serif`, negrita 700 | 24sp | etiquetas, nombre en tarjetas, fecha del saludo |
| botones (`Widget.VitalLog.Button` y derivados) | `sans-serif`, negrita 700 | 26sp (22sp en Volver, Borrar y opciones) | texto de acción |
| `TextAppearance.VitalLog.AlarmName` | `sans-serif`, peso 900 | 34sp | nombre del medicamento o del médico en el modal de alarma |
| `TextAppearance.VitalLog.Error` | `sans-serif`, negrita 700, `status_danger` | 20sp | error junto a un campo |
| `TextAppearance.VitalLog.Body` | `sans-serif` | 20sp, interlineado 1.25 | cuerpo |
| `TextAppearance.VitalLog.Value` | `sans-serif`, peso 900 (negrita en API 26–27) | 60sp | número del stepper |

- Peso 900 **solo en ≥ 32sp**: a menor tamaño cierra los huecos de las letras.
- Sin mayúsculas sostenidas (`textAllCaps=false`), sin `letterSpacing` (cada estilo fija 0; `BodyLarge` de
  Material 3 traía 0.03125), sin cursivas.
- Texto alineado al inicio (izquierda). Solo se centra el número del stepper.

## Forma, borde e iconografía
| Elemento | Alto mín. | Radio | Relleno | Borde |
|---|---|---|---|---|
| Acción primaria (dashboard) | 136dp | 28dp ("piedra") | `vital_*_container` | 3dp `vital_*` |
| Plato del icono (dentro de la primaria) | 64dp, círculo | — | `background` | 3dp `vital_*`, icono 36dp `vital_*` (un solo vector `ic_plate_*`, `iconTint` @null) |
| Acción secundaria (dashboard) | 88dp | 16dp | `surface_sand` | 3dp `brand_primary` (cacao), icono 32dp |
| Guardar | 88dp | 16dp | `surface_success` | 3dp `status_ok` (sin cambio) |
| Subir / Bajar (stepper) | 80dp | píldora (40dp) | contenedor del signo vital (`stepperAccent`); en el límite, `background` | 3dp acento; en el límite, `outline_strong` |
| Volver | 64dp | 16dp | `surface_sand` | 3dp `outline_strong` |
| Modal de alarma (2 botones, fijos al pie) | 96dp | 20dp | Confirmar: `surface_success`; Más tarde: `surface_sand` | 3dp |
| Plato de pictograma general (`bg_plate_brand`) | 128dp en el modal, 96dp en permisos, círculo | — | `surface_sand` | 3dp `brand_primary`, icono cacao de 88 / 64dp |
| Acción general (Agregar, Abrir ajustes, "No, conservar") | 88dp | 16dp | `surface_sand` | 3dp `brand_primary` |
| Borrar (tarjeta) / "Sí, borrar" (diálogo) | 64dp / 88dp | 16dp | `surface_sand` / `background` | 3dp `status_danger` + bote de basura |
| Tarjeta (`bg_outlined`: agenda, historial, estado) | — | 16dp | `background` | 3dp `outline_strong`; no se toca |
| Campo de texto (`bg_text_field`) | 64dp | 16dp | `surface_field` | 3dp `outline_strong`; al escribir, 5dp `brand_primary` |
| Diálogo (`ThemeOverlay.VitalLog.Dialog`) | — | 20dp | `background` | sin tinte de elevación |

- Cada nivel tiene su radio (28 / 20 / 16 / píldora): no hay un mismo radio para todo.
- Iconos de trazo grueso y terminaciones redondeadas, siempre con texto al lado. Los iconos de
  glucosa y presión van dentro de su plato **solo** en las acciones primarias.
- Pictogramas nuevos para el saludo: `ic_greeting_morning` (sol naciente), `ic_greeting_afternoon`
  (sol alto) y `ic_greeting_night` (luna), en `text_primary`, de 48dp. El texto del saludo ya dice lo
  mismo, así que el pictograma es `importantForAccessibility="no"`; aun así cumple 15.62:1.
- Separación entre botones: 16dp entre primarias, 24dp entre el bloque primario y el secundario, 16dp entre secundarias.

## Jerarquía de pantallas

### Dashboard
```
┌────────────────────────────────┐
│ (sol)  Buenos días             │ Greeting 32sp peso 900 · text_primary
│        Martes 17 de noviembre  │ Label · text_secondary
│                                │ 24dp
│ ╭────────────────────────────╮ │ vital_glucose_container · radio 28 · 136dp
│ │ ╭────╮                     │ │ borde 3dp vital_glucose
│ │ │gota│  Registrar glucosa  │ │ plato 64dp (background + borde cobalto)
│ │ ╰────╯                     │ │
│ ╰────────────────────────────╯ │ 16dp
│ ╭────────────────────────────╮ │ vital_pressure_container
│ │ ╭────╮  Registrar presión  │ │
│ │ │ ♥  │  arterial           │ │
│ │ ╰────╯                     │ │
│ ╰────────────────────────────╯ │ 24dp
│ ┌────────────────────────────┐ │ surface_sand + borde cacao · radio 16 · 88dp
│ │ [pastilla] Mis medicamentos│ │
│ │            y citas         │ │
│ └────────────────────────────┘ │ 16dp
│ ┌────────────────────────────┐ │
│ │ [reloj] Ver mi historial   │ │
│ └────────────────────────────┘ │
└────────────────────────────────┘
```
- Saludo: "Buenos días" de 5:00 a 11:59, "Buenas tardes" de 12:00 a 18:59 y "Buenas noches" de 19:00 a 4:59.
  La lógica `DayPeriod` es Java puro, con límites probados. Solo cambian el texto y el icono; la posición nunca cambia.
- La fecha va sin "Hoy," delante (el saludo ya lo implica): "Martes 17 de noviembre".
- A 2.0× el texto de las primarias ocupa 2–3 líneas junto al plato: `wrap_content` + `minHeight`,
  sin `maxLines`, plato centrado en vertical, todo dentro del `ScrollView`.
- **Ajuste del Hito 8 por ancho a 2.0×:** el plato bajó de 72 a 64dp; los botones del dashboard usan
  rellenos de 16dp y la pantalla de inicio, margen lateral de 16dp (las demás conservan 24dp). En un
  teléfono de 360dp esto deja ≈ 216dp al texto de las primarias y ≈ 252dp al de las secundarias, para
  que "Registrar" y "medicamentos" no se partan a la mitad. Se confirma con capturas a 2.0×.

### Captura de glucosa (la de presión es igual, con ciruela)
```
┌────────────────────────────────┐
│ [← Volver]                     │ surface_sand · 64dp
│ (gota) Registrar glucosa       │ Title + plato 48dp (el del inicio)
│                                │
│ Azúcar en la sangre            │ Label
│ ╭────────────────────────────╮ │ píldora · vital_glucose_container · 80dp
│ │         +  Subir           │ │
│ ╰────────────────────────────╯ │
│              118               │ Value 60sp peso 900 · text_primary
│             mg/dL              │ Body · text_secondary
│ ╭────────────────────────────╮ │
│ │         −  Bajar           │ │
│ ╰────────────────────────────╯ │
│ [estado: icono + "En rango"]   │ ReadingStatusView (sin cambio de lógica)
│ ┌────────────────────────────┐ │ surface_success + borde status_ok · 88dp
│ │ ✓  Guardar glucosa         │ │
│ └────────────────────────────┘ │
└────────────────────────────────┘
```
- El acento del signo vital acompaña a la persona desde el inicio hasta su pantalla: el icono del título y los steppers.
  Es solo un refuerzo; el título con texto es lo que ubica.
- Subir/Bajar toman el acento con `app:stepperAccent` (`glucose`, `pressure`; `neutral` = arena + cacao
  para hora, dosis y fecha). En el límite cambian a `background` (blanco cálido) + borde `outline_strong`,
  sin signo y con el texto "Máximo" o "Mínimo": se distingue por relleno, signo y texto, no solo por color.
- El plato junto al título lo pone `BaseScreenActivity.getScreenIconRes()` (decorativo para TalkBack).

## Desviaciones del Hito 9 (resto de pantallas)
- **Modal de alarma con los botones fijos al pie:** con fuente 2.0× quedaban bajo el pliegue y había que
  desplazarse para encontrar "Ya lo tomé". Ahora solo se desplaza la información. Costo: en horizontal con
  fuente 2.0× queda poco espacio para la información (pendiente, no se hizo `layout-land`).
- **Modal alineado al inicio y con plato:** antes estaba centrado. Así sigue la regla "solo se centra el número
  del stepper" y reutiliza el lenguaje de platos del inicio, pero en cacao. El nombre usa peso 900 porque es
  lo primero que hay que leer y mide ≥ 32sp.
- **Iconos de la agenda en el encabezado de cada sección**, no en cada tarjeta: el nombre gana ancho a 2.0×
  y no se repite el mismo icono en cada fila.
- **Agrupación por espacio:** `spacing_section` (40dp) entre grupos de una pantalla y 12–24dp dentro de cada grupo.
- **Negrita 700** en títulos, etiquetas y botones en lugar de medium (ver "Decisiones").
- **Rojo en "Borrar"**: excepción explícita a la regla de colores de acción (ver "Decisiones").
- **Tinte de elevación apagado** en todo el tema (`elevationOverlayEnabled=false`), coherente con "sin sombras ni tintes".

## Anti-patrones (no hacer)
- Cuatro botones iguales de colores pastel; tarjetas idénticas con el mismo radio.
- Verde, ámbar o rojo como color de una acción (son exclusivos del estado de una lectura). Únicas excepciones:
  verde para confirmar y rojo para borrar, siempre con icono y texto.
- Mayúsculas en etiquetas, letras espaciadas, rótulos encima de cada bloque, "→" pegado al texto.
- Sombras, degradados, transparencias o animación decorativa. Solo se conserva la animación del banner de guardado (Hito 6).
- Texto secundario gris claro o "apagado" para restarle importancia: lo secundario se marca con tamaño, nunca con contraste.
- Peso 900 por debajo de 32sp; `sans-serif-black` o `sans-serif-medium` (no se aplican de forma fiable); `maxLines` o `ellipsize`.
- Colores hex literales en layouts o Java; todo sale de `colors.xml`.

## Criterios de aceptación
- `audit_ui.py` sin ERROR y `./gradlew assembleDebug test lint` en verde.
- `contrast.py` ≥ 7:1 y oscuro sobre claro en cada par de la tabla de tokens (y en cualquier par nuevo).
- Revisión estática de escala de fuente: sin alturas fijas en contenedores de texto, `ScrollView` donde haga falta, sin `maxLines`/`ellipsize`.
- Capturas en dispositivo a 1.0×, 1.3× y 2.0×: las hace y revisa el usuario (Claude no usa emulador).
