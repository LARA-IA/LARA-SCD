import numpy as np
from PIL import Image
import io
import base64
import asyncio
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

def process_message(data: dict):
    """Processa mensagens recebidas do RabbitMQ."""
    try:
        if 'name' in data:
            print(f"     Nome: {data.get('name')}, Sobrenome: {data.get('surname')}")
        elif 'imageBase64' in data:
            print(f"     Imagem recebida: {data.get('filename')}")
            image_data = base64.b64decode(data.get('imageBase64'))
            image = Image.open(io.BytesIO(image_data)).convert("RGB")
            img_array = np.array(image)
            
            # DTO com valores padrão
            dto = MetaDadosDTO(idade=0, sexo="N/A", localizacao="N/A")
            
            # Executar função async em contexto sync
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            result = loop.run_until_complete(diagnostic(img_array, dto))
            loop.close()
            
            print(f"     Resultado da Classificação: {result}")
            return result
        else:
            print("     Formato de mensagem desconhecido")
    except Exception as e:
        print(f"     Erro ao processar mensagem no service: {e}")