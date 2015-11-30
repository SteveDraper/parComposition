package test.concurrent

import test.concurrent.TestProcesses.TestProcess

import scalaz.{\/, EitherT}
import scalaz.concurrent.Task
import scalaz.syntax.traverse._
import scalaz.std.list._

/**
 * Created by sdraper on 11/28/15.
 */
object TestProcesses {
  type TestFault = String
  type TestTask[A] = Task[\/[TestFault,A]]
  type TestProcess[A] = EitherT[Task,TestFault,A]

  def toProcess[A](a: => A): TestProcess[A] = EitherT.right[Task,TestFault,A](Task.delay(a))
  def fault[A](f: TestFault): TestProcess[A] = EitherT.left[Task,TestFault,A](Task.delay(f))
  implicit def liftToProcess[A](t: TestTask[A]): TestProcess[A] = EitherT[Task,TestFault,A](t)
}