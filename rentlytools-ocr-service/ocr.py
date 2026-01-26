from flask import Flask, request, jsonify
import easyocr
from deepface import DeepFace
import numpy as np
import os

app = Flask(__name__)

reader = easyocr.Reader(['en'], gpu=False)

@app.route("/ocr", methods=["POST"])
def ocr():
    if 'file' not in request.files:
        return jsonify({"error": "no file"}), 400

    file = request.files['file']
    image_bytes = file.read()

    result = reader.readtext(image_bytes, detail=0)
    text = " ".join(result)

    return jsonify({"text": text})

@app.route("/face-match", methods=["POST"])
def face_match():
    if "selfie" not in request.files or "aadhaar" not in request.files:
        return jsonify({"error": "selfie and aadhaar files required"}), 400

    selfie_path = "temp_selfie.jpg"
    aadhaar_path = "temp_aadhaar.jpg"

    request.files["selfie"].save(selfie_path)
    request.files["aadhaar"].save(aadhaar_path)

    try:
        emb1 = DeepFace.represent(
            img_path=selfie_path,
            model_name="Facenet512",
            detector_backend="opencv",
            enforce_detection=False
        )[0]["embedding"]

        emb2 = DeepFace.represent(
            img_path=aadhaar_path,
            model_name="Facenet512",
            detector_backend="opencv",
            enforce_detection=False
        )[0]["embedding"]

        emb1 = np.array(emb1, dtype=np.float32)
        emb2 = np.array(emb2, dtype=np.float32)

        similarity = float(
            np.dot(emb1, emb2) /
            (np.linalg.norm(emb1) * np.linalg.norm(emb2))
        )

        score = (similarity + 1) / 2
        emb_hash = str(hash(emb1.tobytes()))

        return jsonify({
            "score": score,
            "hash": emb_hash
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

    finally:
        if os.path.exists(selfie_path): os.remove(selfie_path)
        if os.path.exists(aadhaar_path): os.remove(aadhaar_path)

app.run(host="0.0.0.0", port=5000, debug=True)