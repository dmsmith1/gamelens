import csv
import io
import re
import subprocess
import tempfile
import warnings
from collections import defaultdict
from PIL import Image, ImageOps, UnidentifiedImageError
from fastapi import HTTPException

MAX_BYTES = 10 * 1024 * 1024
MAX_PIXELS = 20_000_000
Image.MAX_IMAGE_PIXELS = MAX_PIXELS
HEADER_WORDS = {"name", "number", "jersey", "player", "players", "roster", "lineup", "team", "season", "coach", "position", "bats", "throws"}
POSITIONS = {"P":"P", "C":"C", "1B":"FIRST_BASE", "2B":"SECOND_BASE", "3B":"THIRD_BASE", "SS":"SS", "LF":"LF", "CF":"CF", "RF":"RF", "DH":"DH", "UT":"UTILITY", "UTIL":"UTILITY"}

def parse_lines(lines):
    drafts = []
    for text, confidence in lines:
        original = text.strip()
        tokens = original.replace("|", " ").split()
        if not tokens or any(t.lower().strip(".:#") in HEADER_WORDS for t in tokens):
            continue
        # Ignore trailing handedness columns; users review explicit defaults.
        while tokens and tokens[-1].upper() in {"R", "L", "S", "R/R", "L/R", "L/L", "S/R"}:
            tokens.pop()
        position = "UTILITY"
        if tokens and tokens[-1].upper() in POSITIONS:
            position = POSITIONS[tokens.pop().upper()]
        numbers = []
        while tokens and re.fullmatch(r"#?\d{1,3}[.)]?", tokens[0]):
            numbers.append(int(re.sub(r"\D", "", tokens.pop(0))))
        jersey = numbers[-1] if numbers else None
        if tokens and re.fullmatch(r"#?\d{1,3}", tokens[-1]):
            jersey = int(tokens.pop().lstrip("#"))
        if not tokens or any(re.search(r"\d", t) for t in tokens):
            continue
        name = " ".join(tokens).strip(" .-–")
        if not name or not all(c.isalpha() or c in " '-.,’" for c in name):
            continue
        if "," in name:
            last, first = (s.strip() for s in name.split(",", 1))
        else:
            parts = name.split()
            first, last = (parts[0], " ".join(parts[1:])) if len(parts) > 1 else (name, "")
        if len(first) > 80 or len(last) > 80:
            continue
        drafts.append({"firstName":first, "lastName":last, "jerseyNumber":jersey if jersey is not None and 0 <= jersey <= 99 else None,
                       "primaryPosition":position, "bats":"RIGHT", "throwsHand":"RIGHT", "confidence":round(confidence),
                       "sourceText":original, "include":jersey is not None and 0 <= jersey <= 99 and bool(first and last)})
        if len(drafts) >= 100:
            break
    return drafts

def extract(data):
    if not data:
        raise HTTPException(400, "Choose a roster photo first.")
    if len(data) > MAX_BYTES:
        raise HTTPException(413, "Photo is too large. Choose an image under 10 MB.")
    try:
        with warnings.catch_warnings():
            warnings.simplefilter("error", Image.DecompressionBombWarning)
            with Image.open(io.BytesIO(data)) as source:
                if source.format not in {"JPEG", "PNG", "WEBP"}:
                    raise HTTPException(415, "Use a JPEG, PNG, or WebP photo. Export HEIC photos as JPEG first.")
                if source.width * source.height > MAX_PIXELS:
                    raise HTTPException(413, "Photo resolution is too large. Choose an image under 20 megapixels.")
                image = ImageOps.exif_transpose(source).convert("RGB")
                image.thumbnail((3000, 3000))
                image = ImageOps.autocontrast(ImageOps.grayscale(image))
    except HTTPException:
        raise
    except (UnidentifiedImageError, OSError, ValueError, Image.DecompressionBombError, Image.DecompressionBombWarning):
        raise HTTPException(400, "That file could not be read as a photo. Use a clear JPEG, PNG, or WebP image.")
    with tempfile.TemporaryDirectory(prefix="gamelens-roster-") as folder:
        path = folder + "/roster.png"
        image.save(path)
        try:
            result = subprocess.run(["tesseract", path, "stdout", "-l", "eng", "--psm", "6", "tsv"], capture_output=True, text=True, timeout=25, check=True)
        except subprocess.TimeoutExpired:
            raise HTTPException(504, "Reading the photo took too long. Crop closer to the roster and try again.")
        except (FileNotFoundError, subprocess.CalledProcessError):
            raise HTTPException(503, "Photo reading is unavailable. Please try again later or enter players manually.")
        groups = defaultdict(list)
        for word in csv.DictReader(io.StringIO(result.stdout), delimiter="\t"):
            if word.get("level") == "5" and word.get("text", "").strip():
                groups[(word["page_num"], word["block_num"], word["par_num"], word["line_num"])].append(word)
        lines = [(" ".join(w["text"] for w in words), sum(float(w["conf"]) for w in words)/len(words)) for words in groups.values()]
        return {"players":parse_lines(lines), "rawText":"\n".join(text for text, _ in lines),
                "message":"Review every name and number. Positions default to Utility when unreadable; bats and throws default to Right. Photos are discarded after reading."}
