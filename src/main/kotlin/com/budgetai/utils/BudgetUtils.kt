package com.budgetai.utils

import com.budgetai.models.BudgetItemDTO

object BudgetParser {
    data class ParseResult(
        val items: List<BudgetItemDTO>, val errors: List<String>, val totalAmount: Double
    )

    fun parseSpreadsheetData(spreadsheetData: String): ParseResult {
        val errors = mutableListOf<String>()
        val items = mutableListOf<BudgetItemDTO>()
        var totalAmount = 0.0

        if (spreadsheetData.isBlank()) {
            return ParseResult(emptyList(), listOf("No spreadsheet data provided"), 0.0)
        }

        spreadsheetData.split(";").forEachIndexed { index, row ->
            try {
                val cells = row.split(",")
                val name = cells.getOrNull(0)?.trim() ?: ""
                val amountStr = cells.getOrNull(1)?.trim() ?: ""
                val amount = amountStr.toDoubleOrNull()

                when {
                    name.isBlank() -> {
                        errors.add("Row ${index + 1}: Name is required")
                    }

                    amount == null || amount <= 0 -> {
                        errors.add("Row ${index + 1}: Invalid amount - must be a positive number")
                    }

                    else -> {
                        items.add(
                            BudgetItemDTO(
                                budgetId = 0, name = name, amount = amount
                            )
                        )
                        totalAmount += amount
                    }
                }
            } catch (e: Exception) {
                errors.add("Row ${index + 1}: Failed to parse row - ${e.message}")
            }
        }

        return ParseResult(
            items = items, errors = errors, totalAmount = totalAmount
        )
    }
}