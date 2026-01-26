FROM python:3.10-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    libxcb1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy and install requirements
COPY requirements.txt .
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# --- ADD THESE LINES TO PRE-DOWNLOAD MODELS ---
RUN python -c "import easyocr; easyocr.Reader(['en'])"
RUN python -c "import os; os.environ['TF_CPP_MIN_LOG_LEVEL']='3'; from deepface import DeepFace; DeepFace.build_model('VGG-Face')"
# ----------------------------------------------

COPY . .

# Ensure ocr.py uses app.run(host='0.0.0.0', port=5000)
CMD ["python", "ocr.py"]