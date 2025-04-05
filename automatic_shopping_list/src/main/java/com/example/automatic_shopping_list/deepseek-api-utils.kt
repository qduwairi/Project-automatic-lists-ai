package com.example.automatic_shopping_list

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Function to send a query to DeepSeek API using their OpenAI-compatible endpoint
 * @param query The user's query text
 * @return The response from the DeepSeek API
 */
suspend fun sendQueryToDeepSeek(query: String): String {
    return withContext(Dispatchers.IO) {
        try {
            // DeepSeek Chat API endpoint (OpenAI compatible)
            val url = URL("https://api.deepseek.com/chat/completions")
            val connection = url.openConnection() as HttpURLConnection

            // Set up the connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer sk-29de8386011242aa9723ab62eb742365") // Replace with your actual API key
            connection.doOutput = true

            // Create messages array according to DeepSeek API
            val messagesArray = JSONArray().apply {
                // Add system message with specific instructions
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a shopping list generator. Provide only essential items for events without any introductions, explanations, or conclusions. Return only a numbered list of necessary items.")
                })

                // Add user message (the query)
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", query)
                })
            }

            // Create the request body following DeepSeek's format
            val requestBody = JSONObject().apply {
                put("model", "deepseek-chat") // Uses DeepSeek-V3
                // Alternatively use "deepseek-reasoner" for DeepSeek-R1
                put("messages", messagesArray)
                put("stream", false)
                put("temperature", 0.3) // Lower temperature for more focused response
                put("max_tokens", 2048) // Limit token count since we only need a concise list
            }.toString()

            // Send the request
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(requestBody)
            writer.flush()

            // Get the response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                val response = connection.inputStream.bufferedReader().use { it.readText() }

                // Parse the JSON response to extract the content
                val jsonResponse = JSONObject(response)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    return@withContext message.getString("content")
                } else {
                    "No response content available"
                }
            } else {
                // For error responses, try to read error message from response body
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                "Error: HTTP ${connection.responseCode} - ${connection.responseMessage}\nDetails: $errorResponse"
            }
        } catch (e: Exception) {
            "Error connecting to DeepSeek API: ${e.message}"
        }
    }
}