package com.cafemanager.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val date: String,
    val amount: Long,
    val cash: Long,
    val upi: Long,
    val card: Long = 0,
    val delivery: Long = 0,
    val orderCount: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val date: String,
    val amount: Long,
    val category: String,
    val item: String = "",
    val quantity: Double? = null,
    val unit: String = "",
    val unitPrice: Long? = null,
    val paymentMethod: String,
    val supplier: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val minimumStock: Double = 0.0,
    val purchasePrice: Long = 0,
    val supplier: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
