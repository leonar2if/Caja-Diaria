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

    suspend fun addNewProduct(name: String, price: Double, currency: String = "MXN"): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val existing = dao.getProductByName(trimmed)
        if (existing != null) {
            existing.id
        } else {
            dao.insertProduct(ProductEntity(name = trimmed, price = price, salesCount = 0, currency = currency))
        }
    }

    suspend fun updateProduct(id: Long, name: String, price: Double, currency: String) = withContext(Dispatchers.IO) {
        val existing = dao.getProductById(id) ?: return@withContext
        dao.updateProduct(existing.copy(name = name.trim(), price = price, currency = currency))
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteProduct(id)
    }

    suspend fun startNewDay(commissionPercentage: Double, exchangeRate: Double = 0.0): Long = withContext(Dispatchers.IO) {
        val session = DailySessionEntity(
            commissionPercentage = commissionPercentage,
            startTime = System.currentTimeMillis(),
            isClosed = false,
            exchangeRate = exchangeRate
        )
        dao.insertSession(session)
    }

    /**
     * Cada item de venta viaja como (producto, cantidad, pagarEnUsd).
     * - Si el producto es MXN, pagarEnUsd se ignora (siempre MXN).
     * - Si el producto es USD y pagarEnUsd = true -> se cobra en dólares tal cual (unitPrice = precio USD).
     * - Si el producto es USD y pagarEnUsd = false -> se cobra "al cambio": unitPrice = precio USD * exchangeRate (MXN).
     */
    suspend fun registerSale(
        sessionId: Long,
        paymentMethod: String,
        items: List<Triple<ProductEntity, Int, Boolean>>,
        exchangeRate: Double
    ) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext

        val saleItems = buildSaleItems(items, exchangeRate)
        val totalAmount = saleItems.filter { it.currency == "MXN" }.sumOf { it.subtotal }
        val totalAmountUsd = saleItems.filter { it.currency == "USD" }.sumOf { it.subtotal }

        val sale = SaleEntity(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            totalAmount = totalAmount,
            totalAmountUsd = totalAmountUsd
        )
        val saleId = dao.insertSale(sale)

        dao.insertSaleItems(saleItems.map { it.copy(saleId = saleId) })

        // Update product ranking points
        for ((product, qty, _) in items) {
            dao.updateProductSalesCount(product.id, qty)
        }

        recalculateSessionTotals(sessionId)
    }

    suspend fun editSale(
        saleId: Long,
        newPaymentMethod: String,
        newItems: List<Triple<ProductEntity, Int, Boolean>>,
        exchangeRate: Double
    ) = withContext(Dispatchers.IO) {
        val existingSale = dao.getSaleById(saleId) ?: return@withContext
        val oldItems = dao.getSaleItemsBySaleId(saleId)

        // Revert old ranking counts
        for (item in oldItems) {
            dao.updateProductSalesCount(item.productId, -item.quantity)
        }

        // Delete old sale items
        dao.deleteSaleItems(saleId)

        val newSaleItems = buildSaleItems(newItems, exchangeRate)
        val totalAmount = newSaleItems.filter { it.currency == "MXN" }.sumOf { it.subtotal }
        val totalAmountUsd = newSaleItems.filter { it.currency == "USD" }.sumOf { it.subtotal }

        val updatedSale = existingSale.copy(
            paymentMethod = newPaymentMethod,
            totalAmount = totalAmount,
            totalAmountUsd = totalAmountUsd
        )
        dao.insertSale(updatedSale)

        dao.insertSaleItems(newSaleItems.map { it.copy(saleId = saleId) })

        // Apply new ranking counts
        for ((product, qty, _) in newItems) {
            dao.updateProductSalesCount(product.id, qty)
        }

        recalculateSessionTotals(existingSale.sessionId)
    }

    private fun buildSaleItems(
        items: List<Triple<ProductEntity, Int, Boolean>>,
        exchangeRate: Double
    ): List<SaleItemEntity> {
        return items.map { (product, qty, payInUsd) ->
            val isUsdProduct = product.currency.equals("USD", ignoreCase = true)
            if (isUsdProduct && payInUsd) {
                SaleItemEntity(
                    saleId = 0,
                    productId = product.id,
                    productName = product.name,
                    unitPrice = product.price,
                    quantity = qty,
                    subtotal = product.price * qty,
                    currency = "USD",
                    exchangeRateApplied = null
                )
            } else if (isUsdProduct) {
                // "Al cambio": se multiplica por el valor del dólar del día
                val mxnUnitPrice = product.price * exchangeRate
                SaleItemEntity(
                    saleId = 0,
                    productId = product.id,
                    productName = product.name,
                    unitPrice = mxnUnitPrice,
                    quantity = qty,
                    subtotal = mxnUnitPrice * qty,
                    currency = "MXN",
                    exchangeRateApplied = exchangeRate
                )
            } else {
                SaleItemEntity(
                    saleId = 0,
                    productId = product.id,
                    productName = product.name,
                    unitPrice = product.price,
                    quantity = qty,
                    subtotal = product.price * qty,
                    currency = "MXN",
                    exchangeRateApplied = null
                )
            }
        }
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
        var totalUsd = 0.0

        for (swi in allSales) {
            if (swi.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true)) {
                totalCash += swi.sale.totalAmount
            } else {
                totalTransfer += swi.sale.totalAmount
            }
            totalUsd += swi.sale.totalAmountUsd
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
                    netProfit = net,
                    totalUsd = totalUsd
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
        var totalUsd = 0.0

        for (s in sales) {
            if (s.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true)) {
                totalCash += s.sale.totalAmount
            } else {
                totalTransfer += s.sale.totalAmount
            }
            totalUsd += s.sale.totalAmountUsd

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
            closedSummaryJson = jsonArray.toString(),
            totalUsd = totalUsd
        )

        dao.updateSession(closedSession)
        closedSession
    }
}
