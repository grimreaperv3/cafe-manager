package com.cafemanager.app.data

class CafeRepository(private val db: AppDatabase) {
    val sales = db.saleDao().observeAll()
    val expenses = db.expenseDao().observeAll()
    val inventory = db.inventoryDao().observeAll()

    suspend fun saveSale(x: SaleEntity) = db.saleDao().upsert(x)
    suspend fun deleteSale(x: SaleEntity) = db.saleDao().delete(x)
    suspend fun saveExpense(x: ExpenseEntity) = db.expenseDao().upsert(x)
    suspend fun deleteExpense(x: ExpenseEntity) = db.expenseDao().delete(x)
    suspend fun saveInventory(x: InventoryItemEntity) = db.inventoryDao().upsert(x)
    suspend fun deleteInventory(x: InventoryItemEntity) = db.inventoryDao().delete(x)
}
