package com.doubleclick.calenderview.internal.monthcalendar

import com.doubleclick.calenderview.core.CalendarDay
import com.doubleclick.calenderview.CalendarView
import com.doubleclick.calenderview.MarginValues
import com.doubleclick.calenderview.internal.CalendarLayoutManager
import com.doubleclick.calenderview.internal.dayTag
import java.time.YearMonth

internal class MonthCalendarLayoutManager(private val calView: CalendarView) :
    CalendarLayoutManager<YearMonth, CalendarDay>(calView, calView.orientation) {

    private val adapter: MonthCalendarAdapter
        get() = calView.adapter as MonthCalendarAdapter

    override fun getaItemAdapterPosition(data: YearMonth): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: CalendarDay): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: CalendarDay): Int = dayTag(data.date)
    override fun getItemMargins(): MarginValues = calView.monthMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyMonthScrollListenerIfNeeded()
}
