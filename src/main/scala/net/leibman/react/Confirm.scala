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

package net.leibman.react

import chandu0101.scalajs.react.components.semanticui.SuiConfirm
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{ BackendScope, Callback, Ref, ScalaComponent }
import japgolly.scalajs.react.ReactEvent
import scala.scalajs.js

object Confirm {

  case class ConfirmState(question: String = "",
                          open: Boolean = false,
                          onConfirm: () => Callback = () => Callback.empty,
                          onCancel: Option[() => Callback] = None,
                          cancelText: String = "Cancel",
                          confirmText: String = "Ok",
                          header: Option[String] = None)

  class Backend($ : BackendScope[Unit, ConfirmState]) {
    private def doConfirm(onConfirm: () => Callback)(event: ReactEvent, obj: js.Object) =
      $.modState(s => s.copy(open = false), onConfirm())
    private def doCancel(onCancel: Option[() => Callback])(event: ReactEvent, obj: js.Object) =
      $.modState(s => s.copy(open = false), onCancel.fold(Callback.empty)(fn => fn()))

    def render(state: ConfirmState): VdomNode =
      <.div(
        SuiConfirm(
          open = state.open,
          content = js.Any.fromString(state.question),
          cancelButton = js.Any.fromString(state.cancelText),
          confirmButton = js.Any.fromString(state.confirmText),
          header = state.header
            .fold(js.undefined.asInstanceOf[js.UndefOr[js.Any]])(h => js.Any.fromString(h)),
          onConfirm = doConfirm(state.onConfirm) _,
          onCancel = doCancel(state.onCancel) _
        )()
      )

    def confirm(question: String,
                onConfirm: () => Callback,
                onCancel: Option[() => Callback] = None,
                cancelText: String = "Cancel",
                confirmText: String = "Ok",
                header: Option[String] = None): Callback =
      $.setState(
        ConfirmState(question, open = true, onConfirm, onCancel, cancelText, confirmText, header)
      )
  }

  private val component =
    ScalaComponent
      .builder[Unit]("LeibmanConfirm")
      .initialState(ConfirmState())
      .renderBackend[Backend]
      .build

  private val ref = Ref.toScalaComponent(component)

  def render(): TagMod = component.withRef(ref)()

  def confirm(question: String,
              onConfirm: () => Callback,
              onCancel: Option[() => Callback] = None,
              cancelText: String = "Cancel",
              confirmText: String = "Ok",
              header: Option[String] = None): Callback =
    ref.get
      .map(
        _.backend.confirm(question, onConfirm, onCancel, cancelText, confirmText, header)
      )
      .getOrElse(Callback.empty)
      .flatten

}
