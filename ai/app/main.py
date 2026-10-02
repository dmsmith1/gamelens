from fastapi import FastAPI, UploadFile, File
from starlette.concurrency import run_in_threadpool
from app.roster_ocr import extract, MAX_BYTES

app = FastAPI(title="GameLens AI", version="0.2.0")

@app.get("/health")
def health():
    return {"status": "ok", "service": "gamelens-ai"}

@app.post("/roster/extract")
async def roster_extract(photo: UploadFile = File(...)):
    try:
        data = await photo.read(MAX_BYTES + 1)
        return await run_in_threadpool(extract, data)
    finally:
        await photo.close()
