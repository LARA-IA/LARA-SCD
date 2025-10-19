from fastapi import APIRouter, File, UploadFile, HTTPException, Form
from fastapi.responses import JSONResponse
from PIL import Image
import io
import numpy as np
import service.predict as predict_service
import controller.dto.metadados_dto as metadados_dto

router = APIRouter(prefix="/predict")

@router.post("")
async def predict(file: UploadFile = File(...), dto: metadados_dto.MetaDadosDTO = Form(...)):
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="File provided is not an image")
    
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        img_array = np.array(image)
        
        content = await predict_service.diagnostic(img_array)
        return JSONResponse(content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))