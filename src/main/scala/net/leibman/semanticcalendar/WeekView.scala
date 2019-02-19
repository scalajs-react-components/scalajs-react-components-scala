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

import java.time.{ DayOfWeek, LocalDate, LocalDateTime, LocalTime }
import java.time.temporal.{ ChronoField, TemporalAdjusters, WeekFields }

import japgolly.scalajs.react.{ BackendScope, Callback, CallbackTo, Ref, ScalaComponent }
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.extra.StateSnapshot
import net.leibman.semanticcalendar.SemanticCalendar.{ CalendarProps, CalendarState, Nav, View }
import japgolly.scalajs.react.vdom.html_<^._
import net.leibman.semanticcalendar.SemanticCalendar.Nav.Nav
import org.scalajs.dom.html
import org.scalajs.dom.html.Div

import scala.concurrent.Future

case class WeekView[E <: Event]() extends View[E] {
  private val firstDayOfWeek = WeekFields.ISO.getFirstDayOfWeek
  private val lastDayOfWeek =
    DayOfWeek.of(((firstDayOfWeek.getValue + 5) % DayOfWeek.values.length) + 1)

  val name: String                       = "Week"
  override def drillDownViewName: String = "Day"

  override def navigate(state: CalendarState[E],
                        nav: Nav,
                        startDate: Option[LocalDate],
                        endDate: Option[LocalDate]): CalendarState[E] =
    nav match {
      case Nav.next =>
        setStartAndEndDates(state.startDate.plusWeeks(1), state)
      case Nav.back =>
        setStartAndEndDates(state.startDate.plusWeeks(-1), state)
      case Nav.specific =>
        setStartAndEndDates(startDate.getOrElse(LocalDate.now()), state)
    }

  override def onViewSelected(state: CalendarState[E]): CalendarState[E] =
    setStartAndEndDates(state.currentDate, state)

  def title(state: StateSnapshot[CalendarState[E]],
            refresh: () => CallbackTo[Future[Unit]]): VdomNode =
    s"${state.value.startDate.format(state.value.dateFormat)} — ${state.value.endDate.format(state.value.dateFormat)}"

  private def pctHeight(startTime: LocalDateTime, endTime: LocalDateTime): Double = {
    val diff = if (endTime.toLocalDate.isAfter(startTime.toLocalDate)) {
      //Ends after today, so max
      endTime.toLocalDate
        .atStartOfDay()
        .minusSeconds(1)
        .getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY)
    } else {
      endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY)
    }

    (diff * 100.0) / (4 + 24 * 60 * 60)
  }

  private def pctVerticalLocation(time: LocalTime): Double =
    (time.getLong(ChronoField.SECOND_OF_DAY) * 100.0) / (4 + 24 * 60 * 60)

  def pctVerticalLocation(startTime: LocalDateTime, endTime: Option[LocalDateTime]): Double = {
    val time = if (endTime.fold(true)(t => startTime.toLocalDate.isBefore(t.toLocalDate))) {
      //The event starts before today.
      0
    } else {
      startTime.getLong(ChronoField.SECOND_OF_DAY)
    }
    (time * 100.0) / (4 + 24 * 60 * 60)
  }

  private def setStartAndEndDates(date: LocalDate, state: CalendarState[E]) =
    state.copy(
      startDate = date.`with`(TemporalAdjusters.previousOrSame(firstDayOfWeek)),
      endDate = date.`with`(TemporalAdjusters.nextOrSame(lastDayOfWeek)),
      currentDate = date
    )

  class Backend($ : BackendScope[WeekViewProps, StateSnapshot[CalendarState[E]]]) {
    val hourRange = 0 to 23
    val dayRange  = 0 to 6
    val timeslotRefs =
      hourRange.map(i => LocalDate.now.atTime(i, 0).toLocalTime -> Ref[html.Element]).toMap
    val scrollRef = Ref[html.Element]

    def render(props: WeekViewProps, state: StateSnapshot[CalendarState[E]]): VdomTagOf[Div] = {

      <.div(
        ^.className := "cal-time-view",
        <.div(
          ^.className := "cal-time-header cal-overflowing",
          ^.marginRight := 10.px,
          <.div(^.className := "cal-label cal-time-header-gutter",
                ^.width := 69.6719.px,
                ^.minWidth := 69.6719.px,
                ^.maxWidth := 69.6719.px),
          <.div(
            ^.className := "cal-time-header-content",
            <.div(
              ^.className := "cal-row cal-time-header-cell",
              dayRange.toVdomArray(i => {
                val day = state.value.startDate.plusDays(i)
                <.div(
                  ^.key := s"calHeader$i",
                  ^.className := s"cal-header ${if (day.isEqual(LocalDate.now())) "cal-today"
                  else ""}",
                  <.a(^.href := "#", <.span(day.format(state.value.dayWeekFormat)))
                )
              })
            ),
            <.div(
              ^.className := "cal-allday-cell",
              <.div(
                ^.className := "cal-row-bg",
                dayRange.toVdomArray(i => {
                  val day = state.value.startDate.plusDays(i)
                  <.div(^.key := s"calAllDayCell$i",
                        ^.className := s"cal-day-bg ${if (day.isEqual(LocalDate.now())) "cal-today"
                        else ""}")
                })
              ),
              <.div(
                ^.className := "cal-row-content",
                <.div(^.className := "cal-row"), //TODO add all day events here.
                <.div(^.className := "cal-row")
              )
            )
          )
        ),
        <.div(
          ^.className := "cal-time-content",
          <.div(
            ^.className := "cal-time-gutter cal-time-column",
            hourRange.toVdomArray { hour =>
              val time = state.value.startDate.atTime(hour, 0).toLocalTime
              <.div(
                ^.key := s"calTimeSlotGroup1$hour",
                ^.className := "cal-timeslot-group",
                <.div(
                  ^.className := "cal-time-slot",
                  <.span(^.className := "cal-label", time.format(state.value.timeFormat))
                ),
                <.div(^.className := "cal-time-slot")
              ).withRef(timeslotRefs(time))
            }
          ),
          dayRange.toVdomArray(
            i => {
              val day = state.value.startDate.plusDays(i)
              <.div(
                ^.key := s"calDaySlot$day",
                ^.className := s"cal-day-slot cal-time-column ${if (day.isEqual(LocalDate.now())) "cal-now cal-today"
                else ""}",
                hourRange.toVdomArray(
                  hour =>
                    <.div(^.key := s"calTimeSlotGroup${day}_$hour",
                          ^.className := "cal-timeslot-group",
                          <.div(^.className := "cal-time-slot"),
                          <.div(^.className := "cal-time-slot"))
                ),
                <.div(
                  ^.className := "cal-events-container",
                  state.value.currentEvents
                    .filter(event => event.startTime.toLocalDate.equals(day) && !event.allDay)
                    .zipWithIndex
                    .toVdomArray {
                      case (event, index) =>
                        <.div(
                          ^.key := s"event_$index",
                          ^.className := s"cal-event ${if (state.value.selectedEvent.fold(false)(_ == event)) "cal-selected"
                          else ""}",
                          ^.top := pctVerticalLocation(event.startTime, event.endTime.toOption).pct,
                          ^.height := event.endTime
                            .fold(0.1)(end => pctHeight(event.startTime, end))
                            .pct,
                          ^.left := 0.pct,
                          ^.width := 100.pct,
                          ^.onClick --> props.calendarProps.onEventClicked
                            .fold(props.calendarProps.defaultEventClicked(event, state))(
                              fn => fn(event)
                            ),
                          ^.onDoubleClick --> props.calendarProps.onEventDoubleClicked
                            .fold(props.calendarProps.defaultEventDoubleClicked(event, state))(
                              fn => fn(event)
                            ),
                          <.div(
                            ^.className := "cal-event-label",
                            s"${event.startTime.format(state.value.timeFormat)}${event.endTime
                              .fold("")(end => s" — ${end.format(state.value.timeFormat)}")}"
                          ),
                          <.div(^.className := "cal-event-content", event.name)
                        )
                    }
                ),
                <.div(
                  ^.className := "cal-current-time-indicator",
                  ^.top := pctVerticalLocation(LocalTime.now()).pct
                ).when(day.isEqual(LocalDate.now()))
              )
            }
          )
        ).withRef(scrollRef)
      )
    }
  }

  case class WeekViewProps(calendarProps: CalendarProps[E])

  def apply(
      props: CalendarProps[E],
      stateSnapshot: StateSnapshot[CalendarState[E]]
  ): Scala.Unmounted[_, StateSnapshot[CalendarState[E]], _] = {
    val component =
      ScalaComponent
        .builder[WeekViewProps]("WeekView")
        .initialState(stateSnapshot)
        .renderBackend[Backend]
        .componentDidMount { s =>
          val scrollRef   = s.backend.scrollRef
          val timeSlotRef = s.backend.timeslotRefs(s.props.calendarProps.dayStartTime)
          scrollRef.foreachCB(
            scroll =>
              timeSlotRef.foreachCB { timeSlot =>
                Callback(scroll.scrollTop = timeSlot.offsetTop)
            }
          )
        }
        .componentWillReceiveProps { e =>
          e.state.modState(s => s.copy(endDate = s.startDate, currentDate = s.startDate))
        }
        .build

    component(WeekViewProps(props))
  }
}
