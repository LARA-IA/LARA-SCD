import numpy as np
from PIL import Image
import io
import logging
from controller.dto.metadados_dto import MetaDadosDTO

logger = logging.getLogger(__name__)

try:
    # Em um cenário real seriam:
    # model_binary = YOLO("models/binary_model.pt")
    # model_benigno = YOLO("models/benigno_model.pt")
    # model_maligno = YOLO("models/maligno_model.pt")
    pass
except Exception as e:
    logger.error(f"Failed to load  model: {e}")

async def diagnostic(img_array, dto: MetaDadosDTO):
    if img_array is None:
        return {"predictions": [], "error": "Imagem inválida ou não fornecida"}

    try:
        # Array of primary classes
        classes_binary = ["benigno", "maligno"]

        # TODO: substituir pelo `model_binary(img_array)` real no futuro
        # Simulated result from model_binary
        class_id = 1 # Force maligno for test or use logic
        probabilidade_binary = 0.9542
        
        predicted_binary = classes_binary[class_id]
        
        print(f"Classe Predita (Binária): {predicted_binary}")
        print(f"Probabilidade: {probabilidade_binary:.4f}")
        
        predictions_final = []

        if predicted_binary == "benigno":
            classes_mult_benigno = ["nv", "bkl", "df", "vasc"]
            
            # TODO: substituir pelo `model_benigno(img_array)` real
            class_id_multi = 0 # ex: nv
            probabilidade_multi = 0.8876
            
            predicted_multi = classes_mult_benigno[class_id_multi]
            
            print(f"Classe Multiclasse Benigna Predita: {predicted_multi}")
            print(f"Probabilidade Multiclasse: {probabilidade_multi:.4f}")
            
            predictions_final.append({
                "Class": predicted_binary,
                "Probabilidade": probabilidade_binary,
                "MultClass": predicted_multi,
                "ProbabilidadeMultClass": probabilidade_multi
            })

        elif predicted_binary == "maligno":
            classes_mult_maligno = ["mel", "bcc", "akiec"]
            
            # TODO: substituir pelo `model_maligno(img_array)` real
            class_id_multi = 0 # ex: mel
            probabilidade_multi = 0.9231
            
            predicted_multi = classes_mult_maligno[class_id_multi]
            
            print(f"Classe Multiclasse Maligna Predita: {predicted_multi}")
            print(f"Probabilidade Multiclasse: {probabilidade_multi:.4f}")
            
            predictions_final.append({
                "Class": predicted_binary,
                "Probabilidade": probabilidade_binary,
                "MultClass": predicted_multi,
                "ProbabilidadeMultClass": probabilidade_multi
            })
            
        return {"predictions": predictions_final}

    except Exception as e:
        logger.error(f"Erro: {e}")
        return {"predictions": [], "error": str(e)}