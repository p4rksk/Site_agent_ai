import os
import fitz  # pymupdf
import base64
from PIL import Image
import io
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings, ChatGoogleGenerativeAI
from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.documents import Document
from langchain_core.messages import HumanMessage


load_dotenv()


def extract_images_from_pdf(pdf_path: str) -> list:
    """PDF에서 이미지 추출 후 Gemini Vision으로 텍스트 변환"""
    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )
    
    image_documents = []
    pdf = fitz.open(pdf_path)
    
    for page_num in range(len(pdf)):
        page = pdf[page_num]
        image_list = page.get_images()
        
        for img in image_list:
            xref = img[0]
            base_image = pdf.extract_image(xref)
            image_bytes = base_image["image"]
            
            # base64로 변환
            image_b64 = base64.b64encode(image_bytes).decode("utf-8")
            ext = base_image["ext"]
            
            # LangChain으로 Gemini Vision 호출
            try:
                message = HumanMessage(content=[
                    {"type": "text", "text": "이 이미지에서 텍스트를 모두 추출해주세요. 텍스트가 없으면 이미지를 설명해주세요."},
                    {"type": "image_url", "image_url": {"url": f"data:image/{ext};base64,{image_b64}"}}
                ])
                response = llm.invoke([message])
                text = response.content
                
                doc = Document(
                    page_content=text,
                    metadata={"source": pdf_path, "page": page_num}
                )
                image_documents.append(doc)
            except Exception as e:
                print(f"이미지 처리 실패: {e}")
                continue
    
    pdf.close()
    return image_documents


def create_rag_chain(pdf_path: str):
    embeddings = GoogleGenerativeAIEmbeddings(
        model="gemini-embedding-001",
        google_api_key=os.getenv("GOOGLE_API_KEY")
    )

    if pdf_path is None:
        # 서버 시작 시 기존 vectorstore 불러오기
        vectorstore = FAISS.load_local("data/vectorstore", embeddings, allow_dangerous_deserialization=True)

    elif os.path.exists("data/vectorstore"):
        # 기존 vectorstore 불러오기
        existing = FAISS.load_local("data/vectorstore", embeddings, allow_dangerous_deserialization=True)

        # 새 PDF 임베딩
        loader = PyPDFLoader(pdf_path)
        documents = loader.load()
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        chunks = splitter.split_documents(documents)
        new_vectorstore = FAISS.from_documents(chunks, embeddings)

        # 이미지 OCR 텍스트 추출
        image_docs = extract_images_from_pdf(pdf_path)
        all_chunks = chunks + image_docs

        new_vectorstore = FAISS.from_documents(all_chunks, embeddings)

        # 기존 + 새 PDF 합치기
        existing.merge_from(new_vectorstore)
        existing.save_local("data/vectorstore")
        vectorstore = existing

    else:
        # vectorstore 없으면 새로 만들기
        loader = PyPDFLoader(pdf_path)
        documents = loader.load()
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        chunks = splitter.split_documents(documents)

         # 이미지 OCR 텍스트 추출
        image_docs = extract_images_from_pdf(pdf_path)
        all_chunks = chunks + image_docs

        vectorstore = FAISS.from_documents(all_chunks, embeddings)
        vectorstore.save_local("data/vectorstore")

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

    # 6. RAG 체인 완성 - 유사도 높은 청크 2개만 가져오기 (속도 개선 + 관련없는 파일 제거)
    retriever = vectorstore.as_retriever(
        search_type="similarity_score_threshold",
        search_kwargs={"score_threshold": 0.5, "k": 2}
    )
    return {"retriever": retriever, "llm": llm, "prompt": prompt}