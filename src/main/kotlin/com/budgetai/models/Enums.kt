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
    @SerialName("FIXED") FIXED,
    @SerialName("VARIABLE") VARIABLE,
    @SerialName("ONE_TIME") ONE_TIME,
    @SerialName("RECURRING") RECURRING,
    @SerialName("EMERGENCY") EMERGENCY,
    @SerialName("DISCRETIONARY") DISCRETIONARY,
    @SerialName("NECESSARY") NECESSARY
}

@Serializable
enum class UserRole {
    @SerialName("ADMIN") ADMIN,
    @SerialName("USER") USER,
    @SerialName("GUEST") GUEST,
}
