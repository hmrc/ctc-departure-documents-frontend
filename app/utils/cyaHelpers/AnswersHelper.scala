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

package utils.cyaHelpers

import models.journeyDomain.Stage.AccessingJourney
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess, UserAnswersReader}
import models.{Index, LocalReferenceNumber, Mode, RichOptionalJsArray, UserAnswers}
import pages.QuestionPage
import pages.sections.Section
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Reads}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.{Content, SummaryListRow}
import viewModels.ListItem

class AnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages) extends SummaryListRowHelper {

  protected def lrn: LocalReferenceNumber = userAnswers.lrn

  protected def getAnswerAndBuildRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    for {
      answer <- userAnswers.get(page)
      call   <- page.route(userAnswers, mode)
    } yield buildRow(
      prefix = prefix,
      answer = formatAnswer(answer),
      id = id,
      call = call,
      args = args*
    )

  protected def buildListItems(
    section: Section[JsArray]
  )(block: Index => Option[Either[ListItem, ListItem]]): Seq[Either[ListItem, ListItem]] =
    userAnswers
      .get(section)
      .mapWithIndex {
        (_, index) => block(index)
      }

  protected def buildListItem[A <: JourneyDomainModel](
    nameWhenComplete: A => String,
    nameWhenInProgress: => Option[String],
    removeRoute: Option[Call]
  )(implicit userAnswersReader: UserAnswersReader[A]): Option[Either[ListItem, ListItem]] =
    userAnswersReader.run(userAnswers) match {
      case Left(readerError) =>
        readerError.page.route(userAnswers, mode).flatMap {
          changeRoute =>
            nameWhenInProgress
              .map {
                name =>
                  ListItem(
                    name = name,
                    changeUrl = changeRoute.url,
                    removeUrl = removeRoute.map(_.url)
                  )
              }
              .map(Left(_))
        }
      case Right(ReaderSuccess(journeyDomainModel, _)) =>
        journeyDomainModel.routeIfCompleted(userAnswers, mode, AccessingJourney).map {
          changeRoute =>
            Right(
              ListItem(
                name = nameWhenComplete(journeyDomainModel),
                changeUrl = changeRoute.url,
                removeUrl = removeRoute.map(_.url)
              )
            )
        }
    }
}
