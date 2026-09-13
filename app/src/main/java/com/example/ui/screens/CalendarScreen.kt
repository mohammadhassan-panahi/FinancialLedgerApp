package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DaraGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalendarEvent
import com.example.ui.viewmodel.CalendarEventType
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val monthEvents by viewModel.monthEvents.collectAsStateWithLifecycle()
    val dailyEvents by viewModel.selectedDayEvents.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ObsidianSlate900,
        topBar = {
            TopAppBar(
                title = { Text("تقویم مالی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month Selector Header
            CalendarHeader(
                month = month,
                year = year,
                onNext = { viewModel.nextMonth() },
                onPrev = { viewModel.prevMonth() }
            )

            // Weekday Names
            WeekdayHeader()

            // Calendar Grid
            JalaliMonthGrid(
                year = year,
                month = month,
                selectedDay = selectedDay,
                events = monthEvents,
                onDayClick = { viewModel.selectDay(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Events List
            Text(
                "رویدادهای ${selectedDay?.let { PersianNumberUtils.toPersianDigits(it.toString()) } ?: ""} ${PersianDateUtils.JalaliDate(year, month, 1).monthName}",
                style = DaraTypography.titleMedium,
                color = Slate50,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (dailyEvents.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("هیچ فعالیت مالی در این روز ثبت نشده است.", color = Slate600, style = DaraTypography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dailyEvents) { event ->
                        CalendarEventItem(event)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(month: Int, year: Int, onNext: () -> Unit, onPrev: () -> Unit) {
    val monthName = PersianDateUtils.JalaliDate(year, month, 1).monthName
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Slate400)
        }
        Text(
            text = "$monthName ${PersianNumberUtils.toPersianDigits(year.toString())}",
            style = DaraTypography.titleLarge,
            color = Slate50,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Slate400)
        }
    }
}

@Composable
fun WeekdayHeader() {
    val days = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = DaraTypography.labelSmall,
                color = Slate400
            )
        }
    }
}

@Composable
fun JalaliMonthGrid(
    year: Int,
    month: Int,
    selectedDay: Int?,
    events: List<CalendarEvent>,
    onDayClick: (Int) -> Unit
) {
    val firstDayOfWeek = PersianDateUtils.getDayOfWeek(year, month, 1)
    val daysInMonth = PersianDateUtils.daysInJalaliMonth(year, month)
    
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(8.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val day = index - firstDayOfWeek + 1
                    
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (day in 1..daysInMonth) {
                            val isSelected = day == selectedDay
                            val dayEvents = events.filter { 
                                val j = PersianDateUtils.toJalali(java.util.Date(it.timestamp))
                                j.dayOfMonth == day 
                            }
                            
                            DayCell(
                                day = day,
                                isSelected = isSelected,
                                hasEvents = dayEvents.isNotEmpty(),
                                eventColors = dayEvents.map { it.getIndicatorColor() }.distinct(),
                                onClick = { onDayClick(day) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    hasEvents: Boolean,
    eventColors: List<Color>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) IndigoElectric else Color.Transparent)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = PersianNumberUtils.toPersianDigits(day.toString()),
            color = if (isSelected) Color.White else Slate50,
            style = DaraTypography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (hasEvents && !isSelected) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                eventColors.forEach { color ->
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
                }
            }
        }
    }
}

@Composable
fun CalendarEventItem(event: CalendarEvent) {
    DaraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(event.getIndicatorColor().copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.getIcon(),
                    contentDescription = null,
                    tint = event.getIndicatorColor(),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = DaraTypography.bodyLarge, color = Slate50, fontWeight = FontWeight.Bold)
                Text(event.category, style = DaraTypography.labelSmall, color = Slate400)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PersianNumberUtils.formatCurrency(event.amount, isRial = false),
                    style = DaraTypography.bodyLarge,
                    color = if (event.type == CalendarEventType.INCOME || event.type == CalendarEventType.SALE) EmeraldCore else RoseCoral,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = PersianDateUtils.formatTime(java.util.Date(event.timestamp)),
                    style = DaraTypography.labelSmall,
                    color = Slate600
                )
            }
        }
    }
}

private fun CalendarEvent.getIndicatorColor(): Color = when (type) {
    CalendarEventType.INCOME, CalendarEventType.SALE -> EmeraldCore
    CalendarEventType.EXPENSE, CalendarEventType.PURCHASE -> RoseCoral
    CalendarEventType.REMINDER -> RefinedAmberGold
}

private fun CalendarEvent.getIcon(): androidx.compose.ui.graphics.vector.ImageVector = when (type) {
    CalendarEventType.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
    CalendarEventType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
    CalendarEventType.PURCHASE -> Icons.Default.AddShoppingCart
    CalendarEventType.SALE -> Icons.Default.MonetizationOn
    CalendarEventType.REMINDER -> Icons.Default.NotificationsActive
}
