package com.keresman.springwizard

import com.intellij.openapi.progress.ProgressIndicator
import com.keresman.springbootwizard.SpringBootSettings
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class SpringInitializrDownloader(private val settings: SpringBootSettings) {

    fun downloadAndExtract(targetPath: String, indicator: ProgressIndicator) {
        val url = buildInitializrUrl()
        indicator.text = "Downloading from $url"

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/zip")
        connection.connect()

        if (connection.responseCode != 200) {
            throw RuntimeException("Failed to download project: HTTP ${connection.responseCode}")
        }

        indicator.text = "Extracting project files..."

        ZipInputStream(connection.inputStream).use { zipIn ->
            var entry = zipIn.nextEntry

            while (entry != null) {
                val filePath = File(targetPath, entry.name)

                if (!entry.isDirectory) {
                    extractFile(zipIn, filePath)
                } else {
                    filePath.mkdirs()
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        indicator.text = "Project created successfully"
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: File) {
        filePath.parentFile?.mkdirs()
        FileOutputStream(filePath).use { fos ->
            val buffer = ByteArray(4096)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
        }
    }

    private fun buildInitializrUrl(): String {
        val params = mutableListOf<String>()

        params.add("type=${settings.buildType.value}")
        params.add("language=${settings.language.value}")
        params.add("bootVersion=${settings.bootVersion}")
        params.add("baseDir=${settings.artifact}")
        params.add("groupId=${settings.group}")
        params.add("artifactId=${settings.artifact}")
        params.add("name=${settings.name}")
        params.add("description=${settings.description}")
        params.add("packageName=${settings.packageName}")
        params.add("packaging=${settings.packaging.value}")
        params.add("javaVersion=${settings.javaVersion}")

        // Add dependencies
        if (settings.dependencies.isNotEmpty()) {
            val deps = settings.dependencies.joinToString(",") { it.id }
            params.add("dependencies=$deps")
        }

        return "${settings.serverUrl}/starter.zip?${params.joinToString("&")}"
    }
}