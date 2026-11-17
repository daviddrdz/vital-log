#!/usr/bin/env python3
"""Auditor estático de accesibilidad gerontológica para VitalLog.

Uso:
    audit_ui.py [RUTA ...]   (archivos o directorios; por defecto app/src/main/res y java)

Revisa layouts/recursos XML y clases Java de UI. Imprime ERROR/WARN con
archivo:línea y sale con código 1 si hay algún ERROR. No sustituye la
revisión manual descrita en SKILL.md (jerarquía visual, textos, flujos).
"""
import pathlib
import re
import sys
from xml.parsers import expat

MIN_TOUCH_DP = 60
MIN_GAP_DP = 12
MIN_BODY_SP = 18
DEFAULT_PATHS = ["app/src/main/res", "app/src/main/java"]

TOUCH_TAGS = {"Button", "ImageButton", "CheckBox", "RadioButton", "Switch",
              "ToggleButton", "Spinner", "NumberPicker", "SeekBar"}
TOUCH_SUFFIXES = ("Button", "FloatingActionButton", "Chip", "SwitchMaterial",
                  "MaterialSwitch", "Slider", "CheckBox", "RadioButton", "Stepper", "Dial")
TEXT_INPUT_SUFFIXES = ("EditText", "AutoCompleteTextView")
VITAL_ID_RE = re.compile(r"gluc|azucar|presion|pressure|sistol|systol|diastol|bp_|vital",
                         re.IGNORECASE)
DIMEN_RE = re.compile(r"^(-?\d+(?:\.\d+)?)(dp|dip|sp|px|pt|mm|in)$")
ALPHA_HEX_RE = re.compile(r"#([0-9a-fA-F]{4}|[0-9a-fA-F]{8})\b")
MARGIN_ATTRS = ("android:layout_margin", "android:layout_marginTop",
                "android:layout_marginBottom", "android:layout_marginStart",
                "android:layout_marginEnd", "android:layout_marginLeft",
                "android:layout_marginRight", "android:layout_marginHorizontal",
                "android:layout_marginVertical")

JAVA_RULES = [
    (re.compile(r"new\s+(?:\w+\.)*(?:AppCompat|TextInput)?EditText\s*\("), "ERROR",
     "EditText creado por código: prohibido para signos vitales; usa control +/− grande."),
    (re.compile(r"TYPE_CLASS_NUMBER|TYPE_NUMBER_FLAG"), "ERROR",
     "Entrada numérica por teclado: prohibida para signos vitales."),
    (re.compile(r"showSoftInput\s*\("), "ERROR", "Se abre el teclado virtual."),
    (re.compile(r"\.setAlpha\s*\(\s*0?\.\d+f?\s*\)"), "ERROR",
     "setAlpha < 1: no atenuar texto/controles con transparencia."),
    (re.compile(r"Color\.argb\s*\(|Color\.parseColor\s*\("), "WARN",
     "Color definido en código: usa @color/ de colors.xml y verifica alfa y contraste."),
    (re.compile(r"GradientDrawable|LinearGradient|RadialGradient"), "WARN",
     "Degradado: prohibido detrás de texto."),
    (re.compile(r"setTextSize\s*\(\s*TypedValue\.COMPLEX_UNIT_(?:PX|DIP)"), "ERROR",
     "setTextSize en px/dp: usa COMPLEX_UNIT_SP para respetar la escala de fuente."),
    (re.compile(r"Toast\.makeText"), "WARN",
     "Toast: no basta como confirmación de guardado (requiere banner + tono)."),
    (re.compile(r"setText\s*\(\s*\"[^\"]+\"\s*\)"), "WARN",
     "Texto literal en Java: muévelo a strings.xml."),
]

findings = []


def report(level, path, line, msg):
    findings.append((level, str(path), line or 0, msg))


def find_res_root(path):
    for parent in [path] + list(path.parents):
        if parent.name == "res":
            return parent
    return None


def load_dimens(res_root):
    dimens = {}
    if res_root is None:
        return dimens
    for f in res_root.glob("values*/*.xml"):
        try:
            for node in parse_xml(f):
                if node["tag"] == "dimen" and "name" in node["attrs"]:
                    dimens.setdefault(node["attrs"]["name"], node["text"].strip())
        except expat.ExpatError:
            continue
    return dimens


def parse_xml(path):
    parser = expat.ParserCreate()
    nodes, stack = [], []

    def start(tag, attrs):
        node = {"tag": tag, "attrs": attrs, "line": parser.CurrentLineNumber, "text": ""}
        nodes.append(node)
        stack.append(node)

    def end(_tag):
        stack.pop()

    def chars(data):
        if stack:
            stack[-1]["text"] += data

    parser.StartElementHandler = start
    parser.EndElementHandler = end
    parser.CharacterDataHandler = chars
    parser.Parse(path.read_bytes(), True)
    return nodes


def to_number(value, dimens, depth=0):
    """Devuelve (número, unidad) o None si no es una dimensión fija."""
    if value is None or depth > 5:
        return None
    if value.startswith("@dimen/"):
        return to_number(dimens.get(value[len("@dimen/"):]), dimens, depth + 1)
    m = DIMEN_RE.match(value.strip())
    if not m:
        return None
    unit = "dp" if m.group(2) == "dip" else m.group(2)
    return float(m.group(1)), unit


def short_tag(tag):
    return tag.rsplit(".", 1)[-1]


def is_touch(node):
    tag = short_tag(node["tag"])
    a = node["attrs"]
    return (tag in TOUCH_TAGS or tag.endswith(TOUCH_SUFFIXES)
            or a.get("android:clickable") == "true" or "android:onClick" in a)


def check_touch_size(path, node, dimens):
    a = node["attrs"]
    tag = short_tag(node["tag"])
    for axis, size_attr, min_attr in (("alto", "android:layout_height", "android:minHeight"),
                                      ("ancho", "android:layout_width", "android:minWidth")):
        size = a.get(size_attr)
        minimum = to_number(a.get(min_attr), dimens)
        if size in ("match_parent", "fill_parent", "0dp", "0dip"):
            continue  # ocupa el espacio del padre/constraints: revisar manualmente
        fixed = to_number(size, dimens)
        if fixed is not None:
            if fixed[1] != "dp":
                report("ERROR", path, node["line"], f"<{tag}> {size_attr}={size}: usa dp.")
            elif fixed[0] < MIN_TOUCH_DP and (minimum is None or minimum[0] < MIN_TOUCH_DP):
                report("ERROR", path, node["line"],
                       f"<{tag}> {axis} {size} < {MIN_TOUCH_DP}dp.")
        elif size == "wrap_content" or size is None:
            if minimum is None:
                report("ERROR", path, node["line"],
                       f"<{tag}> {axis} wrap_content sin {min_attr} >= {MIN_TOUCH_DP}dp "
                       "(@dimen/touch_target_min).")
            elif minimum[0] < MIN_TOUCH_DP:
                report("ERROR", path, node["line"],
                       f"<{tag}> {min_attr}={a.get(min_attr)} < {MIN_TOUCH_DP}dp.")


def check_spacing(path, node, dimens):
    for attr in MARGIN_ATTRS:
        value = to_number(node["attrs"].get(attr), dimens)
        if value and value[1] == "dp" and 0 < value[0] < MIN_GAP_DP:
            report("WARN", path, node["line"],
                   f"<{short_tag(node['tag'])}> {attr}={node['attrs'][attr]}: "
                   f"verifica separación >= {MIN_GAP_DP}dp entre elementos táctiles.")


def check_text_input(path, node):
    tag = short_tag(node["tag"])
    if not tag.endswith(TEXT_INPUT_SUFFIXES):
        return
    a = node["attrs"]
    input_type = a.get("android:inputType", "")
    ident = " ".join(filter(None, [a.get("android:id"), a.get("android:hint"),
                                   a.get("android:contentDescription")]))
    if "number" in input_type or "phone" in input_type or VITAL_ID_RE.search(ident):
        report("ERROR", path, node["line"],
               f"<{tag}> para captura numérica/signo vital: prohibido. "
               "Usa control +/− grande con límites de VitalRanges.")
    else:
        report("WARN", path, node["line"],
               f"<{tag}> abre el teclado: confirma que no captura signos vitales.")


def check_xml(path, dimens):
    try:
        nodes = parse_xml(path)
    except expat.ExpatError as exc:
        report("ERROR", path, exc.lineno, f"XML inválido: {exc}")
        return
    for node in nodes:
        a = node["attrs"]
        tag = short_tag(node["tag"])

        if is_touch(node) and path.parent.name.startswith("layout"):
            check_touch_size(path, node, dimens)
            check_spacing(path, node, dimens)
        check_text_input(path, node)

        if tag == "gradient":
            report("ERROR", path, node["line"],
                   "<gradient>: no usar degradados como fondo de texto/controles.")
        if tag == "color" and ALPHA_HEX_RE.fullmatch(node["text"].strip() or "-"):
            check_alpha(path, node["line"], node["text"].strip(), f"color {a.get('name')}")
        if tag == "style" and "DayNight" in a.get("parent", ""):
            report("ERROR", path, node["line"],
                   f"Tema {a.get('name')} hereda de DayNight: usa un tema claro fijo "
                   "(oscuro sobre claro).")
        if tag == "item" and a.get("name") in ("android:alpha", "android:disabledAlpha"):
            report("ERROR", path, node["line"], f"{a.get('name')} en estilo: sin transparencias.")

        for attr, value in a.items():
            if attr.startswith("tools:"):
                continue
            for m in ALPHA_HEX_RE.finditer(value):
                # En vectores, un relleno/trazo transparente suele ser intencional:
                # se avisa para revisar que el icono conserve contraste.
                vector_paint = path.parent.name.startswith("drawable") and attr in (
                    "android:fillColor", "android:strokeColor")
                check_alpha(path, node["line"], m.group(0), attr,
                            "WARN" if vector_paint else "ERROR")
            if attr == "android:alpha":
                try:
                    if float(value) < 1:
                        report("ERROR", path, node["line"],
                               f"<{tag}> android:alpha={value}: sin transparencias.")
                except ValueError:
                    pass
            if attr in ("android:textSize", "app:autoSizeMinTextSize"):
                size = to_number(value, dimens)
                if size and size[1] != "sp":
                    report("ERROR", path, node["line"], f"<{tag}> {attr}={value}: usa sp.")
                elif size and size[0] < MIN_BODY_SP:
                    report("ERROR", path, node["line"],
                           f"<{tag}> {attr}={value} < {MIN_BODY_SP}sp.")
            if attr in ("android:text", "android:hint", "android:contentDescription") \
                    and value and not value.startswith(("@", "?")):
                report("WARN", path, node["line"],
                       f"<{tag}> {attr} literal \"{value}\": muévelo a strings.xml.")
            if attr in ("android:textColor", "android:background", "app:backgroundTint") \
                    and value.startswith("#"):
                report("WARN", path, node["line"],
                       f"<{tag}> {attr}={value} literal: usa @color/ y verifica contraste.")

        if is_touch(node) and path.parent.name.startswith("layout") \
                and tag.endswith(("ImageButton", "FloatingActionButton")) \
                and "android:contentDescription" not in a:
            report("ERROR", path, node["line"], f"<{tag}> sin contentDescription.")


def check_alpha(path, line, hex_value, where, level="ERROR"):
    digits = hex_value[1:]
    alpha = digits[0] * 2 if len(digits) == 4 else digits[:2]
    if alpha.upper() != "FF":
        report(level, path, line, f"{where}={hex_value} tiene transparencia (alfa {alpha}).")


def check_java(path):
    for lineno, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        stripped = line.strip()
        if stripped.startswith(("//", "*", "import ")):
            continue
        for pattern, level, msg in JAVA_RULES:
            if pattern.search(line):
                report(level, path, lineno, msg)


def collect(paths):
    files = []
    for raw in paths:
        p = pathlib.Path(raw)
        if p.is_dir():
            files.extend(sorted(p.rglob("*.xml")))
            files.extend(sorted(p.rglob("*.java")))
        elif p.is_file():
            files.append(p)
        else:
            print(f"Aviso: no existe {p}", file=sys.stderr)
    # Los iconos de launcher no son UI de la app (los genera Android Studio).
    return [f for f in files if not f.name.startswith("ic_launcher")]


def main():
    files = collect(sys.argv[1:] or DEFAULT_PATHS)
    dimens_cache = {}
    for f in files:
        if f.suffix == ".java":
            check_java(f)
        elif f.suffix == ".xml" and find_res_root(f.resolve()) is not None:
            res_root = find_res_root(f.resolve())
            if res_root not in dimens_cache:
                dimens_cache[res_root] = load_dimens(res_root)
            check_xml(f, dimens_cache[res_root])

    errors = sum(1 for f in findings if f[0] == "ERROR")
    warns = len(findings) - errors
    for level, path, line, msg in sorted(findings, key=lambda x: (x[0] != "ERROR", x[1], x[2])):
        print(f"{level:5} {path}:{line}  {msg}")
    print(f"\n{len(files)} archivos revisados: {errors} ERROR, {warns} WARN.")
    if errors == 0:
        print("Auditoría estática superada. Completa la revisión manual de SKILL.md.")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
