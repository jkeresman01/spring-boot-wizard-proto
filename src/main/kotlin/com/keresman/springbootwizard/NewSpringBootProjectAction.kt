package com.keresman.springbootwizard

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class NewSpringBootProjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInfoMessage(
            "Please use File → New → Project and select 'Spring Boot' from the generators list.",
            "Spring Boot Project"
        )
    }
}