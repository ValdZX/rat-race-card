#!/usr/bin/env python3
"""Перекладає однопатчові SVG у Android vector drawable.

Android не вміє SVG у compose-ресурсах, тому іконки, скачані скриптом
fetch_icons.py, треба класти в drawable/ у форматі <vector>.

    python3 scripts/svg_to_vector.py core/src/commonMain/composeResources/drawable/*.svg

Колір лишається чорним: усі ці іконки малюються через Icon(tint) або
ColorFilter.tint, тобто власний колір файлу все одно перекривається.
"""

import os
import re
import sys
import xml.etree.ElementTree as ET

SVG_NS = "{http://www.w3.org/2000/svg}"
INK = "#FF000000"
TRANSPARENT = "#00000000"
CAPS = {"butt": "butt", "round": "round", "square": "square"}
JOINS = {"miter": "miter", "round": "round", "bevel": "bevel"}


def viewport(root):
    box = root.get("viewBox")
    if box:
        parts = [float(value) for value in re.split(r"[ ,]+", box.strip())]
        if len(parts) == 4 and parts[0] == 0 and parts[1] == 0:
            return parts[2], parts[3]
        raise SystemExit(f"viewBox зі зсувом не підтримується: {box}")
    return float(root.get("width", 24)), float(root.get("height", 24))


def number(value):
    text = f"{value:.4f}".rstrip("0").rstrip(".")
    return text or "0"


def path_element(node):
    data = node.get("d")
    if not data:
        return None
    fill = node.get("fill", "black")
    stroke = node.get("stroke")
    attributes = [f'        android:pathData="{data}"']
    attributes.append(f'        android:fillColor="{TRANSPARENT if fill == "none" else INK}"')
    if node.get("fill-rule") == "evenodd":
        attributes.append('        android:fillType="evenOdd"')
    if stroke and stroke != "none":
        attributes.append(f'        android:strokeColor="{INK}"')
        attributes.append(f'        android:strokeWidth="{node.get("stroke-width", "1")}"')
        cap = CAPS.get(node.get("stroke-linecap", ""))
        join = JOINS.get(node.get("stroke-linejoin", ""))
        if cap:
            attributes.append(f'        android:strokeLineCap="{cap}"')
        if join:
            attributes.append(f'        android:strokeLineJoin="{join}"')
    return "    <path\n" + "\n".join(attributes) + " />"


def convert(source):
    root = ET.parse(source).getroot()
    if root.tag != f"{SVG_NS}svg":
        raise SystemExit(f"{source}: не SVG")
    unsupported = [child.tag for child in root.iter() if child.tag not in (f"{SVG_NS}svg", f"{SVG_NS}path")]
    if unsupported:
        raise SystemExit(f"{source}: підтримуються лише <path>, а тут {sorted(set(unsupported))}")
    width, height = viewport(root)
    paths = [path_element(node) for node in root.iter(f"{SVG_NS}path")]
    paths = [path for path in paths if path]
    if not paths:
        raise SystemExit(f"{source}: жодного <path> з даними")
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{number(width)}dp"\n'
        f'    android:height="{number(height)}dp"\n'
        f'    android:viewportWidth="{number(width)}"\n'
        f'    android:viewportHeight="{number(height)}">\n'
        + "\n".join(paths)
        + "\n</vector>\n"
    )


def main(sources):
    if not sources:
        raise SystemExit(__doc__)
    for source in sources:
        target = os.path.splitext(source)[0] + ".xml"
        with open(target, "w", encoding="utf-8") as file:
            file.write(convert(source))
        os.remove(source)
        print(f"{os.path.basename(source)} -> {os.path.basename(target)}")


if __name__ == "__main__":
    main(sys.argv[1:])
