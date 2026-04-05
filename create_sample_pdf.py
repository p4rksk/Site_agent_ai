from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import os

def create_sample_pdf():
    # 윈도우 기본 한글 폰트 등록
    font_path = "C:/Windows/Fonts/malgun.ttf"
    pdfmetrics.registerFont(TTFont("Korean", font_path))

    file_path = "data/sample_safety.pdf"
    pdf = canvas.Canvas(file_path, pagesize=A4)
    width, height = A4
    pdf.setFont("Korean", 12)

    lines = [
        "현장 안전수칙 가이드",
        "",
        "1. 화재 대피 요령",
        "- 화재 발생 시 즉시 비상벨을 누르세요.",
        "- 엘리베이터 사용을 금지하고 비상계단을 이용하세요.",
        "- 연기가 많을 경우 낮은 자세로 이동하세요.",
        "",
        "2. 전기 안전수칙",
        "- 젖은 손으로 전기 기기를 만지지 마세요.",
        "- 전선이 끊어진 경우 즉시 관리자에게 보고하세요.",
        "",
        "3. 추락 방지 수칙",
        "- 2미터 이상 고소 작업 시 안전벨트를 착용하세요.",
        "- 안전모는 현장 진입 시 반드시 착용하세요.",
        "",
        "4. 비상 연락처",
        "- 현장 관리자: 010-1234-5678",
        "- 소방서: 119",
        "- 응급실: 112",
    ]

    y = height - 50
    for line in lines:
        pdf.drawString(50, y, line)
        y -= 25

    pdf.save()
    print(f"PDF 생성 완료: {file_path}")

create_sample_pdf()