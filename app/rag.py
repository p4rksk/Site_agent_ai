import os
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings, ChatGoogleGenerativeAI
from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser

load_dotenv()

def create_rag_chain(pdf_path: str):
    # 1. PDF 읽기
    loader = PyPDFLoader(pdf_path)
    documents = loader.load()

    # 2. 텍스트 청크로 쪼개기
    splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
    chunks = splitter.split_documents(documents)

    # 3. 임베딩 → 벡터DB 저장
    embeddings = GoogleGenerativeAIEmbeddings(
        model="gemini-embedding-001",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )
    vectorstore = FAISS.from_documents(chunks, embeddings)

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
    chain = (
        {"context": retriever, "question": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
    )
    return chain