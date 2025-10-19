package com.keresman.springbootwizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.*

class SpringBootDependenciesStep(private val builder: SpringBootModuleBuilder) : ModuleWizardStep() {

    private val settings get() = builder.settings
    private val versionCombo: ComboBox<String>
    private val searchField: SearchTextField
    private val dependenciesPanel: JPanel
    private val selectedDependenciesPanel: JPanel
    private val dependencyCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val categoryPanels = mutableMapOf<String, JPanel>()

    private val availableDependencies = createDefaultDependencies()
    private val panel: JPanel

    init {
        versionCombo = ComboBox(arrayOf("3.5.6", "3.4.1", "3.3.5", "3.2.11"))
        versionCombo.selectedItem = settings.bootVersion

        searchField = SearchTextField(false)
        searchField.textEditor.emptyText.text = "Search"

        dependenciesPanel = JPanel()
        dependenciesPanel.layout = BoxLayout(dependenciesPanel, BoxLayout.Y_AXIS)
        dependenciesPanel.background = UIUtil.getListBackground()

        selectedDependenciesPanel = JPanel()
        selectedDependenciesPanel.layout = BoxLayout(selectedDependenciesPanel, BoxLayout.Y_AXIS)
        selectedDependenciesPanel.background = UIUtil.getListBackground()
        selectedDependenciesPanel.border = JBUI.Borders.empty(10)

        populateDependencies()

        searchField.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = filterDependencies(searchField.text)
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = filterDependencies(searchField.text)
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = filterDependencies(searchField.text)
        })

        panel = createMainPanel()
    }

    private fun createMainPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.border = JBUI.Borders.empty()

        // Top panel with Spring Boot version
        val topPanel = JPanel(BorderLayout(10, 0))
        topPanel.border = JBUI.Borders.empty(10, 10, 5, 10)
        topPanel.background = UIUtil.getPanelBackground()

        val versionLabel = JBLabel("Spring Boot:")
        topPanel.add(versionLabel, BorderLayout.WEST)
        topPanel.add(versionCombo, BorderLayout.CENTER)
        topPanel.add(Box.createHorizontalStrut(300), BorderLayout.EAST) // Spacer

        // Dependencies label and search
        val depsHeaderPanel = JPanel(BorderLayout(10, 5))
        depsHeaderPanel.border = JBUI.Borders.empty(5, 10, 5, 10)
        depsHeaderPanel.background = UIUtil.getPanelBackground()

        val depsLabel = JBLabel("Dependencies:")
        depsLabel.font = depsLabel.font.deriveFont(Font.BOLD)
        depsHeaderPanel.add(depsLabel, BorderLayout.NORTH)
        depsHeaderPanel.add(searchField, BorderLayout.CENTER)

        // Left panel with dependencies
        val leftPanel = JPanel(BorderLayout())
        leftPanel.background = UIUtil.getListBackground()

        val scrollPane = JBScrollPane(dependenciesPanel)
        scrollPane.border = JBUI.Borders.empty()
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        leftPanel.add(scrollPane, BorderLayout.CENTER)

        // Right panel with selected dependencies
        val rightPanel = JPanel(BorderLayout())
        rightPanel.preferredSize = Dimension(280, 0)
        rightPanel.background = UIUtil.getPanelBackground()

        val selectedLabel = JBLabel("Added dependencies:")
        selectedLabel.font = selectedLabel.font.deriveFont(Font.BOLD)
        selectedLabel.border = JBUI.Borders.empty(10)

        val selectedScrollPane = JBScrollPane(selectedDependenciesPanel)
        selectedScrollPane.border = JBUI.Borders.customLine(UIUtil.getBoundsColor(), 0, 1, 0, 0)
        selectedScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        selectedScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER

        rightPanel.add(selectedLabel, BorderLayout.NORTH)
        rightPanel.add(selectedScrollPane, BorderLayout.CENTER)

        // Combine left and right
        val contentSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel)
        contentSplitPane.dividerLocation = 550
        contentSplitPane.resizeWeight = 0.7
        contentSplitPane.border = JBUI.Borders.empty()

        // Main layout
        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(depsHeaderPanel, BorderLayout.NORTH)
        centerPanel.add(contentSplitPane, BorderLayout.CENTER)

        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(centerPanel, BorderLayout.CENTER)

        return mainPanel
    }

    private fun populateDependencies() {
        dependenciesPanel.removeAll()

        val categorizedDeps = availableDependencies.groupBy { it.category }

        categorizedDeps.forEach { (category, deps) ->
            val categoryPanel = JPanel()
            categoryPanel.layout = BoxLayout(categoryPanel, BoxLayout.Y_AXIS)
            categoryPanel.alignmentX = Component.LEFT_ALIGNMENT
            categoryPanel.background = UIUtil.getListBackground()
            categoryPanel.border = JBUI.Borders.empty(5, 10, 5, 10)

            // Category header
            val headerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
            headerPanel.background = UIUtil.getListBackground()
            headerPanel.border = JBUI.Borders.emptyBottom(5)

            val expandButton = JButton("▼")
            expandButton.isBorderPainted = false
            expandButton.isContentAreaFilled = false
            expandButton.font = Font(Font.SANS_SERIF, Font.PLAIN, 10)
            expandButton.preferredSize = Dimension(16, 16)
            expandButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            val categoryLabel = JBLabel(category)
            categoryLabel.font = categoryLabel.font.deriveFont(Font.BOLD, 12f)

            headerPanel.add(expandButton)
            headerPanel.add(Box.createHorizontalStrut(5))
            headerPanel.add(categoryLabel)

            val depsContainer = JPanel()
            depsContainer.layout = BoxLayout(depsContainer, BoxLayout.Y_AXIS)
            depsContainer.background = UIUtil.getListBackground()
            depsContainer.border = JBUI.Borders.emptyLeft(20)

            deps.forEach { dep ->
                val checkboxPanel = JPanel(BorderLayout())
                checkboxPanel.background = UIUtil.getListBackground()
                checkboxPanel.maximumSize = Dimension(Integer.MAX_VALUE, 30)
                checkboxPanel.border = JBUI.Borders.empty(2, 0, 2, 0)

                val checkbox = JBCheckBox(dep.name)
                checkbox.toolTipText = dep.description
                checkbox.background = UIUtil.getListBackground()
                checkbox.addActionListener {
                    updateSelectedDependencies()
                }

                checkboxPanel.add(checkbox, BorderLayout.WEST)

                dependencyCheckboxes[dep.id] = checkbox
                depsContainer.add(checkboxPanel)
            }

            // Toggle visibility on click
            expandButton.addActionListener {
                depsContainer.isVisible = !depsContainer.isVisible
                expandButton.text = if (depsContainer.isVisible) "▼" else "▶"
                dependenciesPanel.revalidate()
                dependenciesPanel.repaint()
            }

            categoryPanel.add(headerPanel)
            categoryPanel.add(depsContainer)

            categoryPanels[category] = categoryPanel
            dependenciesPanel.add(categoryPanel)
        }

        updateSelectedDependencies()
    }

    private fun filterDependencies(query: String) {
        val lowerQuery = query.lowercase().trim()

        if (lowerQuery.isEmpty()) {
            // Show all
            categoryPanels.values.forEach { it.isVisible = true }
            dependencyCheckboxes.values.forEach { it.parent?.isVisible = true }
        } else {
            // Filter
            categoryPanels.forEach { (category, panel) ->
                var hasVisibleChild = false

                for (component in (panel.components[1] as JPanel).components) {
                    if (component is JPanel) {
                        val checkbox = component.components[0] as? JBCheckBox
                        if (checkbox != null) {
                            val visible = checkbox.text.lowercase().contains(lowerQuery) ||
                                    checkbox.toolTipText?.lowercase()?.contains(lowerQuery) == true
                            component.isVisible = visible
                            if (visible) hasVisibleChild = true
                        }
                    }
                }

                panel.isVisible = hasVisibleChild
            }
        }

        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun updateSelectedDependencies() {
        selectedDependenciesPanel.removeAll()

        val selected = dependencyCheckboxes.filter { it.value.isSelected }

        if (selected.isEmpty()) {
            val emptyLabel = JBLabel("<html><center>No dependencies added</center></html>")
            emptyLabel.foreground = UIUtil.getInactiveTextColor()
            emptyLabel.horizontalAlignment = SwingConstants.CENTER
            emptyLabel.border = JBUI.Borders.empty(20)
            selectedDependenciesPanel.add(emptyLabel)
        } else {
            selected.forEach { (id, _) ->
                val dep = availableDependencies.find { it.id == id }
                if (dep != null) {
                    val depPanel = JPanel(BorderLayout())
                    depPanel.background = UIUtil.getListBackground()
                    depPanel.maximumSize = Dimension(Integer.MAX_VALUE, 40)
                    depPanel.border = JBUI.Borders.empty(5, 5, 5, 5)

                    val nameLabel = JBLabel(dep.name)
                    nameLabel.font = nameLabel.font.deriveFont(Font.BOLD, 11f)

                    val descLabel = JBLabel("<html><font color='gray'>${dep.description}</font></html>")
                    descLabel.font = descLabel.font.deriveFont(10f)

                    val textPanel = JPanel()
                    textPanel.layout = BoxLayout(textPanel, BoxLayout.Y_AXIS)
                    textPanel.background = UIUtil.getListBackground()
                    textPanel.add(nameLabel)
                    textPanel.add(descLabel)

                    depPanel.add(textPanel, BorderLayout.CENTER)
                    selectedDependenciesPanel.add(depPanel)
                }
            }
        }

        selectedDependenciesPanel.revalidate()
        selectedDependenciesPanel.repaint()
    }

    override fun getComponent(): JComponent = panel

    override fun updateDataModel() {
        settings.bootVersion = versionCombo.selectedItem as String
        settings.dependencies.clear()

        dependencyCheckboxes.filter { it.value.isSelected }.forEach { (id, _) ->
            availableDependencies.find { it.id == id }?.let {
                settings.dependencies.add(it)
            }
        }
    }

    override fun validate(): Boolean = true

    private fun createDefaultDependencies(): List<Dependency> {
        return listOf(
            // Developer Tools
            Dependency("devtools", "Spring Boot DevTools", "Fast application restarts and LiveReload", "Developer Tools"),
            Dependency("configuration-processor", "Spring Configuration Processor", "Generate metadata for configuration properties", "Developer Tools"),
            Dependency("docker-compose", "Docker Compose Support", "Docker Compose support", "Developer Tools"),
            Dependency("lombok", "Lombok", "Java annotation library", "Developer Tools"),
            Dependency("modulith", "Spring Modulith", "Support for building modular applications", "Developer Tools"),

            // Web
            Dependency("web", "Spring Web", "Build web applications with Spring MVC and Tomcat", "Web"),
            Dependency("webflux", "Spring Reactive Web", "Build reactive web applications with Spring WebFlux", "Web"),
            Dependency("graphql", "Spring for GraphQL", "Build GraphQL applications with Spring", "Web"),
            Dependency("websocket", "WebSocket", "WebSocket support with SockJS and STOMP", "Web"),
            Dependency("hateoas", "Spring HATEOAS", "Build hypermedia-based REST APIs", "Web"),

            // Template Engines
            Dependency("thymeleaf", "Thymeleaf", "Server-side Java template engine", "Template Engines"),
            Dependency("freemarker", "Apache Freemarker", "Java template engine", "Template Engines"),
            Dependency("mustache", "Mustache", "Logic-less templates", "Template Engines"),
            Dependency("groovy-templates", "Groovy Templates", "Groovy templating engine", "Template Engines"),

            // Security
            Dependency("security", "Spring Security", "Authentication and authorization", "Security"),
            Dependency("oauth2-client", "OAuth2 Client", "OAuth2/OpenID Connect client features", "Security"),
            Dependency("oauth2-resource-server", "OAuth2 Resource Server", "OAuth2 resource server features", "Security"),

            // SQL
            Dependency("data-jpa", "Spring Data JPA", "Persist data with Java Persistence API and Hibernate", "SQL"),
            Dependency("data-jdbc", "Spring Data JDBC", "Persist data with plain JDBC", "SQL"),
            Dependency("mysql", "MySQL Driver", "MySQL JDBC driver", "SQL"),
            Dependency("postgresql", "PostgreSQL Driver", "PostgreSQL JDBC driver", "SQL"),
            Dependency("h2", "H2 Database", "In-memory database", "SQL"),
            Dependency("liquibase", "Liquibase Migration", "Database migration with Liquibase", "SQL"),
            Dependency("flyway", "Flyway Migration", "Database migration with Flyway", "SQL"),

            // NoSQL
            Dependency("data-mongodb", "Spring Data MongoDB", "Store data in MongoDB", "NoSQL"),
            Dependency("data-redis", "Spring Data Redis", "Advanced Redis operations", "NoSQL"),
            Dependency("data-elasticsearch", "Spring Data Elasticsearch", "Elasticsearch search and analytics", "NoSQL"),
            Dependency("data-cassandra", "Spring Data Cassandra", "Distributed database support", "NoSQL"),

            // Messaging
            Dependency("amqp", "Spring for RabbitMQ", "Messaging with RabbitMQ", "Messaging"),
            Dependency("kafka", "Spring for Apache Kafka", "Messaging with Kafka", "Messaging"),
            Dependency("artemis", "Spring for Apache ActiveMQ Artemis", "Messaging with Artemis", "Messaging"),
            Dependency("pulsar", "Spring for Apache Pulsar", "Messaging with Pulsar", "Messaging"),

            // I/O
            Dependency("batch", "Spring Batch", "Batch processing framework", "I/O"),
            Dependency("validation", "Validation", "Bean Validation with Hibernate validator", "I/O"),
            Dependency("mail", "Java Mail Sender", "Send email using Java Mail", "I/O"),

            // Ops
            Dependency("actuator", "Spring Boot Actuator", "Production-ready features", "Ops"),
            Dependency("prometheus", "Prometheus", "Expose Prometheus metrics", "Ops"),

            // Observability
            Dependency("zipkin", "Zipkin", "Distributed tracing with Zipkin", "Observability"),

            // Testing
            Dependency("testcontainers", "Testcontainers", "Integration testing with Docker containers", "Testing")
        )
    }
}