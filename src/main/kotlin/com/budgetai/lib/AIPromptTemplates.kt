package com.budgetai.lib

import com.budgetai.models.BudgetDTO
import com.budgetai.models.BudgetItemDTO
import com.budgetai.models.PromptType

object AIPromptTemplates {
    private const val BUDGET_ANALYSIS_HEADER = "Analyze the following budget data:\nBudget Name: %s\nTotal Budget: %s\nBudget Items:\n%s\n"

    private val PROMPT_TYPE_TEMPLATES: Map<PromptType, String>
        get() = mapOf(
            PromptType.COST_REDUCTION to """
                Please analyze this budget for cost reduction opportunities. 
                Identify specific items where costs could be reduced and suggest practical ways to achieve these reductions.
                Format your response in clear, actionable bullet points.
            """.trimIndent(),

            PromptType.PRICE_ALTERNATIVES to """
                Review the budget items and suggest alternative options or suppliers that could offer better value.
                For each suggestion, explain the potential benefits and savings.
            """.trimIndent(),

            PromptType.SPENDING_PATTERNS to """
                Analyze the spending patterns in this budget.
                Identify any notable trends, unusual spending, or areas that might need attention.
                Provide specific insights about spending distribution and efficiency.
            """.trimIndent(),

            PromptType.CATEGORY_ANALYSIS to """
                Perform a detailed category analysis of this budget.
                Group similar items, identify the highest spending categories,
                and suggest any category-specific optimizations.
            """.trimIndent(),

            PromptType.CUSTOM_ANALYSIS to """
                Provide a comprehensive analysis of this budget.
                Include insights about spending patterns, potential savings,
                and recommendations for better budget management.
            """.trimIndent()
        )

    fun generatePrompt(
        budget: BudgetDTO, budgetItems: List<BudgetItemDTO>, promptType: PromptType
    ): String {
        val itemsList = budgetItems.joinToString("\n") {
            "- ${it.name}: ${it.amount}"
        }

        val header = BUDGET_ANALYSIS_HEADER.format(
            budget.name, budget.totalExpenses, itemsList
        )

        return header + (PROMPT_TYPE_TEMPLATES[promptType] ?: throw IllegalArgumentException("Unknown prompt type"))
    }
}