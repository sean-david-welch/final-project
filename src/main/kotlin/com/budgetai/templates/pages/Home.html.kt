package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import kotlinx.html.h1
import kotlinx.html.main

fun HomeTemplate() = BaseTemplate {
    main(classes = "dashboard-layout") {
       h1() { +"hello" }
    }
}