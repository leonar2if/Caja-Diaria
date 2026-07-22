package com.example.cajadiaria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajadiaria.data.local.entity.DailySessionEntity
import com.example.cajadiaria.data.local.entity.ProductEntity
import com.example.cajadiaria.data.local.entity.SaleWithItems
import com.example.cajadiaria.ui.components.OvalSaleCard
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    activeSession: DailySessionEntity?,
    activeSales: List<SaleWithItems>,
    commissionInput: String,
    onCommissionInputChange: (String) -> Unit,
    onStartDayClick: () -> Unit,
    onAddSaleClick: () -> Unit,
    onEditSaleClick: (SaleWithItems) -> Unit,
    onDeleteSaleClick: (Long) -> Unit,
    onAcabarDiaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply { maximumFractionDigits = 0 } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        if (activeSession == null) {
            // STATE A: DAY CLOSED (Estado Inicial)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Hero Badge
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Indigo50,
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Indigo600,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Apertura de Caja",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Text(
                    text = "Configura la comisión y presiona Iniciar Día para registrar ventas.",
                    fontSize = 14.sp,
                    color = Slate700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Commission Selector Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate200, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Percent,
                                contentDescription = null,
                                tint = Indigo600,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fijar Comisión del Vendedor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = commissionInput,
                            onValueChange = { onCommissionInputChange(it) },
                            label = { Text("Comisión (%)") },
                            placeholder = { Text("6", color = Slate400) },
                            suffix = { Text("%", color = Slate900, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(color = Slate900, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = Indigo600,
                                unfocusedBorderColor = Slate400,
                                focusedLabelColor = Indigo600,
                                unfocusedLabelColor = Slate700
                            ),
                            modifier = Modifier
                                .width(180.dp)
                                .testTag("commission_percent_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Big Centered Button: "Iniciar Día"
                Button(
                    onClick = onStartDayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_day_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Iniciar Día",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // STATE B: DURING THE DAY (Feed de Ventas)
            val totalCash = remember(activeSales) {
                activeSales.filter { it.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true) }
                    .sumOf { it.sale.totalAmount }
            }

            val totalTransfer = remember(activeSales) {
                activeSales.filter { it.sale.paymentMethod.equals("TRANSFERENCIA", ignoreCase = true) }
                    .sumOf { it.sale.totalAmount }
            }

            val totalSalesSum = totalCash + totalTransfer

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp) // Leave space for bottom bar + status
            ) {
                // Geometric Balance Commission Bar
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Slate200)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left side: Commission
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "COMISIÓN ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 0.5.sp
                                )
                                Surface(
                                    color = Indigo50,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Indigo100)
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", activeSession.commissionPercentage)}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Indigo600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Right side: Sales today
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "VENTAS HOY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = currencyFormat.format(totalSalesSum),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Efectivo: ${currencyFormat.format(totalCash)}",
                                fontSize = 11.sp,
                                color = Emerald600,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Transferencia: ${currencyFormat.format(totalTransfer)}",
                                fontSize = 11.sp,
                                color = Indigo600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Feed Section
                if (activeSales.isEmpty()) {
                    // Estado Vacío
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin ventas registradas aún",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Presiona el botón (+) para agregar la primera venta de la jornada.",
                                fontSize = 14.sp,
                                color = Slate700,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Feed de Ventas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(activeSales, key = { it.sale.id }) { saleWithItems ->
                            OvalSaleCard(
                                saleWithItems = saleWithItems,
                                onEditClick = { onEditSaleClick(saleWithItems) },
                                onDeleteClick = { onDeleteSaleClick(saleWithItems.sale.id) }
                            )
                        }
                    }
                }
            }

            // Fixed Bottom Bar area for "Acabar Día" button + SQLite status indicator
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onAcabarDiaClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BlackHeader),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("acabar_dia_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ACABAR DÍA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Indigo600)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOCAL SQLITE ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Floating Action Button (+) in bottom right corner (Indigo-600, rounded-2xl)
            FloatingActionButton(
                onClick = onAddSaleClick,
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 96.dp)
                    .testTag("add_sale_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Venta",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

