# 🏭 Predictive Maintenance - AI Model Development

## 📋 프로젝트 개요

설비 센서 데이터를 활용한 고장 예측 머신러닝 모델 개발 영역

**역할**: ML 모델 개발, 데이터 분석, 학습 및 모델 아티팩트 생성
- 운영 서버는 `ai-server/` 디렉토리 참조

## 📁 파일 구조

```
ai-model/
├── README.md                              # 이 파일
├── requirements.txt                       # ML 개발용 Python 패키지
├── predictive_maintenance.csv             # 원본 데이터셋 (10,000 records)
├── predictive_maintenance_analysis.ipynb  # Jupyter 분석 노트북 ⭐
├── model-predict-result.md               # 분석 결과 보고서
├── test_main.http                         # HTTP 테스트 파일 (ai-server용)
└── models/                                # 학습된 모델 아티팩트 (ai-server에서 사용)
    ├── final_model_xgboost.pkl            # XGBoost 모델
    ├── scaler.pkl                         # StandardScaler
    ├── label_encoder_type.pkl             # 머신 타입 인코더
    ├── feature_names.pkl                  # 특징 순서 정보
    └── model_comparison_results.csv       # 모델 비교 결과
```

## 🚀 시작하기

### 1. Python 환경 설정

Python 3.8 이상이 필요합니다.

```bash
# 가상환경 생성 (선택사항)
python -m venv venv
source venv/bin/activate  # macOS/Linux
# venv\Scripts\activate   # Windows

# 필요한 패키지 설치
pip install -r requirements.txt
```

### 2. Jupyter Notebook 실행

```bash
# Jupyter Notebook 시작
jupyter notebook

# 또는 JupyterLab 사용
jupyter lab
```

브라우저에서 `predictive_maintenance_analysis.ipynb` 파일을 열어 분석을 시작하세요.

### 3. 분석 실행

노트북의 모든 셀을 순차적으로 실행하세요:
- **Cell → Run All** 또는
- 각 셀을 하나씩 실행하며 결과 확인

## 📊 분석 프로세스

### Phase 1: 라이브러리 임포트 및 데이터 로드
- 필요한 라이브러리 임포트
- 데이터 로드 및 기본 정보 확인

### Phase 2: 탐색적 데이터 분석 (EDA)
- 타겟 변수 분포 분석
- 고장 유형 분석
- 제품 타입별 분석
- 수치형 특성 분포 및 상관관계
- 이상치 탐지

### Phase 3: 데이터 전처리 & Feature Engineering
- 6개의 파생 특성 생성
- Feature Scaling (StandardScaler)
- Class Imbalance 처리 (SMOTE)
- Train-Test Split (80/20)

### Phase 4: 모델 학습 및 비교
다음 9개 모델을 학습하고 비교:
1. Logistic Regression
2. Decision Tree
3. Random Forest
4. Gradient Boosting
5. XGBoost
6. LightGBM
7. CatBoost
8. SVM
9. KNN

### Phase 5: 모델 평가 및 선정
- 성능 지표 비교 (Accuracy, Precision, Recall, F1, ROC-AUC)
- Overfitting 분석
- 최적 모델 선정
- Feature Importance 분석

### Phase 6: 모델 저장 및 배포 준비
- 최종 모델 저장
- Scaler 및 Encoder 저장
- 분석 결과 리포트 작성

## 📈 평가 지표

각 모델은 다음 지표로 평가됩니다:

- **Accuracy**: 전체 정확도
- **Precision**: Failure 예측의 정밀도
- **Recall**: 실제 Failure 탐지율 (중요!)
- **F1-Score**: Precision과 Recall의 조화 평균
- **ROC-AUC**: ROC 곡선 아래 면적
- **Overfitting**: Train-Test Accuracy 차이 (< 0.1이 바람직)

## 🎯 선정 기준

최적 모델은 다음 기준으로 선정:

1. **높은 F1-Score** (우선순위 1)
2. **낮은 Overfitting** (Train-Test 차이 < 0.1)
3. **높은 Recall** (False Negative 최소화)
4. **높은 ROC-AUC**

## 📝 결과 확인

### 1. 노트북 실행 결과
- 각 셀의 출력을 확인하며 분석 결과를 검토
- 시각화를 통해 데이터 패턴 이해
- 모델 성능 비교 테이블 확인

### 2. 보고서 작성
- `model-predict-result.md` 파일에 분석 결과 기록
- 노트북의 실행 결과를 바탕으로 "[실행 후 기록]" 부분 채우기
- 비즈니스 인사이트 및 권고사항 작성

### 3. 저장된 모델 확인
```bash
ls -lh models/
```

## 🔧 트러블슈팅

### 문제 1: 패키지 설치 오류
```bash
# pip 업그레이드
pip install --upgrade pip

# 개별 패키지 설치
pip install pandas numpy matplotlib seaborn scikit-learn
pip install xgboost lightgbm catboost
pip install imbalanced-learn optuna
```

### 문제 2: Jupyter Notebook이 열리지 않음
```bash
# Jupyter 재설치
pip install --upgrade jupyter notebook

# 특정 포트로 실행
jupyter notebook --port=8889
```

### 문제 3: 메모리 부족 (대용량 데이터셋의 경우)
- 일부 모델만 선택적으로 실행
- 배치 크기 조정
- 불필요한 변수 삭제

## 📚 참고 자료

- [Kaggle: Failure Prediction with XGBoost](https://www.kaggle.com/code/huda1102/failure-pred-xgboost)
- [Scikit-learn Documentation](https://scikit-learn.org/)
- [XGBoost Documentation](https://xgboost.readthedocs.io/)
- [LightGBM Documentation](https://lightgbm.readthedocs.io/)
- [CatBoost Documentation](https://catboost.ai/docs/)
- [Imbalanced-learn Documentation](https://imbalanced-learn.org/)

## 🎓 완료된 작업

1. ✅ **노트북 실행**: 모든 셀 실행하여 분석 완료
2. ✅ **결과 검토**: 모델 성능 비교 및 최적 모델 확인 (XGBoost 98% accuracy)
3. ✅ **보고서 작성**: `model-predict-result.md` 완성
4. ✅ **모델 아티팩트 생성**: 5개 pkl 파일 저장
5. ✅ **AI Server 구현**: `ai-server/` 디렉토리에 운영 서버 완성

## 🔄 모델 재학습

새로운 데이터로 모델을 재학습하려면:

1. `predictive_maintenance.csv`에 새 데이터 추가 또는 교체
2. Jupyter 노트북 전체 재실행
3. `models/` 디렉토리의 pkl 파일들이 자동 업데이트됨
4. ai-server 재시작하여 새 모델 로드

## 💡 팁

- 노트북을 실행하면서 각 단계의 결과를 이해하고 기록하세요
- Feature Importance를 통해 비즈니스 인사이트를 도출하세요
- 여러 모델을 비교하여 최적의 모델을 선정하세요
- Overfitting이 심한 모델은 실제 운영에서 성능이 떨어질 수 있으니 주의하세요

## 🚀 운영 서버

실시간 예측 서버는 `../ai-server/` 디렉토리를 참조하세요:
- FastAPI 기반 REST API
- Kafka Consumer/Producer
- PostgreSQL 캐싱
- Outbox 패턴

---

**최종 업데이트**: 2025-11-19
**버전**: 2.0 (ai-model/ai-server 분리 완료)