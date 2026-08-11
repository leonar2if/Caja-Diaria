package com.example.cajadiaria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajadiaria.data.local.entity.ProductEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductsScreen(
    products: List<ProductEntity>,
    onAddProductClick: () -> Unit,
    onEditProductClick: (ProductEntity) -> Unit,
    onDeleteProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val mxnFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply { maximumFractionDigits = 0 } }
    val usdFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    var productPendingDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        if (products.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aún no tienes productos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Presiona el botón (+) para agregar tu primer producto al catálogo.",
                    fontSize = 14.sp,
                    color = Slate700,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    val isUsd = product.currency.equals("USD", ignoreCase = true)
                    val priceFormatted = if (isUsd) usdFormat.format(product.price) else mxnFormat.format(product.price)

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate200, RoundedCornerShape(18.dp))
                            .testTag("product_row_${product.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = priceFormatted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate700
                                    )
                                    if (isUsd) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Amber600.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "USD",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Amber600,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onEditProductClick(product) },
                                    modifier = Modifier.testTag("edit_product_${product.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = Indigo600)
                                }
                                IconButton(
                                    onClick = { productPendingDelete = product },
                                    modifier = Modifier.testTag("delete_product_${product.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Red600)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        FloatingActionButton(
            onClick = onAddProductClick,
            containerColor = Indigo600,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("add_product_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Producto", modifier = Modifier.size(28.dp))
        }
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text("Eliminar producto", fontWeight = FontWeight.Bold, color = Slate900) },
            text = { Text("¿Seguro que quieres eliminar \"${product.name}\" del catálogo?", color = Slate700) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProductClick(product)
                        productPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    modifier = Modifier.testTag("confirm_delete_product_button")
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productPendingDelete = null }) {
                    Text("Cancelar", color = Slate700)
                }
            },
            containerColor = Color.White
        )
    }
}
