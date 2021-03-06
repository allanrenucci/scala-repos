/**
  * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
  */
package akka.remote

import akka.remote.transport.AssociationHandle

import language.postfixOps
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.remote.testconductor.RoleName
import akka.remote.transport.ThrottlerTransportAdapter.{ForceDisassociateExplicitly, ForceDisassociate, Direction}
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.remote.testkit.STMultiNodeSpec
import akka.testkit._
import akka.actor.ActorIdentity
import akka.remote.testconductor.RoleName
import akka.actor.Identify
import scala.concurrent.Await

object RemoteRestartedQuarantinedSpec extends MultiNodeConfig {
  val first = role("first")
  val second = role("second")

  commonConfig(debugConfig(on = false).withFallback(ConfigFactory.parseString("""
      akka.loglevel = WARNING
      akka.remote.log-remote-lifecycle-events = WARNING

      # Keep it long, we don't want reconnects
      akka.remote.retry-gate-closed-for  = 1 s

      # Important, otherwise it is very racy to get a non-writing endpoint: the only way to do it if the two nodes
      # associate to each other at the same time. Setting this will ensure that the right scenario happens.
      akka.remote.use-passive-connections = off

      # TODO should not be needed, but see TODO at the end of the test
      akka.remote.transport-failure-detector.heartbeat-interval = 1 s
      akka.remote.transport-failure-detector.acceptable-heartbeat-pause = 10 s
                              """)))

  testTransport(on = true)

  class Subject extends Actor {
    def receive = {
      case "shutdown" ⇒ context.system.terminate()
      case "identify" ⇒
        sender() ! (AddressUidExtension(context.system).addressUid -> self)
    }
  }
}

class RemoteRestartedQuarantinedSpecMultiJvmNode1
    extends RemoteRestartedQuarantinedSpec
class RemoteRestartedQuarantinedSpecMultiJvmNode2
    extends RemoteRestartedQuarantinedSpec

abstract class RemoteRestartedQuarantinedSpec
    extends MultiNodeSpec(RemoteRestartedQuarantinedSpec) with STMultiNodeSpec
    with ImplicitSender {

  import RemoteRestartedQuarantinedSpec._

  override def initialParticipants = 2

  def identifyWithUid(role: RoleName, actorName: String): (Int, ActorRef) = {
    system.actorSelection(node(role) / "user" / actorName) ! "identify"
    expectMsgType[(Int, ActorRef)]
  }

  "A restarted quarantined system" must {

    "should not crash the other system (#17213)" taggedAs LongRunningTest in {

      system.actorOf(Props[Subject], "subject")
      enterBarrier("subject-started")

      runOn(first) {
        val secondAddress = node(second).address

        val (uid, ref) = identifyWithUid(second, "subject")

        RARP(system).provider.transport
          .quarantine(node(second).address, Some(uid))

        enterBarrier("quarantined")
        enterBarrier("still-quarantined")

        testConductor.shutdown(second).await

        within(30.seconds) {
          awaitAssert {
            system.actorSelection(
                RootActorPath(secondAddress) / "user" / "subject") ! Identify(
                "subject")
            expectMsgType[ActorIdentity](1.second).ref.get
          }
        }

        system.actorSelection(
            RootActorPath(secondAddress) / "user" / "subject") ! "shutdown"
      }

      runOn(second) {
        val addr =
          system.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress
        val firstAddress = node(first).address
        system.eventStream.subscribe(testActor,
                                     classOf[ThisActorSystemQuarantinedEvent])

        val (_, ref) = identifyWithUid(first, "subject")

        enterBarrier("quarantined")

        // Check that quarantine is intact
        within(10.seconds) {
          awaitAssert {
            EventFilter
              .warning(
                  pattern = "The remote system has quarantined this system",
                  occurrences = 1)
              .intercept {
                ref ! "boo!"
              }
          }
        }

        expectMsgPF(10 seconds) {
          case ThisActorSystemQuarantinedEvent(local, remote) ⇒
        }

        enterBarrier("still-quarantined")

        Await.result(system.whenTerminated, 10.seconds)

        val freshSystem =
          ActorSystem(system.name, ConfigFactory.parseString(s"""
                    akka.remote.retry-gate-closed-for = 0.5 s
                    akka.remote.netty.tcp {
                      hostname = ${addr.host.get}
                      port = ${addr.port.get}
                    }
                    """).withFallback(system.settings.config))

        val probe = TestProbe()(freshSystem)

        freshSystem
          .actorSelection(RootActorPath(firstAddress) / "user" / "subject")
          .tell(Identify("subject"), probe.ref)
        // TODO sometimes it takes long time until the new connection is established,
        //      It seems like there must first be a transport failure detector timeout, that triggers
        //      "No response from remote. Handshake timed out or transport failure detector triggered".
        probe.expectMsgType[ActorIdentity](30.second).ref should not be (None)

        // Now the other system will be able to pass, too
        freshSystem.actorOf(Props[Subject], "subject")

        Await.ready(freshSystem.whenTerminated, 10.seconds)
      }
    }
  }
}
