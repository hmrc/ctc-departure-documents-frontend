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

package models

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.{JsObject, JsPath, Json}

import java.util.UUID
import scala.util.Try

class UserAnswersSpec extends SpecBase {

  private val testPageAnswer  = "foo"
  private val testPageAnswer2 = "bar"
  private val testPagePath    = "testPath"

  private val testCleanupPagePath   = "testCleanupPagePath"
  private val testCleanupPageAnswer = "testCleanupPageAnswer"

  case object TestPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testPagePath

    override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] =
      value match {
        case Some(_) => userAnswers.remove(TestCleanupPage)
        case _       => super.cleanup(value, userAnswers)
      }
  }

  case object TestCleanupPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testCleanupPagePath
  }

  "UserAnswers" - {

    "set" - {
      "must run cleanup when given a new answer" in {

        val userAnswers = emptyUserAnswers.setValue(TestCleanupPage, testCleanupPageAnswer)
        val result      = userAnswers.setValue(TestPage, testPageAnswer)

        val expectedData = Json.obj(
          testPagePath -> testPageAnswer
        )

        result.data mustBe expectedData
      }

      "must run cleanup when given a different answer" in {

        val result = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)
          .setValue(TestCleanupPage, testCleanupPageAnswer)
          .setValue(TestPage, testPageAnswer2)

        val expectedData = Json.obj(
          testPagePath -> testPageAnswer2
        )

        result.data mustBe expectedData
      }

      "must not run cleanup when given the same answer" in {

        val result = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)
          .setValue(TestCleanupPage, testCleanupPageAnswer)
          .setValue(TestPage, testPageAnswer)

        val expectedData = Json.obj(
          testCleanupPagePath -> testCleanupPageAnswer,
          testPagePath        -> testPageAnswer
        )

        result.data mustBe expectedData
      }
    }

    "updateTask" - {
      val section = ".documents"

      "must set task status" - {
        "when task has not previously been set" in {
          val result = emptyUserAnswers.updateTask(section, TaskStatus.InProgress)
          result.tasks mustBe Map(section -> TaskStatus.InProgress)
        }

        "when task has previously been set" in {
          val tasks  = Map(section -> TaskStatus.InProgress)
          val result = emptyUserAnswers.copy(tasks = tasks).updateTask(section, TaskStatus.Completed)
          result.tasks mustBe Map(section -> TaskStatus.Completed)
        }

        "when there are other tasks" in {
          val tasks = Map(
            section             -> TaskStatus.InProgress,
            ".routeDetails"     -> TaskStatus.NotStarted,
            ".transportDetails" -> TaskStatus.CannotStartYet
          )
          val result = emptyUserAnswers.copy(tasks = tasks).updateTask(section, TaskStatus.Completed)
          result.tasks mustBe Map(
            section             -> TaskStatus.Completed,
            ".routeDetails"     -> TaskStatus.NotStarted,
            ".transportDetails" -> TaskStatus.CannotStartYet
          )
        }
      }
    }

    "removeDocumentFromItems" - {

      val uuid = "1794d93b-17d5-44fe-a18d-aaa2059d06fe"

      "must remove any document from items with given UUID" in {
        val data = Json
          .parse(s"""
             |{
             |  "items" : [
             |    {
             |      "addDocumentsYesNo" : true,
             |      "documents" : [
             |        {
             |          "document" : "$uuid"
             |        },
             |        {
             |          "document" : "a573bfd3-6470-40c4-a290-ea2d8d43c02a"
             |        },
             |        {
             |          "document" : "3882459f-b7bc-478d-9d24-359533aa8fe3"
             |        }
             |      ]
             |    },
             |    {
             |      "addDocumentsYesNo" : true,
             |      "documents" : [
             |        {
             |          "document" : "$uuid"
             |        }
             |      ]
             |    },
             |    {
             |      "addDocumentsYesNo" : true,
             |      "documents" : [
             |        {
             |          "document" : "ac50154c-cad1-4320-8def-d282eea63b2e"
             |        },
             |        {
             |          "document" : "$uuid"
             |        }
             |      ]
             |    },
             |    {
             |      "addDocumentsYesNo" : false,
             |      "documents" : []
             |    }
             |  ]
             |}
             |""".stripMargin)
          .as[JsObject]

        val userAnswers = emptyUserAnswers.copy(data = data)

        val result = userAnswers.removeDocumentFromItems(Some(UUID.fromString(uuid))).data

        val expectedResult = Json
          .parse(s"""
             |{
             |  "items" : [
             |    {
             |      "addDocumentsYesNo" : true,
             |      "documents" : [
             |        {
             |          "document" : "a573bfd3-6470-40c4-a290-ea2d8d43c02a"
             |        },
             |        {
             |          "document" : "3882459f-b7bc-478d-9d24-359533aa8fe3"
             |        }
             |      ]
             |    },
             |    {
             |      "documents" : []
             |    },
             |    {
             |      "addDocumentsYesNo" : true,
             |      "documents" : [
             |        {
             |          "document" : "ac50154c-cad1-4320-8def-d282eea63b2e"
             |        }
             |      ]
             |    },
             |    {
             |      "addDocumentsYesNo" : false,
             |      "documents" : []
             |    }
             |  ]
             |}
             |""".stripMargin)
          .as[JsObject]

        result mustBe expectedResult
      }
    }
  }
}
