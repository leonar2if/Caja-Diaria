package com.example.cajadiaria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajadiaria.data.local.entity.DailySessionEntity
import com.example.cajadiaria.data.local.entity.ProductEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    closedSessions: List<DailySessionEntity>,
    topProducts: List<ProductEntity>,
    onTicketClick: (DailySessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply { maximumFractionDigits = 0 } }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy | HH:mm", Locale.getDefault()) }

    // Last 30 days statistics computations
    val thirtyDaysAgo = remember { System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) }
    val last30Sessions = remember(closedSessions) {
        closedSessions.filter { (it.endTime ?: it.startTime) >= thirtyDaysAgo }
    }

    val totalIngresosBrutos = remember(last30Sessions) { last30Sessions.sumOf { it.totalSales } }
    val totalEfectivo = remember(last30Sessions) { last30Sessions.sumOf { it.totalCash } }
    val totalTransferencia = remember(last30Sessions) { last30Sessions.sumOf { it.totalTransfer } }
    val totalComisiones = remember(last30Sessions) { last30Sessions.sumOf { it.commissionAmount } }
    val promedioDiario = remember(last30Sessions) {
        if (last30Sessions.isNotEmpty()) totalIngresosBrutos / last30Sessions.size else 0.0
    }

    // Filter tickets
    val filteredClosedSessions = remember(closedSessions, searchQuery) {
        if (searchQuery.isBlank()) {
            closedSessions
        } else {
            closedSessions.filter { session ->
                val dateStr = dateFormat.format(Date(session.endTime ?: session.startTime))
                dateStr.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title: Control Mensual
        item {
            Text(
                text = "Control Mensual (Últimos 30 días)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main KPI Metrics Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Ingresos Brutos", fontSize = 12.sp, color = Slate700)
                            Text(
                                text = currencyFormat.format(totalIngresosBrutos),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Indigo50
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${last30Sessions.size} Cierres",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo600
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Slate100,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Efectivo", fontSize = 11.sp, color = Slate700)
                            Text(
                                text = currencyFormat.format(totalEfectivo),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Column {
                            Text("Transferencia", fontSize = 11.sp, color = Slate700)
                            Text(
                                text = currencyFormat.format(totalTransferencia),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Column {
                            Text("Promedio Diario", fontSize = 11.sp, color = Slate700)
                            Text(
                                text = currencyFormat.format(promedioDiario),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600
                            )
                        }

                        Column {
                            Text("Comisiones", fontSize = 11.sp, color = Slate700)
                            Text(
                                text = currencyFormat.format(totalComisiones),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber600
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Top Productos
        item {
            Text(
                text = "Top Productos Más Vendidos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val maxSales = topProducts.maxOfOrNull { it.salesCount }?.coerceAtLeast(1) ?: 1

                    topProducts.take(5).forEachIndexed { index, product ->
                        val progressFraction = product.salesCount.toFloat() / maxSales.toFloat()

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${product.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "${product.salesCount} vendidos",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo600
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Indigo600,
                                trackColor = Slate100
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Historial de Tickets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Tickets de Cierre",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por fecha (DD/MM/AAAA)...", color = Slate400) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate700) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Slate900, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Slate900,
                    unfocusedTextColor = Slate900,
                    focusedBorderColor = Indigo600,
                    unfocusedBorderColor = Slate400,
                    focusedLabelColor = Indigo600,
                    unfocusedLabelColor = Slate700
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_tickets_input")
            )
        }

        if (filteredClosedSessions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron cierres de caja en el historial.",
                            fontSize = 14.sp,
                            color = Slate700
                        )
                    }
                }
            }
        } else {
            items(filteredClosedSessions, key = { it.id }) { session ->
                val dateStr = dateFormat.format(Date(session.endTime ?: session.startTime))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(20.dp))
                        .clickable { onTicketClick(session) }
                        .testTag("ticket_history_item_${session.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Indigo50,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Indigo600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = dateStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Efec: ${currencyFormat.format(session.totalCash)} | Transf: ${currencyFormat.format(session.totalTransfer)}",
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currencyFormat.format(session.totalSales),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver Ticket",
                                tint = Slate700
                            )
                        }
                    }
                }
            }
        }
    }
}

