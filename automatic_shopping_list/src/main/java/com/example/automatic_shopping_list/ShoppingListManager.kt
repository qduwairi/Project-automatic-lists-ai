package com.example.automatic_shopping_list

class ShoppingListManager {
    private val items = mutableListOf<ShoppingItem>()

    fun addItem(item: ShoppingItem) {
        items.add(item)
    }

    fun removeItem(id: Long) {
        items.removeIf { it.id == id }
    }

    fun getItems(): List<ShoppingItem> {
        return items.toList()
    }

    fun clearList() {
        items.clear()
    }
}