from fastapi import APIRouter, File, UploadFile, HTTPException, Form
from fastapi.responses import JSONResponse
from PIL import Image
import io
import numpy as np
import service.predict as predict_service
from controller.dto.metadados_dto import MetaDadosDTO

router = APIRouter(prefix="/predict")

@router.post("/")
async def predict(
    file: UploadFile = File(...),
    idade: int = Form(...),
    sexo: str = Form(...),
    localizacao: str = Form(...)
):
    dto = MetaDadosDTO(
        idade=idade,
        sexo=sexo,
        localizacao=localizacao
    )

    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="O arquivo enviado não é uma imagem válida.")
    
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        img_array = np.array(image)

        result = await predict_service.diagnostic(img_array, dto)
        return JSONResponse(result)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao processar a imagem: {str(e)}")

@router.get("/hello")
async def hello_world():
    try:
        with open("hello_world.txt", "a") as f:
            f.write("ola mundo\n")
        return {"message": "Ola mundo escrito com sucesso!"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao escrever no arquivo: {str(e)}")
