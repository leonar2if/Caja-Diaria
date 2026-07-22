package com.example.cajadiaria.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cajadiaria.data.local.AppDatabase
import com.example.cajadiaria.data.local.entity.*
import com.example.cajadiaria.data.repository.CajaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Statistics : Screen()
}

class CajaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CajaRepository

    init {
        val dao = AppDatabase.getInstance(application).cajaDao()
        repository = CajaRepository(dao)
        viewModelScope.launch {
            repository.seedInitialProductsIfEmpty()
        }
    }

    val products: StateFlow<List<ProductEntity>> = repository.productsByRanking
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<DailySessionEntity?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSales: StateFlow<List<SaleWithItems>> = activeSession
        .flatMapLatest { session ->
            if (session != null) {
                repository.getSalesForSession(session.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedSessions: StateFlow<List<DailySessionEntity>> = repository.closedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _commissionInput = MutableStateFlow("6")
    val commissionInput: StateFlow<String> = _commissionInput.asStateFlow()

    private val _isAddProductDialogOpen = MutableStateFlow(false)
    val isAddProductDialogOpen: StateFlow<Boolean> = _isAddProductDialogOpen.asStateFlow()

    private val _isRegisterSaleDialogOpen = MutableStateFlow(false)
    val isRegisterSaleDialogOpen: StateFlow<Boolean> = _isRegisterSaleDialogOpen.asStateFlow()

    private val _editingSale = MutableStateFlow<SaleWithItems?>(null)
    val editingSale: StateFlow<SaleWithItems?> = _editingSale.asStateFlow()

    private val _receiptToShow = MutableStateFlow<DailySessionEntity?>(null)
    val receiptToShow: StateFlow<DailySessionEntity?> = _receiptToShow.asStateFlow()

    private val _isReceiptActiveClosing = MutableStateFlow(false)
    val isReceiptActiveClosing: StateFlow<Boolean> = _isReceiptActiveClosing.asStateFlow()

    fun updateCommissionInput(valStr: String) {
        _commissionInput.value = valStr
    }

    fun startDay() {
        val comm = _commissionInput.value.toDoubleOrNull() ?: 6.0
        viewModelScope.launch {
            repository.startNewDay(comm)
        }
    }

    fun openAddProductDialog() {
        _isAddProductDialogOpen.value = true
    }

    fun closeAddProductDialog() {
        _isAddProductDialogOpen.value = false
    }

    fun addNewProduct(name: String, price: Double) {
        viewModelScope.launch {
            repository.addNewProduct(name, price)
            closeAddProductDialog()
        }
    }

    fun openRegisterSaleDialog(editing: SaleWithItems? = null) {
        _editingSale.value = editing
        _isRegisterSaleDialogOpen.value = true
    }

    fun closeRegisterSaleDialog() {
        _isRegisterSaleDialogOpen.value = false
        _editingSale.value = null
    }

    fun saveSale(
        paymentMethod: String,
        items: List<Pair<ProductEntity, Int>>
    ) {
        val currentSession = activeSession.value ?: return
        val editing = _editingSale.value
        viewModelScope.launch {
            if (editing != null) {
                repository.editSale(editing.sale.id, paymentMethod, items)
            } else {
                repository.registerSale(currentSession.id, paymentMethod, items)
            }
            closeRegisterSaleDialog()
        }
    }

    fun deleteSale(saleId: Long) {
        viewModelScope.launch {
            repository.deleteSale(saleId)
        }
    }

    fun prepareAcabarDia() {
        val current = activeSession.value ?: return
        _receiptToShow.value = current
        _isReceiptActiveClosing.value = true
    }

    fun confirmCloseDay() {
        val current = activeSession.value ?: return
        viewModelScope.launch {
            repository.closeSession(current.id)
            _receiptToShow.value = null
            _isReceiptActiveClosing.value = false
        }
    }

    fun showPastTicket(session: DailySessionEntity) {
        _receiptToShow.value = session
        _isReceiptActiveClosing.value = false
    }

    fun closeReceiptDialog() {
        _receiptToShow.value = null
        _isReceiptActiveClosing.value = false
    }

    fun navigateToStatistics() {
        _currentScreen.value = Screen.Statistics
    }

    fun navigateToHome() {
        _currentScreen.value = Screen.Home
    }
}
