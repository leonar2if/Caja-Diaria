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
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var priceInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Producto al Catálogo",
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

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        errorMessage = null
                    },
                    label = { Text("Precio ($)") },
                    placeholder = { Text("Ej: 200", color = Slate400) },
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
                    onConfirm(trimmedName, priceVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_product_confirm_button")
            ) {
                Text("Guardar Producto", color = Color.White, fontWeight = FontWeight.Bold)
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

