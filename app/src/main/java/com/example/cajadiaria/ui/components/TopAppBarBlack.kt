package com.example.cajadiaria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.ui.theme.BlackHeader
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarBlack(
    title: String = "CajaDiaria",
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onProductsClick: () -> Unit = {},
    onViewStatisticsClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!canNavigateBack) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Indigo500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("top_app_bar_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menú de opciones",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Slate800)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddBox,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Productos",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onProductsClick()
                        },
                        modifier = Modifier.testTag("menu_item_add_product")
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Ver Estadísticas (Últimos 30 días)",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onViewStatisticsClick()
                        },
                        modifier = Modifier.testTag("menu_item_statistics")
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BlackHeader,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

