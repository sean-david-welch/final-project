package com.budgetai.templates.components

import com.budgetai.models.BudgetDTO
import com.budgetai.models.PromptType
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.SavingsGoalForm(context: BaseTemplateContext) {
    form(classes = "auth-form") {
        attributes["hx-post"] = "/api/savings-goals"
        attributes["hx-target"] = "#response-message"
        attributes["hx-on::after-request"] = """
           if(event.detail.successful) {
               this.reset();
               document.getElementById('modal-dialog').close();
               // Optional: Refresh the goals list
               htmx.trigger('#response-message', 'refreshGoals');
           }
       """.trimIndent()

        input(type = InputType.hidden) {
            name = "userId"
            value = context.auth.user?.id!!
        }

        div(classes = "spreadsheet-wrapper") {
            div(classes = "form-group") {
                label { +"Goal Name" }
                input(type = InputType.text, classes = "input-field") {
                    placeholder = "Enter the name for your savings goal"
                    name = "name"
                    required = true
                }
            }

            div(classes = "form-group") {
                label { +"Description" }
                input(type = InputType.text, classes = "input-field") {
                    placeholder = "Description of your savings goal"
                    name = "description"
                }
            }

            div(classes = "form-group") {
                label { +"Target Amount" }
                input(type = InputType.number, classes = "input-field") {
                    placeholder = "Enter your target savings amount"
                    name = "targetAmount"
                    required = true
                    attributes["step"] = "0.01"
                    attributes["min"] = "0"
                }
            }

            div(classes = "form-group") {
                label { +"Current Amount" }
                input(type = InputType.number, classes = "input-field") {
                    placeholder = "Enter current savings amount"
                    name = "currentAmount"
                    attributes["step"] = "0.01"
                    attributes["min"] = "0"
                }
            }

            div(classes = "form-group") {
                label { +"Target Date" }
                input(type = InputType.date, classes = "input-field") {
                    name = "targetDate"
                }
            }
        }

        button(type = ButtonType.submit, classes = "submit-button") {
            +"Create Savings Goal"
        }
    }
}

fun FlowContent.AIInsightForm(context: BaseTemplateContext, budgets: List<BudgetDTO>) {
    form(classes = "auth-form") {
        attributes["hx-post"] = "/api/reports/ai-insight"
        attributes["hx-target"] = "#response-message"
        attributes["hx-on::after-request"] = """
            if(event.detail.successful) {
                this.reset();
                document.getElementById('modal-dialog').close();
                // Optional: Refresh the insights list
                htmx.trigger('#response-message', 'refreshInsights');
            }
        """.trimIndent()

        input(type = InputType.hidden) {
            name = "userId"
            value = context.auth.user?.id!!
        }

        div(classes = "spreadsheet-wrapper") {
            div(classes = "form-group") {
                label { +"Select Prompt" }
                select(classes = "input-field") {
                    name = "prompt"
                    required = true

                    option {
                        value = ""
                        +"Select a prompt..."
                    }

                    PromptType.entries.forEach { promptType ->
                        option {
                            value = promptType.name.lowercase()
                            +when (promptType) {
                                PromptType.COST_REDUCTION -> "Find Cost Reduction Opportunities"
                                PromptType.PRICE_ALTERNATIVES -> "Discover Price Alternatives"
                                PromptType.SPENDING_PATTERNS -> "Analyze Spending Patterns"
                                PromptType.CATEGORY_ANALYSIS -> "Analyze Spending Categories"
                                PromptType.CUSTOM_ANALYSIS -> "Custom Analysis"
                            }
                        }
                    }
                }
            }

            div(classes = "form-group") {
                label { +"Select Budget" }
                select(classes = "input-field") {
                    name = "budget"
                    required = true

                    option {
                        value = ""
                        +"Select a budget..."
                    }

                    budgets.forEach { budget ->
                        option {
                            value = budget.id
                            +"${budget.name} (${budget.period})"
                        }
                    }
                }
            }
        }

        button(type = ButtonType.submit, classes = "submit-button") {
            +"Generate AI Insight"
        }
    }
}