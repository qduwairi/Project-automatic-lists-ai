package com.example.automatic_shopping_list

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val quantity: Int = 1,
    val category: String = "",
    val isChecked: Boolean = false
)