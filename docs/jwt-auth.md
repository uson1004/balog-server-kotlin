# 일반 API JWT 인증

일반 API는 `Authorization: Bearer <access JWT>`가 필요합니다. 서명, 만료,
`type=access`, UUID subject와 해당 사용자의 DB 존재 여부를 검증합니다.
거래·리포트·정기결제·알림은 `UserFacade`가 읽은 인증 사용자를 사용합니다.
기존 무인증 클라이언트는 이 변경 후 401을 받으므로 토큰을 준비한 뒤 배포하세요.

## 접근 규칙

| 경로 | 인증 |
| --- | --- |
| `/transactions/**`, `/reports/**`, `/recurring-payments/**`, `/push-notifications/**`, `/device-tokens/**` 및 기타 일반 API | 자체 access JWT |
| `/mcp` | 기존 MCP opaque bearer와 연결 scope |
| `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-resources/**`, `/webjars/**` | 공개 문서·정적 자원 |
| `GET /auth/local/login` | `local` 프로필에서만 개발용 무인증 발급 |

운영에 `local` 프로필을 활성화하지 마세요. 개발용 로그인은 사용자 확인 없이 고정
사용자의 토큰을 발급합니다. 운영용 로그인/회원가입/공개 토큰 발급 API는 없습니다.
MCP 토큰으로 일반 API를 호출하거나 JWT로 `/mcp`를 호출할 수 없습니다.
MCP 도구가 작업 스레드에서 실행될 때에는 SDK 요청 컨텍스트로 principal을 전달하고,
호출 종료 후 스레드의 이전 보안 컨텍스트를 복원합니다.

## 관리자 사전 발급

Java 17과 프로젝트 checkout이 있는 신뢰할 수 있는 관리자 환경에서 실행합니다.
발급 도구는 Spring 서버, MySQL, Redis를 시작하지 않습니다. POSIX 파일 권한을
지원하는 macOS/Linux에서 새 토큰 파일을 0600으로 생성하며 기존 파일은 덮어쓰지 않습니다.
POSIX 권한을 지원하지 않는 파일 시스템은 토큰 생성 전에 명시적으로 거부합니다.

1. 운영 DB에 사용할 User UUID가 존재하는지 확인합니다. 기존 단일 사용자 데이터는
   동일한 UUID를 subject로 쓰면 그대로 접근됩니다. 발급 도구는 사용자를 생성하지 않습니다.
2. 서버와 **동일한** `JWT_SECRET`을 비밀 관리 도구로 환경에 주입합니다. 명령 인자,
   Git, CI 로그에 키나 토큰을 넣지 마세요. HMAC 키는 충분히 무작위인 32바이트 이상이어야 합니다.
3. 아래 환경변수를 지정하고 발급합니다. 토큰 경로는 저장소 바깥의 개인 디렉터리로 지정합니다.

```bash
export BALOG_USER_ID='<기존 User UUID>'
export BALOG_TOKEN_FILE='/absolute/private/path/access.jwt'
export ACCESS_EXP=900
bash ./gradlew issueAccessToken --console=plain
```

`ACCESS_EXP`는 초 단위이며 도구의 기본값은 900초, 허용 범위는 1~86400초입니다.
`bash ./gradlew issueAccessToken --args='--help'`로 도움말을 볼 수 있습니다.
`JWT_SECRET`을 아는 관리자는 모든 사용자 토큰을 발급할 수 있습니다. 클라이언트에는
서명 키를 전달하지 말고 해당 사용자의 토큰만 보안 저장소로 전달하세요.

```http
GET /transactions/recent?limit=5
Authorization: Bearer <사전 발급한 access JWT>
```

정상 요청은 기존 응답을 반환합니다. 인증 실패 예시는 다음과 같습니다.

```json
{"status":401,"code":"MISSING_TOKEN","message":"토큰이 없습니다."}
```

잘못된 토큰·refresh 토큰·존재하지 않는 사용자에는 `INVALID_TOKEN`, 만료에는
`EXPIRED_TOKEN`을 반환합니다. 타인의 거래 삭제는 기존 소유권 검사에 따라 404입니다.
클라이언트는 TLS로 요청하고, 토큰 만료 시 새 파일로 재발급받아 교체해야 합니다.

## 갱신과 폐기

이 도구는 access 토큰만 발급합니다. 현재 운영용 refresh API가 없으므로 기존 Redis
refresh 저장 코드를 자동 갱신 수단으로 기대하지 마세요. 반복적인 수동 발급이 부담이
되는 사용 환경이라면 별도 인증·갱신 정책을 결정한 후 확장해야 합니다.

발급된 개별 JWT를 즉시 폐기하는 기능은 없습니다. 만료까지 유효하며, 긴급한 서명 키
교체는 그 키로 발급한 **모든** JWT를 무효화합니다. 기존 사용자 데이터 삭제를 토큰
폐기 수단으로 사용하지 마세요. MCP 연결의 활성화/폐기는 기존 DB 정책을 따릅니다.

서버는 기존 `JWT_SECRET`, `ACCESS_EXP`, `REFRESH_EXP`, `HEADER`, `PREFIX` 설정을
유지합니다. 실제 HTTP 규약은 `Authorization: Bearer`입니다. 도구에는
`BALOG_USER_ID`, `BALOG_TOKEN_FILE`만 추가로 필요합니다.

## 검증

```bash
bash ./gradlew test --tests '*JwtSecurityIntegrationTest' --tests '*UserFacadeTest' --tests '*JwtServiceTest' --tests '*IssueAccessTokenTest'
bash ./gradlew build
```

HTTP 테스트는 실제 내장 Tomcat과 보안 체인을 실행하며 저장소만 mock으로 대체합니다.
실제 운영 DB·Redis·FCM 또는 운영 비밀키는 테스트에 사용하지 않습니다.
