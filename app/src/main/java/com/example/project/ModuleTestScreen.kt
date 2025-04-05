package com.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automatic_shopping_list.ShoppingItem
import com.example.automatic_shopping_list.ShoppingListManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleTestScreen() {
    // Create instance of our module's main class
    val shoppingListManager = remember { ShoppingListManager() }

    // State to hold test results
    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }

    // Function to add a test result
    fun addResult(testName: String, result: Boolean, details: String = "") {
        testResults = testResults + TestResult(testName, result, details)
    }

    // Function to run all tests
    fun runAllTests() {
        testResults = emptyList()

        // Test 1: Adding an item
        try {
            val testItem = ShoppingItem(1, "Test Item")
            shoppingListManager.addItem(testItem)
            val items = shoppingListManager.getItems()
            if (items.contains(testItem)) {
                addResult("Add Item", true, "Successfully added item to list")
            } else {
                addResult("Add Item", false, "Failed to add item to list")
            }
        } catch (e: Exception) {
            addResult("Add Item", false, "Exception: ${e.message}")
        }

        // Test 2: Removing an item
        try {
            val itemToRemove = ShoppingItem(2, "Remove Test")
            shoppingListManager.addItem(itemToRemove)
            val beforeSize = shoppingListManager.getItems().size
            shoppingListManager.removeItem(2)
            val afterSize = shoppingListManager.getItems().size

            if (beforeSize > afterSize && !shoppingListManager.getItems().any { it.id == 2L }) {
                addResult("Remove Item", true, "Successfully removed item from list")
            } else {
                addResult("Remove Item", false, "Failed to remove item from list")
            }
        } catch (e: Exception) {
            addResult("Remove Item", false, "Exception: ${e.message}")
        }

        // Test 3: Clearing the list
        try {
            // Add some items first
            shoppingListManager.addItem(ShoppingItem(3, "Clear Test 1"))
            shoppingListManager.addItem(ShoppingItem(4, "Clear Test 2"))

            shoppingListManager.clearList()

            if (shoppingListManager.getItems().isEmpty()) {
                addResult("Clear List", true, "Successfully cleared the list")
            } else {
                addResult("Clear List", false, "Failed to clear the list")
            }
        } catch (e: Exception) {
            addResult("Clear List", false, "Exception: ${e.message}")
        }

        // Add more tests based on your module's functionality
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Module Functionality Tests") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "automatic_shopping_list Module Test",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { runAllTests() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run All Tests")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (testResults.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    testResults.forEach { result ->
                        TestResultItem(result)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Click 'Run All Tests' to test the module functionality",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

data class TestResult(
    val testName: String,
    val passed: Boolean,
    val details: String
)

@Composable
fun TestResultItem(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed)
                Color(0xFFE8F5E9) // Light green for success
            else
                Color(0xFFFFEBEE) // Light red for failure
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = if (result.passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = result.testName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (result.passed) "PASSED" else "FAILED",
                    color = if (result.passed) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }

            if (result.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.details,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}