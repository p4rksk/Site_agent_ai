import os
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings, ChatGoogleGenerativeAI
from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain_core.prompts import ChatPromptTemplate

load_dotenv()

def create_rag_chain(pdf_path: str):
    embeddings = GoogleGenerativeAIEmbeddings(
        model="gemini-embedding-001",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )

    # 벡터 DB 파일이 있으면 불러오기 
    if os.path.exists("data/vectorstore"):
        vectorstore = FAISS.load_local("data/vectorstore", embeddings, allow_dangerous_deserialization=True) # 경로, 임베딩, 위험한 직렬화(객제를 파일이나 네트워크로 전송할 수있는 형태로 변환) 허용
 
    else:
        # 1. PDF 읽기
        loader = PyPDFLoader(pdf_path)
        documents = loader.load()

        # 2. 텍스트 청크로 쪼개기
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        chunks = splitter.split_documents(documents)
    
        vectorstore = FAISS.from_documents(chunks, embeddings)
        vectorstore.save_local("data/vectorstore") #3. 벡터DB를 파일로 저장 (서버 재시작해도 유지)

    # 4. LLM 설정
    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )

    # 5. 프롬프트 설정 - LLM에게 어떻게 답변할지 지시하는 것
    prompt = ChatPromptTemplate.from_template("""
아래 문서 내용을 바탕으로 질문에 답변해주세요.
문서에 없는 내용은 "해당 내용은 문서에서 찾을 수 없습니다"라고 답변하세요.

문서 내용:
{context}

질문: {question}
""")

    # 6. RAG 체인 완성
    retriever = vectorstore.as_retriever()
    return {"retriever": retriever, "llm": llm, "prompt": prompt}