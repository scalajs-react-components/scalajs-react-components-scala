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

package net.leibman.react.calendar

import java.time.format.DateTimeFormatter
import java.time.{ DateTimeException, LocalDate }

import chandu0101.scalajs.react.components.semanticui._
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{
  BackendScope,
  Callback,
  CallbackTo,
  ReactEventFromInput,
  ScalaComponent
}
import net.leibman.react.calendar.SemanticCalendar.Nav.Nav
import net.leibman.react.calendar.SemanticCalendar._

import scala.concurrent.Future
import scala.scalajs.js

case class AgendaView[E <: Event]() extends View[E] {
  val name: String                       = "Agenda"
  override def drillDownViewName: String = "Day"

  private def setStartAndEndDates(date: LocalDate, state: CalendarState[E]) =
    state.copy(
      startDate = date,
      endDate = date.plusDays(state.startDate.until(state.endDate).getDays),
      currentDate = date
    )

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
        val start = startDate.getOrElse(LocalDate.now())
        state.copy(
          startDate = start,
          endDate = endDate.getOrElse(start.plusDays(state.startDate.until(state.endDate).getDays)),
          currentDate = startDate.getOrElse(LocalDate.now)
        )
    }

  override def onViewSelected(state: CalendarState[E]): CalendarState[E] =
    state //Keep the same dates that we have.

  val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  def title(state: StateSnapshot[CalendarState[E]],
            refresh: () => CallbackTo[Future[Unit]]): VdomNode = {
    def onStartDateChange(event: ReactEventFromInput, changeObj: SuiChangeObject): Callback =
      state.modState { s =>
        {
          try {
            val startDate = LocalDate.parse(changeObj.value.get.asInstanceOf[String], df)
            if (startDate.isAfter(s.endDate)) {
              //Don't allow changes to start dates after the end date
              s
            } else {
              s.copy(startDate = startDate)
            }
          } catch {
            case e: DateTimeException => //Invalid date, don't do anything
              println(e)
              s
            case e: Throwable =>
              println(e)
              s
          }
        }
      } >> refresh().map(_ => ())

    def onEndDateChange(event: ReactEventFromInput, changeObj: SuiChangeObject): Callback =
      state.modState { s =>
        try {
          val endDate = LocalDate.parse(changeObj.value.get.asInstanceOf[String], df)
          if (endDate.isBefore(s.startDate)) {
            //Don't allow changes to start dates after the end date
            s
          } else {
            s.copy(endDate = endDate)
          }
        } catch {
          case e: DateTimeException => //Invalid date, don't do anything
            println(e)
            s
          case e: Throwable =>
            println(e)
            s
        }
      } >> refresh().map(_ => ())

    SuiForm(unstackable = true)(
      SuiFormGroup()(
        SuiFormField()(
          SuiInput(
            `type` = "date",
            fluid = false,
            value = state.value.startDate.toString,
            onChange = onStartDateChange _
          )()
        ),
        "-",
        SuiFormField()(
          SuiInput(
            `type` = "date",
            fluid = false,
            value = state.value.endDate.toString,
            onChange = onEndDateChange _
          )()
        )
      )
    )
  }

  class Backend($ : BackendScope[AgendaViewProps, StateSnapshot[CalendarState[E]]]) {
    def render(props: AgendaViewProps, state: StateSnapshot[CalendarState[E]]) =
      <.div(
        ^.className := "cal-agenda-view",
        <.table(
          ^.className := "cal-agenda-table",
          <.thead(
            <.tr(<.th(^.className := "cal-header", ^.width := 92.px, "Date"),
                 <.th(^.className := "cal-header", ^.width := 145.px, "Time"),
                 <.th(^.className := "cal-header", "Event"))
          ),
          <.tbody(
            state.value.currentEvents.zipWithIndex
              .toVdomArray {
                case (event, index) =>
                  <.tr(
                    ^.key := s"event_$index",
                    <.td(^.className := "cal-agenda-date-cell",
                         event.startTime.format(state.value.dateFormat)),
                    <.td(
                      ^.className := "cal-agenda-time-cell",
                      if (event.allDay) {
                        "All Day"
                      } else {
                        s"${event.startTime.format(state.value.timeFormat)}${event.endTime
                          .fold("")(end => s" — ${end.format(state.value.timeFormat)}")}"
                      }
                    ),
                    <.td(
                      ^.className := "cal-agenda-event-cell",
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
                      event.name
                    )
                  )
              }
          )
        )
      )
  }
  case class AgendaViewProps(calendarProps: CalendarProps[E])

  def apply(
      props: CalendarProps[E],
      stateSnapshot: StateSnapshot[CalendarState[E]]
  ): Scala.Unmounted[_, StateSnapshot[CalendarState[E]], _] = {
    val component =
      ScalaComponent
        .builder[AgendaViewProps]("AgendaView")
        .initialState(stateSnapshot)
        .renderBackend[Backend]
        .build

    component(AgendaViewProps(props))
  }

}
