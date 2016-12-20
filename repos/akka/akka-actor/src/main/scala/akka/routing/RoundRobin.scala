/**
  * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
  */
package akka.routing

import java.util.concurrent.atomic.AtomicLong
import scala.collection.immutable
import akka.dispatch.Dispatchers
import com.typesafe.config.Config
import akka.actor.SupervisorStrategy
import akka.japi.Util.immutableSeq
import akka.actor.ActorSystem

object RoundRobinRoutingLogic {
  def apply(): RoundRobinRoutingLogic = new RoundRobinRoutingLogic
}

/**
  * Uses round-robin to select a routee. For concurrent calls,
  * round robin is just a best effort.
  */
@SerialVersionUID(1L)
final class RoundRobinRoutingLogic extends RoutingLogic {
  val next = new AtomicLong

  override def select(message: Any,
                      routees: immutable.IndexedSeq[Routee]): Routee =
    if (routees.nonEmpty) {
      val size = routees.size
      val index = (next.getAndIncrement % size).asInstanceOf[Int]
      routees(if (index < 0) size + index - 1 else index)
    } else NoRoutee
}

/**
  * A router pool that uses round-robin to select a routee. For concurrent calls,
  * round robin is just a best effort.
  *
  * The configuration parameter trumps the constructor arguments. This means that
  * if you provide `nrOfInstances` during instantiation they will be ignored if
  * the router is defined in the configuration file for the actor being used.
  *
  * <h1>Supervision Setup</h1>
  *
  * Any routees that are created by a router will be created as the router's children.
  * The router is therefore also the children's supervisor.
  *
  * The supervision strategy of the router actor can be configured with
  * [[#withSupervisorStrategy]]. If no strategy is provided, routers default to
  * a strategy of “always escalate”. This means that errors are passed up to the
  * router's supervisor for handling.
  *
  * The router's supervisor will treat the error as an error with the router itself.
  * Therefore a directive to stop or restart will cause the router itself to stop or
  * restart. The router, in turn, will cause its children to stop and restart.
  *
  * @param nrOfInstances initial number of routees in the pool
  *
  * @param resizer optional resizer that dynamically adjust the pool size
  *
  * @param supervisorStrategy strategy for supervising the routees, see 'Supervision Setup'
  *
  * @param routerDispatcher dispatcher to use for the router head actor, which handles
  *   supervision, death watch and router management messages
  */
@SerialVersionUID(1L)
final case class RoundRobinPool(
    override val nrOfInstances: Int,
    override val resizer: Option[Resizer] = None,
    override val supervisorStrategy: SupervisorStrategy =
      Pool.defaultSupervisorStrategy,
    override val routerDispatcher: String = Dispatchers.DefaultDispatcherId,
    override val usePoolDispatcher: Boolean = false)
    extends Pool
    with PoolOverrideUnsetConfig[RoundRobinPool] {

  def this(config: Config) =
    this(
      nrOfInstances = config.getInt("nr-of-instances"),
      resizer = Resizer.fromConfig(config),
      usePoolDispatcher = config.hasPath("pool-dispatcher"))

  /**
    * Java API
    * @param nr initial number of routees in the pool
    */
  def this(nr: Int) = this(nrOfInstances = nr)

  override def createRouter(system: ActorSystem): Router =
    new Router(RoundRobinRoutingLogic())

  override def nrOfInstances(sys: ActorSystem) = this.nrOfInstances

  /**
    * Setting the supervisor strategy to be used for the “head” Router actor.
    */
  def withSupervisorStrategy(strategy: SupervisorStrategy): RoundRobinPool =
    copy(supervisorStrategy = strategy)

  /**
    * Setting the resizer to be used.
    */
  def withResizer(resizer: Resizer): RoundRobinPool =
    copy(resizer = Some(resizer))

  /**
    * Setting the dispatcher to be used for the router head actor,  which handles
    * supervision, death watch and router management messages.
    */
  def withDispatcher(dispatcherId: String): RoundRobinPool =
    copy(routerDispatcher = dispatcherId)

  /**
    * Uses the resizer and/or the supervisor strategy of the given RouterConfig
    * if this RouterConfig doesn't have one, i.e. the resizer defined in code is used if
    * resizer was not defined in config.
    */
  override def withFallback(other: RouterConfig): RouterConfig =
    this.overrideUnsetConfig(other)
}

/**
  * A router group that uses round-robin to select a routee. For concurrent calls,
  * round robin is just a best effort.
  *
  * The configuration parameter trumps the constructor arguments. This means that
  * if you provide `paths` during instantiation they will be ignored if
  * the router is defined in the configuration file for the actor being used.
  *
  * @param paths string representation of the actor paths of the routees, messages are
  *   sent with [[akka.actor.ActorSelection]] to these paths
  *
  * @param routerDispatcher dispatcher to use for the router head actor, which handles
  *   router management messages
  */
@SerialVersionUID(1L)
final case class RoundRobinGroup(
    override val paths: immutable.Iterable[String],
    override val routerDispatcher: String = Dispatchers.DefaultDispatcherId)
    extends Group {

  def this(config: Config) =
    this(paths = immutableSeq(config.getStringList("routees.paths")))

  /**
    * Java API
    * @param routeePaths string representation of the actor paths of the routees, messages are
    *   sent with [[akka.actor.ActorSelection]] to these paths
    */
  def this(routeePaths: java.lang.Iterable[String]) =
    this(paths = immutableSeq(routeePaths))

  override def paths(system: ActorSystem): immutable.Iterable[String] =
    this.paths

  override def createRouter(system: ActorSystem): Router =
    new Router(RoundRobinRoutingLogic())

  /**
    * Setting the dispatcher to be used for the router head actor, which handles
    * router management messages
    */
  def withDispatcher(dispatcherId: String): RoundRobinGroup =
    copy(routerDispatcher = dispatcherId)
}
