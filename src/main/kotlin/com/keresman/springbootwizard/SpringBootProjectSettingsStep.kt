package com.keresman.springbootwizard

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.keresman.springwizard.SimpleDocumentListener
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class SpringBootProjectSettingsStep(
    private val projectGenerator: SpringBootProjectGenerator,
    private val callback: AbstractNewProjectStep.AbstractCallback<SpringBootSettings>
) : AbstractActionWithPanel() {

    private val versionCombo: ComboBox<String>
    private val searchField: JBTextField
    private val dependenciesPanel: JPanel
    private val selectedDependenciesPanel: JPanel
    private val dependencyCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val actionButton: JButton

    private val availableDependencies = createDefaultDependencies()

    init {
        versionCombo = ComboBox(arrayOf("3.5.6", "3.4.1", "3.3.5", "3.2.11"))
        searchField = JBTextField()
        searchField.emptyText.text = "Search"

        dependenciesPanel = JPanel()
        dependenciesPanel.layout = BoxLayout(dependenciesPanel, BoxLayout.Y_AXIS)

        selectedDependenciesPanel = JPanel()
        selectedDependenciesPanel.layout = BoxLayout(selectedDependenciesPanel, BoxLayout.Y_AXIS)
        selectedDependenciesPanel.border = BorderFactory.createTitledBorder("Added dependencies:")

        actionButton = JButton("Create")
        actionButton.addActionListener {
            performAction()
        }

        populateDependencies()

        searchField.document.addDocumentListener(SimpleDocumentListener {
            filterDependencies(searchField.text)
        })
    }

    override fun createPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout(10, 10))
        mainPanel.border = JBUI.Borders.empty(10)

        // Top panel with version selector
        val topPanel = JPanel(BorderLayout())
        topPanel.add(JBLabel("Spring Boot:"), BorderLayout.WEST)
        topPanel.add(versionCombo, BorderLayout.CENTER)

        // Left panel with dependencies
        val leftPanel = JPanel(BorderLayout(0, 5))
        leftPanel.add(JBLabel("Dependencies:"), BorderLayout.NORTH)
        leftPanel.add(searchField, BorderLayout.CENTER)

        val scrollPane = JBScrollPane(dependenciesPanel)
        scrollPane.preferredSize = Dimension(400, 400)
        leftPanel.add(scrollPane, BorderLayout.SOUTH)

        // Right panel with selected dependencies
        val rightScrollPane = JBScrollPane(selectedDependenciesPanel)
        rightScrollPane.preferredSize = Dimension(300, 400)

        // Main content
        val contentPanel = JPanel(BorderLayout(10, 0))
        contentPanel.add(leftPanel, BorderLayout.CENTER)
        contentPanel.add(rightScrollPane, BorderLayout.EAST)

        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(contentPanel, BorderLayout.CENTER)

        return mainPanel
    }

    private fun populateDependencies() {
        dependenciesPanel.removeAll()

        val categorizedDeps = availableDependencies.groupBy { it.category }

        categorizedDeps.forEach { (category, deps) ->
            val categoryPanel = JPanel()
            categoryPanel.layout = BoxLayout(categoryPanel, BoxLayout.Y_AXIS)
            categoryPanel.border = BorderFactory.createTitledBorder(category)
            categoryPanel.alignmentX = JPanel.LEFT_ALIGNMENT

            deps.forEach { dep ->
                val checkbox = JBCheckBox(dep.name)
                checkbox.toolTipText = dep.description
                checkbox.addActionListener {
                    updateSelectedDependencies()
                }
                dependencyCheckboxes[dep.id] = checkbox
                categoryPanel.add(checkbox)
            }

            dependenciesPanel.add(categoryPanel)
        }

        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun filterDependencies(query: String) {
        val lowerQuery = query.lowercase()
        dependencyCheckboxes.values.forEach { checkbox ->
            checkbox.isVisible = query.isEmpty() ||
                    checkbox.text.lowercase().contains(lowerQuery) ||
                    checkbox.toolTipText?.lowercase()?.contains(lowerQuery) == true
        }
        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun updateSelectedDependencies() {
        selectedDependenciesPanel.removeAll()

        val selected = dependencyCheckboxes.filter { it.value.isSelected }

        if (selected.isEmpty()) {
            selectedDependenciesPanel.add(JBLabel("No dependencies added"))
        } else {
            selected.forEach { (id, checkbox) ->
                val dep = availableDependencies.find { it.id == id }
                if (dep != null) {
                    val label = JBLabel("• ${dep.name}")
                    label.toolTipText = dep.description
                    selectedDependenciesPanel.add(label)
                }
            }
        }

        selectedDependenciesPanel.revalidate()
        selectedDependenciesPanel.repaint()
    }

    override fun actionPerformed(e: AnActionEvent) {
        performAction()
    }

    private fun performAction() {
        val peer = projectGenerator.createPeer()

        // Update settings with selected Spring Boot version
        peer.settings.bootVersion = versionCombo.selectedItem as String

        // Update dependencies
        peer.settings.dependencies.clear()
        dependencyCheckboxes.filter { it.value.isSelected }.forEach { (id, _) ->
            availableDependencies.find { it.id == id }?.let {
                peer.settings.dependencies.add(it)
            }
        }

        callback.consume(null, peer)
    }

    override fun getActionButton(): JButton {
        return actionButton
    }

    override fun dispose() {
        // Clean up resources if needed
    }

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