---
name: gerontological-ui-auditor
description: Auditoría obligatoria de accesibilidad gerontológica para VitalLog. Úsala SIEMPRE que crees o modifiques layouts XML (res/layout*/), estilos o temas (styles.xml, themes.xml, colors.xml, dimens.xml, res/values*/), drawables, o vistas/controles personalizados en Java (clases que extienden View, ViewGroup, Button, Activity, Dialog o que construyen UI por código). También antes de dar por terminada cualquier pantalla, diálogo o Activity modal. Verifica tamaño táctil ≥60dp, separación ≥12dp, alto contraste oscuro-sobre-claro, ausencia de EditText para signos vitales, lenguaje llano y confirmación explícita en guardado y borrado.
---

# Auditor de UI gerontológica — VitalLog

Usuarios: adultos mayores con diabetes tipo 2 e hipertensión (baja visión, temblor, poca experiencia digital). Esta skill **complementa** `CLAUDE.md` §3; si hay discrepancia, gana el valor **más estricto**.

**Una pantalla NO está terminada hasta que esta auditoría pasa sin errores.** No se reporta como completa una tarea de UI con hallazgos pendientes: se corrigen primero, o se explica explícitamente al usuario por qué no se pudo.

## Procedimiento

1. **Identifica el alcance:** todos los XML de `res/` y clases Java de UI creados o modificados en la tarea (`git status` / `git diff --name-only`), más los recursos que referencian (`@dimen`, `@color`, `@style`, `@drawable`).
2. **Ejecuta el auditor estático** sobre esos archivos (o sobre todo `res/` si no estás seguro):
   ```bash
   python3 .claude/skills/gerontological-ui-auditor/scripts/audit_ui.py app/src/main/res app/src/main/java
   ```
   Detecta errores mecánicos (tamaños, `EditText`, transparencias, `textSize`, degradados). Sale con código 1 si hay `ERROR`.
3. **Verifica el contraste** de cada par texto/fondo nuevo o modificado:
   ```bash
   python3 .claude/skills/gerontological-ui-auditor/scripts/contrast.py @color/text_primary @color/background
   python3 .claude/skills/gerontological-ui-auditor/scripts/contrast.py "#1A1A1A" "#FFFFFF"
   ```
4. **Revisión manual** con la lista de comprobación de abajo: el script no ve la jerarquía visual real, el texto de los diálogos ni el flujo de confirmación.
5. **Corrige** todo hallazgo y vuelve a ejecutar los pasos 2–4 hasta que no queden errores.
6. **Reporta** al final de la tarea una tabla breve: regla → estado (✅ / corregido / ⚠️ pendiente con motivo).

## Regla 1 — Tamaño táctil y separación

- Todo control interactivo (botones, `ImageButton`, `MaterialButton`, FAB, chips, checkboxes, radios, switches, steppers +/−, diales, filas clicables, cualquier vista con `onClick`/`clickable`) mide **≥ 60dp de alto Y ≥ 60dp de ancho**.
  - Valores fijos `< 60dp` → corregir.
  - `wrap_content` sin `android:minHeight`/`android:minWidth` ≥ 60dp → corregir (los botones Material miden 48dp por defecto).
  - Usar `@dimen/touch_target_min` (60dp) / `@dimen/touch_target` (64dp+) en `res/values/dimens.xml`, no literales.
- **Separación ≥ 12dp** entre los bordes de dos elementos táctiles adyacentes (vía `layout_margin`, `spacing` de chips, `itemSpacing`, o `Space`). Una separación provista por el padre (padding/divider) cuenta solo si deja ≥ 12dp efectivos.
- Botones binarios críticos (p. ej. "Ya lo tomé" / "Recordar más tarde") se apilan o separan lo suficiente para que un toque impreciso no active el otro; preferir ancho completo y ≥ 72dp de alto.
- En vistas personalizadas Java: `onMeasure` respeta el mínimo de 60dp convertido con `TypedValue.applyDimension(COMPLEX_UNIT_DIP, …)`; si el área visual es menor, ampliar el área táctil con `TouchDelegate`.

## Regla 2 — Contraste alto, oscuro sobre claro

- Texto **oscuro sobre fondo claro sólido**. Relación de contraste **≥ 7:1 (WCAG AAA)** para todo texto e iconos informativos, comprobada con `contrast.py`.
- **Prohibido:** colores con canal alfa (`#80xxxxxx`, `#Axxx`), `android:alpha` < 1 en vistas con texto o iconos, `setAlpha()` para "atenuar" texto, texto sobre imágenes, `<gradient>` u otros fondos degradados detrás de texto, sombras de texto como sustituto de contraste.
- Estados deshabilitados: en lugar de bajar opacidad, usar un color sólido que siga cumpliendo ≥ 7:1 más una etiqueta textual ("No disponible").
- **Tema:** debe ser un tema claro fijo (p. ej. `Theme.Material3.Light.NoActionBar`), **no** `DayNight`. Si al tocar temas encuentras `DayNight` o un `values-night/themes.xml` que invierte colores, corrígelo (tema claro y sin variante nocturna que rompa la regla).
- Colores siempre desde `@color/…` en `res/values/colors.xml`; nunca hex literales en layouts ni `Color.parseColor` en Java.
- No transmitir información solo con color: rojo/verde siempre acompañado de texto ("Alta", "Normal") o icono con etiqueta.

## Regla 3 — Cero `EditText` para glucosa o presión arterial

- **Prohibido** `EditText`, `TextInputEditText`, `AppCompatEditText`, `AutoCompleteTextView` o cualquier vista que abra el teclado (`InputType.TYPE_CLASS_NUMBER`, `inputType="number…"`, `showSoftInput`) para capturar glucosa, presión sistólica/diastólica o cualquier signo vital.
- Sustituir por el control numérico visual del proyecto: botones **+ / −** grandes con valor central ≥ 48sp, o dial/`NumberPicker` estilizado, con límites tomados de `VitalRanges` (CLAUDE.md §4.2) y valor inicial razonable (último registro), no 0.
- Si aparece cualquier `EditText` en otra parte, confirmar que no captura un signo vital; aun así, evitarlo si hay alternativa sin teclado.

## Regla 4 — Lenguaje directo y confirmación explícita

### Lenguaje
- Todo texto en `res/values/strings.xml`, en español, frases cortas, segunda persona, botones con **verbo de acción** ("Guardar presión", no "Aceptar"/"OK").
- Sin tecnicismos innecesarios. Sustituciones de referencia:

  | Evitar | Usar |
  |---|---|
  | Glucemia / glicemia | Azúcar en la sangre (glucosa) |
  | Presión sistólica | Presión alta (número de arriba) |
  | Presión diastólica | Presión baja (número de abajo) |
  | Hipoglucemia / hiperglucemia | Azúcar baja / azúcar alta |
  | Preprandial / posprandial | En ayunas / después de comer |
  | PA, TA, FC (abreviaturas) | Presión arterial, pulso |
  | "Error de validación", "valor inválido" | "Ese número no parece correcto. Revísalo." |
  | Eliminar registro | Borrar esta medición |

- Unidades (`mg/dL`, `mmHg`) se muestran junto al número, pero nunca como único rótulo.
- Mensajes de error dicen **qué pasó y qué hacer**, sin culpar al usuario.

### Confirmación explícita
- **Acciones destructivas** (borrar medición, borrar medicamento, cancelar recordatorio): diálogo de confirmación obligatorio antes de ejecutar.
  - Título como pregunta concreta: "¿Borrar la medición de azúcar de hoy a las 8:30?"
  - Dos botones grandes con texto explícito: **"Sí, borrar"** / **"No, conservar"** (nunca "OK/Cancelar"); la opción segura es la predeterminada y no está junto a la destructiva sin separación ≥ 12dp.
  - Sin acciones destructivas por gestos (swipe, long-press).
- **Guardado:** toda acción de guardar muestra una confirmación explícita de **qué** se guardó ("Guardado: azúcar 120 mg/dL, hoy 8:30") con banner/animación visible ≥ 2–3 s **y** tono (`ToneGenerator`/`SoundPool`), según CLAUDE.md §3.4. Un `Toast` solo no cuenta.
- **Salir con cambios sin guardar:** preguntar "¿Salir sin guardar?" con "Sí, salir" / "No, seguir aquí".

## Lista de comprobación final

- [ ] `audit_ui.py` sin `ERROR`; cada `WARN` revisado y resuelto o justificado.
- [ ] Todos los controles táctiles ≥ 60×60dp; separación ≥ 12dp.
- [ ] Todo par texto/fondo ≥ 7:1, oscuro sobre claro, sin alfa ni degradados; tema claro fijo.
- [ ] Ningún `EditText`/teclado para signos vitales.
- [ ] Textos en `strings.xml`, lenguaje llano, botones con verbo.
- [ ] Borrado con diálogo "Sí, borrar / No, conservar"; guardado con confirmación visual + auditiva que nombra el valor guardado.
- [ ] `textSize` en `sp`: cuerpo ≥ 18sp, títulos ≥ 24sp, valores capturados ≥ 48sp; `contentDescription` en controles sin texto.
