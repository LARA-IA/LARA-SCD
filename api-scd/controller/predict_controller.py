from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse
from keras.models import load_model
from PIL import Image
import base64
import numpy as np
from predictions.predict_service import model_predict

router =APIRouter(prefix="/predict")
pathImage = "predictions/image"


@router.post("")
async def predict(image: UploadFile = File(...)):
    path = f"tempImage/{image.filename}"
    with open(path,"wb+") as file:
       file.write(await image.read()) 
    pred , img= model_predict(path)
    data={
            "predict":pred
        }
    return JSONResponse(status_code=200,content=data)

@router.get("")
def test():
    return JSONResponse(status_code=200,content={"message":"oi"})

@router.post("/uploadfile/")
async def create_upload_file(file: UploadFile = File(...)):
    img_name = pathImage + "/Predicition_"+file.filename

    with open(img_name,"wb+") as img:
        img.write(await file.read())

    with open(img_name,"rb") as f:
        data={
            "responseIMG": f"{base64.b64encode(f.read())}",
            "predict":"lepra"
        }
        return JSONResponse(status_code=200,content=data)




