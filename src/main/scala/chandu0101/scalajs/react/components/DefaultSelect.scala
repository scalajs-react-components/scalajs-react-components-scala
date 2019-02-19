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

package chandu0101.scalajs.react.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js

object DefaultSelect {

  class Backend(t: BackendScope[Props, Unit]) {

    def onChange(P: Props)(e: ReactEventFromInput) =
      P.onChange(e.target.value)

    def render(P: Props) = {
      <.div(
        <.label(<.strong(P.label)),
        <.select(^.paddingLeft := "5px",
                 ^.id := "reactselect",
                 ^.value := P.value,
                 ^.onChange ==> onChange(P))(
          P.options.map(item => <.option(item)).toTagMod
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("DefaultSelect")
    .stateless
    .renderBackend[Backend]
    .build

  case class Props(label: String,
                   options: List[String],
                   value: String,
                   onChange: String => Callback)

  def apply(key: js.UndefOr[Key] = js.undefined,
            label: String,
            options: List[String],
            value: String,
            onChange: String => Callback): Unmounted[Props, Unit, Backend] = {
    val props = Props(label, options, value, onChange)
    key.fold(component(props))(key => component.withKey(key)(props))
  }
}
