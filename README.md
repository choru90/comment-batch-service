# COMMENT BATCH SERVICE

## 실행 방법

1. jar file 생성
```
./gradlew bootJar
```

2. docker compose 실행
```
$ docker-compose up --build -d 
```
3. batch server가 docker 실행된 후 결과물로 파일이 생성됩니다.
- 생성 경로 src/main/resources
- result.txt, result.log 파일이 생성되어 있음을 확인하실 수 있습니다.

## 스택

- Java 17
- Spring Batch
- Redis
- Mysql
- JPA

## 데이터 출처

- 전국 초중고등학교 목록 :공공데이터 포털 
- 전국 대학교 목록 : 한국교육개발원

## 구조
**https://pool-olive-3e4.notion.site/Comment-Batch-Server-1e995b2a013a4496a3f83a4e33f0423d?pvs=4**
![](../../Desktop/스크린샷 2024-03-19 오후 8.14.33.png)