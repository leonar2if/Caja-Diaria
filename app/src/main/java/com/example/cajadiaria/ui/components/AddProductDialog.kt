package com.example.cajadiaria.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AddProductDialog(
    initialName: String = "",
    initialPrice: Double? = null,
    initialCurrency: String = "MXN",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, currency: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var priceInput by remember {
        mutableStateOf(initialPrice?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "")
    }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Producto" else "Agregar Producto al Catálogo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        errorMessage = null
                    },
                    label = { Text("Nombre del Producto") },
                    placeholder = { Text("Ej: Espuma de afeitar", color = Slate400) },
                    singleLine = true,
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
                        .testTag("add_product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Selector de Moneda: MXN / USD
                Text(
                    text = "Moneda del Producto",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
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
                            onClick = { selectedCurrency = "MXN" },
                            color = if (selectedCurrency == "MXN") Indigo600 else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_currency_mxn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "Pesos ($)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCurrency == "MXN") Color.White else Slate700
                                )
                            }
                        }
                        Surface(
                            onClick = { selectedCurrency = "USD" },
                            color = if (selectedCurrency == "USD") Amber600 else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_currency_usd")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "Dólares (US$)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCurrency == "USD") Color.White else Slate700
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        errorMessage = null
                    },
                    label = { Text(if (selectedCurrency == "USD") "Precio (US$)" else "Precio ($)") },
                    placeholder = { Text(if (selectedCurrency == "USD") "Ej: 10" else "Ej: 200", color = Slate400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
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
                        .testTag("add_product_price_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedCurrency == "USD") {
                    Text(
                        text = "Al venderlo podrás elegir cobrarlo en dólares o al cambio del día.",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = nameInput.trim()
                    val priceVal = priceInput.toDoubleOrNull()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Ingresa un nombre para el producto"
                        return@Button
                    }
                    if (priceVal == null || priceVal <= 0) {
                        errorMessage = "Ingresa un precio válido mayor a 0"
                        return@Button
                    }
                    onConfirm(trimmedName, priceVal, selectedCurrency)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_product_confirm_button")
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Guardar Producto", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("add_product_cancel_button")
            ) {
                Text("Cancelar", color = Slate700)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
