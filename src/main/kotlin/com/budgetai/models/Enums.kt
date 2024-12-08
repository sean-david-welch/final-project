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
enum class PromptType {
    @SerialName("COST_REDUCTION") COST_REDUCTION,
    @SerialName("PRICE_ALTERNATIVES") PRICE_ALTERNATIVES,
    @SerialName("SPENDING_PATTERNS") SPENDING_PATTERNS,
    @SerialName("CATEGORY_ANALYSIS") CATEGORY_ANALYSIS,
    @SerialName("CUSTOM_ANALYSIS") CUSTOM_ANALYSIS
}

@Serializable
enum class CategoryType {
    @SerialName("FIXED") FIXED,
    @SerialName("VARIABLE") VARIABLE,
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
