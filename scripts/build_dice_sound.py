#!/usr/bin/env python3
"""Збирає звук кидка кубика з CC0 деревʼяних ударів у sound-candidates/step.

Удари розставлені під тайминг lottie-анімації cube_*.json (60 fps): кидок на 0,
приземлення на кадрі 60, підскок на 80, зупинка на 100. Готовий асет — один файл,
який програється в момент початку анімації:

    python3 scripts/build_dice_sound.py

Потрібен лише afconvert з macOS для розпаковки mp3 у PCM, решта — stdlib.
"""

import os
import struct
import subprocess
import tempfile
import wave

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = os.path.join(ROOT, "sound-candidates", "step")
TARGET = os.path.join(ROOT, "core", "src", "commonMain", "composeResources", "files", "dice_roll.wav")
SR = 32000
LANDING_LEAD = 0.07

KNOCK = "369710__Wood-Knock"
EDGE = "405549__Wood-knock"
TAP = "704935__Wooden-Tap"
SETTLE = "427393__Tap-on-Wooden-Table"


def decode(name, folder):
    path = os.path.join(SOURCES, f"{name}.mp3")
    out = os.path.join(folder, f"{name}.wav")
    subprocess.run(
        ["afconvert", "-f", "WAVE", "-d", f"LEI16@{SR}", "-c", "1", path, out],
        check=True,
        capture_output=True,
    )
    reader = wave.open(out)
    frames = reader.getnframes()
    samples = struct.unpack(f"<{frames}h", reader.readframes(frames))
    peak = max(abs(value) for value in samples)
    if peak == 0:
        raise SystemExit(f"{name}: тиша після декодування")
    onset = next(i for i, value in enumerate(samples) if abs(value) > 0.08 * peak)
    return [value / peak for value in samples[onset:]]


def clip(samples, seconds, fade=0.012):
    cut = samples[: int(SR * seconds)]
    steps = int(SR * fade)
    for i in range(min(steps, len(cut))):
        cut[len(cut) - 1 - i] *= i / steps
    return cut


def pitched(samples, ratio):
    out = []
    position = 0.0
    while position < len(samples) - 1:
        low = int(position)
        fraction = position - low
        out.append(samples[low] * (1 - fraction) + samples[low + 1] * fraction)
        position += ratio
    return out


def hits(knock, edge, tap, settle):
    return [
        (0.000, knock, 0.34, 1.18),
        (0.038, edge, 0.26, 1.32),
        (0.079, tap, 0.30, 1.05),
        (0.126, knock, 0.20, 1.45),

        (1.000, knock, 1.00, 0.94),
        (1.038, edge, 0.42, 1.10),
        (1.082, tap, 0.34, 1.22),
        (1.140, knock, 0.22, 1.36),

        (1.370, tap, 0.52, 1.00),
        (1.406, edge, 0.24, 1.24),

        (1.670, settle, 0.44, 0.98),
        (1.718, knock, 0.18, 1.30),
        (1.770, tap, 0.10, 1.42),
    ]


def main():
    with tempfile.TemporaryDirectory() as folder:
        knock = clip(decode(KNOCK, folder), 0.20)
        edge = clip(decode(EDGE, folder), 0.12)
        tap = clip(decode(TAP, folder), 0.16)
        settle = clip(decode(SETTLE, folder), 0.45)

    length = int(SR * 2.15)
    mix = [0.0] * length
    for at, source, gain, ratio in hits(knock, edge, tap, settle):
        start = int(SR * max(0.0, at - LANDING_LEAD if at > 0.5 else at))
        for i, value in enumerate(pitched(source, ratio)):
            if start + i < length:
                mix[start + i] += value * gain

    scale = 0.89 / max(abs(value) for value in mix)
    frames = b"".join(
        struct.pack("<h", int(max(-1.0, min(1.0, value * scale)) * 32767)) for value in mix
    )

    writer = wave.open(TARGET, "wb")
    writer.setnchannels(1)
    writer.setsampwidth(2)
    writer.setframerate(SR)
    writer.writeframes(frames)
    writer.close()
    print(f"{TARGET}: {round(length / SR, 2)}s, {os.path.getsize(TARGET) // 1024} КБ")


if __name__ == "__main__":
    main()
