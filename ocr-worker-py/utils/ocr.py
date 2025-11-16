import pytesseract
from pdf2image import convert_from_path

def perform_ocr(pdf_path):
    images = convert_from_path(pdf_path)
    text = ""
    for i, img in enumerate(images, 1):
        print(f"OCR page {i}")
        text += pytesseract.image_to_string(img, lang="eng") + "\n\n"
    return text
