/*
 * Copyright 2019 scalajs-react-components
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.leibman.semanticcalendar

import java.time.temporal.{ TemporalAdjusters, WeekFields }
import java.time.{ DayOfWeek, LocalDate }

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.vdom.html_<^.{ <, _ }
import japgolly.scalajs.react.{ BackendScope, CallbackTo, ScalaComponent }
import net.leibman.semanticcalendar.SemanticCalendar.Nav.Nav
import net.leibman.semanticcalendar.SemanticCalendar.{ CalendarProps, CalendarState, Nav, View }

import scala.concurrent.Future

case class MonthView[E <: Event]() extends View[E] {
  val name: String                       = "Month"
  override def drillDownViewName: String = "Day"

  private val firstDayOfWeek = WeekFields.ISO.getFirstDayOfWeek
  private val lastDayOfWeek =
    DayOfWeek.of(((firstDayOfWeek.getValue + 5) % DayOfWeek.values.length) + 1)

  def firstDayOfMonth(state: CalendarState[E]) = state.startDate.plusDays(7).withDayOfMonth(1)

  def title(state: StateSnapshot[CalendarState[E]],
            refresh: () => CallbackTo[Future[Unit]]): VdomNode =
    s"${firstDayOfMonth(state.value).format(state.value.monthLabelFormat)}"

  private def setStartAndEndDates(date: LocalDate, state: CalendarState[E]) =
    state.copy(
      startDate = date.withDayOfMonth(1).`with`(TemporalAdjusters.previousOrSame(firstDayOfWeek)),
      endDate = date
        .withDayOfMonth(date.lengthOfMonth())
        .`with`(TemporalAdjusters.nextOrSame(lastDayOfWeek)),
      currentDate = firstDayOfMonth(state)
    )

  override def navigate(state: CalendarState[E],
                        nav: Nav,
                        startDate: Option[LocalDate],
                        endDate: Option[LocalDate]): CalendarState[E] = {
    val firstDay = firstDayOfMonth(state)

    nav match {
      case Nav.next =>
        setStartAndEndDates(firstDay.plusMonths(1), state)
      case Nav.back =>
        setStartAndEndDates(firstDay.plusMonths(-1), state)
      case Nav.specific =>
        setStartAndEndDates(startDate.getOrElse(LocalDate.now()), state)
    }
  }

  override def onViewSelected(state: CalendarState[E]): CalendarState[E] =
    setStartAndEndDates(state.currentDate, state)

  class Backend($ : BackendScope[MonthViewProps, StateSnapshot[CalendarState[E]]]) {

    def render(props: MonthViewProps, state: StateSnapshot[CalendarState[E]]) = {
      val dayRange      = 0 to 6
      val firstDay      = firstDayOfMonth(state.value)
      val weekRange     = 0 to (firstDay.lengthOfMonth() / 7)
      val now           = LocalDate.now()
      val oneSeventhPct = (100.0 / 7.0).pct

      <.div(
        ^.className := "cal-month-view",
        <.div(
          ^.className := "cal-row cal-month-header",
          dayRange.toVdomArray { i =>
            <.div(^.key := s"calHeader$i",
                  ^.className := "cal-header",
                  state.value.startDate.plusDays(i).format(state.value.dayOfWeekFormat))
          }
        ),
        weekRange.toVdomArray(
          week =>
            <.div(
              ^.key := s"calMonthRow$week",
              ^.className := "cal-month-row",
              <.div(
                ^.className := "cal-row-bg",
                dayRange.toVdomArray(day => {
                  val date = state.value.startDate.plusDays((week * 7) + day)
                  val isOutsideMonth = date.isBefore(firstDay) || date.isAfter(
                    firstDay.withDayOfMonth(firstDay.lengthOfMonth())
                  )
                  <.div(
                    ^.key := s"calDayBg${week}_$day",
                    ^.className := s"cal-day-bg ${if (isOutsideMonth) "cal-off-range-bg" else ""} ${if (date.isEqual(now)) "cal-today" else ""}"
                  )
                })
              ),
              <.div(
                ^.className := "cal-row-content",
                <.div(
                  ^.className := "cal-row",
                  dayRange
                    .toVdomArray(
                      day => {
                        val date = state.value.startDate.plusDays((week * 7) + day)
                        val isOutsideMonth = date.isBefore(firstDay) || date.isAfter(
                          firstDay.withDayOfMonth(firstDay.lengthOfMonth())
                        )
                        <.div(
                          ^.key := s"calDayBg${week}_$day",
                          ^.className := s"cal-date-cell ${if (isOutsideMonth) "cal-off-range"
                          else ""} ${if (date.isEqual(now)) "cal-now cal-current" else ""}",
                          <.a(^.href := "$", date.getDayOfMonth)
                        )
                      }
                    )
                ),
                <.div(
                  ^.className := "cal-row",
                  dayRange
                    .toVdomArray(
                      day => {
                        val date = state.value.startDate.plusDays((week * 7) + day)
                        val isOutsideMonth = date.isBefore(firstDay) || date.isAfter(
                          firstDay.withDayOfMonth(firstDay.lengthOfMonth())
                        )

                        <.div(
                          ^.key := s"calDaySegment${week}_$day",
                          ^.className := s"cal-row-segment",
                          ^.flexBasis := oneSeventhPct,
                          ^.maxWidth := oneSeventhPct,
                          state.value.currentEvents
                            .filter(
                              event => {
                                val eventStart = event.startTime.toLocalDate
                                val eventEnd   = event.endTime.fold(eventStart)(_.toLocalDate)
                                (date.isAfter(eventStart) || date.isEqual(eventStart)) &&
                                (date.isBefore(eventEnd) || date.isEqual(eventEnd))
                              }
                            )
                            .zipWithIndex
                            .toVdomArray {
                              case (event, index) =>
                                <.button(
                                  ^.key := s"eventButton${day}_$index",
                                  ^.className := s"cal-event ${if (state.value.selectedEvent.fold(false)(_ == event)) "cal-selected"
                                  else ""}",
                                  ^.onClick --> props.calendarProps.onEventClicked
                                    .fold(props.calendarProps.defaultEventClicked(event, state))(
                                      fn => fn(event)
                                    ),
                                  ^.onDoubleClick --> props.calendarProps.onEventDoubleClicked
                                    .fold(
                                      props.calendarProps.defaultEventDoubleClicked(event, state)
                                    )(
                                      fn => fn(event)
                                    ),
                                  <.div(^.className := "cal-event-content",
                                        ^.title := event.name,
                                        event.name)
                                )
                            }
                        )
                      }
                    )
                )
              )
          )
        )
      )
    }
  }

  case class MonthViewProps(calendarProps: CalendarProps[E])

  def apply(
      props: CalendarProps[E],
      stateSnapshot: StateSnapshot[CalendarState[E]]
  ): Scala.Unmounted[_, StateSnapshot[CalendarState[E]], _] = {
    val component =
      ScalaComponent
        .builder[MonthViewProps]("MonthView")
        .initialState(stateSnapshot)
        .renderBackend[Backend]
        .build

    component(MonthViewProps(props))
  }
}
