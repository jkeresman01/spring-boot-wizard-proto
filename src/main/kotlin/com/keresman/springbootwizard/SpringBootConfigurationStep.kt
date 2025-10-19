package com.keresman.springbootwizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.keresman.springwizard.SimpleDocumentListener
import java.awt.BorderLayout
import javax.swing.*

class SpringBootConfigurationStep(private val builder: SpringBootModuleBuilder) : ModuleWizardStep() {

    private val settings get() = builder.settings
    private val serverUrlField: JBTextField
    private val groupField: JBTextField
    private val artifactField: JBTextField
    private val packageNameField: JBTextField
    private val languageButtonGroup: ButtonGroup
    private val javaButton: JToggleButton
    private val kotlinButton: JToggleButton
    private val groovyButton: JToggleButton
    private val buildTypeCombo: ComboBox<BuildType>
    private val javaVersionCombo: ComboBox<String>
    private val packagingButtonGroup: ButtonGroup
    private val jarButton: JToggleButton
    private val warButton: JToggleButton

    private val panel: JPanel

    init {
        serverUrlField = JBTextField(settings.serverUrl, 30)
        groupField = JBTextField(settings.group, 30)
        artifactField = JBTextField(settings.artifact, 30)
        packageNameField = JBTextField(settings.packageName, 30)

        // Language selection
        languageButtonGroup = ButtonGroup()
        javaButton = JToggleButton("Java")
        kotlinButton = JToggleButton("Kotlin")
        groovyButton = JToggleButton("Groovy")
        languageButtonGroup.add(javaButton)
        languageButtonGroup.add(kotlinButton)
        languageButtonGroup.add(groovyButton)
        javaButton.isSelected = true

        buildTypeCombo = ComboBox(BuildType.values())
        buildTypeCombo.selectedItem = settings.buildType
        buildTypeCombo.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value is BuildType) {
                    text = value.displayName
                }
                return this
            }
        }

        javaVersionCombo = ComboBox(arrayOf("21", "17", "11"))
        javaVersionCombo.selectedItem = settings.javaVersion

        // Packaging selection
        packagingButtonGroup = ButtonGroup()
        jarButton = JToggleButton("Jar")
        warButton = JToggleButton("War")
        packagingButtonGroup.add(jarButton)
        packagingButtonGroup.add(warButton)
        jarButton.isSelected = true

        // Auto-update package name
        val updatePackageName = {
            val group = groupField.text.trim()
            val artifact = artifactField.text.trim()
            if (group.isNotEmpty() && artifact.isNotEmpty()) {
                packageNameField.text = "$group.$artifact"
            }
        }

        groupField.document.addDocumentListener(SimpleDocumentListener { updatePackageName() })
        artifactField.document.addDocumentListener(SimpleDocumentListener { updatePackageName() })

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Server URL:"), createServerUrlPanel())
            .addVerticalGap(10)
            .addLabeledComponent(JBLabel("Language:"), createLanguagePanel())
            .addLabeledComponent(JBLabel("Type:"), buildTypeCombo)
            .addVerticalGap(10)
            .addLabeledComponent(JBLabel("Group:"), groupField)
            .addLabeledComponent(JBLabel("Artifact:"), artifactField)
            .addLabeledComponent(JBLabel("Package name:"), packageNameField)
            .addVerticalGap(10)
            .addLabeledComponent(JBLabel("Java:"), javaVersionCombo)
            .addLabeledComponent(JBLabel("Packaging:"), createPackagingPanel())
            .addComponentFillVertically(JPanel(), 0)
            .panel
            .apply {
                border = JBUI.Borders.empty(10)
            }
    }

    private fun createServerUrlPanel(): JPanel {
        val panel = JPanel(BorderLayout(5, 0))
        panel.add(serverUrlField, BorderLayout.CENTER)
        return panel
    }

    private fun createLanguagePanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(javaButton)
        panel.add(kotlinButton)
        panel.add(groovyButton)
        return panel
    }

    private fun createPackagingPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(jarButton)
        panel.add(warButton)
        return panel
    }

    override fun getComponent(): JComponent = panel

    override fun updateDataModel() {
        settings.serverUrl = serverUrlField.text.trim()
        settings.group = groupField.text.trim()
        settings.artifact = artifactField.text.trim()
        settings.name = artifactField.text.trim()
        settings.packageName = packageNameField.text.trim()

        settings.language = when {
            javaButton.isSelected -> Language.JAVA
            kotlinButton.isSelected -> Language.KOTLIN
            groovyButton.isSelected -> Language.GROOVY
            else -> Language.JAVA
        }

        settings.buildType = buildTypeCombo.selectedItem as BuildType
        settings.javaVersion = javaVersionCombo.selectedItem as String

        settings.packaging = when {
            jarButton.isSelected -> Packaging.JAR
            warButton.isSelected -> Packaging.WAR
            else -> Packaging.JAR
        }
    }

    @Throws(ConfigurationException::class)
    override fun validate(): Boolean {
        if (groupField.text.trim().isEmpty()) {
            throw ConfigurationException("Group cannot be empty")
        }
        if (artifactField.text.trim().isEmpty()) {
            throw ConfigurationException("Artifact cannot be empty")
        }
        if (packageNameField.text.trim().isEmpty()) {
            throw ConfigurationException("Package name cannot be empty")
        }
        return true
    }
}