package com.example.cajadiaria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double,
    // Moneda en la que efectivamente se cobró este renglón: "MXN" o "USD"
    val currency: String = "MXN",
    // Si el producto era en USD y se cobró "al cambio", aquí queda el valor del dólar usado
    val exchangeRateApplied: Double? = null
)
