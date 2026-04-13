from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import shutil
import os
from app.rag import create_rag_chain 
from langchain_core.output_parsers import StrOutputParser
from fastapi.responses import FileResponse
import asyncio

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
        file = os.path.basename(result.metadata.get("source", "unknown"))
        if {"page": page, "file": file} not in sources: # 중복제거
            sources.append({"page": page, "file": file})

    # LLM 답변
    context = "\n".join([result.page_content for result in search])
    chain = prompt | llm | StrOutputParser() # 응답 파이프라인 
    answer = chain.invoke({"context": context, "question": request.question})

    # 503 에러 시 3번까지 재시도 (Gemini 서버 과부하 대비)
    for attempt in range(3):
        try:
            answer = chain.invoke({"context": context, "question": request.question})
            break
        except Exception as e:
            if attempt == 2:  # 3번 다 실패하면
                return {
                    "answer": "현재 AI 서버가 혼잡합니다. 잠시 후 다시 시도해주세요.",
                    "sources": []
                }
            await asyncio.sleep(2) # 2초 기다렸다가 재시도

    if "찾을 수 없습니다" in answer:
        return {"answer": answer, "sources": []}

    return {"answer": answer, "sources": sources}

@app.get("/download/{filename}")
async def download_pdf(filename: str):
    file_path = f"data/{filename}"
    if not os.path.exists(file_path):
        return {"error": "파일을 찾을 수 없습니다."}
    return FileResponse(file_path, media_type="application/pdf", filename=filename)