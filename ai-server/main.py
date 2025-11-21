"""
AI Anomaly Detection Server - Main Application

FastAPI 기반 실시간 이상 탐지 서비스
"""
from fastapi import FastAPI
from contextlib import asynccontextmanager
import logging
import uvicorn

from src.api.routers import router
from src.config import settings, validate_settings
from src.db.repositories import get_outbox_repository

# 로깅 설정
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    애플리케이션 생명주기 관리 (lifespan 이벤트)

    startup/shutdown 이벤트를 대체하는 권장 방식
    """
    # Startup
    logger.info("=" * 50)
    logger.info("AI Anomaly Detection Server 시작 중...")
    logger.info("=" * 50)

    try:
        # 1. 설정 검증
        logger.info("설정 검증 중...")
        validate_settings()
        logger.info("✅ 설정 검증 완료")

        # 2. 데이터베이스 테이블 생성 (개발 환경용)
        logger.info("데이터베이스 테이블 확인 중...")
        repository = get_outbox_repository()
        repository.create_tables()
        logger.info("✅ 데이터베이스 준비 완료")

        # 3. ML 모델 로딩 확인
        logger.info("ML 모델 로딩 확인 중...")
        from src.ml.predictor import get_predictor
        predictor = get_predictor()
        model_info = predictor.get_system_info()
        logger.info(f"✅ ML 모델 로딩 완료: {model_info['model_info']['model_type']}")

        # 4. 캐시 초기화
        logger.info("캐시 시스템 초기화 중...")
        from src.cache.machine_cache import get_machine_cache
        cache = get_machine_cache()
        logger.info(f"✅ 캐시 초기화 완료 (TTL: {settings.CACHE_TTL}초)")

        logger.info("=" * 50)
        logger.info("🚀 AI Server 준비 완료!")
        logger.info(f"📡 API: http://{settings.API_HOST}:{settings.API_PORT}")
        logger.info(f"📚 Docs: http://{settings.API_HOST}:{settings.API_PORT}/docs")
        logger.info("=" * 50)

        yield  # 애플리케이션 실행

    except Exception as e:
        logger.error(f"❌ 시작 중 오류 발생: {e}")
        raise

    finally:
        # Shutdown
        logger.info("=" * 50)
        logger.info("AI Anomaly Detection Server 종료 중...")
        logger.info("=" * 50)

        # 리소스 정리
        logger.info("리소스 정리 중...")
        try:
            from src.kafka.producer import get_alert_producer
            from src.cache.machine_cache import get_machine_cache

            get_alert_producer().close()
            get_machine_cache().close()
            get_outbox_repository().close()

            logger.info("✅ 리소스 정리 완료")
        except Exception as e:
            logger.error(f"리소스 정리 중 오류: {e}")

        logger.info("=" * 50)
        logger.info("👋 AI Server 종료 완료")
        logger.info("=" * 50)


# FastAPI 애플리케이션 생성
app = FastAPI(
    title="AI Anomaly Detection Server",
    description="실시간 머신 이상 탐지 서비스",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json"
)

# 라우터 등록
app.include_router(router)


# 직접 실행 시
if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.API_HOST,
        port=settings.API_PORT,
        reload=settings.API_RELOAD,
        log_level=settings.LOG_LEVEL.lower()
    )