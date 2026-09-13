package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.data.repository.PortfolioRepository
import com.example.util.PersianDateUtils
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.util.*

enum class CalendarEventType { INCOME, EXPENSE, PURCHASE, SALE, REMINDER }

data class CalendarEvent(
    val id: String,
    val title: String,
    val amount: BigDecimal,
    val type: CalendarEventType,
    val timestamp: Long,
    val category: String
)

class CalendarViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(PersianDateUtils.toJalali(Date()).month)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(PersianDateUtils.toJalali(Date()).year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int?>(PersianDateUtils.toJalali(Date()).dayOfMonth)
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()

    val allEvents: StateFlow<List<CalendarEvent>> = combine(
        financialRepository.allTransactions,
        portfolioRepository.purchases,
        portfolioRepository.sales,
        portfolioRepository.reminders
    ) { txs, purchases, sales, reminders ->
        val events = mutableListOf<CalendarEvent>()
        
        txs.forEach {
            events.add(CalendarEvent(
                id = "tx_${it.id}",
                title = it.title,
                amount = it.amount,
                type = if (it.type == com.example.data.local.TransactionType.DEPOSIT) CalendarEventType.INCOME else CalendarEventType.EXPENSE,
                timestamp = it.timestamp,
                category = it.category
            ))
        }

        purchases.forEach {
            events.add(CalendarEvent(
                id = "pur_${it.id}",
                title = "خرید ${it.assetName}",
                amount = it.totalPaidRial.divide(BigDecimal("10")), // Show in Toman for consistency in calendar
                type = CalendarEventType.PURCHASE,
                timestamp = it.purchaseDate,
                category = it.assetType.name
            ))
        }

        sales.forEach {
            events.add(CalendarEvent(
                id = "sale_${it.id}",
                title = "فروش ${it.assetName}",
                amount = it.totalReceivedRial.divide(BigDecimal("10")),
                type = CalendarEventType.SALE,
                timestamp = it.saleDate,
                category = it.assetType.name
            ))
        }

        reminders.forEach {
            events.add(CalendarEvent(
                id = "rem_${it.id}",
                title = it.title,
                amount = it.amountRial.divide(BigDecimal("10")),
                type = CalendarEventType.REMINDER,
                timestamp = it.dueDate,
                category = it.type.name
            ))
        }

        events.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthEvents = combine(allEvents, _selectedMonth, _selectedYear) { events, month, year ->
        events.filter { 
            val j = PersianDateUtils.toJalali(Date(it.timestamp))
            j.month == month && j.year == year
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDayEvents = combine(monthEvents, _selectedDay) { events, day ->
        if (day == null) emptyList()
        else events.filter { PersianDateUtils.toJalali(Date(it.timestamp)).dayOfMonth == day }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(month: Int) { _selectedMonth.value = month }
    fun selectYear(year: Int) { _selectedYear.value = year }
    fun selectDay(day: Int?) { _selectedDay.value = day }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value++
        } else {
            _selectedMonth.value++
        }
        _selectedDay.value = null
    }

    fun prevMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value--
        } else {
            _selectedMonth.value--
        }
        _selectedDay.value = null
    }
}

class CalendarViewModelFactory(
    private val portfolioRepository: PortfolioRepository,
    private val financialRepository: FinancialRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarViewModel(portfolioRepository, financialRepository) as T
    }
}
