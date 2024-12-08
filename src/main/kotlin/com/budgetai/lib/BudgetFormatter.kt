package com.budgetai.lib

import com.budgetai.models.BudgetWithItemsDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BudgetFormatter {
    private val logger: Logger = LoggerFactory.getLogger("BudgetFormatter")

    fun formatBudgetsToCSV(budgets: List<BudgetWithItemsDTO>): String {
        logger.info("Starting CSV formatting for ${budgets.size} budgets")
        val csvBuilder = StringBuilder()
        var totalAmount = 0.0  // Track total amount across all items

        try {
            // Add header row
            csvBuilder.appendLine("Budget Name,Budget Period,Total Income,Total Expenses,Category,Item Name,Amount")
            logger.debug("Added CSV headers")

            // Add data rows
            budgets.forEach { budget ->
                logger.debug("Processing budget: ${budget.id} - ${budget.name}")

                // If budget has no items, still show the budget summary
                if (budget.items.isEmpty()) {
                    logger.debug("Budget ${budget.id} has no items")
                    csvBuilder.appendLine(
                        buildBudgetRow(
                            budget = budget, itemName = "", categoryName = "", itemAmount = 0.0
                        )
                    )
                } else {
                    logger.debug("Processing ${budget.items.size} items for budget ${budget.id}")
                    // Add a row for each budget item
                    budget.items.forEach { item ->
                        totalAmount += item.amount  // Add to running total
                        csvBuilder.appendLine(
                            buildBudgetRow(
                                budget = budget, itemName = item.name, categoryName = item.category?.name ?: "Uncategorized",
                                itemAmount = item.amount
                            )
                        )
                    }
                }
            }

            // Add total row at the end
            csvBuilder.appendLine(buildTotalRow(totalAmount))

            logger.info("Successfully formatted CSV data")
            return csvBuilder.toString()

        } catch (e: Exception) {
            logger.error("Error formatting budgets to CSV", e)
            throw e
        }
    }

    private fun buildBudgetRow(
        budget: BudgetWithItemsDTO, itemName: String, categoryName: String, itemAmount: Double
    ): String {
        try {
            val period = if (budget.startDate != null && budget.endDate != null) {
                "${budget.startDate} to ${budget.endDate}"
            } else {
                "No period set"
            }

            return listOf(
                budget.name.escapeCsv(), period.escapeCsv(), budget.totalIncome.toString(), budget.totalExpenses.toString(),
                categoryName.escapeCsv(), itemName.escapeCsv(), itemAmount.toString()
            ).joinToString(",")

        } catch (e: Exception) {
            logger.error("Error building budget row for budget ${budget.id}", e)
            throw e
        }
    }

    private fun buildTotalRow(totalAmount: Double): String {
        return listOf(
            "TOTAL".escapeCsv(),  // Budget Name
            "",                   // Budget Period
            "",                   // Total Income
            "",                   // Total Expenses
            "",                   // Category
            "Total of all items".escapeCsv(), // Item Name
            totalAmount.toString() // Amount
        ).joinToString(",")
    }

    // Helper function to escape CSV special characters
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