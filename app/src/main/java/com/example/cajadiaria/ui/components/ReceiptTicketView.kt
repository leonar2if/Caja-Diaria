package com.example.cajadiaria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajadiaria.data.local.entity.DailySessionEntity
import com.example.cajadiaria.data.local.entity.SaleWithItems
import com.example.ui.theme.*
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TicketProductLine(
    val productName: String,
    val quantity: Int,
    val totalAmount: Double
)

@Composable
fun ReceiptTicketDialog(
    session: DailySessionEntity,
    liveSales: List<SaleWithItems> = emptyList(),
    isClosingActiveDay: Boolean = true,
    onDismiss: () -> Unit,
    onConfirmCloseDay: () -> Unit = {}
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply { maximumFractionDigits = 0 } }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy | HH:mm", Locale.getDefault()) }

    val formattedDate = remember(session.startTime, session.endTime) {
        val dateToUse = session.endTime ?: session.startTime
        dateFormat.format(Date(dateToUse))
    }

    // Process aggregated products
    val productLines = remember(session, liveSales) {
        val lines = mutableListOf<TicketProductLine>()
        if (session.closedSummaryJson.isNullOrBlank()) {
            // Aggregate from liveSales
            val map = mutableMapOf<String, Pair<Int, Double>>()
            for (sale in liveSales) {
                for (item in sale.items) {
                    val curr = map[item.productName] ?: Pair(0, 0.0)
                    map[item.productName] = Pair(curr.first + item.quantity, curr.second + item.subtotal)
                }
            }
            for ((name, pair) in map) {
                lines.add(TicketProductLine(name, pair.first, pair.second))
            }
        } else {
            // Parse JSON
            try {
                val jsonArr = JSONArray(session.closedSummaryJson)
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    lines.add(
                        TicketProductLine(
                            productName = obj.optString("name", "Producto"),
                            quantity = obj.optInt("quantity", 0),
                            totalAmount = obj.optDouble("total", 0.0)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lines
    }

    val totalCash = if (isClosingActiveDay) {
        liveSales.filter { it.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true) }.sumOf { it.sale.totalAmount }
    } else {
        session.totalCash
    }

    val totalTransfer = if (isClosingActiveDay) {
        liveSales.filter { it.sale.paymentMethod.equals("TRANSFERENCIA", ignoreCase = true) }.sumOf { it.sale.totalAmount }
    } else {
        session.totalTransfer
    }

    val totalUsdCollected = if (isClosingActiveDay) {
        liveSales.sumOf { it.sale.totalAmountUsd }
    } else {
        session.totalUsd
    }
    val usdFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val totalVendido = totalCash + totalTransfer
    val commissionAmount = totalVendido * (session.commissionPercentage / 100.0)
    val netProfit = totalVendido - commissionAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Indigo600)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isClosingActiveDay) "Ticket de Cierre de Día" else "Detalle de Ticket",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Slate700)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFAFAFA))
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "========================================",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "             RESUMEN DE CIERRE          ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Slate900
                )
                Text(
                    text = "         Fecha: $formattedDate",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate700
                )
                Text(
                    text = "========================================",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "PRODUCTO        x CANTIDAD  = TOTAL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Slate900
                )
                Text(
                    text = "----------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                if (productLines.isEmpty()) {
                    Text(
                        text = "(Sin ventas registradas en la jornada)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    productLines.forEach { line ->
                        val namePadded = if (line.productName.length > 15) {
                            line.productName.substring(0, 15)
                        } else {
                            line.productName.padEnd(15, ' ')
                        }
                        val qtyStr = "x ${line.quantity}".padEnd(10, ' ')
                        val amtStr = "= ${currencyFormat.format(line.totalAmount)}"

                        Text(
                            text = "$namePadded $qtyStr $amtStr",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Slate900,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "----------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MÉTODO DE PAGO:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Slate900
                )
                Text(
                    text = "  - Efectivo:                 ${currencyFormat.format(totalCash)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate900
                )
                Text(
                    text = "  - Transferencia:            ${currencyFormat.format(totalTransfer)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate900
                )

                if (totalUsdCollected > 0) {
                    Text(
                        text = "  - Dólares (US$):             ${usdFormat.format(totalUsdCollected)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Amber600
                    )
                }

                Text(
                    text = "----------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Text(
                    text = "TOTAL VENDIDO:                ${currencyFormat.format(totalVendido)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Slate900
                )
                Text(
                    text = "Comisión Vendedor (${session.commissionPercentage.toInt()}%):       ${currencyFormat.format(commissionAmount)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate700
                )

                Text(
                    text = "----------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Text(
                    text = "GANANCIA NETO NEGOCIO:        ${currencyFormat.format(netProfit)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Emerald600
                )

                Text(
                    text = "========================================",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        },
        confirmButton = {
            if (isClosingActiveDay) {
                Button(
                    onClick = onConfirmCloseDay,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_close_day_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar y Cerrar Jornada", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BlackHeader),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar Ticket", color = Color.White)
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

