from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse
import base64

router =APIRouter(prefix="/predict")
pathImage = "predictions/image"

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




