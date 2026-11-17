#!/usr/bin/env python3
"""Calcula la relación de contraste WCAG entre un color de texto y uno de fondo.

Uso:
    contrast.py TEXTO FONDO [--colors RUTA_colors.xml]

Cada color puede ser "#RRGGBB", "#AARRGGBB" (alfa debe ser FF), "@color/nombre"
o "nombre" (se resuelve en colors.xml). Sale con código 1 si no cumple las
reglas de VitalLog: ratio >= 7:1 y texto más oscuro que el fondo.
"""
import argparse
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

AAA_RATIO = 7.0
DEFAULT_COLORS = pathlib.Path("app/src/main/res/values/colors.xml")
HEX_RE = re.compile(r"^#([0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")


def load_colors(path):
    if not path.exists():
        return {}
    root = ET.parse(path).getroot()
    return {c.get("name"): (c.text or "").strip() for c in root.iter("color")}


def resolve(value, colors, seen=None):
    seen = seen or set()
    name = value[len("@color/"):] if value.startswith("@color/") else value
    if HEX_RE.match(value):
        return value
    if name in seen:
        sys.exit(f"Referencia circular de color: {name}")
    if name not in colors:
        sys.exit(f"Color no encontrado: {value}")
    seen.add(name)
    return resolve(colors[name], colors, seen)


def to_rgb(hex_value, label):
    digits = hex_value[1:]
    if len(digits) in (3, 4):
        digits = "".join(ch * 2 for ch in digits)
    if len(digits) == 8:
        alpha, digits = digits[:2], digits[2:]
        if alpha.upper() != "FF":
            sys.exit(f"ERROR: {label} {hex_value} tiene transparencia (alfa {alpha}). "
                     "Usa colores sólidos.")
    return tuple(int(digits[i:i + 2], 16) / 255 for i in (0, 2, 4))


def luminance(rgb):
    def channel(c):
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (channel(c) for c in rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("text")
    parser.add_argument("background")
    parser.add_argument("--colors", type=pathlib.Path, default=DEFAULT_COLORS)
    args = parser.parse_args()

    colors = load_colors(args.colors)
    text_hex = resolve(args.text, colors)
    bg_hex = resolve(args.background, colors)
    text_l = luminance(to_rgb(text_hex, "texto"))
    bg_l = luminance(to_rgb(bg_hex, "fondo"))

    ratio = (max(text_l, bg_l) + 0.05) / (min(text_l, bg_l) + 0.05)
    dark_on_light = text_l < bg_l

    print(f"Texto {args.text} ({text_hex}) sobre fondo {args.background} ({bg_hex})")
    print(f"Contraste: {ratio:.2f}:1  (mínimo VitalLog {AAA_RATIO}:1)")
    print(f"Oscuro sobre claro: {'sí' if dark_on_light else 'NO'}")

    ok = ratio >= AAA_RATIO and dark_on_light
    print("RESULTADO: CUMPLE" if ok else "RESULTADO: NO CUMPLE")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
