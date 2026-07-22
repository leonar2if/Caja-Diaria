package com.example.cajadiaria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.cajadiaria.data.local.entity.ProductEntity
import com.example.cajadiaria.data.local.entity.SaleWithItems
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RegisterSaleDialog(
    productsByRanking: List<ProductEntity>,
    editingSale: SaleWithItems? = null,
    onDismiss: () -> Unit,
    onCreateNewProductRequested: (suggestedName: String) -> Unit,
    onConfirmSale: (paymentMethod: String, items: List<Pair<ProductEntity, Int>>) -> Unit
) {
    // Payment method state
    var selectedPaymentMethod by remember {
        mutableStateOf(editingSale?.sale?.paymentMethod ?: "EFECTIVO")
    }

    // Search query
    var searchQuery by remember { mutableStateOf("") }

    // Selected cart items: Map<ProductId, Pair<ProductEntity, Quantity>>
    val cartState = remember {
        mutableStateMapOf<Long, Pair<ProductEntity, Int>>().apply {
            editingSale?.items?.forEach { item ->
                val prod = ProductEntity(
                    id = item.productId,
                    name = item.productName,
                    price = item.unitPrice,
                    salesCount = 0
                )
                put(item.productId, Pair(prod, item.quantity))
            }
        }
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply { maximumFractionDigits = 0 } }

    // Filter products keeping ranking
    val filteredProducts = remember(searchQuery, productsByRanking) {
        if (searchQuery.isBlank()) {
            productsByRanking
        } else {
            productsByRanking.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val totalSale = cartState.values.sumOf { it.first.price * it.second }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingSale != null) "Editar Venta" else "Registrar Venta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_register_sale_dialog")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Slate700)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                // 1. Selector de Pago Discreto (Switch / Segmented)
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = { selectedPaymentMethod = "EFECTIVO" },
                            color = if (selectedPaymentMethod == "EFECTIVO") Emerald600 else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("payment_method_cash")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Efectivo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedPaymentMethod == "EFECTIVO") Color.White else Slate700
                                )
                            }
                        }

                        Surface(
                            onClick = { selectedPaymentMethod = "TRANSFERENCIA" },
                            color = if (selectedPaymentMethod == "TRANSFERENCIA") Indigo600 else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("payment_method_transfer")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Transferencia",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedPaymentMethod == "TRANSFERENCIA") Color.White else Slate700
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Buscador con Ranking
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar producto...", color = Slate400) },
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
                        .testTag("sale_product_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Slate50, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // IF SEARCH IS BLANK: First fixed option is "➕ Crear nuevo producto"
                    if (searchQuery.isBlank()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCreateNewProductRequested("") }
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                    .testTag("create_product_option_top")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "➕ Crear nuevo producto",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo600
                                )
                            }
                            HorizontalDivider(color = Slate200)
                        }
                    }

                    // Catalog items ordered by ranking
                    items(filteredProducts, key = { it.id }) { product ->
                        val currentQty = cartState[product.id]?.second ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val newQty = currentQty + 1
                                    cartState[product.id] = Pair(product, newQty)
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = currencyFormat.format(product.price),
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }

                            if (currentQty > 0) {
                                Surface(
                                    color = Indigo600,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${currentQty}x",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar",
                                    tint = Slate700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // IF SEARCH IS NOT BLANK: The "➕ Crear 'query' como nuevo producto" is moved to the END of suggestions
                    if (searchQuery.isNotBlank()) {
                        item {
                            HorizontalDivider(color = Slate200)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCreateNewProductRequested(searchQuery.trim()) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                    .testTag("create_product_option_bottom")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "➕ Crear \"$searchQuery\" como nuevo producto",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo600
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cart Breakdown Section
                if (cartState.isNotEmpty()) {
                    Text(
                        text = "Productos en la Venta:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            cartState.values.forEach { (product, qty) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate900,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (qty > 1) {
                                                    cartState[product.id] = Pair(product, qty - 1)
                                                } else {
                                                    cartState.remove(product.id)
                                                }
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = Slate700, modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = "$qty",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        IconButton(
                                            onClick = { cartState[product.id] = Pair(product, qty + 1) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Más", tint = Slate700, modifier = Modifier.size(16.dp))
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = currencyFormat.format(product.price * qty),
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cartState.isEmpty()) return@Button
                    val itemsList = cartState.values.toList()
                    onConfirmSale(selectedPaymentMethod, itemsList)
                },
                enabled = cartState.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_sale_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Confirmar ${currencyFormat.format(totalSale)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Slate700)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

