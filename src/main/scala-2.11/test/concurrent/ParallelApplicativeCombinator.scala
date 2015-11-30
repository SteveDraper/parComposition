package test.concurrent

import test.concurrent.TestProcesses._

import scalaz.{Nondeterminism, \/, Applicative, Monoid}
import scalaz.concurrent.Task
import scalaz.std.list._
import ParallelApplicative._

/**
 * Created by sdraper on 11/28/15.
 */
object ParallelApplicativeCombinator extends Combinator{
  override def sum[A](subTasks: List[TestProcess[A]])(implicit m: Monoid[A]): TestProcess[A] = {
    subTasks.sequenceP map { l => sum(l)(m) }
  }
}

object ParallelApplicative {
  implicit val T = new Applicative[TestProcess] {
    def point[A](a: => A) = toProcess(a)
    def ap[A,B](a: => TestProcess[A])(f: => TestProcess[A => B]): TestProcess[B] = apply2(f,a)(_(_))
    override def apply2[A,B,C](a: => TestProcess[A], b: => TestProcess[B])(f: (A,B) => C): TestProcess[C] = {
      def disjunctf(da: \/[TestFault,A], db: \/[TestFault,B]): \/[TestFault,C] = {
        for {
          a <- da
          b <- db
        } yield f(a,b)
      }

      liftToProcess(Nondeterminism[Task].mapBoth(Task.fork(a.run),Task.fork(b.run))(disjunctf))
    }
  }

  case class ParallelApplicativeOps[A](l: List[TestProcess[A]]) {
    def sequenceP = T.sequence(l)
  }

  implicit def toParallelApplicativeOps[A](l: List[TestProcess[A]]): ParallelApplicativeOps[A] = ParallelApplicativeOps(l)
}
