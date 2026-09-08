# 월 예산 API

`GET /budgets/monthly?year=2026&month=9`로 조회하고, 같은 URL에
`PUT`과 JSON `{"amount":1000000}`을 보내 월 예산을 설정하거나 교체합니다.
두 요청 모두 `Authorization: Bearer <access JWT>`가 필요하며 JWT 사용자의 데이터만 처리합니다.
사용자 ID를 입력받지 않습니다. 성공 응답은 모두 200입니다.

```json
{
  "yearMonth": "2026-09",
  "budgetAmount": 1000000,
  "totalExpense": 200000,
  "remainingAmount": 800000,
  "remainingDays": 23,
  "dailyAvailableAmount": 34782
}
```

위 예시는 서버 기준 날짜가 2026-09-08인 경우입니다.

- 금액은 원 단위 정수입니다. 예산은 0~9223372036854775807까지 허용합니다.
  누락, null, 음수, 소수, 범위 초과 금액은 `400 INVALID_REQUEST`입니다.
- 연도는 2000~2999, 월은 1~12입니다. 누락하거나 유효하지 않으면 400입니다.
- `totalExpense`는 선택한 월에 기록된 EXPENSE 합계입니다. 수입은 차감하지 않습니다.
  미래 날짜로 이미 등록한 지출도 포함하지만, 아직 거래로 기록되지 않은 정기결제는 포함하지 않습니다.
- `remainingAmount = budgetAmount - totalExpense`이며 예산 초과 시 음수입니다.
- 현재 월은 오늘을 포함한 남은 일수, 미래 월은 그 달 전체 일수, 과거 월은 0일입니다.
  날짜는 기존 `Clock` 빈의 서버 시간대를 따릅니다. 한국 기준 운영 시 `TZ=Asia/Seoul`을 설정합니다.
- 하루 사용 가능액은 음수가 아닌 남은 예산을 남은 일수로 나눈 뒤 원 미만을 버립니다.
  예산을 소진했거나 지난달이면 0입니다.
- 예산 미설정 시 `budgetAmount`, `remainingAmount`, `dailyAvailableAmount`는 null입니다.
  지출과 남은 일수는 계속 반환합니다. 조회가 예산을 생성하지 않습니다.
- PUT은 같은 사용자·월에 원자적으로 생성/수정합니다. 동시 요청도 행은 하나이며 마지막 DB 갱신 값이 남습니다.

## 저장 및 검증

Flyway `V3__create_monthly_budgets.sql`이 전용 테이블과 사용자·연도·월 유일 제약을 추가합니다.
새 환경변수나 운영 의존성은 없습니다. 기존 MySQL·JWT 설정을 사용합니다.

```bash
bash ./gradlew test --tests '*MonthlyBudget*' --tests '*JwtSecurityIntegrationTest'
bash ./gradlew build
```
