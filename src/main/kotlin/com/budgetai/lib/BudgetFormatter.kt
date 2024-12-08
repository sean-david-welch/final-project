package com.budgetai.lib

import com.budgetai.models.BudgetWithItemsDTO

class BudgetFormatter {
    fun formatBudgetsToCSV(budgets: List<BudgetWithItemsDTO>): String {
        val csvBuilder = StringBuilder()

        // Add header row
        csvBuilder.appendLine("Budget Name,Budget Period,Total Income,Total Expenses,Category,Item Name,Amount")

        // Add data rows
        budgets.forEach { budget ->
            // If budget has no items, still show the budget summary
            if (budget.items.isEmpty()) {
                csvBuilder.appendLine(buildBudgetRow(
                    budget = budget,
                    itemName = "",
                    categoryName = "",
                    itemAmount = 0.0
                ))
            } else {
                // Add a row for each budget item
                budget.items.forEach { item ->
                    csvBuilder.appendLine(buildBudgetRow(
                        budget = budget,
                        itemName = item.name,
                        categoryName = item.category?.name ?: "Uncategorized",
                        itemAmount = item.amount
                    ))
                }
            }
        }

        return csvBuilder.toString()
    }

    private fun buildBudgetRow(
        budget: BudgetWithItemsDTO,
        itemName: String,
        categoryName: String,
        itemAmount: Double
    ): String {
        val period = if (budget.startDate != null && budget.endDate != null) {
            "${budget.startDate} to ${budget.endDate}"
        } else {
            "No period set"
        }

        return listOf(
            budget.name.escapeCsv(),
            period.escapeCsv(),
            budget.totalIncome.toString(),
            budget.totalExpenses.toString(),
            categoryName.escapeCsv(),
            itemName.escapeCsv(),
            itemAmount.toString()
        ).joinToString(",")
    }

    // Helper function to escape CSV special characters
    private fun String.escapeCsv(): String {
        return if (this.contains(",") || this.contains("\"") || this.contains("\n")) {
            "\"${this.replace("\"", "\"\"")}\""
        } else {
            this
        }
    }
}