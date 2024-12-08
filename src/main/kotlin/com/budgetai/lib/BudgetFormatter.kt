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

            // Process each budget
            budgets.forEach { budget ->
                logger.debug("Processing budget: ${budget.id} - ${budget.name}")

                // Group items by category
                val itemsByCategory = budget.items.groupBy { it.category?.name ?: "Uncategorized" }

                if (budget.items.isEmpty()) {
                    // If budget has no items, show only the budget summary with total expenses
                    logger.debug("Budget ${budget.id} has no items")
                    csvBuilder.appendLine(
                        buildBudgetRow(
                            budget = budget,
                            itemName = "",
                            categoryName = "",
                            itemAmount = budget.totalExpenses
                        )
                    )
                } else {
                    // Process items by category
                    itemsByCategory.forEach { (categoryName, items) ->
                        items.forEach { item ->
                            csvBuilder.appendLine(
                                buildBudgetRow(
                                    budget = budget,
                                    itemName = item.name,
                                    categoryName = categoryName,
                                    itemAmount = item.amount
                                )
                            )
                        }
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

    private fun buildBudgetRow(
        budget: BudgetWithItemsDTO,
        itemName: String,
        categoryName: String,
        itemAmount: Double
    ): String {
        try {
            val period = when {
                budget.startDate != null && budget.endDate != null ->
                    "${budget.startDate} to ${budget.endDate}"
                else -> "No period set"
            }

            return listOf(
                budget.name.escapeCsv(),
                period.escapeCsv(),
                budget.totalIncome.toString(),
                budget.totalExpenses.toString(), // This is the total expenses for the entire budget
                categoryName.escapeCsv(),
                itemName.escapeCsv(),
                itemAmount.toString()  // This is the individual item amount
            ).joinToString(",")
        } catch (e: Exception) {
            logger.error("Error building budget row for budget ${budget.id}", e)
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