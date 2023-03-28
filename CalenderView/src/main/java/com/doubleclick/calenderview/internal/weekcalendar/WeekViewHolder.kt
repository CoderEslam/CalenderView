package com.doubleclick.calenderview.internal.weekcalendar

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubleclick.calenderview.core.Week
import com.doubleclick.calenderview.core.WeekDay
import com.doubleclick.calenderview.ViewContainer
import com.doubleclick.calenderview.WeekHeaderFooterBinder
import com.doubleclick.calenderview.internal.WeekHolder

internal class WeekViewHolder constructor(
    rootLayout: ViewGroup,
    private val headerView: View?,
    private val footerView: View?,
    private val weekHolder: WeekHolder<WeekDay>,
    private var weekHeaderBinder: WeekHeaderFooterBinder<ViewContainer>?,
    private var weekFooterBinder: WeekHeaderFooterBinder<ViewContainer>?,
) : RecyclerView.ViewHolder(rootLayout) {

    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null

    lateinit var week: Week

    fun bindWeek(week: Week) {
        this.week = week
        headerView?.let { view ->
            val headerContainer = headerContainer ?: weekHeaderBinder!!.create(view).also {
                headerContainer = it
            }
            weekHeaderBinder?.bind(headerContainer, week)
        }
        weekHolder.bindWeekView(week.days)
        footerView?.let { view ->
            val footerContainer = footerContainer ?: weekFooterBinder!!.create(view).also {
                footerContainer = it
            }
            weekFooterBinder?.bind(footerContainer, week)
        }
    }

    fun reloadDay(day: WeekDay) {
        weekHolder.reloadDay(day)
    }
}
