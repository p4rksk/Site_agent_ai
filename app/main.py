from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import shutil
import os
from app.rag import create_rag_chain 

app = FastAPI()

# CORS 설정 - 다른 주소에서 오는 요청을 별도의 인증 없이 허용하는 설정
# Spring의 @CrossOrigin 이랑 똑같은 역할
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

if os.path.exists("data/vectorstore"):
    rag_chain = create_rag_chain(None)
else:
    rag_chain = None

class QuestionRequest(BaseModel):
    question: str

@app.post("/")
async def root():
    return {"status": "ok"}

@app.post("/upload-pdf")
async def upload_pdf(file: UploadFile = File(...)):
    # PDF 파일을 data 폴더에 저장
    global rag_chain
    file_path = f"data/{file.filename}"
    with open(file_path, "wb") as f:
        shutil.copyfileobj(file.file, f)
    
    # RAG 체인 생성
    rag_chain = create_rag_chain(file_path)
    return {"message": f"{file.filename} 업로드 완료. 질문할 수 있어요!"}

@app.post("/ask")
async def ask_question(request: QuestionRequest):
    if rag_chain is None:
        return {"error": "등록된 PDF가 없습니다."}
    
    result = rag_chain.invoke(request.question)
    return {"answer": result}