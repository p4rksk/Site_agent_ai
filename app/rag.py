import os
import tempfile
import requests
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings, ChatGoogleGenerativeAI
from langchain_community.document_loaders import PyMuPDFLoader
from langchain_community.vectorstores import FAISS
from langchain_community.document_loaders.parsers import LLMImageBlobParser
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.prompts import ChatPromptTemplate


load_dotenv()


def create_rag_chain(pdf_url: str):
    embeddings = GoogleGenerativeAIEmbeddings(
        model="gemini-embedding-001",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )

    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )

    response = requests.get(pdf_url)

    with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
        tmp.write(response.content)
        tmp_path = tmp.name

    try:
        loader = PyMuPDFLoader(
            tmp_path,
            mode="page",
            extract_images=True,
            images_inner_format="markdown-img",
            images_parser=LLMImageBlobParser(model=llm)
        )

        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        chunks = []

        for documents in loader.lazy_load():
            print(f"[DEBUG] 페이지 내용 길이: {len(documents.page_content)}")
            print(f"[DEBUG] 페이지 내용 미리보기: {documents.page_content[:500]}...")
            chunks.extend(splitter.split_documents([documents]))
            print(f"[DEBUG] 총 청크 수: {len(chunks)}")
        vectorstore = FAISS.from_documents(chunks, embeddings)

    finally:
        # 임시파일 삭제
        os.unlink(tmp_path)

    
    # 5. 프롬프트 설정 - LLM에게 어떻게 답변할지 지시하는 것
    prompt = ChatPromptTemplate.from_template("""
아래 문서 내용을 바탕으로 질문에 답변해주세요.
문서에 없는 내용은 "해당 내용은 문서에서 찾을 수 없습니다"라고 답변하세요.

문서 내용:
{context}

질문: {question}
""")

    # 6. RAG 체인 완성 - 유사도 높은 청크 2개만 가져오기 (속도 개선 + 관련없는 파일 제거)
    retriever = vectorstore.as_retriever(
        search_kwargs={"k": 3}
    )
    return {"retriever": retriever, "llm": llm, "prompt": prompt}