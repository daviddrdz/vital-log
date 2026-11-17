#!/usr/bin/env bash
# Hook PostToolUse (Edit|Write|MultiEdit): ejecuta audit_ui.py cuando se toca
# la UI de VitalLog. Si hay ERROR sale con código 2 y envía el reporte a stderr,
# que Claude Code devuelve a Claude para que lo corrija antes de continuar.
# Los WARN no bloquean. Cualquier otro archivo se ignora en silencio.

input="$(cat)"

file="$(printf '%s' "$input" | python3 -c '
import json, sys
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("file_path", ""))
except Exception:
    print("")
')"

case "$file" in
*/app/src/main/res/* | */app/src/main/java/*/ui/* | */app/src/main/java/*/feedback/*) ;;
*) exit 0 ;;
esac

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

report="$(python3 .claude/skills/gerontological-ui-auditor/scripts/audit_ui.py \
    app/src/main/res app/src/main/java 2>&1)"
status=$?

if [ "$status" -ne 0 ]; then
    {
        echo "Auditoría gerontológica (audit_ui.py) falló tras editar: $file"
        echo "$report"
        echo "Corrige los ERROR antes de continuar (ver .claude/skills/gerontological-ui-auditor/SKILL.md)."
    } >&2
    exit 2
fi

exit 0
