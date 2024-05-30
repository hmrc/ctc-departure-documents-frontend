/*
 * Copyright 2024 HM Revenue & Customs
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

package views.behaviours

import config.FrontendAppConfig
import generators.Generators
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import viewModels.{AddAnotherViewModel, ListItem}

import scala.jdk.CollectionConverters._

trait ListWithActionsViewBehaviours extends YesNoViewBehaviours with Generators {

  implicit override def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  val notMaxedOutViewModel: AddAnotherViewModel
  val maxedOutViewModel: AddAnotherViewModel

  def applyMaxedOutView: HtmlFormat.Appendable

  def pageWithMoreItemsAllowed(viewModel: AddAnotherViewModel): Unit =
    "page with more items allowed" - {

      behave like pageWithTitle(doc, viewModel.title)

      behave like pageWithHeading(doc, viewModel.heading)

      behave like pageWithListWithActions(doc, notMaxedOutViewModel.listItems)

      behave like pageWithRadioItems(legendIsHeading = false, args = viewModel.count.toString)
    }

  def pageWithItemsMaxedOutWithoutRadioItems(viewModel: AddAnotherViewModel): Unit =
    "page with items maxed out without radio items" - {

      behave like pageWithItemsMaxedOut(viewModel)

      behave like pageWithoutRadioItems(doc)
    }

  def pageWithItemsMaxedOutWithRadioItems(viewModel: AddAnotherViewModel): Unit =
    "page with items maxed out with radio items" - {

      behave like pageWithItemsMaxedOut(viewModel)

      behave like pageWithRadioItems(legendIsHeading = false, args = viewModel.count.toString)
    }

  private def pageWithItemsMaxedOut(viewModel: AddAnotherViewModel): Unit =
    "page with items maxed out" - {

      val doc = parseView(applyMaxedOutView)

      behave like pageWithTitle(doc, viewModel.title)

      behave like pageWithHeading(doc, viewModel.heading)

      behave like pageWithListWithActions(doc, viewModel.listItems)

      behave like pageWithContent(doc, "p", viewModel.maxLimitLabel)
    }

  // scalastyle:off method.length
  private def pageWithListWithActions(doc: Document, listItems: Seq[ListItem]): Unit =
    "page with a list with actions" - {
      "must contain a description list" in {
        val descriptionLists = getElementsByTag(doc, "dl")
        descriptionLists.size mustBe 1
      }

      val renderedItems = doc.getElementsByClass("govuk-summary-list__row").asScala

      listItems.zipWithIndex.foreach {
        case (listItem, index) =>
          val renderedItem = renderedItems(index)

          s"item ${index + 1}" - {
            "must contain a name" in {
              val name = renderedItem.getElementsByClass("govuk-summary-list__key").text()
              name mustBe listItem.name
            }

            listItem.removeUrl match {
              case Some(removeUrl) =>
                val actions = renderedItem.getElementsByClass("govuk-summary-list__actions-list-item")
                "must contain 2 actions" in {
                  actions.size() mustBe 2
                }
                withActionLink(actions, "Change", 0, listItem.changeUrl)
                withActionLink(actions, "Remove", 1, removeUrl)
              case None =>
                val actions = renderedItem.getElementsByClass("govuk-summary-list__actions")
                "must contain 1 action" in {
                  actions.size() mustBe 1
                }
                withActionLink(actions, "Change", 0, listItem.changeUrl)
            }

            def withActionLink(actions: Elements, linkType: String, index: Int, url: String): Unit =
              s"must contain a $linkType link" in {
                val link = actions
                  .asScala(index)
                  .getElementsByClass("govuk-link")
                  .first()

                assertElementContainsHref(link, url)

                val spans = link.getElementsByTag("span")
                spans.size() mustBe 2

                spans.first().text() mustBe linkType
                assert(spans.first().hasAttr("aria-hidden"))

                spans.last().text() mustBe s"$linkType ${listItem.name}"
                assert(spans.last().hasClass("govuk-visually-hidden"))
              }
          }
      }
    }
  // scalastyle:on method.length
}
