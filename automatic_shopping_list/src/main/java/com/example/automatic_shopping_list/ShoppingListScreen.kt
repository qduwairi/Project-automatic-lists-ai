
package com.example.automatic_shopping_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen() {
    // Create a shopping list manager instance
    val shoppingListManager = remember { ShoppingListManager() }

    // State for the event name input
    var eventName by remember { mutableStateOf("") }

    // State for showing loading indicator and storing response
    var isLoading by remember { mutableStateOf(false) }

    // State for shopping items
    var items by remember { mutableStateOf(shoppingListManager.getItems()) }

    // Coroutine scope for API calls
    val coroutineScope = rememberCoroutineScope()

    // Function to refresh items list
    val refreshItems = {
        items = shoppingListManager.getItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Shopping List Generator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Event input section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Generate Shopping List",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            label = { Text("Event Name") },
                            placeholder = { Text("e.g., Birthday Party, Camping Trip") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (eventName.isNotEmpty()) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        try {
                                            // Clear existing items
                                            shoppingListManager.clearList()

                                            // Create prompt for DeepSeek
                                            val prompt = "Generate a list of only the essential items needed for a ${eventName}. " +
                                                    "Return ONLY a numbered list of items with no introduction or conclusion. " +
                                                    "Keep the list concise with only necessary items."

                                            // Get response from DeepSeek API
                                            val response = sendQueryToDeepSeek(prompt)

                                            // Parse the response into shopping items
                                            parseAndAddItems(response, shoppingListManager)

                                            // Refresh the UI
                                            refreshItems()
                                        } catch (e: Exception) {
                                            // Handle error
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = eventName.isNotEmpty() && !isLoading
                        ) {
                            Text("Generate List")
                        }
                    }

                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shopping list display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Shopping List Items",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Enter an event name and click 'Generate List'")
                        }
                    } else {
                        LazyColumn {
                            items(items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { checked ->
                                            // Update item checked status
                                            val updatedItem = item.copy(isChecked = checked)
                                            shoppingListManager.removeItem(item.id)
                                            shoppingListManager.addItem(updatedItem)
                                            refreshItems()
                                        }
                                    )

                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parse the API response and add items to the shopping list manager
 */
private fun parseAndAddItems(response: String, shoppingListManager: ShoppingListManager) {
    // Split the response by new lines and process each line
    response.split("\n").forEach { line ->
        // Clean up and extract the item name
        val trimmedLine = line.trim()
        if (trimmedLine.isNotEmpty()) {
            // Remove numbering (e.g., "1. ", "1) ", etc.)
            val itemName = trimmedLine.replace(Regex("^\\d+[.)]\\s*"), "").trim()
            if (itemName.isNotEmpty()) {
                val newItem = ShoppingItem(
                    id = System.currentTimeMillis() + itemName.hashCode(),
                    name = itemName
                )
                shoppingListManager.addItem(newItem)
            }
        }
    }
}