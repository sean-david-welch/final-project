package com.budgetai.utils

import com.budgetai.models.BudgetItemDTO

object BudgetParser {
    data class ParseResult(
        val items: List<BudgetItemDTO>,
        val errors: List<String>
    )

    fun parseSpreadsheetData(
        spreadsheetData: String,
        budgetId: Int,
        categoryMap: Map<String, Int>
    ): ParseResult {
        val errors = mutableListOf<String>()
        val items = mutableListOf<BudgetItemDTO>()

        if (spreadsheetData.isBlank()) {
            return ParseResult(emptyList(), listOf("No spreadsheet data provided"))
        }

        spreadsheetData.split(";").forEachIndexed { index, row ->
            try {
                val cells = row.split(",")
                if (cells.size >= 3) {
                    val name = cells[0].trim()
                    val amount = cells[1].trim().toDoubleOrNull()
                    val category = cells[2].trim()

                    when {
                        name.isBlank() -> {
                            errors.add("Row ${index + 1}: Name is required")
                        }
                        amount == null || amount <= 0 -> {
                            errors.add("Row ${index + 1}: Invalid amount - must be a positive number")
                        }
                        category.isBlank() -> {
                            errors.add("Row ${index + 1}: Category is required")
                        }
                        !categoryMap.containsKey(category) -> {
                            errors.add("Row ${index + 1}: Invalid category '$category'")
                        }
                        else -> {
                            items.add(
                                BudgetItemDTO(
                                    budgetId = budgetId,
                                    categoryId = categoryMap[category] ?: 0,
                                    name = name,
                                    amount = amount,
                                )
                            )
                        }
                    }
                } else if (cells.any { it.isNotBlank() }) {
                    errors.add("Row ${index + 1}: Invalid number of columns")
                }
            } catch (e: Exception) {
                errors.add("Row ${index + 1}: Failed to parse row - ${e.message}")
            }
        }

        return ParseResult(items, errors)
    }
}