package test.concurrent

import test.concurrent.TestProcesses.TestProcess

import scalaz.{Monoid}
import scalaz.syntax.monoid._
import scalaz.syntax.traverse._
import scalaz.std.list._

/**
 * Created by sdraper on 11/28/15.
 */
object SequentialCombinator extends Combinator {
  override def sum[A](subTasks: List[TestProcess[A]])(implicit m: Monoid[A]): TestProcess[A] = {
    subTasks.sequenceU map { l => sum(l)(m) }
  }
}
