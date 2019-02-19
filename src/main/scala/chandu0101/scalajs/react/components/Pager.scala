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
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

object Pager {

  val cssSettings = scalacss.devOrProdDefaults
  import cssSettings._

  class Style extends StyleSheet.Inline {
    import dsl._

    val pager = style(
      margin :=! "15px 0",
      unsafeChild("a")(
        display.inlineBlock,
        padding :=! "5px 14px",
        backgroundColor :=! "#EF5350",
        border :=! "1px solid transparent",
        borderRadius :=! "2px",
        cursor.pointer,
        color :=! "#ffffff",
        fontWeight.bold,
        boxShadow := "0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 1px 2px 0 rgba(0, 0, 0, 0.24)",
        &.hover.apply(
          textDecoration := "none",
          backgroundColor :=! "#ff1034"
        ),
        &.focus.apply(
          textDecoration := "none",
          backgroundColor :=! "#ff1034"
        )
      )
    )
  }
  case class Backend(t: BackendScope[Props, Unit]) {
    def render(P: Props) = {
      <.div(P.style.pager)(
        <.a(
          ^.onClick --> P.previousClick,
          ^.float := "left",
          "← Previous"
        ).when(P.offset > 0),
        <.a(
          ^.onClick --> P.nextClick,
          ^.float := "right",
          "Next →"
        ).when(P.offset + P.itemsPerPage < P.totalItems)
      )
    }
  }
  object DefaultStyle extends Style

  val component = ScalaComponent
    .builder[Props]("Pager")
    .renderBackend[Backend]
    .build

  case class Props(itemsPerPage: Int,
                   totalItems: Int,
                   offset: Int,
                   nextClick: Callback,
                   previousClick: Callback,
                   style: Style)

  def apply(itemsPerPage: Int,
            totalItems: Int,
            offset: Int,
            nextClick: Callback,
            previousClick: Callback,
            style: Style = DefaultStyle) = {
    component(Props(itemsPerPage, totalItems, offset, nextClick, previousClick, style))
  }

}
