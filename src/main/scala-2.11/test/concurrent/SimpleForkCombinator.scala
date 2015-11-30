package test.concurrent

import test.concurrent.TestProcesses.{TestFault, TestProcess}

import scalaz.{\/, EitherT, Monoid}
import scalaz.concurrent.Task
import scalaz.syntax.traverse._
import scalaz.std.list._

/**
 * Created by sdraper on 11/28/15.
 */
object SimpleForkCombinator extends Combinator {
  override def sum[A](subTasks: List[TestProcess[A]])(implicit m: Monoid[A]): TestProcess[A] = {
    def conditionalSum(results: List[\/[TestFault,A]]): \/[TestFault,A] = {
      results.sequenceU map(sum(_)(m))
    }
    def runForked(subTask: TestProcess[A]): TestProcess[A] = {
      EitherT[Task,TestFault,A](Task.fork(subTask.run))
    }
    val forkedSubTasks = subTasks map (runForked(_))

    EitherT[Task,TestFault,A](Task.gatherUnordered(forkedSubTasks map(_.run)) map conditionalSum)
  }
}
