package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.SavingsGoalForm(context: BaseTemplateContext) {
    div {
        attributes["id"] = "savings-response-div"
        attributes["class"] = "response-div"
    }

    form(classes = "auth-form") {
        attributes["hx-post"] = "/api/savings-goals"
        attributes["hx-target"] = "#savings-response-div"
        attributes["hx-on::after-request"] = """
           if(event.detail.successful) {
               this.reset();
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