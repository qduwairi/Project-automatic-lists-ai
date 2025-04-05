package com.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.automatic_shopping_list.ShoppingItem
import com.example.automatic_shopping_list.ShoppingListManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen() {
    // Create a shopping list manager instance
    val shoppingListManager = remember { ShoppingListManager() }

    // State for the list of shopping items
    var items by remember { mutableStateOf(shoppingListManager.getItems()) }

    // State for the text field
    var newItemText by remember { mutableStateOf("") }

    // Function to refresh items list
    val refreshItems = {
        items = shoppingListManager.getItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automatic Shopping List") },
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
            // Input field and add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("New item") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newItemText.isNotEmpty()) {
                            val newItem = ShoppingItem(
                                id = System.currentTimeMillis(),
                                name = newItemText
                            )
                            shoppingListManager.addItem(newItem)
                            newItemText = ""
                            refreshItems()
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shopping list
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Your shopping list is empty")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(items) { item ->
                            ShoppingItemRow(
                                item = item,
                                onItemChecked = { checked ->
                                    // Create a new item with updated checked status
                                    val updatedItem = item.copy(isChecked = checked)
                                    // Remove the old item and add the updated one
                                    shoppingListManager.removeItem(item.id)
                                    shoppingListManager.addItem(updatedItem)
                                    refreshItems()
                                },
                                onDeleteItem = {
                                    shoppingListManager.removeItem(item.id)
                                    refreshItems()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onItemChecked: (Boolean) -> Unit,
    onDeleteItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onItemChecked
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )

        if (item.quantity > 1) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "×${item.quantity}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        IconButton(onClick = onDeleteItem) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    Divider()
}