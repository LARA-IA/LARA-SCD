import logging
from controller.dto.metadados_dto import MetaDadosDTO


logger = logging.getLogger(__name__)
classes = ["benigno", "maligno"]

mult_classes_benigno = ["nv","bkl","df","vasc"]
mult_classes_maligno = ["mel","bcc","akiec"]

async def diagnostic(img_array, dto: MetaDadosDTO):
    if img_array is None:
        return {"predictions": [], "error": "Imagem inválida ou não fornecida"}

    try:
        results = [] # model(img_array)

        
        predictions = {}

        if(dto):
            return {"predictions": dto.idade}

        else:
            return {"predictions": dto.idade}

    except Exception as e:
        logger.error(f"Erro: {e}")
        return {"predictions": [], "error": str(e)}
    
async def mult_benigno():
     return True

async def mult_maligno():
     return True