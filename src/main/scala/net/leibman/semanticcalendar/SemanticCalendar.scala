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

import java.time.format.DateTimeFormatter
import java.time.{ LocalDate, LocalDateTime, LocalTime }

import chandu0101.scalajs.react.components.semanticui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.vdom.html_<^._

import scala.concurrent.Future
import scala.scalajs.js

trait Event {
  def id: Long

  def name: String

  def description: String

  def startTime: LocalDateTime

  def endTime: js.UndefOr[LocalDateTime]

  def allDay: Boolean

  def toolTip: String

}

case class EventImpl(id: Long,
                     name: String,
                     description: String,
                     startTime: LocalDateTime,
                     endTime: js.UndefOr[LocalDateTime] = js.undefined,
                     allDay: Boolean = false,
                     toolTip: String = "")
    extends Event

object SemanticCalendar {
  import scala.concurrent.ExecutionContext.Implicits.global

  object Nav extends Enumeration {
    type Nav = Value
    val specific, back, next = Value
  }

  trait View[E <: Event] {
    import Nav._

    def name: String

    def title(state: StateSnapshot[CalendarState[E]],
              refresh: () => CallbackTo[Future[Unit]]): VdomNode

    def onViewSelected(state: CalendarState[E]): CalendarState[E] = state

    def navigate(state: CalendarState[E],
                 nav: Nav,
                 startDate: Option[LocalDate] = None,
                 endDate: Option[LocalDate] = None): CalendarState[E] = state

    def canNavigate = true

    def apply(
        props: CalendarProps[E],
        stateSnapshot: StateSnapshot[CalendarState[E]]
    ): Scala.Unmounted[_, StateSnapshot[CalendarState[E]], _]

    def drillDownViewName: String
  }

  case class CalendarState[E <: Event](
      currentView: View[E],
      startDate: LocalDate = LocalDate.now(),
      endDate: LocalDate = LocalDate.now().plusDays(7),
      currentDate: LocalDate = LocalDate.now(),
      currentEvents: Seq[E] = Seq.empty,
      selectedEvent: Option[E] = None,
      dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm"),
      dayWeekFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd EEE"),
      timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a"),
      dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy"),
      dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd"),
      dayOfWeekFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE"),
      monthLabelFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
  )

  case class CalendarProps[E <: Event](
      events: (LocalDate, LocalDate) => Future[Seq[E]],
      currentDate: LocalDate = LocalDate.now(),
//      selectable: Boolean, //TODO write this
//      onSlotClicked: js.UndefOr[LocalDate => Callback],
      minDate: js.UndefOr[LocalDate] = js.undefined,
      maxDate: js.UndefOr[LocalDate] = js.undefined,
      views: Seq[View[E]] = Seq(MonthView[E](), WeekView[E](), DayView[E](), AgendaView[E]()),
      defaultView: js.UndefOr[String] = js.undefined,
      dayStartTime: LocalTime = LocalTime.of(9, 0),
      onEventClicked: js.UndefOr[E => Callback] = js.undefined,
      onEventDoubleClicked: js.UndefOr[E => Callback] = js.undefined,
      onViewChanged: js.UndefOr[View[E] => Callback] = js.undefined,
      onDateRangeChanged: js.UndefOr[(LocalDate, LocalDate) => Callback] = js.undefined,
      onEventSelected: js.UndefOr[E => Callback] = js.undefined,
      renderFooter: js.UndefOr[(CalendarProps[E], StateSnapshot[CalendarState[E]]) => VdomElement] =
        js.undefined
  ) {
    //Default methods
    private[semanticcalendar] def defaultEventClicked(
        e: E,
        stateSnapshot: StateSnapshot[CalendarState[E]]
    ): Callback =
      Callback.log("Called defaultEventClicked") << stateSnapshot.modState(
        _.copy(selectedEvent = Option(e))
      )
    private[semanticcalendar] def defaultEventDoubleClicked(
        e: E,
        stateSnapshot: StateSnapshot[CalendarState[E]]
    ): Callback =
      Callback.empty

  }

  class Backend[E <: Event]($ : BackendScope[CalendarProps[E], CalendarState[E]]) {
    //TODO consider using     $.forceUpdate instead of refresh like this.
    def refresh(): CallbackTo[Future[Unit]] =
      for {
        s         <- $.state
        p         <- $.props
        futEvents <- CallbackTo(p.events(s.startDate, s.endDate))
        moded <- CallbackTo
          .future(
            futEvents.map(events => $.modState(_.copy(currentEvents = events)))
          )

      } yield moded

    def init(state: CalendarState[E]): Callback = Callback.empty

    def onChangeFromCalendarView()(opt: Option[CalendarState[E]], callback: Callback): Callback =
      opt.fold(callback)(s => $.setState(s))

    private def doChangeView(view: View[E]): Callback =
      $.modState(_.copy(currentView = view)) >>
      $.modState(s => view.onViewSelected(s), refresh().map(_ => ())) >>
      $.props.map(_.onViewChanged.fold(Callback.empty)(fn => fn(view)).map(_ => ())).flatten

    private def defaultRenderFooter() = <.div()

    def render(props: CalendarProps[E], state: CalendarState[E]): VdomNode = {
      val snapshot = StateSnapshot(state)(onChangeFromCalendarView())

      def renderHeader() =
        SuiHeader()(
          SuiGrid(padded = SuiHorizontallyOrVertically.vertically)(
            SuiGridRow(columns = Equal.equal)(
              SuiGridColumn(textAlign = SuiTextAlignment.left)(
                <.span(
                  <.span(
                    SuiButton(onClick = { _: japgolly.scalajs.react.ReactMouseEventFromInput =>
                      $.modState(s =>
                                   s.currentView.navigate(s,
                                                          Nav.specific,
                                                          Some(LocalDate.now()),
                                                          Some(LocalDate.now())),
                                 refresh().map(_ => ()))
                    })("Today"),
                    SuiButton(onClick = { _: japgolly.scalajs.react.ReactMouseEventFromInput =>
                      $.modState(
                        s =>
                          s.currentView.navigate(s, Nav.back, Some(s.startDate), Some(s.endDate)),
                        refresh().map(_ => ())
                      )
                    })("Back"),
                    SuiButton(onClick = { _: japgolly.scalajs.react.ReactMouseEventFromInput =>
                      $.modState(
                        s =>
                          s.currentView.navigate(s, Nav.next, Some(s.startDate), Some(s.endDate)),
                        refresh().map(_ => ())
                      )
                    })("Next")
                  ).when(state.currentView.canNavigate)
                )
              ),
              SuiGridColumn(textAlign = SuiTextAlignment.center)(
                state.currentView.title(snapshot, () => refresh())
              ),
              SuiGridColumn(textAlign = SuiTextAlignment.right)(
                props.views.toVdomArray(
                  view =>
                    SuiButton(key = s"${view.name}_Button", onClick = {
                      _: ReactMouseEventFromInput =>
                        doChangeView(view)
                    })(
                      view.name
                  )
                )
              )
            )
          )
        )

      <.div(
        ^.height := 700.px,
        <.div(
          ^.className := "cal-calendar",
          renderHeader(),
          state.currentView(props, snapshot),
          props.renderFooter
            .fold(defaultRenderFooter().asInstanceOf[VdomElement])(fn => fn(props, snapshot))
        )
      )
    }
  }

  def component[E <: Event] =
    ScalaComponent
      .builder[CalendarProps[E]]("SemanticCalendar")
      .initialStateFromProps { props =>
        val viewOpt = props.defaultView.toOption.fold(props.views.headOption)(
          defName => props.views.find(_.name == defName)
        )
        //Let it fail if you don't have a view, or if you couldn't find the default view by name
        viewOpt.map(view => view.onViewSelected(CalendarState(currentView = view))).get
      }
      .renderBackend[Backend[E]]
      .componentDidMount($ ⇒ $.backend.init($.state) >> $.backend.refresh().map(_ => ()))
      .componentWillReceiveProps { _ =>
        Callback.empty
      }
      .build

  def apply[E <: Event](
      events: (LocalDate, LocalDate) => Future[Seq[E]],
      currentDate: LocalDate = LocalDate.now(),
      minDate: js.UndefOr[LocalDate] = js.undefined,
      maxDate: js.UndefOr[LocalDate] = js.undefined,
      views: Seq[View[E]] = Seq(MonthView[E](), WeekView[E](), DayView[E](), AgendaView[E]()),
      defaultView: js.UndefOr[String] = js.undefined,
      dayStartTime: LocalTime = LocalTime.of(9, 0),
      onEventClicked: js.UndefOr[E => Callback] = js.undefined,
      onEventDoubleClicked: js.UndefOr[E => Callback] = js.undefined,
      onViewChanged: js.UndefOr[View[E] => Callback] = js.undefined,
      onDateRangeChanged: js.UndefOr[(LocalDate, LocalDate) => Callback] = js.undefined,
      onEventSelected: js.UndefOr[E => Callback] = js.undefined,
      renderFooter: js.UndefOr[(CalendarProps[E], StateSnapshot[CalendarState[E]]) => VdomElement] =
        js.undefined
  ): Unmounted[CalendarProps[E], CalendarState[E], Backend[E]] =
    component[E](
      CalendarProps[E](
        events,
        currentDate,
        minDate,
        maxDate,
        views,
        defaultView,
        dayStartTime,
        onEventClicked,
        onEventDoubleClicked,
        onViewChanged,
        onDateRangeChanged,
        onEventSelected,
        renderFooter
      )
    )
}
