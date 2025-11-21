#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Python 의존성 설치 스크립트 ===${NC}\n"

# Python 버전 확인
echo -e "${YELLOW}Python 버전 확인 중...${NC}"
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Error: Python3가 설치되어 있지 않습니다.${NC}"
    exit 1
fi

PYTHON_VERSION=$(python3 --version)
echo -e "${GREEN}✓ $PYTHON_VERSION 감지됨${NC}\n"

# pip 업그레이드
echo -e "${YELLOW}pip 업그레이드 중...${NC}"
python3 -m pip install --upgrade pip

# requirements.txt 존재 확인
if [ ! -f "requirements.txt" ]; then
    echo -e "${RED}Error: requirements.txt 파일을 찾을 수 없습니다.${NC}"
    exit 1
fi

# 의존성 설치
echo -e "${YELLOW}requirements.txt에서 의존성 설치 중...${NC}"
python3 -m pip install -r requirements.txt

# 설치 결과 확인
if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}=== 의존성 설치 완료 ===${NC}"
    echo -e "${GREEN}✓ 모든 패키지가 성공적으로 설치되었습니다.${NC}"

    # 설치된 패키지 목록 표시
    echo -e "\n${YELLOW}설치된 패키지 목록:${NC}"
    python3 -m pip list
else
    echo -e "\n${RED}Error: 의존성 설치 중 오류가 발생했습니다.${NC}"
    exit 1
fi