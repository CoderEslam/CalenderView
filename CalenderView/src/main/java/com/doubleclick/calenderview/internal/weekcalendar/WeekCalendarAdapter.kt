package com.doubleclick.calenderview.internal.weekcalendar

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubleclick.calenderview.core.Week
import com.doubleclick.calenderview.core.WeekDay
import com.doubleclick.calenderview.data.DataStore
import com.doubleclick.calenderview.data.getWeekCalendarAdjustedRange
import com.doubleclick.calenderview.data.getWeekCalendarData
import com.doubleclick.calenderview.data.getWeekIndex
import com.doubleclick.calenderview.data.getWeekIndicesCount
import com.doubleclick.calenderview.ViewContainer
import com.doubleclick.calenderview.WeekCalendarView
import com.doubleclick.calenderview.WeekDayBinder
import com.doubleclick.calenderview.WeekHeaderFooterBinder
import com.doubleclick.calenderview.internal.NO_INDEX
import com.doubleclick.calenderview.internal.dayTag
import com.doubleclick.calenderview.internal.setupItemRoot
import java.time.DayOfWeek
import java.time.LocalDate

internal class WeekCalendarAdapter(
    private val calView: WeekCalendarView,
    private var startDate: LocalDate,
    private var endDate: LocalDate,
    private var firstDayOfWeek: DayOfWeek,
) : RecyclerView.Adapter<WeekViewHolder>() {

    private var adjustedData = getWeekCalendarAdjustedRange(startDate, endDate, firstDayOfWeek)
    private val startDateAdjusted: LocalDate get() = adjustedData.startDateAdjusted
    private val endDateAdjusted: LocalDate get() = adjustedData.endDateAdjusted
    private var itemCount =
        getWeekIndicesCount(adjustedData.startDateAdjusted, adjustedData.endDateAdjusted)
    private val dataStore = DataStore { offset ->
        getWeekCalendarData(startDateAdjusted, offset, startDate, endDate).week
    }

    init {
        setHasStableIds(true)
    }

    private val isAttached: Boolean
        get() = calView.adapter === this

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        calView.post { notifyWeekScrollListenerIfNeeded() }
    }

    private fun getItem(position: Int): Week = dataStore[position]

    override fun getItemId(position: Int): Long =
        getItem(position).days.first().date.hashCode().toLong()

    override fun getItemCount(): Int = itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val content = setupItemRoot(
            itemMargins = calView.weekMargins,
            daySize = calView.daySize,
            context = calView.context,
            dayViewResource = calView.dayViewResource,
            itemHeaderResource = calView.weekHeaderResource,
            itemFooterResource = calView.weekFooterResource,
            weekSize = 1,
            itemViewClass = calView.weekViewClass,
            dayBinder = calView.dayBinder as WeekDayBinder,
        )

        @Suppress("UNCHECKED_CAST")
        return WeekViewHolder(
            rootLayout = content.itemView,
            headerView = content.headerView,
            footerView = content.footerView,
            weekHolder = content.weekHolders.first(),
            weekHeaderBinder = calView.weekHeaderBinder as WeekHeaderFooterBinder<ViewContainer>?,
            weekFooterBinder = calView.weekFooterBinder as WeekHeaderFooterBinder<ViewContainer>?,
        )
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach {
                holder.reloadDay(it as WeekDay)
            }
        }
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        holder.bindWeek(getItem(position))
    }

    fun reloadDay(date: LocalDate) {
        val position = getAdapterPosition(date)
        if (position != NO_INDEX) {
            notifyItemChanged(position, dataStore[position].days.first { it.date == date })
        }
    }

    fun reloadWeek(date: LocalDate) {
        notifyItemChanged(getAdapterPosition(date))
    }

    fun reloadCalendar() {
        notifyItemRangeChanged(0, itemCount)
    }

    private var visibleWeek: Week? = null
    fun notifyWeekScrollListenerIfNeeded() {
        if (!isAttached) return

        if (calView.isAnimating) {
            calView.itemAnimator?.isRunning {
                notifyWeekScrollListenerIfNeeded()
            }
            return
        }
        val visibleItemPos = findFirstVisibleWeekPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleWeek = dataStore[visibleItemPos]

            if (visibleWeek != this.visibleWeek) {
                this.visibleWeek = visibleWeek
                calView.weekScrollListener?.invoke(visibleWeek)
            }
        }
    }

    internal fun getAdapterPosition(date: LocalDate): Int {
        return getWeekIndex(startDateAdjusted, date)
    }

    private val layoutManager: WeekCalendarLayoutManager
        get() = calView.layoutManager as WeekCalendarLayoutManager

    fun findFirstVisibleWeek(): Week? {
        val index = findFirstVisibleWeekPosition()
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun findLastVisibleWeek(): Week? {
        val index = findLastVisibleWeekPosition()
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun findFirstVisibleDay(): WeekDay? = findVisibleDay(true)

    fun findLastVisibleDay(): WeekDay? = findVisibleDay(false)

    private fun findFirstVisibleWeekPosition(): Int = layoutManager.findFirstVisibleItemPosition()

    private fun findLastVisibleWeekPosition(): Int = layoutManager.findLastVisibleItemPosition()

    private fun findVisibleDay(isFirst: Boolean): WeekDay? {
        val visibleIndex =
            if (isFirst) findFirstVisibleWeekPosition() else findLastVisibleWeekPosition()
        if (visibleIndex == NO_INDEX) return null

        val visibleItemView = layoutManager.findViewByPosition(visibleIndex) ?: return null
        val weekRect = Rect()
        visibleItemView.getGlobalVisibleRect(weekRect)

        val dayRect = Rect()
        return dataStore[visibleIndex].days
            .run { if (isFirst) this else reversed() }
            .firstOrNull {
                val dayView = visibleItemView.findViewWithTag<View>(dayTag(it.date))
                    ?: return@firstOrNull false
                dayView.getGlobalVisibleRect(dayRect)
                dayRect.intersect(weekRect)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun updateData(
        startDate: LocalDate,
        endDate: LocalDate,
        firstDayOfWeek: DayOfWeek,
    ) {
        this.startDate = startDate
        this.endDate = endDate
        this.firstDayOfWeek = firstDayOfWeek
        this.adjustedData = getWeekCalendarAdjustedRange(startDate, endDate, firstDayOfWeek)
        this.itemCount = getWeekIndicesCount(startDateAdjusted, endDateAdjusted)
        dataStore.clear()
        notifyDataSetChanged()
    }
}
