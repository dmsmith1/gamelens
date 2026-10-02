import io
from unittest.mock import patch
import subprocess
import pytest
from PIL import Image
from fastapi.testclient import TestClient
from app.main import app
from app.roster_ocr import parse_lines, extract, MAX_BYTES

client = TestClient(app)

def photo():
    out = io.BytesIO()
    Image.new("RGB", (100, 100), "white").save(out, "PNG")
    return out.getvalue()

def test_parser_numbered_rows_and_review_only_uncertain_names():
    rows = parse_lines([("Jersey Name Position", 95), ("1 12 Alex Smith SS", 92), ("8 O’Neil, Jamie", 81), ("Taylor Jones 4", 89), ("Rockets", 90), ("123 Sam Brown", 80)])
    assert len(rows) == 5
    assert rows[0]["jerseyNumber"] == "12" and rows[0]["primaryPosition"] == "SS"
    assert rows[1]["firstName"] == "Jamie" and rows[1]["lastName"] == "O’Neil"
    assert rows[2]["jerseyNumber"] == "4"
    assert not rows[3]["include"] and not rows[4]["include"]

def test_file_validation():
    assert client.post("/roster/extract", files={"photo":("bad.jpg", b"not an image", "image/jpeg")}).status_code == 400
    assert client.post("/roster/extract", files={"photo":("big.jpg", b"x"*(MAX_BYTES+1), "image/jpeg")}).status_code == 413

def test_ocr_endpoint_produces_draft_without_saving():
    tsv = "level\tpage_num\tblock_num\tpar_num\tline_num\tconf\ttext\n5\t1\t1\t1\t1\t96\t12\n5\t1\t1\t1\t1\t94\tAlex\n5\t1\t1\t1\t1\t95\tSmith\n"
    with patch("app.roster_ocr.subprocess.run", return_value=subprocess.CompletedProcess([], 0, stdout=tsv)):
        response = client.post("/roster/extract", files={"photo":("roster.png", photo(), "image/png")})
    assert response.status_code == 200
    assert response.json()["players"][0]["firstName"] == "Alex"
    assert response.json()["players"][0]["include"]

def test_ocr_timeout_has_actionable_error():
    with patch("app.roster_ocr.subprocess.run", side_effect=subprocess.TimeoutExpired("tesseract",25)):
        response=client.post("/roster/extract",files={"photo":("roster.png",photo(),"image/png")})
    assert response.status_code==504

def test_real_printed_roster_ocr():
    import shutil
    from pathlib import Path
    from PIL import ImageDraw, ImageFont
    if not shutil.which('tesseract'):
        pytest.skip('Tesseract is installed in Docker and CI')
    font_path=Path('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf')
    if not font_path.exists():
        pytest.skip('Printed fixture font is installed in CI')
    image=Image.new('RGB',(1600,700),'white')
    draw=ImageDraw.Draw(image)
    font=ImageFont.truetype(str(font_path),48)
    for i,line in enumerate(['Jersey Name Position','12 Alex Smith SS','8 Jamie Brown CF']):
        draw.text((80,80+i*140),line,font=font,fill='black')
    out=io.BytesIO();image.save(out,'PNG')
    response=client.post('/roster/extract',files={'photo':('printed.png',out.getvalue(),'image/png')})
    assert response.status_code==200
    drafts=response.json()['players']
    assert [(p['firstName'],p['lastName'],p['jerseyNumber']) for p in drafts]==[('Alex','Smith','12'),('Jamie','Brown','8')]


def test_lineup_columns_numbered_positions_extra_hitter_and_double_zero():
    rows=parse_lines([("1 9 Alex Smith 2",90),("2 00 Jamie Brown 3",90),("3 2 Taylor Jones EH",90)])
    assert rows[0]["jerseyNumber"]=="9" and rows[0]["primaryPosition"]=="C"
    assert rows[1]["jerseyNumber"]=="00" and rows[1]["primaryPosition"]=="FIRST_BASE"
    assert rows[2]["primaryPosition"] is None and rows[2]["lineupRole"]=="EXTRA_HITTER"

def test_low_confidence_is_not_preselected():
    assert not parse_lines([("12 Alex Smith 6",35)])[0]["include"]
