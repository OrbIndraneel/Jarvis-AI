package com.example.client

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.example.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Models ---

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Client Instance ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun getJarvisResponse(
        prompt: String,
        contextHistory: List<Content> = emptyList(),
        modelName: String = "gemini-3.1-flash-lite-preview"
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Sir, I require an atmospheric calibration key (Gemini API Secret) declared in your console settings to tap into Stark Mainframe databases. Please populate your Gemini API Key in the Secrets panel."
        }

        // Configure historic context + new prompt
        val contentsList = mutableListOf<Content>()
        contentsList.addAll(contextHistory)
        contentsList.add(Content(listOf(Part(text = prompt))))

        val systemPrompt = """
            You are J.A.R.V.I.S., a sleek, tactical, highly intelligent artificial intelligence butler and virtual assistant created by Tony Stark.
            Speak in a highly polished, witty, respectful British assistant tone, addressing the user as 'Sir' or 'Ma'am'.
            Keep replies crisp, smart, and highly optimized, inserting subtle cybernetic technical references (reactor output, mainframe sync, security protocols, system loads etc.) when relevant.
            If the prompt is about emails, calendar, tasks, or analysis, organize your answer clearly and neatly.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1000
            ),
            systemInstruction = Content(listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = service.generateContent(modelName, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Jarvis core diagnostic: Received an empty transmission pipeline, Sir."
        } catch (e: retrofit2.HttpException) {
            var details = "HTTP ${e.code()} ${e.message()}"
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val apiMessage = extractErrorMessage(errorBody)
                if (!apiMessage.isNullOrEmpty()) {
                    details = apiMessage
                }
            } catch (ex: Exception) {
                // Keep standard HTTP description
            }
            "Core synchronization link failure, Sir. Details: $details"
        } catch (e: java.net.UnknownHostException) {
            "No internet connection detected, Sir. Please check your system uplinks."
        } catch (e: Exception) {
            "Core synchronization link failure, Sir. Details: ${e.localizedMessage ?: "Unknown link interrupt."}"
        }
    }

    private fun extractErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrEmpty()) return null
        return try {
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(errorBody)
            val errorMap = map?.get("error") as? Map<*, *>
            errorMap?.get("message") as? String
        } catch (ex: Exception) {
            null
        }
    }

    suspend fun getAutocompleteSuggestion(partialPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return ""

        val systemPrompt = """
            You are an expert command autocomplete suggestion system for JARVIS.
            The user is currently typing a command.
            Look at what they typed: "$partialPrompt"
            Generate a short, sleek, highly customized command suffix (1 to 4 words max) to complete their request inline.
            Output ONLY the raw suffix completion text that starts EXACTLY at the end of what they typed, with no starting space, no quotes, and no formatting.
            Examples:
            If user typed: "draft em", output: "ail to Tony Stark"
            If user typed: "calibrate arc re", output: "actor core"
            If user typed: "what is my sc", output: "hedule today"
            If user typed: "turn on li", output: "ghts in penthouse"
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(listOf(Part(text = "Complete user query: $partialPrompt")))),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                maxOutputTokens = 15
            ),
            systemInstruction = Content(listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = service.generateContent("gemini-3.1-flash-lite-preview", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
