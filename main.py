
from fastapi import FastAPI
import uvicorn
from fastapi.middleware.cors import CORSMiddleware
from controller.predict_controller import router as router_predict
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router_predict)

if __name__ == "__main__":
    
    uvicorn.run(app, host="0.0.0.0", port=8000)
