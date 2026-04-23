import os
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings, ChatGoogleGenerativeAI
from langchain_community.document_loaders import PyMuPDFLoader
from langchain_community.vectorstores import FAISS
from langchain_community.document_loaders.parsers import LLMImageBlobParser
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.prompts import ChatPromptTemplate


load_dotenv()


def create_rag_chain(pdf_path: str):
    embeddings = GoogleGenerativeAIEmbeddings(
        model="gemini-embedding-001",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )

    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )


    if pdf_path is None:
        # 서버 시작 시 기존 vectorstore 불러오기
        vectorstore = FAISS.load_local("data/vectorstore", embeddings, allow_dangerous_deserialization=True)

    elif os.path.exists("data/vectorstore"):
        # 기존 vectorstore 불러오기
        existing = FAISS.load_local("data/vectorstore", embeddings, allow_dangerous_deserialization=True)

        # 새 PDF 임베딩
        loader = PyMuPDFLoader(
            pdf_path, 
            mode="page",
            images_inner_format="markdown-img", #  ![이미지설명](링크) 형태로 저장
            images_parser=LLMImageBlobParser(model=llm)
        )
    
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100) 
        chunks = []
        try:
            for documents in loader.lazy_load():
             
                chunks.extend(splitter.split_documents([documents]))
        except Exception as e:
            print(f"PDF 로딩 에러: {e}")
           
        new_vectorstore = FAISS.from_documents(chunks, embeddings)
        # 기존 + 새 PDF 합치기
        existing.merge_from(new_vectorstore)
        existing.save_local("data/vectorstore")
        vectorstore = existing

    else:
        # vectorstore 없으면 새로 만들기
        loader = PyMuPDFLoader(
            pdf_path, 
            mode="page",
            images_inner_format="markdown-img", #  ![이미지설명](링크) 형태로 저장
            images_parser=LLMImageBlobParser(model=llm)
        )
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        chunks = []

        for documents in loader.lazy_load(): # PDF의 한페이지씩 쪼개서 문서 파일로 변환 메모리 효율적 사용
            chunks.extend(splitter.split_documents([documents]))

        vectorstore = FAISS.from_documents(chunks, embeddings)
        vectorstore.save_local("data/vectorstore")

    
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
        search_type="similarity_score_threshold",
        search_kwargs={"score_threshold": 0.6, "k": 2}
    )
    return {"retriever": retriever, "llm": llm, "prompt": prompt}