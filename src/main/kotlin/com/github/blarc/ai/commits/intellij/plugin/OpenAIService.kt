package com.github.blarc.ai.commits.intellij.plugin

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service
class OpenAIService {
    companion object {
        const val model = "gpt-3.5-turbo"
        val instance: OpenAIService
            get() = ApplicationManager.getApplication().getService(OpenAIService::class.java)
    }

    @OptIn(BetaOpenAI::class)
    suspend fun generateCommitMessage(diff: String, completions: Int): String {
        val openAiToken = AppSettings.instance.getOpenAIToken() ?: throw Exception("OpenAI Token is not set")

        val openAI = OpenAI(openAiToken)

        val prompt = AppSettings.instance.getPrompt().replace("{diffs}", diff)

        val chatCompletionRequest = ChatCompletionRequest(
            ModelId(model),
            listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            temperature = 0.7,
            topP = 1.0,
            frequencyPenalty = 0.0,
            presencePenalty = 0.0,
            maxTokens = 200,
            n = completions
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        return completion.choices[0].message!!.content
    }
    @Throws(Exception::class)
    suspend fun verifyToken(token: String){
        OpenAI(token).models()
    }
}