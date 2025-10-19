import logging
import controller.dto.metadados_dto as metadados_dto


logger = logging.getLogger(__name__)


async def diagnostic(img_array, dto: metadados_dto.MetaDadosDTO):
    if img_array is None:
        return {"predictions": [], "error": "Imagem inválida ou não fornecida"}

    try:
        results = [] # model(img_array)

        classes = ["Benigno", "Maligno"]

        predictions = {}

        if(dto):
                return {"predictions": dto.idade}

        else:
            return {"predictions": dto.idade}

    except Exception as e:
        logger.error(f"Erro: {e}")
        return {"predictions": [], "error": str(e)}