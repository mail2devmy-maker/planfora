package com.mail2dev.planfora.ui.logs

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>,
    calendarMode: CalendarMode,
    onModeChange: (CalendarMode) -> Unit = {}
) {
    AnimatedContent(
        targetState = calendarMode,
        transitionSpec = {
            if (targetState == CalendarMode.MONTH || initialState == CalendarMode.MONTH) {
                expandVertically(expandFrom = Alignment.Top) + fadeIn() togetherWith
                        shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        },
        label = "CalendarModeTransition"
    ) { mode ->
        when (mode) {
            CalendarMode.MONTH -> MonthCalendarView(selectedDate, onDateSelected, onDateLongClick, eventDates, onCollapse = { onModeChange(CalendarMode.WEEK) })
            CalendarMode.WEEK -> WeekStripView(selectedDate, onDateSelected, onDateLongClick, eventDates, onExpand = { onModeChange(CalendarMode.MONTH) })
            CalendarMode.DAY -> DayTimelineHeader(selectedDate, onDateSelected, onDateLongClick, eventDates)
        }
    }

    if (calendarMode != CalendarMode.DAY) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clickable {
                    onModeChange(if (calendarMode == CalendarMode.MONTH) CalendarMode.WEEK else CalendarMode.MONTH)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
fun MonthCalendarView(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    eventDates: Set<Long>,
    onCollapse: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val initialDate = remember {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    
    val totalPages = 2400
    val startPage = 1200
    
    val pagerState = rememberPagerState(initialPage = startPage) { totalPages }
    
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = MaterialTheme.colorScheme.primary)
                }
                
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentViewMonth.time),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = MaterialTheme.colorScheme.primary)
                }
            }

            IconButton(onClick = onCollapse) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Collapse", tint = Color.Gray)
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
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
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
                                        if (isToday) Modifier.border(1.5.dp, if (isSelected) com.mail2dev.planfora.ui.theme.ForestGreen else MaterialTheme.colorScheme.primary, CircleShape)
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
                                        .background(if (isSelected) DarkBackground else MaterialTheme.colorScheme.primary)
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
    eventDates: Set<Long>,
    onExpand: () -> Unit = {}
) {
    val initialDate = remember {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            // Start at the Sunday of the current week
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
        }
    }

    val totalPages = 10000
    val startPage = 5000
    val pagerState = rememberPagerState(initialPage = startPage) { totalPages }

    LaunchedEffect(selectedDate) {
        val selCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val curCal = Calendar.getInstance().apply {
            timeInMillis = initialDate.timeInMillis
            add(Calendar.WEEK_OF_YEAR, pagerState.currentPage - startPage)
        }
        
        // Calculate weeks difference
        val diffMillis = selCal.timeInMillis - curCal.timeInMillis
        val weekDiff = Math.floor(diffMillis.toDouble() / (7 * 24 * 60 * 60 * 1000)).toInt()
        
        if (weekDiff != 0) {
            pagerState.scrollToPage(pagerState.currentPage + weekDiff)
        }
    }

    val currentWeekMonth = remember(pagerState.currentPage) {
        Calendar.getInstance().apply {
            timeInMillis = initialDate.timeInMillis
            add(Calendar.WEEK_OF_YEAR, pagerState.currentPage - startPage)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentWeekMonth.time),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onExpand, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand", tint = Color.Gray)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp
        ) { page ->
            val weekCalendar = Calendar.getInstance().apply {
                timeInMillis = initialDate.timeInMillis
                add(Calendar.WEEK_OF_YEAR, page - startPage)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..6).forEach { _ ->
                    val timestamp = weekCalendar.timeInMillis
                    DayStripItem(
                        timestamp = timestamp,
                        selectedDate = selectedDate,
                        eventDates = eventDates,
                        onDateSelected = onDateSelected,
                        onDateLongClick = onDateLongClick,
                        modifier = Modifier.weight(1f)
                    )
                    weekCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
    }
}

@Composable
fun DayStripItem(
    timestamp: Long,
    selectedDate: Long,
    eventDates: Set<Long>,
    onDateSelected: (Long) -> Unit,
    onDateLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val isSelected = isSameDay(timestamp, selectedDate)
    val isToday = isSameDay(timestamp, System.currentTimeMillis())
    val hasEvent = eventDates.contains(normalizeToStartOfDay(timestamp))
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
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
                    if (isToday) Modifier.border(1.5.dp, if (isSelected) com.mail2dev.planfora.ui.theme.ForestGreen else MaterialTheme.colorScheme.primary, CircleShape)
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
                    .background(if (isSelected) DarkBackground else MaterialTheme.colorScheme.primary)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
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
    calendarMode: CalendarMode,
    onModeChange: (CalendarMode) -> Unit = {}
) {
    CalendarView(selectedDate, onDateSelected, onDateLongClick, eventDates, calendarMode, onModeChange)
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
                                        Box(modifier = Modifier.size(4.dp, 24.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
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
