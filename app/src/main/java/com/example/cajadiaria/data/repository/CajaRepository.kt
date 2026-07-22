package com.example.cajadiaria.data.repository

import com.example.cajadiaria.data.local.dao.CajaDao
import com.example.cajadiaria.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CajaRepository(private val dao: CajaDao) {

    val productsByRanking: Flow<List<ProductEntity>> = dao.getAllProductsByRanking()
    val activeSession: Flow<DailySessionEntity?> = dao.getActiveSession()
    val closedSessions: Flow<List<DailySessionEntity>> = dao.getAllClosedSessions()

    fun getSalesForSession(sessionId: Long): Flow<List<SaleWithItems>> {
        return dao.getSalesForSession(sessionId)
    }

    suspend fun seedInitialProductsIfEmpty() = withContext(Dispatchers.IO) {
        // App starts empty without default products
    }

    suspend fun addNewProduct(name: String, price: Double): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val existing = dao.getProductByName(trimmed)
        if (existing != null) {
            existing.id
        } else {
            dao.insertProduct(ProductEntity(name = trimmed, price = price, salesCount = 0))
        }
    }

    suspend fun startNewDay(commissionPercentage: Double): Long = withContext(Dispatchers.IO) {
        val session = DailySessionEntity(
            commissionPercentage = commissionPercentage,
            startTime = System.currentTimeMillis(),
            isClosed = false
        )
        dao.insertSession(session)
    }

    suspend fun registerSale(
        sessionId: Long,
        paymentMethod: String,
        items: List<Pair<ProductEntity, Int>>
    ) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext

        val totalAmount = items.sumOf { it.first.price * it.second }
        val sale = SaleEntity(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            totalAmount = totalAmount
        )
        val saleId = dao.insertSale(sale)

        val saleItems = items.map { (product, qty) ->
            SaleItemEntity(
                saleId = saleId,
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                quantity = qty,
                subtotal = product.price * qty
            )
        }
        dao.insertSaleItems(saleItems)

        // Update product ranking points
        for ((product, qty) in items) {
            dao.updateProductSalesCount(product.id, qty)
        }

        recalculateSessionTotals(sessionId)
    }

    suspend fun editSale(
        saleId: Long,
        newPaymentMethod: String,
        newItems: List<Pair<ProductEntity, Int>>
    ) = withContext(Dispatchers.IO) {
        val existingSale = dao.getSaleById(saleId) ?: return@withContext
        val oldItems = dao.getSaleItemsBySaleId(saleId)

        // Revert old ranking counts
        for (item in oldItems) {
            dao.updateProductSalesCount(item.productId, -item.quantity)
        }

        // Delete old sale items
        dao.deleteSaleItems(saleId)

        val totalAmount = newItems.sumOf { it.first.price * it.second }
        val updatedSale = existingSale.copy(
            paymentMethod = newPaymentMethod,
            totalAmount = totalAmount
        )
        dao.insertSale(updatedSale)

        val newSaleItems = newItems.map { (product, qty) ->
            SaleItemEntity(
                saleId = saleId,
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                quantity = qty,
                subtotal = product.price * qty
            )
        }
        dao.insertSaleItems(newSaleItems)

        // Apply new ranking counts
        for ((product, qty) in newItems) {
            dao.updateProductSalesCount(product.id, qty)
        }

        recalculateSessionTotals(existingSale.sessionId)
    }

    suspend fun deleteSale(saleId: Long) = withContext(Dispatchers.IO) {
        val existingSale = dao.getSaleById(saleId) ?: return@withContext
        val oldItems = dao.getSaleItemsBySaleId(saleId)

        // Subtract ranking counts
        for (item in oldItems) {
            dao.updateProductSalesCount(item.productId, -item.quantity)
        }

        dao.deleteSaleItems(saleId)
        dao.deleteSale(saleId)

        recalculateSessionTotals(existingSale.sessionId)
    }

    private suspend fun recalculateSessionTotals(sessionId: Long) {
        val allSales = dao.getSalesForSessionSync(sessionId)
        var totalCash = 0.0
        var totalTransfer = 0.0

        for (swi in allSales) {
            if (swi.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true)) {
                totalCash += swi.sale.totalAmount
            } else {
                totalTransfer += swi.sale.totalAmount
            }
        }

        val totalSales = totalCash + totalTransfer
        val active = dao.getActiveSessionSync()
        if (active != null && active.id == sessionId) {
            val commPercent = active.commissionPercentage
            val commAmt = totalSales * (commPercent / 100.0)
            val net = totalSales - commAmt

            dao.updateSession(
                active.copy(
                    totalCash = totalCash,
                    totalTransfer = totalTransfer,
                    totalSales = totalSales,
                    commissionAmount = commAmt,
                    netProfit = net
                )
            )
        }
    }

    suspend fun closeSession(sessionId: Long): DailySessionEntity? = withContext(Dispatchers.IO) {
        val active = dao.getActiveSessionSync() ?: return@withContext null
        if (active.id != sessionId) return@withContext null

        val sales = dao.getSalesForSessionSync(sessionId)

        // Calculate aggregated items
        val itemSummaryMap = mutableMapOf<String, Pair<Int, Double>>() // productName -> (quantity, total)
        var totalCash = 0.0
        var totalTransfer = 0.0

        for (s in sales) {
            if (s.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true)) {
                totalCash += s.sale.totalAmount
            } else {
                totalTransfer += s.sale.totalAmount
            }

            for (item in s.items) {
                val current = itemSummaryMap[item.productName] ?: Pair(0, 0.0)
                itemSummaryMap[item.productName] = Pair(
                    current.first + item.quantity,
                    current.second + item.subtotal
                )
            }
        }

        val totalSales = totalCash + totalTransfer
        val commissionAmt = totalSales * (active.commissionPercentage / 100.0)
        val netProfit = totalSales - commissionAmt

        // Create JSON array for ticket item breakdown
        val jsonArray = JSONArray()
        for ((name, pair) in itemSummaryMap) {
            val obj = JSONObject().apply {
                put("name", name)
                put("quantity", pair.first)
                put("total", pair.second)
            }
            jsonArray.put(obj)
        }

        val closedSession = active.copy(
            endTime = System.currentTimeMillis(),
            isClosed = true,
            totalCash = totalCash,
            totalTransfer = totalTransfer,
            totalSales = totalSales,
            commissionAmount = commissionAmt,
            netProfit = netProfit,
            closedSummaryJson = jsonArray.toString()
        )

        dao.updateSession(closedSession)
        closedSession
    }
}
