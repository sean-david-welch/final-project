package com.budgetai.lib

import com.budgetai.models.BudgetWithItemsDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BudgetFormatter {
    private val logger: Logger = LoggerFactory.getLogger("BudgetFormatter")

    fun formatBudgetsToCSV(budgets: List<BudgetWithItemsDTO>): String {
        logger.info("Starting CSV formatting for ${budgets.size} budgets")
        val csvBuilder = StringBuilder()

        try {
            // Add header row
            csvBuilder.appendLine("Budget Name,Budget Period,Total Income,Total Expenses,Category,Item Name,Amount")
            logger.debug("Added CSV headers")

            // Add data rows
            budgets.forEach { budget ->
                logger.debug("Processing budget: ${budget.id} - ${budget.name}")

                // If budget has no items, show the budget summary with empty values
                if (budget.items.isEmpty()) {
                    logger.debug("Budget ${budget.id} has no items")
                    csvBuilder.appendLine(
                        buildBudgetRow(
                            budgetName = budget.name,
                            period = getBudgetPeriod(budget),
                            totalIncome = budget.totalIncome,
                            totalExpenses = budget.totalExpenses,
                            categoryName = "",
                            itemName = "",
                            itemAmount = 0.0,
                            isFirstRow = true
                        )
                    )
                } else {
                    logger.debug("Processing ${budget.items.size} items for budget ${budget.id}")
                    // Add rows for each budget item
                    budget.items.forEachIndexed { index, item ->
                        csvBuilder.appendLine(
                            buildBudgetRow(
                                budgetName = budget.name,
                                period = getBudgetPeriod(budget),
                                totalIncome = budget.totalIncome,
                                totalExpenses = budget.totalExpenses,
                                categoryName = item.category?.name ?: "Uncategorized",
                                itemName = item.name,
                                itemAmount = item.amount,
                                isFirstRow = index == 0  // Only show totals in first row for this budget
                            )
                        )
                    }
                }
            }

            logger.info("Successfully formatted CSV data")
            return csvBuilder.toString()
        } catch (e: Exception) {
            logger.error("Error formatting budgets to CSV", e)
            throw e
        }
    }

    private fun getBudgetPeriod(budget: BudgetWithItemsDTO): String {
        return if (budget.startDate != null && budget.endDate != null) {
            "${budget.startDate} to ${budget.endDate}"
        } else {
            "No period set"
        }
    }

    private fun buildBudgetRow(
        budgetName: String,
        period: String,
        totalIncome: Double,
        totalExpenses: Double,
        categoryName: String,
        itemName: String,
        itemAmount: Double,
        isFirstRow: Boolean
    ): String {
        try {
            return listOf(
                budgetName.escapeCsv(),
                period.escapeCsv(),
                if (isFirstRow) totalIncome.toString() else "",  // Only show totals in first row
                if (isFirstRow) totalExpenses.toString() else "",
                categoryName.escapeCsv(),
                itemName.escapeCsv(),
                itemAmount.toString()
            ).joinToString(",")
        } catch (e: Exception) {
            logger.error("Error building budget row", e)
            throw e
        }
    }

    private fun String.escapeCsv(): String {
        return try {
            if (this.contains(",") || this.contains("\"") || this.contains("\n")) {
                "\"${this.replace("\"", "\"\"")}\""
            } else {
                this
            }
        } catch (e: Exception) {
            logger.error("Error escaping CSV string: $this", e)
            throw e
        }
    }
}