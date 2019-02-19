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

import chandu0101.scalajs.react.components.semanticui._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{ BackendScope, Callback, Ref, ScalaComponent }

object Spinny {
  def on: Callback  = ref.get.map(_.backend.on).getOrElse(Callback.empty).flatten
  def off: Callback = ref.get.map(_.backend.off).getOrElse(Callback.empty).flatten

  case class SpinnyState(on: Boolean = false)
  class Backend($ : BackendScope[Unit, SpinnyState]) {
    def on: Callback  = $.modState(_.copy(on = true))
    def off: Callback = $.modState(_.copy(on = false))

    def render(state: SpinnyState): VdomNode =
      if (state.on) {
        VdomArray(
          SuiDimmer(key = "spinny", active = true)(
            SuiLoader(active = true, size = SuiSize.large)("Parsing")
          )
        )
      } else {
        EmptyVdom
      }
  }

  private val component =
    ScalaComponent
      .builder[Unit]("LeibmanSpinny")
      .initialState(SpinnyState())
      .renderBackend[Backend]
      .build

  private val ref      = Ref.toScalaComponent(component)
  def render(): TagMod = ref.component()()

}
