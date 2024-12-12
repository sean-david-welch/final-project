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
    div(classes = "relative w-full") {
        div(classes = "loading-indicator fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50") {
            id = "loading-indicator"
            style = "display: none"
            div(classes = "bg-white p-4 rounded-lg shadow-lg flex items-center space-x-2") {
                div(classes = "animate-spin rounded-full h-4 w-4 border-b-2 border-gray-900") {}
                span { +"Generating AI Insight..." }
            }
        }
    }

    form(classes = "auth-form") {
        attributes["hx-post"] = "/api/reports/ai-insights"
        attributes["hx-target"] = "#response-message"
        attributes["hx-indicator"] = "#loading-indicator"
        // Add loading state class
        attributes["hx-request"] = "class toggle:loading"
        attributes["hx-on::before-request"] = """
        const submitBtn = document.querySelector('button[type="submit"]');
        if(submitBtn) submitBtn.disabled = true;
        """
        attributes["hx-on::after-request"] = """
        const submitBtn = document.querySelector('button[type="submit"]');
        if(submitBtn) submitBtn.disabled = false;
        if(event.detail.successful) {
            this.reset();
        }
        """

        input(type = InputType.hidden) {
            name = "userId"
            value = context.auth.user?.id ?: ""
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
                            value = budget.id.toString()
                            +budget.name
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