#!/usr/bin/env python3
"""Тягне кандидатів звуків для клітинок дошки з Freesound API.

Нічого не встановлює: лише stdlib. Ключ читається з оточення, у репозиторій не потрапляє:

    FREESOUND_TOKEN=... python3 scripts/fetch_sounds.py

Береться тільки CC0 — інші ліцензії Freesound вимагають згадки автора в застосунку
або забороняють комерційне використання. Якщо звук CC0, з ним нічого робити не треба.

Тягнуться прев'ю-mp3: оригінали за документацією потребують OAuth2, а не токена.
"""

import json
import os
import urllib.error
import urllib.parse
import urllib.request

API = "https://freesound.org/apiv2"
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "sound-candidates")
PER_QUERY = 8
MAX_SECONDS = 2.0

SLOTS = {
    "service": ("Обслуговування: зарплата, старт, відпустка, податкова", [
        "coin pickup", "cash register", "reward chime",
    ]),
    "card": ("Карта: шанс, ринок, покупки, депутат", [
        "card flip", "card deal", "page turn",
    ]),
    "loss": ("Втрата: витрати, банкрутство, розлучення", [
        "error buzz", "fail low", "negative beep",
    ]),
    "asset": ("Актив: бізнес, великий бізнес", [
        "success chime", "unlock", "level up short",
    ]),
    "life": ("Життя: дитина, кохання, звільнення, мрія", [
        "magic sparkle", "twinkle", "bell short",
    ]),
    "step": ("Крок фішки по клітинці", [
        "wood tap", "click soft", "token place",
    ]),
    "dice": ("Падіння кубика", [
        "dice roll", "dice throw", "dice shake",
    ]),
}


def token():
    value = os.environ.get("FREESOUND_TOKEN")
    if not value:
        raise SystemExit("немає FREESOUND_TOKEN у оточенні")
    return value


def get(url):
    request = urllib.request.Request(url, headers={"Authorization": f"Token {token()}"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read()


def search(query):
    params = urllib.parse.urlencode({
        "query": query,
        "filter": f'license:"Creative Commons 0" duration:[0.1 TO {MAX_SECONDS}]',
        "fields": "id,name,username,license,duration,previews",
        "page_size": PER_QUERY,
        "sort": "score",
    })
    try:
        return json.loads(get(f"{API}/search/text/?{params}")).get("results", [])
    except urllib.error.HTTPError as error:
        print(f"  ! пошук '{query}': {error.code} {error.read()[:200]!r}")
        return []
    except Exception as error:
        print(f"  ! пошук '{query}': {error}")
        return []


def main():
    os.makedirs(OUT, exist_ok=True)
    report = {}
    for slot, (title, queries) in SLOTS.items():
        folder = os.path.join(OUT, slot)
        os.makedirs(folder, exist_ok=True)
        found = {}
        for query in queries:
            for sound in search(query):
                found.setdefault(sound["id"], sound)
        saved = []
        for sound in found.values():
            preview = sound["previews"].get("preview-hq-mp3") or sound["previews"].get("preview-lq-mp3")
            if not preview:
                continue
            file_name = f"{sound['id']}__{safe(sound['name'])}.mp3"
            path = os.path.join(folder, file_name)
            if not os.path.exists(path):
                try:
                    with urllib.request.urlopen(preview, timeout=30) as response:
                        data = response.read()
                except Exception as error:
                    print(f"  ! {sound['id']}: {error}")
                    continue
                with open(path, "wb") as handle:
                    handle.write(data)
            saved.append({
                "file": file_name,
                "id": sound["id"],
                "name": sound["name"],
                "author": sound["username"],
                "license": sound["license"],
                "duration": round(sound["duration"], 2),
            })
        report[slot] = {"title": title, "sounds": saved}
        print(f"{slot}: {len(saved)}")
    with open(os.path.join(OUT, "credits.json"), "w") as handle:
        json.dump(report, handle, ensure_ascii=False, indent=2)
    write_index(report)
    print(f"\nГотово: {OUT}/index.html")


def safe(name):
    keep = "-_"
    cleaned = "".join(char if char.isalnum() or char in keep else "-" for char in name)
    return cleaned[:48].strip("-")


def write_index(report):
    parts = [
        "<meta charset='utf-8'><title>Кандидати звуків</title>",
        "<style>body{font-family:system-ui;margin:24px;background:#111;color:#eee}"
        "h2{margin:28px 0 8px;font-size:16px}"
        ".row{display:flex;align-items:center;gap:12px;padding:6px 0;border-bottom:1px solid #222}"
        ".name{font-size:12px;color:#9aa;min-width:280px}"
        ".meta{font-size:11px;color:#667}"
        "audio{height:32px}</style>",
    ]
    for slot, data in report.items():
        parts.append(f"<h2>{data['title']} — <code>{slot}</code></h2>")
        for sound in data["sounds"]:
            parts.append(
                f"<div class='row'><audio controls preload='none' src='{slot}/{sound['file']}'></audio>"
                f"<div class='name'>{sound['name']}</div>"
                f"<div class='meta'>{sound['duration']}s · {sound['author']} · {sound['license']}</div></div>"
            )
    with open(os.path.join(OUT, "index.html"), "w") as handle:
        handle.write("\n".join(parts))


if __name__ == "__main__":
    main()
