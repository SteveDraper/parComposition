package test.concurrent

import test.concurrent.TestProcesses._

import scalaz.{Traverse, Monoid}

/**
 * Created by sdraper on 11/28/15.
 */
trait Combinator {
  protected  def sum[A](values: List[A])(implicit m: Monoid[A]) = values.foldLeft(m.zero)((x,y)=> m.append(x,y))
  def sum[A](subTasks: List[TestProcess[A]])(implicit m: Monoid[A]): TestProcess[A]
}
