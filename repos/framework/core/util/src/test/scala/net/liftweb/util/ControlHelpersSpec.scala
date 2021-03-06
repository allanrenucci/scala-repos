/*
 * Copyright 2006-2011 WorldWide Conferencing, LLC
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

package net.liftweb
package util

import org.specs2.mutable.Specification

import common._
import ControlHelpers._

/**
  * Systems under specification for ControlHelpers.
  */
object ControlHelpersSpec extends Specification {
  "ControlHelpers Specification".title

  "the tryo function" should {
    "return a Full can if the tested block doesn't throw an exception" in {
      tryo { "valid" } must_== Full("valid")
    }
    val exception = new RuntimeException("ko")
    def failureBlock = { throw exception; () }

    "return a Failure if the tested block throws an exception" in {
      tryo { failureBlock } must_==
        Failure(exception.getMessage, Full(exception), Empty)
    }
    "return Empty if the tested block throws an exception whose class is in the ignore list - with one element" in {
      tryo(classOf[RuntimeException]) { failureBlock } must_== Empty
    }
    "return Empty if the tested block throws an exception whose class is in the ignore list - with 2 elements" in {
      tryo(List(classOf[RuntimeException], classOf[NullPointerException])) {
        failureBlock
      } must_== Empty
    }
    "trigger a callback function with the exception if the tested block throws an exception" in {
      val callback = (e: Throwable) => { e must_== exception; () }
      tryo(callback) { failureBlock }
      success
    }
    "trigger a callback function with the exception if the tested block throws an exception even if it is ignored" in {
      val callback = (e: Throwable) => { e must_== exception; () }
      tryo(List(classOf[RuntimeException]), Full(callback)) { failureBlock }
      success
    }
    "don't trigger a callback if the tested block doesn't throw an exception" in {
      var x = false
      val callback = (e: Throwable) => { x = true }
      tryo(callback) { "valid" }
      x must_== false
    }
    "don't trigger a callback if the tested block doesn't throw an exception, even with an ignore list" in {
      var x = false
      val callback = (e: Throwable) => { x = true }
      tryo(List(classOf[RuntimeException]), Full(callback)) { "valid" }
      x must_== false
    }
  }
}
