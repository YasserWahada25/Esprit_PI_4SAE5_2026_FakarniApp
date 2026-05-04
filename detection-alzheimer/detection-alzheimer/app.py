import os
import tensorflow as tf
import numpy as np
from PIL import Image
import io
from flask import Flask, request, jsonify
from flask_cors import CORS
from tensorflow.keras.applications.efficientnet import preprocess_input

app = Flask(__name__)
CORS(app)

# ══════════════════════════════════════
#  CHARGEMENT DU MODÈLE
# ══════════════════════════════════════
MODEL_PATH = "./alzheimer_final_v3"

print("⏳ Chargement du modèle EfficientNetB3...")
loaded = tf.saved_model.load(MODEL_PATH)
infer = loaded.signatures["serving_default"]

# ── Détecter clé entrée / sortie ──
input_keys = list(infer.structured_input_signature[1].keys())
output_keys = list(infer.structured_outputs.keys())

IN_KEY = input_keys[0]
OUT_KEY = output_keys[0]

print(f"🔑 Clé entrée  : {IN_KEY}")
print(f"🔑 Clé sortie  : {OUT_KEY}")

# ── Test dummy (diagnostic modèle) ──
print("\n🧪 Test modèle:")
print("Image noire:")
print(infer(**{IN_KEY: tf.zeros((1,224,224,3), dtype=tf.float32)})[OUT_KEY].numpy())

print("Image blanche:")
print(infer(**{IN_KEY: tf.ones((1,224,224,3), dtype=tf.float32)})[OUT_KEY].numpy())

print("✅ Modèle prêt !")


# ══════════════════════════════════════
#  CONFIGURATION
# ══════════════════════════════════════
CLASS_NAMES = [
    "Mild_Demented",
    "Moderate_Demented",
    "Non_Demented",
    "Very_Mild_Demented"
]

RISK_CONFIG = {
    "Non_Demented": {
        "level": 0,
        "label": "AUCUN",
        "color": "GREEN",
        "description": "Aucun signe de démence détecté.",
        "recommendations": [
            "Suivi annuel recommandé",
            "Maintenir une activité physique régulière",
            "Alimentation saine et équilibrée"
        ]
    },
    "Very_Mild_Demented": {
        "level": 1,
        "label": "FAIBLE",
        "color": "YELLOW",
        "description": "Signes très précoces détectés.",
        "recommendations": [
            "Consultation neurologique conseillée",
            "Tests cognitifs MMSE recommandés",
            "Suivi IRM dans 6 mois"
        ]
    },
    "Mild_Demented": {
        "level": 2,
        "label": "MODERE",
        "color": "ORANGE",
        "description": "Démence légère détectée.",
        "recommendations": [
            "Consultation neurologique urgente",
            "Traitement médicamenteux à discuter",
            "Suivi IRM dans 3 mois",
            "Informer la famille"
        ]
    },
    "Moderate_Demented": {
        "level": 3,
        "label": "ELEVE",
        "color": "RED",
        "description": "Démence modérée détectée.",
        "recommendations": [
            "Hospitalisation ou consultation immédiate",
            "Protocole thérapeutique urgent",
            "Prise en charge complète",
            "Alerte famille et aidants"
        ]
    }
}

ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg"}


# ══════════════════════════════════════
#  PRÉTRAITEMENT (CORRIGÉ)
# ══════════════════════════════════════
def preprocess_image(file_bytes):
    """
    Preprocessing correct pour EfficientNet
    """
    img = Image.open(io.BytesIO(file_bytes)).convert("RGB")
    img = img.resize((224, 224))

    arr = np.array(img, dtype=np.float32)

    # ⭐ PREPROCESSING OFFICIEL EFFICIENTNET
    arr = preprocess_input(arr)

    arr = np.expand_dims(arr, axis=0)

    tensor = tf.constant(arr, dtype=tf.float32)

    print(f"  📐 Shape  : {tensor.shape}")
    print(f"  📊 Min    : {float(tf.reduce_min(tensor)):.3f}")
    print(f"  📊 Max    : {float(tf.reduce_max(tensor)):.3f}")
    print(f"  📊 Mean   : {float(tf.reduce_mean(tensor)):.3f}")

    return tensor


# ══════════════════════════════════════
#  ROUTES
# ══════════════════════════════════════

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "UP",
        "model": "EfficientNetB3",
        "version": "3.1.0",
        "input_key": IN_KEY,
        "output_key": OUT_KEY
    }), 200


@app.route("/api/v1/predict", methods=["POST"])
def predict():

    if "image" not in request.files:
        return jsonify({"error": "Aucune image fournie"}), 400

    file = request.files["image"]

    if file.filename == "":
        return jsonify({"error": "Nom fichier vide"}), 400

    ext = file.filename.rsplit(".", 1)[-1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        return jsonify({"error": f"Format non supporté: {ext}"}), 400

    try:
        file_bytes = file.read()
        print(f"\n📁 Fichier reçu : {file.filename} ({len(file_bytes)} bytes)")

        # preprocessing
        img_tensor = preprocess_image(file_bytes)

        # prediction
        result = infer(**{IN_KEY: img_tensor})
        probs = result[OUT_KEY].numpy()[0]

        class_idx = int(np.argmax(probs))
        predicted = CLASS_NAMES[class_idx]
        confidence = round(float(probs[class_idx]) * 100, 2)

        print(f"  🎯 Prédit : {predicted} ({confidence}%)")
        print(f"  📊 Probs  : {dict(zip(CLASS_NAMES, probs.round(3)))}")

        return jsonify({
            "prediction": predicted,
            "confidence": confidence,
            "risk": RISK_CONFIG[predicted],
            "probabilities": {
                CLASS_NAMES[i]: round(float(probs[i]) * 100, 2)
                for i in range(len(CLASS_NAMES))
            }
        }), 200

    except Exception as e:
        import traceback
        print("❌ ERREUR:", traceback.format_exc())
        return jsonify({"error": str(e)}), 500


@app.route("/api/v1/classes", methods=["GET"])
def get_classes():
    return jsonify({
        "classes": CLASS_NAMES,
        "total": len(CLASS_NAMES)
    })


# ══════════════════════════════════════
#  LANCEMENT
# ══════════════════════════════════════
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=False)
