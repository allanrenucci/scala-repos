/*
 *  ____    ____    _____    ____    ___     ____ 
 * |  _ \  |  _ \  | ____|  / ___|  / _/    / ___|        Precog (R)
 * | |_) | | |_) | |  _|   | |     | |  /| | |  _         Advanced Analytics Engine for NoSQL Data
 * |  __/  |  _ <  | |___  | |___  |/ _| | | |_| |        Copyright (C) 2010 - 2013 SlamData, Inc.
 * |_|     |_| \_\ |_____|  \____|   /__/   \____|        All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU Affero General Public License as published by the Free Software Foundation, either version 
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this 
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.precog.accounts

import com.precog.common.client._
import com.precog.common.security._
import com.precog.common.security.service._
import com.precog.util.email.DirectoryTemplateEmailer

import blueeyes.bkka._
import blueeyes.BlueEyesServer
import blueeyes.persistence.mongo._

import akka.dispatch.Future
import akka.util.Timeout

import java.io.File

import org.I0Itec.zkclient.ZkClient
import org.streum.configrity.Configuration

import scalaz._

object MongoAccountServer
    extends BlueEyesServer with AccountService with AkkaDefaults {
  val executionContext = defaultFutureDispatch
  implicit val M: Monad[Future] = new FutureMonad(executionContext)

  val clock = blueeyes.util.Clock.System

  def AccountManager(
      config: Configuration): (AccountManager[Future], Stoppable) = {
    val mongo = RealMongo(config.detach("mongo"))

    val zkHosts = config[String]("zookeeper.hosts", "localhost:2181")
    val database = config[String]("mongo.database", "accounts_v1")

    val settings0 = new MongoAccountManagerSettings
    with ZkAccountManagerSettings {
      val zkAccountIdPath = config[String]("zookeeper.accountId.path")
      val accounts = config[String]("mongo.collection", "accounts")
      val deletedAccounts =
        config[String]("mongo.deletedCollection", "deleted_accounts")
      val timeout = new Timeout(config[Int]("mongo.timeout", 30000))
      val resetTokens =
        config[String]("mongo.resetTokenCollection", "reset_tokens")
      val resetTokenExpirationMinutes = config[Int]("resetTokenTimeout", 60)
    }

    val accountManager = new MongoAccountManager(
        mongo, mongo.database(database), settings0) with ZKAccountIdSource {
      val zkc = new ZkClient(zkHosts)
      val settings = settings0
    }

    (accountManager, Stoppable.fromFuture(accountManager.close()))
  }

  def APIKeyFinder(config: Configuration) =
    new CachingAPIKeyFinder(
        WebAPIKeyFinder(config).map(_.withM[Future]) valueOr { errs =>
      sys.error("Unable to build new WebAPIKeyFinder: " +
          errs.list.mkString("\n", "\n", ""))
    })

  def RootKey(config: Configuration) = config[String]("rootKey")

  def Emailer(config: Configuration) = {
    val emailProps = new java.util.Properties
    emailProps.setProperty(
        "mail.smtp.host", config[String]("host", "localhost"))
    emailProps.setProperty("mail.smtp.port", config[String]("port", "25"))
    emailProps.setProperty(
        "mail.from", config[String]("from", "support@precog.com"))
    val templateDir = new File(config[String]("template_dir"))
    require(templateDir.isDirectory,
            "Provided template directory %s is not a directory".format(
                templateDir))
    require(
        templateDir.canRead,
        "Provided template directory %s is not readable".format(templateDir))
    new DirectoryTemplateEmailer(
        templateDir, config.detach("params").data, Some(emailProps))
  }
}
