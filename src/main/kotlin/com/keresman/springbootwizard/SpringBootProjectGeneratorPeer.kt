package com.keresman.springwizard

import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.keresman.springbootwizard.BuildType
import com.keresman.springbootwizard.Language
import com.keresman.springbootwizard.Packaging
import com.keresman.springbootwizard.SpringBootSettings
import java.awt.BorderLayout
import javax.swing.*

class SpringBootProjectGeneratorPeer : ProjectGeneratorPeer<SpringBootSettings> {

    private val settings = SpringBootSettings()
    private val panel: JPanel

    private val serverUrlField: JBTextField
    private val groupField: JBTextField
    private val artifactField: JBTextField
    private val packageNameField: JBTextField
    private val languageCombo: ComboBox<Language>
    private val buildTypeCombo: ComboBox<BuildType>
    private val javaVersionCombo: ComboBox<String>
    private val packagingCombo: ComboBox<Packaging>

    init {
        serverUrlField = JBTextField(settings.serverUrl)
        groupField = JBTextField(settings.group)
        artifactField = JBTextField(settings.artifact)
        packageNameField = JBTextField(settings.packageName)

        languageCombo = ComboBox(Language.entries.toTypedArray())
        languageCombo.selectedItem = settings.language

        buildTypeCombo = ComboBox(BuildType.values())
        buildTypeCombo.selectedItem = settings.buildType

        javaVersionCombo = ComboBox(arrayOf("23", "21", "17", "11", "8"))
        javaVersionCombo.selectedItem = settings.javaVersion

        packagingCombo = ComboBox(Packaging.values())
        packagingCombo.selectedItem = settings.packaging

        // Auto-update package name when group or artifact changes
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
            .addLabeledComponent(JBLabel("Group:"), groupField)
            .addLabeledComponent(JBLabel("Artifact:"), artifactField)
            .addLabeledComponent(JBLabel("Package name:"), packageNameField)
            .addLabeledComponent(JBLabel("JDK:"), javaVersionCombo)
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

        val settingsButton = JButton()
        settingsButton.icon = null
        settingsButton.text = "⚙"
        settingsButton.toolTipText = "Server settings"
        panel.add(settingsButton, BorderLayout.EAST)

        return panel
    }

    private fun createLanguagePanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        Language.values().forEach { lang ->
            val button = JToggleButton(lang.displayName)
            button.addActionListener {
                languageCombo.selectedItem = lang
            }
            panel.add(button)
        }

        return panel
    }

    private fun createPackagingPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        val jarButton = JToggleButton(Packaging.JAR.displayName)
        val warButton = JToggleButton(Packaging.WAR.displayName)

        val group = ButtonGroup()
        group.add(jarButton)
        group.add(warButton)

        jarButton.isSelected = true

        jarButton.addActionListener { packagingCombo.selectedItem = Packaging.JAR }
        warButton.addActionListener { packagingCombo.selectedItem = Packaging.WAR }

        panel.add(jarButton)
        panel.add(warButton)

        return panel
    }

    override fun getComponent(): JComponent = panel

    override fun buildUI(settingsStep: SettingsStep) {
        settingsStep.addSettingsComponent(component)
    }

    override fun getSettings(): SpringBootSettings {
        settings.serverUrl = serverUrlField.text.trim()
        settings.group = groupField.text.trim()
        settings.artifact = artifactField.text.trim()
        settings.name = artifactField.text.trim()
        settings.packageName = packageNameField.text.trim()
        settings.language = languageCombo.selectedItem as Language
        settings.buildType = buildTypeCombo.selectedItem as BuildType
        settings.javaVersion = javaVersionCombo.selectedItem as String
        settings.packaging = packagingCombo.selectedItem as Packaging

        return settings
    }

    override fun validate(): ValidationInfo? {
        if (groupField.text.trim().isEmpty()) {
            return ValidationInfo("Group cannot be empty", groupField)
        }
        if (artifactField.text.trim().isEmpty()) {
            return ValidationInfo("Artifact cannot be empty", artifactField)
        }
        if (packageNameField.text.trim().isEmpty()) {
            return ValidationInfo("Package name cannot be empty", packageNameField)
        }
        return null
    }

    override fun isBackgroundJobRunning(): Boolean = false

    override fun addSettingsListener(listener: ProjectGeneratorPeer.SettingsListener) {}
}

// Helper class for document listeners
class SimpleDocumentListener(private val action: () -> Unit) : javax.swing.event.DocumentListener {
    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = action()
    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = action()
    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = action()
}