package com.example.cajadiaria.data.local.dao

import androidx.room.*
import com.example.cajadiaria.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CajaDao {

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY salesCount DESC, name ASC")
    fun getAllProductsByRanking(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY salesCount DESC, name ASC")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("UPDATE products SET salesCount = salesCount + :delta WHERE id = :productId")
    suspend fun updateProductSalesCount(productId: Long, delta: Int)

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): ProductEntity?

    // --- Daily Sessions ---
    @Query("SELECT * FROM daily_sessions WHERE isClosed = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<DailySessionEntity?>

    @Query("SELECT * FROM daily_sessions WHERE isClosed = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSessionSync(): DailySessionEntity?

    @Query("SELECT * FROM daily_sessions WHERE isClosed = 1 ORDER BY endTime DESC")
    fun getAllClosedSessions(): Flow<List<DailySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DailySessionEntity): Long

    @Update
    suspend fun updateSession(session: DailySessionEntity)

    // --- Sales ---
    @Transaction
    @Query("SELECT * FROM sales WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getSalesForSession(sessionId: Long): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    suspend fun getSalesForSessionSync(sessionId: Long): List<SaleWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSale(saleId: Long)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteSaleItems(saleId: Long)

    @Query("SELECT * FROM sales WHERE id = :saleId LIMIT 1")
    suspend fun getSaleById(saleId: Long): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsBySaleId(saleId: Long): List<SaleItemEntity>
}
