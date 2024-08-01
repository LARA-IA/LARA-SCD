from keras.models import load_model
from keras.preprocessing import image
from PIL import Image
import numpy as np

MODEL_PATH='models/model_v1.h5'

model = load_model(MODEL_PATH)
class_dict = {
    0: "Basal_Cell_Carcinoma (Cancer)",
    1: "Melanoma (Cancer)",
    2: "Nevus (Non-Cancerous)"
}

def model_predict(img_path):
    img = Image.open(img_path).convert('RGB').resize((224, 224))  # Convert to RGB and resize
    img = image.img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = img.astype('float32') / 255
    preds = model.predict(img)[0]
    prediction = sorted(
        [(class_dict[i], round(j * 100, 2)) for i, j in enumerate(preds)],
        reverse=True,
        key=lambda x: x[1]
    )
    return prediction, img

pach = 'logo.png'

pred,img = model_predict(pach)

print(pred)