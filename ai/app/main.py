from fastapi import FastAPI

app = FastAPI(title="GameLens AI", version="0.1.0")

@app.get("/health")
def health():
    return {"status": "ok", "service": "gamelens-ai"}
