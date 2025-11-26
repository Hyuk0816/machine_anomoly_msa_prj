"""
Centralized Configuration Management
Loads environment variables and provides application settings
"""
import os
from pathlib import Path
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Application settings loaded from environment variables"""

    # Portal Database (for machine type lookup)
    DB_HOST: str
    DB_PORT: int
    DB_NAME: str
    DB_USER: str
    DB_PASSWORD: str

    # AI Server Database (for Outbox pattern)
    AI_DB_HOST: str
    AI_DB_PORT: int
    AI_DB_NAME: str
    AI_DB_USER: str
    AI_DB_PASSWORD: str

    # Kafka Configuration
    KAFKA_BOOTSTRAP_SERVERS: str
    KAFKA_TOPIC_SENSOR: str
    KAFKA_TOPIC_ALERT: str
    KAFKA_GROUP_ID: str

    # Cache Configuration
    CACHE_TTL: int
    CACHE_MAXSIZE: int

    # ML Model Paths
    MODEL_PATH: str
    SCALER_PATH: str
    ENCODER_PATH: str
    FEATURE_NAMES_PATH: str

    # API Configuration
    API_HOST: str
    API_PORT: int
    API_RELOAD: bool

    # Logging
    LOG_LEVEL: str

    # Anomaly Detection Thresholds (다단계 알람)
    WARNING_THRESHOLD: float = 0.3   # 30% - 경고 (WARNING)
    ALERT_THRESHOLD: float = 0.5     # 50% - 주의 (ALERT)
    CRITICAL_THRESHOLD: float = 0.7  # 70% - 위험 (CRITICAL)

    # 기존 호환성을 위한 별칭 (WARNING 이상부터 이상으로 간주)
    ANOMALY_THRESHOLD: float = 0.3

    class Config:
        env_file = ".env"
        case_sensitive = True

    @property
    def portal_db_url(self) -> str:
        """PostgreSQL connection URL for Portal DB (machine type lookup)"""
        return f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    @property
    def ai_db_url(self) -> str:
        """PostgreSQL connection URL for AI Server DB (Outbox)"""
        return f"postgresql://{self.AI_DB_USER}:{self.AI_DB_PASSWORD}@{self.AI_DB_HOST}:{self.AI_DB_PORT}/{self.AI_DB_NAME}"

    @property
    def kafka_servers_list(self) -> list:
        """Kafka bootstrap servers as list"""
        return self.KAFKA_BOOTSTRAP_SERVERS.split(',')

    def get_model_path(self, relative_path: str) -> Path:
        """
        Resolve model path relative to ai-server directory

        Args:
            relative_path: Relative path from .env (e.g., MODEL_PATH)

        Returns:
            Absolute Path object
        """
        base_dir = Path(__file__).parent.parent  # ai-server/
        return (base_dir / relative_path).resolve()


# Global settings instance
settings = Settings()


# Validation on import
def validate_settings():
    """Validate critical settings on startup"""
    errors = []

    # Check model files exist
    model_paths = {
        'model': settings.get_model_path(settings.MODEL_PATH),
        'scaler': settings.get_model_path(settings.SCALER_PATH),
        'encoder': settings.get_model_path(settings.ENCODER_PATH),
    }

    for name, path in model_paths.items():
        if not path.exists():
            errors.append(f"Model file not found: {name} at {path}")

    if errors:
        error_msg = "\n".join(errors)
        raise FileNotFoundError(f"Missing required model files:\n{error_msg}")


if __name__ == "__main__":
    # Test configuration
    print("=== AI Server Configuration ===")
    print(f"Portal DB URL: {settings.portal_db_url}")
    print(f"AI Server DB URL: {settings.ai_db_url}")
    print(f"Kafka Servers: {settings.kafka_servers_list}")
    print(f"Model Path: {settings.get_model_path(settings.MODEL_PATH)}")
    print(f"API: {settings.API_HOST}:{settings.API_PORT}")
    print(f"Log Level: {settings.LOG_LEVEL}")

    print("\n=== Validating Model Files ===")
    try:
        validate_settings()
        print("All model files found")
    except FileNotFoundError as e:
        print(f"Validation failed:\n{e}")