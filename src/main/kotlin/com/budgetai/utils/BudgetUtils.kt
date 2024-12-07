package com.budgetai.utils

import com.budgetai.models.BudgetItemDTO
import org.slf4j.LoggerFactory

object BudgetParser {
    private val logger = LoggerFactory.getLogger(BudgetParser::class.java)

    data class ParseResult(
        val items: List<BudgetItemDTO>,
        val errors: List<String>,
        val totalAmount: Double
    )

    fun parseSpreadsheetData(spreadsheetData: String): ParseResult {
        logger.info("Starting to parse spreadsheet data")
        logger.debug("Raw spreadsheet data: ${spreadsheetData.take(100)}${if (spreadsheetData.length > 100) "..." else ""}")

        val errors = mutableListOf<String>()
        val items = mutableListOf<BudgetItemDTO>()
        var totalAmount = 0.0

        if (spreadsheetData.isBlank()) {
            logger.warn("Empty spreadsheet data provided")
            return ParseResult(emptyList(), listOf("No spreadsheet data provided"), 0.0)
        }

        val rows = spreadsheetData.split(";")
        logger.info("Processing ${rows.size} rows from spreadsheet")

        rows.forEachIndexed { index, row ->
            try {
                logger.debug("Processing row ${index + 1}: $row")
                val cells = row.split(",")
                val name = cells.getOrNull(0)?.trim() ?: ""
                val amountStr = cells.getOrNull(1)?.trim() ?: ""
                val amount = amountStr.toDoubleOrNull()

                when {
                    name.isBlank() -> {
                        logger.warn("Row ${index + 1}: Empty name detected")
                        errors.add("Row ${index + 1}: Name is required")
                    }

                    amount == null || amount <= 0 -> {
                        logger.warn("Row ${index + 1}: Invalid amount '$amountStr'")
                        errors.add("Row ${index + 1}: Invalid amount - must be a positive number")
                    }

                    else -> {
                        logger.debug("Row ${index + 1}: Adding budget item - name: $name, amount: $amount")
                        items.add(
                            BudgetItemDTO(
                                budgetId = 0,
                                name = name,
                                amount = amount
                            )
                        )
                        totalAmount += amount
                        logger.debug("Running total amount: $totalAmount")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing row ${index + 1}", e)
                errors.add("Row ${index + 1}: Failed to parse row - ${e.message}")
            }
        }

        logger.info("Completed parsing spreadsheet data: ${items.size} valid items, ${errors.size} errors, total amount: $totalAmount")
        if (errors.isNotEmpty()) {
            logger.warn("Parsing errors encountered: $errors")
        }

        return ParseResult(
            items = items,
            errors = errors,
            totalAmount = totalAmount
        )
    }
}