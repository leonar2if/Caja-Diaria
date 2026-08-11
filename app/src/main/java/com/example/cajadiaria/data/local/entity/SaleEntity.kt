package com.example.cajadiaria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String, // "EFECTIVO" or "TRANSFERENCIA"
    val totalAmount: Double, // Total en MXN (incluye productos "al cambio")
    val totalAmountUsd: Double = 0.0 // Total pagado directamente en USD
)
