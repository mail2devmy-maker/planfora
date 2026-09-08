package com.mail2dev.planfora.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>,
    calendarMode: CalendarMode
) {
    when (calendarMode) {
        CalendarMode.MONTH -> MonthCalendarView(selectedDate, onDateSelected, onDateLongClick, eventDates)
        CalendarMode.WEEK -> WeekStripView(selectedDate, onDateSelected, onDateLongClick, eventDates)
        CalendarMode.DAY -> DayTimelineHeader(selectedDate, onDateSelected, onDateLongClick, eventDates)
    }
}

@Composable
fun MonthCalendarView(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>
) {
    val scope = rememberCoroutineScope()
    val initialDate = remember {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    
    // Total pages: 200 years (100 past, 100 future)
    val totalPages = 2400
    val startPage = 1200
    
    val pagerState = rememberPagerState(initialPage = startPage) { totalPages }
    
    // Sync pager with selectedDate changes (external)
    LaunchedEffect(selectedDate) {
        val selCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val curCal = Calendar.getInstance().apply { 
            timeInMillis = initialDate.timeInMillis
            add(Calendar.MONTH, pagerState.currentPage - startPage)
        }
        
        val monthDiff = (selCal.get(Calendar.YEAR) - curCal.get(Calendar.YEAR)) * 12 + 
                       (selCal.get(Calendar.MONTH) - curCal.get(Calendar.MONTH))
        
        if (monthDiff != 0) {
            pagerState.scrollToPage(pagerState.currentPage + monthDiff)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Month Navigation Header
        val currentViewMonth = remember(pagerState.currentPage) {
            Calendar.getInstance().apply {
                timeInMillis = initialDate.timeInMillis
                add(Calendar.MONTH, pagerState.currentPage - startPage)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = SageGreen)
            }
            
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentViewMonth.time),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = SageGreen)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(260.dp),
            verticalAlignment = Alignment.Top
        ) { page ->
            val monthCalendar = Calendar.getInstance().apply {
                timeInMillis = initialDate.timeInMillis
                add(Calendar.MONTH, page - startPage)
                set(Calendar.DAY_OF_MONTH, 1)
                // Adjust to start of week
                while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    add(Calendar.DAY_OF_YEAR, -1)
                }
            }

            val days = (0..41).map {
                val day = monthCalendar.timeInMillis
                monthCalendar.add(Calendar.DAY_OF_YEAR, 1)
                day
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(days) { timestamp ->
                    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                    val isSelected = isSameDay(timestamp, selectedDate)
                    val isToday = isSameDay(timestamp, System.currentTimeMillis())
                    val isCurrentMonth = Calendar.getInstance().apply { 
                        timeInMillis = initialDate.timeInMillis
                        add(Calendar.MONTH, page - startPage) 
                    }.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                    val hasEvent = eventDates.contains(normalizeToStartOfDay(timestamp))

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SageGreen else Color.Transparent)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onDateSelected(timestamp) },
                                    onLongPress = { onDateLongClick(timestamp) }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .then(
                                        if (isToday) Modifier.border(1.5.dp, if (isSelected) ForestGreen else SageGreen, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                                    color = when {
                                        isSelected -> DarkBackground
                                        isCurrentMonth -> Color.White
                                        else -> Color.DarkGray
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (hasEvent) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) DarkBackground else SageGreen)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekStripView(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>
) {
    val dates = remember {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        (0..28).map { offset ->
            Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, offset)
            }.timeInMillis
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dates) { timestamp ->
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val isSelected = isSameDay(timestamp, selectedDate)
            val isToday = isSameDay(timestamp, System.currentTimeMillis())
            val hasEvent = eventDates.contains(normalizeToStartOfDay(timestamp))
            
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) SageGreen else Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onDateSelected(timestamp) },
                            onLongPress = { onDateLongClick(timestamp) }
                        )
                    }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time).uppercase(),
                    color = if (isSelected) DarkBackground else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .then(
                            if (isToday) Modifier.border(1.5.dp, if (isSelected) ForestGreen else SageGreen, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                        color = if (isSelected) DarkBackground else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                if (hasEvent) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) DarkBackground else SageGreen)
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DayTimelineHeader(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>
) {
    WeekStripView(selectedDate, onDateSelected, onDateLongClick, eventDates)
}

@Composable
fun CalendarDateStrip(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>,
    calendarMode: CalendarMode
) {
    CalendarView(selectedDate, onDateSelected, onDateLongClick, eventDates, calendarMode)
}

@Composable
fun DayTimelineView(
    logs: List<com.mail2dev.planfora.data.local.entity.JournalLogEntity>,
    use24Hour: Boolean,
    onLogClick: (com.mail2dev.planfora.data.local.entity.JournalLogEntity) -> Unit,
    onHourLongClick: (Int) -> Unit
) {
    val hours = (0..23).toList()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    LaunchedEffect(Unit) {
        listState.scrollToItem(currentHour)
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(hours) { hour ->
            val logsInHour = logs.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.HOUR_OF_DAY) == hour
            }
            
            Column {
                if (hour == currentHour) {
                    // Current Time Indicator line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 68.dp)
                            .height(2.dp)
                            .background(ForestGreen)
                    )
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onHourLongClick(hour) }
                        )
                    }
                ) {
                    val timeLabel = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, 0)
                    }.timeInMillis
                    
                    Text(
                        text = com.mail2dev.planfora.util.TimeFormatter.formatTime(timeLabel, use24Hour),
                        color = if (hour == currentHour) ForestGreen else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (hour == currentHour) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(60.dp).padding(top = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (logsInHour.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.DarkGray.copy(alpha = 0.2f))
                                    .padding(top = 40.dp)
                            )
                        } else {
                            logsInHour.forEach { log ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().clickable { onLogClick(log) }
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(4.dp, 24.dp).background(SageGreen, RoundedCornerShape(2.dp)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(log.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun normalizeToStartOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
