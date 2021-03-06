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

import scala.scalajs.js
import scalacss.ProdDefaults._
import scalacss.ScalaCssReact._
import Implicits._

object ReactListView {

  class Style extends StyleSheet.Inline {
    import dsl._

    val listGroup = style(
      marginBottom(20.px),
      paddingLeft.`0`,
      &.firstChild.lastChild(
        borderBottomLeftRadius(4 px),
        borderBottomRightRadius(4 px)
      )
    )

    val listItem = styleF.bool(
      selected =>
        styleS(
          position.relative,
          display.block,
          padding(v = 10.px, h = 15.px),
          border :=! "1px solid #ecf0f1",
          cursor.pointer,
          mixinIfElse(selected)(
            color.white,
            fontWeight._500,
            backgroundColor :=! "#146699"
          )(
            backgroundColor.white,
            &.hover(
              color :=! "#555555",
              backgroundColor :=! "#ecf0f1"
            )
          )
      ))

  }

  object DefaultStyle extends Style

  case class State(filterText: String, selectedItem: String)

  class Backend(t: BackendScope[Props, State]) {

    def onTextChange(text: String): Callback =
      t.modState(_.copy(filterText = text))

    def onItemSelect(onItemSelect: js.UndefOr[String => Callback])(value: String): Callback = {
      val setSelected = t.modState(_.copy(selectedItem = value))
      val onSelect    = onItemSelect.asCbo(value)

      setSelected >> onSelect
    }

    def render(P: Props, S: State) = {
      val fItems =
        P.items.filter(item => item.toString.toLowerCase.contains(S.filterText.toLowerCase))
      <.div(
        ReactSearchBox(onTextChange = onTextChange).when(P.showSearchBox),
        <.ul(
          P.style.listGroup,
          fItems.map { item =>
            val selected = item.toString == S.selectedItem
            <.li(
              P.style.listItem(selected),
              ^.onClick --> onItemSelect(P.onItemSelect)(item.toString),
              item
            )
          }.toTagMod
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("ReactListView")
    .initialState(State(filterText = "", selectedItem = ""))
    .renderBackend[Backend]
    .build

  case class Props(
      items: List[String],
      onItemSelect: js.UndefOr[String => Callback],
      showSearchBox: Boolean,
      style: Style
  )

  def apply(
      items: List[String],
      onItemSelect: js.UndefOr[String => Callback] = js.undefined,
      showSearchBox: Boolean = false,
      style: Style = DefaultStyle,
      ref: js.UndefOr[String] = js.undefined,
      key: js.Any = {}
  ) =
    component /*.set(key, ref)*/ (Props(items, onItemSelect, showSearchBox, style))

}
