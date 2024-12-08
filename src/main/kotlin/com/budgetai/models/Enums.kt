package com.budgetai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class InsightType {
    BUDGET_ANALYSIS, ITEM_ANALYSIS, SAVING_SUGGESTION, GENERAL_ADVICE
}

enum class Sentiment {
    POSITIVE, NEGATIVE, NEUTRAL
}

@Serializable
enum class CategoryType {
    @SerialName("EXPENSE") EXPENSE,
    @SerialName("INCOME") INCOME
}


@Serializable
enum class UserRole {
    @SerialName("ADMIN") ADMIN,
    @SerialName("USER") USER,
    @SerialName("GUEST") GUEST,
}
