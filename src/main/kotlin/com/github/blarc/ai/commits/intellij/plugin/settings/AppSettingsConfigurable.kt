package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.api.exception.OpenAIAPIException
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.OpenAIService
import com.intellij.icons.AllIcons
import com.intellij.ide.starters.shared.withValidation
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.swing.JPasswordField

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {

    private val tokenPasswordField = JPasswordField()
    private val verifyLabel = JBLabel()
    override fun createPanel() = panel {
        // OpenAI Token
        row {
            cell(tokenPasswordField)
                .label(message("settings.openAIToken"))
                .bindText(
                    { AppSettings.instance.getOpenAIToken().orEmpty() },
                    { AppSettings.instance.saveOpenAIToken(it) }
                )
                .align(Align.FILL)
                .resizableColumn()
                .focused()
            button(message("settings.verifyToken")) {
                verifyToken()
            }.align(AlignX.RIGHT)
        }
        // Get OpenAI Token
        row {
            comment(message("settings.openAITokenComment"))
                .align(AlignX.LEFT)
            cell(verifyLabel)
                .align(AlignX.RIGHT)
        }

        // Prompt to use with OpenAI
        row {
            textField()
                .label(message("settings.prompt"))
                .bindText(
                    { AppSettings.instance.getPrompt() },
                    { AppSettings.instance.savePrompt(it) }
                )
                .align(Align.FILL)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun verifyToken() {
        runBackgroundableTask(message("settings.verify.running")) {
            if (tokenPasswordField.password.isEmpty()) {
                verifyLabel.icon = AllIcons.General.InspectionsError
                verifyLabel.text = message("settings.verify.token-is-empty")
            } else {
                verifyLabel.icon = AllIcons.General.InlineRefreshHover
                verifyLabel.text = message("settings.verify.running")

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        println(String(tokenPasswordField.password))
                        OpenAIService.instance.verifyToken(String(tokenPasswordField.password))
                        verifyLabel.text = message("settings.verify.valid")
                        verifyLabel.icon = AllIcons.General.InspectionsOK
                    }
                    catch (e: OpenAIAPIException) {
                        verifyLabel.text = message("settings.verify.invalid", e.statusCode)
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                    catch (e: Exception) {
                        verifyLabel.text = message("settings.verify.invalid", "Unknown")
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                }
            }
        }

    }
}