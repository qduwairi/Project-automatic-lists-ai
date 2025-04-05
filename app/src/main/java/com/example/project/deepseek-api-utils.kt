package com.example.project

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
                // Add system message
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a helpful assistant.")
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
                // Add other optional parameters as needed
                // put("temperature", 0.7)
                // put("max_tokens", 4096)
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
                "Error: HTTP ${connection.responseCode} - ${connection.responseMessage}"
            }
        } catch (e: Exception) {
            "Error connecting to DeepSeek API: ${e.message}"
        }
    }
}