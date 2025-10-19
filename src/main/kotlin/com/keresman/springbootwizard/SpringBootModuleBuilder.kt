package com.keresman.springbootwizard

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.LocalFileSystem
import com.keresman.springwizard.SpringInitializrDownloader

class SpringBootModuleBuilder : ModuleBuilder() {

    val settings: SpringBootSettings = SpringBootSettings()
    private var configStep: SpringBootConfigurationStep? = null
    private var depsStep: SpringBootDependenciesStep? = null

    override fun getModuleType(): ModuleType<*> = StdModuleTypes.JAVA

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val contentEntry = doAddContentEntry(modifiableRootModel) ?: return
        val contentRoot = contentEntry.file ?: return

        val project = modifiableRootModel.project

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Creating Spring Boot Project", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Downloading project from Spring Initializr..."
                indicator.isIndeterminate = true

                try {
                    val downloader = SpringInitializrDownloader(settings)
                    downloader.downloadAndExtract(contentRoot.path, indicator)

                    LocalFileSystem.getInstance().refresh(false)
                } catch (e: Exception) {
                    throw RuntimeException("Failed to create Spring Boot project: ${e.message}", e)
                }
            }
        })
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep {
        configStep = SpringBootConfigurationStep(this)
        return configStep!!
    }

    fun getDependenciesStep(parentDisposable: Disposable): ModuleWizardStep {
        if (depsStep == null) {
            depsStep = SpringBootDependenciesStep(this)
        }
        return depsStep!!
    }

    override fun getBuilderId(): String = "spring.boot.wizard"

    override fun getPresentableName(): String = "Spring Boot"

    override fun getDescription(): String = "Spring Boot projects using Spring Initializr"

    override fun getNodeIcon(): javax.swing.Icon? = null

    override fun getGroupName(): String = "Spring Boot"

    override fun getWeight(): Int = 100
}