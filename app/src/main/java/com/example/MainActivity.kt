package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cajadiaria.ui.components.AddProductDialog
import com.example.cajadiaria.ui.components.ReceiptTicketDialog
import com.example.cajadiaria.ui.components.RegisterSaleDialog
import com.example.cajadiaria.ui.components.TopAppBarBlack
import com.example.cajadiaria.ui.screens.HomeScreen
import com.example.cajadiaria.ui.screens.StatisticsScreen
import com.example.cajadiaria.ui.viewmodel.CajaViewModel
import com.example.cajadiaria.ui.viewmodel.Screen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CajaViewModel = viewModel()

                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
                val activeSales by viewModel.activeSales.collectAsStateWithLifecycle()
                val products by viewModel.products.collectAsStateWithLifecycle()
                val closedSessions by viewModel.closedSessions.collectAsStateWithLifecycle()
                val commissionInput by viewModel.commissionInput.collectAsStateWithLifecycle()

                val isAddProductDialogOpen by viewModel.isAddProductDialogOpen.collectAsStateWithLifecycle()
                val isRegisterSaleDialogOpen by viewModel.isRegisterSaleDialogOpen.collectAsStateWithLifecycle()
                val editingSale by viewModel.editingSale.collectAsStateWithLifecycle()

                val receiptToShow by viewModel.receiptToShow.collectAsStateWithLifecycle()
                val isReceiptActiveClosing by viewModel.isReceiptActiveClosing.collectAsStateWithLifecycle()

                var newProductSuggestedName by remember { mutableStateOf("") }

                Scaffold(
                    topBar = {
                        TopAppBarBlack(
                            title = if (currentScreen is Screen.Statistics) "Estadísticas" else "CajaDiaria",
                            canNavigateBack = currentScreen is Screen.Statistics,
                            onNavigateBack = { viewModel.navigateToHome() },
                            onAddProductClick = { viewModel.openAddProductDialog() },
                            onViewStatisticsClick = { viewModel.navigateToStatistics() }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                activeSession = activeSession,
                                activeSales = activeSales,
                                commissionInput = commissionInput,
                                onCommissionInputChange = { viewModel.updateCommissionInput(it) },
                                onStartDayClick = { viewModel.startDay() },
                                onAddSaleClick = { viewModel.openRegisterSaleDialog(null) },
                                onEditSaleClick = { sale -> viewModel.openRegisterSaleDialog(sale) },
                                onDeleteSaleClick = { saleId -> viewModel.deleteSale(saleId) },
                                onAcabarDiaClick = { viewModel.prepareAcabarDia() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is Screen.Statistics -> {
                            StatisticsScreen(
                                closedSessions = closedSessions,
                                topProducts = products,
                                onTicketClick = { session -> viewModel.showPastTicket(session) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }

                    // --- DIALOGS ---

                    // 1. Add Product Dialog
                    if (isAddProductDialogOpen) {
                        AddProductDialog(
                            initialName = newProductSuggestedName,
                            onDismiss = {
                                newProductSuggestedName = ""
                                viewModel.closeAddProductDialog()
                            },
                            onConfirm = { name, price ->
                                newProductSuggestedName = ""
                                viewModel.addNewProduct(name, price)
                            }
                        )
                    }

                    // 2. Register / Edit Sale Dialog
                    if (isRegisterSaleDialogOpen) {
                        RegisterSaleDialog(
                            productsByRanking = products,
                            editingSale = editingSale,
                            onDismiss = { viewModel.closeRegisterSaleDialog() },
                            onCreateNewProductRequested = { suggestedName ->
                                newProductSuggestedName = suggestedName
                                viewModel.openAddProductDialog()
                            },
                            onConfirmSale = { paymentMethod, items ->
                                viewModel.saveSale(paymentMethod, items)
                            }
                        )
                    }

                    // 3. Receipt Ticket Dialog
                    receiptToShow?.let { session ->
                        ReceiptTicketDialog(
                            session = session,
                            liveSales = activeSales,
                            isClosingActiveDay = isReceiptActiveClosing,
                            onDismiss = { viewModel.closeReceiptDialog() },
                            onConfirmCloseDay = { viewModel.confirmCloseDay() }
                        )
                    }
                }
            }
        }
    }
}
