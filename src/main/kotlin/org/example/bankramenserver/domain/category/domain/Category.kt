package org.example.bankramenserver.domain.category.domain

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "거래 카테고리 코드")
enum class Category(val displayName: String) {
    FOOD("식비"), CAFE_SNACK("카페/간식"), CONVENIENCE_MART_MISC("편의점/마트/잡화"), SHOPPING("쇼핑"),
    HOBBY_LEISURE("취미/여가"), HEALTH_FITNESS("의료/건강/피트니스"), BEAUTY("미용"), TRANSPORT_CAR("교통"),
    TRAVEL_STAY("여행/숙박"), EDUCATION("교육"), LIVING("생활"), DONATION_SPONSORSHIP("기부/후원"),
    UNCATEGORIZED("카테고리 없음"), ATM_WITHDRAWAL("ATM출금"), TRANSFER("이체"), SALARY("급여"), SAVINGS_INVESTMENT("저축/투자")
}
