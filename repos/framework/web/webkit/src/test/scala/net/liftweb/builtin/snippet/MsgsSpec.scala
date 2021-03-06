/*
 * Copyright 2010-2011 WorldWide Conferencing, LLC
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
package builtin.snippet

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import common._
import http._
import util.Helpers.secureXML

/**
  * System under specification for Msgs.
  */
object MsgsSpec extends Specification with XmlMatchers {
  "Msgs Specification".title

  def withSession[T](f: => T): T =
    S.initIfUninitted(new LiftSession("test", "", Empty))(f)

  "The built-in Msgs snippet" should {
    "Properly render static content" in {
      val result = withSession {
        // Set some notices
        S.error("Error")
        S.warning("Warning")
        S.notice("Notice")

        // We reparse due to inconsistencies with UnparsedAttributes
        secureXML.loadString(Msgs
              .render(
                  <lift:warning_msg>Warning:</lift:warning_msg><lift:notice_class>funky</lift:notice_class>
              )
              .toString)
      }

      result must ==/(<div id="lift__noticesContainer__">
          <div id="lift__noticesContainer___error">
            <ul>
              <li>Error</li>
            </ul>
          </div>
          <div id="lift__noticesContainer___warning">Warning:
            <ul>
              <li>Warning</li>
            </ul>
          </div>
          <div class="funky" id="lift__noticesContainer___notice">
            <ul>
              <li>Notice</li>
            </ul>
          </div>
        </div>)
    }

    "Properly render AJAX content" in {
      // TODO : Figure out how to test this
      pending
    }
  }
}
