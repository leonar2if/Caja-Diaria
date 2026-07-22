package com.example.cajadiaria.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajadiaria.data.local.entity.SaleWithItems
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OvalSaleCard(
    saleWithItems: SaleWithItems,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val formattedTime = remember(saleWithItems.sale.timestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(saleWithItems.sale.timestamp))
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply { maximumFractionDigits = 0 } }
    val totalFormatted = currencyFormat.format(saleWithItems.sale.totalAmount)

    val isCash = saleWithItems.sale.paymentMethod.equals("EFECTIVO", ignoreCase = true)
    val badgeBg = if (isCash) Emerald50 else Indigo50
    val badgeTextColor = if (isCash) Emerald600 else Indigo600

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Slate200, RoundedCornerShape(24.dp))
            .animateContentSize()
            .testTag("sale_card_${saleWithItems.sale.id}"),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Collapsed Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Side: [ Time ] + [ Payment Badge ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = formattedTime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = if (isCash) "EFECTIVO" else "TRANSFERENCIA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Right Side (Strict Order: 1. Total Amount, 2. Expand Arrow, 3. 3 Horizontal Dots)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Total Amount
                    Text(
                        text = totalFormatted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.testTag("sale_card_total_${saleWithItems.sale.id}")
                    )

                    // 2. Expand Arrow
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sale_card_expand_${saleWithItems.sale.id}")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Colapsar detalle" else "Desplegar detalle",
                            tint = Slate500
                        )
                    }

                    // 3. 3 Horizontal Dots (⋯)
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("sale_card_menu_${saleWithItems.sale.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Opciones de venta",
                                tint = Slate500
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Slate900,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Editar", color = Slate900, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                                modifier = Modifier.testTag("sale_card_edit_option_${saleWithItems.sale.id}")
                            )

                            HorizontalDivider(color = Slate100)

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Red600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Eliminar", color = Red600, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                                modifier = Modifier.testTag("sale_card_delete_option_${saleWithItems.sale.id}")
                            )
                        }
                    }
                }
            }

            // Expanded Product Item Details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Surface(
                        color = Slate50,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            saleWithItems.items.forEach { item ->
                                val unitPriceFormatted = currencyFormat.format(item.unitPrice)
                                val subtotalFormatted = currencyFormat.format(item.subtotal)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${item.productName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate700
                                    )
                                    Text(
                                        text = subtotalFormatted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

