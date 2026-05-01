import os
import tempfile
import requests
import base64
import fitz
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings, ChatGoogleGenerativeAI
from langchain_community.document_loaders import PyMuPDFLoader
from langchain_community.vectorstores import FAISS
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.documents import Document
from langchain_core.messages import HumanMessage

load_dotenv()


def extract_images_from_pdf(pdf_path: str, llm) -> list:
    """PDF에서 이미지 추출 후 Gemini Vision으로 텍스트 변환"""
    image_documents = []
    pdf = fitz.open(pdf_path)

    for page_num in range(len(pdf)):
        page = pdf[page_num]
        image_list = page.get_images()

        for img in image_list:
            xref = img[0]
            base_image = pdf.extract_image(xref)
            image_bytes = base_image["image"]
            image_b64 = base64.b64encode(image_bytes).decode("utf-8")
            ext = base_image["ext"]

            try:
                message = HumanMessage(content=[
                    {"type": "text", "text": "이 이미지에서 텍스트를 모두 추출해주세요. 텍스트가 없으면 이미지를 설명해주세요."},
                    {"type": "image_url", "image_url": {"url": f"data:image/{ext};base64,{image_b64}"}}
                ])
                response = llm.invoke([message])
                doc = Document(
                    page_content=response.content,
                    metadata={"source": pdf_path, "page": page_num}
                )
                image_documents.append(doc)
            except Exception as e:
                print(f"이미지 처리 실패: {e}")
                continue

    pdf.close()
    return image_documents


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
        # 1. 텍스트 추출 (lazy_load로 페이지별 처리)
        loader = PyMuPDFLoader(tmp_path, mode="page")
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        text_chunks = []

        for documents in loader.lazy_load():
            text_chunks.extend(splitter.split_documents([documents]))

        # 2. 이미지 OCR 
        image_docs = extract_images_from_pdf(tmp_path, llm)
        image_chunks = splitter.split_documents(image_docs)

        # 3. 합치기
        chunks = text_chunks + image_chunks
        print(f"[DEBUG] 텍스트 청크: {len(text_chunks)}, 이미지 청크: {len(image_chunks)}")
        print(f"[DEBUG] 총 청크 수: {len(chunks)}")

        vectorstore = FAISS.from_documents(chunks, embeddings)

    finally:
        try:
            os.unlink(tmp_path)
        except:
            pass

    prompt = ChatPromptTemplate.from_template("""
아래 문서 내용을 바탕으로 질문에 답변해주세요.
문서에 없는 내용은 "해당 내용은 문서에서 찾을 수 없습니다"라고 답변하세요.

문서 내용:
{context}

질문: {question}
""")

    retriever = vectorstore.as_retriever(
    search_type="similarity_score_threshold",
    search_kwargs={"score_threshold": 0.3, "k": 3}
)
    return {"retriever": retriever, "llm": llm, "prompt": prompt}