package com.keresman.springbootwizard

import com.intellij.facet.ui.ValidationResult
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.ProjectGeneratorPeer
import com.keresman.springwizard.SpringBootProjectGeneratorPeer
import com.keresman.springwizard.SpringInitializrDownloader
import javax.swing.Icon

class SpringBootProjectGenerator : DirectoryProjectGenerator<SpringBootSettings>,
    CustomStepProjectGenerator<SpringBootSettings> {

    override fun getName(): String = "Spring Boot"

    override fun getLogo(): Icon? = null

    override fun createPeer(): ProjectGeneratorPeer<SpringBootSettings> {
        return SpringBootProjectGeneratorPeer()
    }

    override fun validate(baseDirPath: String): ValidationResult {
        return ValidationResult.OK
    }

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: SpringBootSettings,
        module: Module
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Creating Spring Boot Project", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Downloading project from Spring Initializr..."
                indicator.isIndeterminate = true

                try {
                    val downloader = SpringInitializrDownloader(settings)
                    downloader.downloadAndExtract(baseDir.path, indicator)

                    baseDir.refresh(false, true)
                } catch (e: Exception) {
                    throw RuntimeException("Failed to create Spring Boot project: ${e.message}", e)
                }
            }
        })
    }

    override fun createStep(
        projectGenerator: DirectoryProjectGenerator<SpringBootSettings>,
        callback: AbstractNewProjectStep.AbstractCallback<SpringBootSettings>
    ): AbstractActionWithPanel {
        return SpringBootProjectSettingsStep(projectGenerator as SpringBootProjectGenerator, callback)
    }
}