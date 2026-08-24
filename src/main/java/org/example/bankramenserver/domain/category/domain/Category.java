package org.example.bankramenserver.domain.category.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "거래 카테고리 코드")
public enum Category {

    /** 식사, 배달, 외식 등 식비 지출 카테고리 */
    FOOD("식비"),

    /** 카페, 디저트, 간식 관련 지출 카테고리 */
    CAFE_SNACK("카페/간식"),

    /** 편의점, 마트, 생필품·잡화 구매 지출 카테고리 */
    CONVENIENCE_MART_MISC("편의점/마트/잡화"),

    /** 의류, 잡화 등 일반 쇼핑 지출 카테고리 */
    SHOPPING("쇼핑"),

    /** 게임, 문화생활, 여가 활동 관련 지출 카테고리 */
    HOBBY_LEISURE("취미/여가"),

    /** 병원, 약국, 운동, 건강관리 관련 지출 카테고리 */
    HEALTH_FITNESS("의료/건강/피트니스"),

    /** 헤어, 네일, 화장품 등 미용 관련 지출 카테고리 */
    BEAUTY("미용"),

    /** 대중교통, 주유, 차량 유지비 등 이동 관련 지출 카테고리 */
    TRANSPORT_CAR("교통"),

    /** 여행 경비, 숙박비 등 여행 관련 지출 카테고리 */
    TRAVEL_STAY("여행/숙박"),

    /** 학원비, 수강료, 교재비 등 교육 관련 지출 카테고리 */
    EDUCATION("교육"),

    /** 생활용품, 집안 관련 고정·일상 지출 카테고리 */
    LIVING("생활"),

    /** 기부금, 후원금 등 나눔 관련 지출 카테고리 */
    DONATION_SPONSORSHIP("기부/후원"),

    /** 분류되지 않은 거래를 임시로 담는 기본 카테고리 */
    UNCATEGORIZED("카테고리 없음"),

    /** 현금 인출 거래를 분류하기 위한 지출 카테고리 */
    ATM_WITHDRAWAL("ATM출금"),

    /** 계좌 간 이체 등 송금성 거래를 위한 카테고리 */
    TRANSFER("이체"),

    /** 월급, 급여 입금 등 대표적인 수입 카테고리 */
    SALARY("급여"),

    /** 예금 이자, 투자 수익 등 저축·투자 관련 수입 카테고리 */
    SAVINGS_INVESTMENT("저축/투자");

    private final String displayName;
}
