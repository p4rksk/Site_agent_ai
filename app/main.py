from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import shutil
import os
from app.rag import create_rag_chain 
from langchain_core.output_parsers import StrOutputParser

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

@app.get("/")
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

    retriever = rag_chain["retriever"]
    llm = rag_chain["llm"]
    prompt = rag_chain["prompt"]

    # 관련 문서 검색
    search = retriever.invoke(request.question)

    # 페이지 번호 + 파일명 추출
    sources = []
    for result in search:
        page = result.metadata.get("page", 0) +1 # LangChain의 페이지 번호는 0부터 시작하므로 1을 더함
        file = result.metadata.get("source", "unknown")
        if {"page": page, "file": file} not in sources: # 중복제거
            sources.append({"page": page, "file": file})

    # LLM 답변
    context = "\n".join([result.page_content for result in search])
    chain = prompt | llm | StrOutputParser() # 응답 파이프라인 
    answer = chain.invoke({"context": context, "question": request.question})
    
    return {"answer": answer, "sources": sources}