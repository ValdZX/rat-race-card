#!/usr/bin/env python3
"""Тягне кандидатів іконок для клітинок дошки й колод з Iconify API.

Нічого не встановлює: лише stdlib. Кладе SVG у icon-candidates/<слот>/ і
збирає index.html, щоб вибирати очима, а не за назвами файлів.

    python3 scripts/fetch_icons.py

Набори прибиті навмисно: різностильові знаки на дошці виглядають як звалище,
навіть коли кожен окремо гарний.
"""

import json
import os
import re
import urllib.parse
import urllib.request

API = "https://api.iconify.design"
SETS = ["material-symbols", "mdi", "ph", "tabler"]
PER_QUERY = 6
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "icon-candidates")

SLOTS = {
    "cell_salary": ("Зарплата / Прибуток", ["payments", "attach money", "wallet", "coins"]),
    "cell_start": ("Старт", ["flag", "race flag", "start"]),
    "cell_chance": ("Шанс", ["casino", "dice", "sparkles", "question mark"]),
    "cell_store": ("Ринок", ["storefront", "market", "shop"]),
    "cell_shopping": ("Покупки", ["shopping bag", "shopping cart"]),
    "cell_expenses": ("Витрати", ["money off", "receipt", "cash minus"]),
    "cell_business": ("Бізнес", ["briefcase", "store", "warehouse"]),
    "cell_big_business": ("Великий бізнес", ["factory", "office building", "domain"]),
    "cell_deputy": ("Депутат", ["gavel", "government", "podium"]),
    "cell_rest": ("Відпочинок", ["beach", "palm tree", "umbrella beach"]),
    "cell_exaltation": ("Піднесення", ["trending up", "rocket", "arrow up"]),
    "cell_divorce": ("Розлучення", ["heart broken"]),
    "cell_bankruptcy": ("Банкрутство", ["trending down", "bankrupt", "piggy bank"]),
    "cell_child": ("Дитина", ["child care", "stroller", "baby"]),
    "cell_love": ("Кохання", ["heart", "love", "hearts"]),
    "cell_tax": ("Податкова", ["account balance", "tax", "policy"]),
    "cell_dream": ("Мрія", ["auto awesome", "star", "diamond"]),
    "deck_small_business": ("Колода: малий бізнес", ["coffee", "cafe", "kiosk"]),
    "deck_medium_business": ("Колода: середній бізнес", ["store", "warehouse"]),
    "deck_big_business": ("Колода: великий бізнес", ["factory", "corporate"]),
}


def get(url):
    # Без свого User-Agent Iconify віддає 403 на дефолтний урлліб.
    request = urllib.request.Request(url, headers={"User-Agent": "rat-race-card-icon-fetch"})
    with urllib.request.urlopen(request, timeout=20) as response:
        return response.read()


def search(query, prefix):
    url = f"{API}/search?query={urllib.parse.quote(query)}&prefix={prefix}&limit={PER_QUERY}"
    try:
        return json.loads(get(url)).get("icons", [])
    except Exception as error:
        print(f"  ! пошук '{query}' у {prefix}: {error}")
        return []


def main():
    os.makedirs(OUT, exist_ok=True)
    report = {}
    for slot, (title, queries) in SLOTS.items():
        folder = os.path.join(OUT, slot)
        os.makedirs(folder, exist_ok=True)
        found = []
        for query in queries:
            for prefix in SETS:
                for icon in search(query, prefix):
                    if icon not in found:
                        found.append(icon)
        saved = []
        for icon in found:
            prefix, name = icon.split(":", 1)
            file_name = f"{prefix}__{name}.svg"
            path = os.path.join(folder, file_name)
            if not os.path.exists(path):
                try:
                    svg = get(f"{API}/{prefix}/{name}.svg?height=48")
                except Exception as error:
                    print(f"  ! {icon}: {error}")
                    continue
                with open(path, "wb") as handle:
                    handle.write(svg)
            saved.append(file_name)
        report[slot] = (title, saved)
        print(f"{slot}: {len(saved)}")
    write_index(report)
    print(f"\nГотово: {OUT}/index.html")


def write_index(report):
    parts = [
        "<meta charset='utf-8'><title>Кандидати іконок</title>",
        "<style>body{font-family:system-ui;margin:24px;background:#111;color:#eee}"
        "h2{margin:28px 0 8px;font-size:16px}"
        ".grid{display:flex;flex-wrap:wrap;gap:10px}"
        ".item{width:104px;text-align:center;background:#1c1c1c;border:1px solid #333;"
        "border-radius:10px;padding:8px}"
        ".item img{width:44px;height:44px;filter:invert(1)}"
        ".name{font-size:9px;color:#9aa;word-break:break-all;margin-top:6px}"
        "code{background:#222;padding:2px 6px;border-radius:4px;font-size:12px}</style>",
    ]
    for slot, (title, files) in report.items():
        parts.append(f"<h2>{title} — <code>{slot}.xml</code></h2><div class='grid'>")
        for file_name in files:
            parts.append(
                f"<div class='item'><img src='{slot}/{file_name}'>"
                f"<div class='name'>{file_name[:-4]}</div></div>"
            )
        parts.append("</div>")
    with open(os.path.join(OUT, "index.html"), "w") as handle:
        handle.write("\n".join(parts))


if __name__ == "__main__":
    main()
