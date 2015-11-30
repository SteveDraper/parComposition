package test.concurrent

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import scala.concurrent.duration._
import test.concurrent.TestProcesses.{TestFault, TestTask, TestProcess}
import test.concurrent.TestProcesses._
import scala.concurrent.Await
import akka.pattern.ask

import scala.collection.mutable
import scalaz.concurrent.Task
import scalaz.{\/, Monoid}
import scalaz.syntax.either._
import scalaz.syntax.traverse._
import scalaz.std.list._
/**
 * Created by sdraper on 11/29/15.
 */
case class AkkaCombinator(numWorkers: Int) extends Combinator {
  class AkkaPool[A] {

    sealed trait CombinatorMessage
    case class Run(subTasks: List[TestProcess[A]]) extends CombinatorMessage
    case class Work(task: TestTask[A]) extends CombinatorMessage
    case class WorkResult(value: \/[TestFault, A]) extends CombinatorMessage
    case class Result(value: \/[TestFault, List[A]]) extends CombinatorMessage

    class Worker extends Actor {
      def receive = {
        case Work(task) => sender ! WorkResult(task.run)
      }
    }

    class Master extends Actor {
      val routees = for(n <- 1 to numWorkers) yield context.actorOf(Props(new Worker()), s"worker.$n")
      val workerRouter = context.actorOf(
        Props.empty.withRouter(RoundRobinRouter(routees)), name = "workerRouter")

      var results = mutable.MutableList[A]()
      var numOutstandingSubTasks = 0
      var originator: ActorRef = null

      def receive = {
        case Run(processes) => {
          originator = sender
          for (process <- processes) {
            numOutstandingSubTasks += 1
            workerRouter ! Work(process.run)
          }
        }
        case WorkResult(result) => {
          def handleError(f: TestFault) = {
            originator ! Result(f.left)
            context.stop(self)
          }
          def handleResult(a: A) = {
            results += a
            numOutstandingSubTasks -= 1
            if (numOutstandingSubTasks == 0) {
              originator ! Result(results.toList.right)
              context.stop(self)
            }
          }

          result fold(handleError, handleResult)
        }
      }
    }

    def runUnordered(subTasks: List[TestProcess[A]]) = {
      val system = ActorSystem("CombinatorSystem")  //  TODO - need to disambiguate by instance?
      val master = system.actorOf(Props(new Master()), name="master")
      implicit val timeout = Timeout(50 seconds)
      val resultFuture = master ? Run(subTasks)

      val result = Await.result(resultFuture, timeout.duration).asInstanceOf[Result]
      liftToProcess(Task.now(result.value))
    }
  }

  override def sum[A](subTasks: List[TestProcess[A]])(implicit m: Monoid[A]): TestProcess[A] = {
    val workerPool = new AkkaPool[A]

    workerPool.runUnordered(subTasks) map { l => sum(l)(m) }
  }
}
