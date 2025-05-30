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

import cats.data.ReaderT
import config.FrontendAppConfig
import models.TaskStatus.InProgress
import models.journeyDomain.OpsError.WriterError
import models.journeyDomain.{DocumentsDomain, UserAnswersReader}
import models.requests.MandatoryDataRequest
import models.{LocalReferenceNumber, UserAnswers}
import navigation.UserAnswersNavigator
import pages.QuestionPage
import play.api.libs.json.Format
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

package object controllers {
  type EitherType[A]        = Either[WriterError, A]
  type UserAnswersWriter[A] = ReaderT[EitherType, UserAnswers, A]
  type Write[A]             = (QuestionPage[A], UserAnswers)

  implicit class SettableOps[A](page: QuestionPage[A]) {

    def writeToUserAnswers(value: A)(implicit format: Format[A]): UserAnswersWriter[Write[A]] =
      ReaderT[EitherType, UserAnswers, Write[A]](
        userAnswers =>
          userAnswers.set[A](page, value) match {
            case Success(userAnswers) => Right((page, userAnswers))
            case Failure(exception)   => Left(WriterError(page, Some(s"Failed to write $value to page ${page.path} with exception: ${exception.toString}")))
          }
      )

    def removeFromUserAnswers(): UserAnswersWriter[Write[A]] =
      ReaderT[EitherType, UserAnswers, Write[A]] {
        userAnswers =>
          userAnswers.remove(page) match {
            case Success(value)     => Right((page, value))
            case Failure(exception) => Left(WriterError(page, Some(s"Failed to remove ${page.path} with exception: ${exception.toString}")))
          }
      }
  }

  implicit class SettableOpsRunner[A](userAnswersWriter: UserAnswersWriter[Write[A]]) {

    def appendValueIfNotPresent[B](subPage: QuestionPage[B], value: B)(implicit format: Format[B]): UserAnswersWriter[Write[A]] =
      userAnswersWriter.flatMapF {
        case (page, userAnswers) =>
          userAnswers.get(subPage) match {
            case Some(_) => Right((page, userAnswers))
            case None =>
              userAnswers.set(subPage, value) match {
                case Success(value)     => Right((page, value))
                case Failure(exception) => Left(WriterError(page, Some(s"Failed to append value to answer: ${exception.getMessage}")))
              }
          }
      }

    def removeDocumentFromItems(uuid: Option[UUID]): UserAnswersWriter[Write[A]] =
      userAnswersWriter.flatMapF {
        case (page, userAnswers) =>
          Right((page, userAnswers.removeDocumentFromItems(uuid)))
      }

    def updateTask(): UserAnswersWriter[Write[A]] =
      userAnswersWriter.flatMapF {
        case (page, userAnswers) =>
          page.path.path.headOption.map(_.toJsonString) match {
            case Some(section) =>
              val status = UserAnswersReader[DocumentsDomain].run(userAnswers) match {
                case Left(_)  => InProgress
                case Right(_) => userAnswers.status.taskStatus
              }
              Right((page, userAnswers.updateTask(section, status)))
            case None =>
              Left(WriterError(page, Some(s"Failed to find section in JSON path ${page.path}")))
          }
      }

    def writeToSession(
      userAnswers: UserAnswers,
      sessionRepository: SessionRepository
    )(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[Write[A]] =
      userAnswersWriter.run(userAnswers) match {
        case Left(opsError) => Future.failed(new Exception(s"${opsError.toString}"))
        case Right(value) =>
          sessionRepository
            .set(value._2)
            .map(
              _ => value
            )
      }

    def writeToSession(sessionRepository: SessionRepository)(implicit
      dataRequest: MandatoryDataRequest[?],
      ex: ExecutionContext,
      hc: HeaderCarrier
    ): Future[Write[A]] = writeToSession(dataRequest.userAnswers, sessionRepository)
  }

  implicit class NavigatorOps[A](write: Future[Write[A]]) {

    def and(block: Future[Write[A]] => Future[Result]): Future[Result] =
      block(write)

    def navigateWith(
      navigator: UserAnswersNavigator
    )(implicit executionContext: ExecutionContext): Future[Result] =
      navigate {
        case (page, userAnswers) => navigator.nextPage(userAnswers, Some(page))
      }

    def navigateTo(call: Call)(implicit executionContext: ExecutionContext): Future[Result] =
      navigate {
        _ => call
      }

    def getNextPage(block: Write[A] => Call)(implicit executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig): Future[Call] =
      write.map {
        case (page, userAnswers) =>
          val call = block(page, userAnswers)
          val url  = frontendAppConfig.absoluteURL(call.url)
          call.copy(url = url)
      }

    private def navigate(result: Write[A] => Call)(implicit executionContext: ExecutionContext): Future[Result] =
      write.map {
        w => Redirect(result(w))
      }
  }

  implicit class UpdateOps(call: Future[Call]) {

    private def updateTask(frontendUrl: String, lrn: LocalReferenceNumber)(implicit ex: ExecutionContext): Future[Call] =
      call.map {
        x => x.copy(url = s"$frontendUrl/$lrn/update-task?continue=${x.url}")
      }

    def updateItems(lrn: LocalReferenceNumber)(implicit ex: ExecutionContext, config: FrontendAppConfig): Future[Call] =
      updateTask(config.itemsUrl, lrn)

    def navigate()(implicit executionContext: ExecutionContext): Future[Result] =
      call.map(Redirect)
  }
}
