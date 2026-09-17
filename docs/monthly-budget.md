# 월 예산 API

`GET /budgets/monthly?year=2026&month=9`로 조회하고, 같은 URL에
`PUT`과 JSON `{"amount":1000000}`을 보내 월 예산을 설정하거나 교체합니다.
두 요청 모두 `Authorization: Bearer <access JWT>`가 필요하며 JWT 사용자의 데이터만 처리합니다.
사용자 ID를 입력받지 않습니다. 성공 응답은 모두 200입니다.

아래는 기존 요약 필드만 표시한 예시입니다. 추가 캘린더 필드는 하단을 참고하세요.

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

## 캘린더 응답 (GET과 PUT 공통)

기존 필드를 유지하고 `dailyTargetAmount`와 `days`를 추가합니다. 추가 API/테이블/환경변수는 없습니다.
`dailyTargetAmount`는 월 예산을 해당 월 전체 일수로 나눈 원 단위 정수(버림)입니다.
`days`는 월 첫날부터 마지막 날까지 날짜 오름차순으로 반환합니다.

```json
{"dailyTargetAmount":33333,"days":[{"date":"2026-09-01","targetAmount":33333,"expenseAmount":28000,"status":"SUCCESS"}]}
```

위 `days`는 첫 항목만 보여주는 축약 예시입니다.
- 예산 미설정: 목표 금액 null, 모든 날짜 UNSET.
- 오늘과 미래 날짜: PENDING. 오늘 지출도 expenseAmount에 표시하지만 하루가 끝나기 전에는 성공/실패를 확정하지 않습니다.
- 지난 날짜: 지출 <= 목표이면 SUCCESS, 초과하면 FAIL. 거래가 없으면 0원입니다.
- 예산 변경 또는 거래 추가/삭제 시 과거 판정도 재계산됩니다. 판정 이력은 저장하지 않습니다.
- 목표는 매일 동일하고 이월하지 않습니다. 나눗셈의 원 미만 잔액은 목표에 배분하지 않습니다.
- 월 중간에 예산을 설정해도 월 전체 날짜를 판정합니다. 0원 예산에서도 0원 지출은 성공입니다.
- 지출은 기존 EXPENSE 거래만 포함합니다. 수입, 미등록 정기결제는 제외됩니다.
- 기존 dailyAvailableAmount는 월 전체 지출을 차감한 남은 예산 기준 참고 금액이며, 성공/실패 목표가 아닙니다.
- 앱은 이 응답으로 캘린더를 그리고, 날짜 선택 시 기존 GET /transactions/expenses 결과를 날짜로 필터링할 수 있습니다.
