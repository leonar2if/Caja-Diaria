package com.example.cajadiaria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_sessions")
data class DailySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commissionPercentage: Double = 6.0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isClosed: Boolean = false,
    val totalCash: Double = 0.0,
    val totalTransfer: Double = 0.0,
    val totalSales: Double = 0.0,
    val commissionAmount: Double = 0.0,
    val netProfit: Double = 0.0,
    val closedSummaryJson: String? = null
)
