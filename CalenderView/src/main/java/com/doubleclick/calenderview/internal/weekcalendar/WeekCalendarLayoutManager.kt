package com.doubleclick.calenderview.internal.weekcalendar

import com.doubleclick.calenderview.MarginValues
import com.doubleclick.calenderview.WeekCalendarView
import com.doubleclick.calenderview.internal.CalendarLayoutManager
import com.doubleclick.calenderview.internal.dayTag
import java.time.LocalDate

internal class WeekCalendarLayoutManager(private val calView: WeekCalendarView) :
    CalendarLayoutManager<LocalDate, LocalDate>(calView, HORIZONTAL) {

    private val adapter: WeekCalendarAdapter
        get() = calView.adapter as WeekCalendarAdapter

    override fun getaItemAdapterPosition(data: LocalDate): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: LocalDate): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: LocalDate): Int = dayTag(data)
    override fun getItemMargins(): MarginValues = calView.weekMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyWeekScrollListenerIfNeeded()
}
