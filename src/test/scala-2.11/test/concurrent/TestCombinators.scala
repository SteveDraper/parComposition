package test.concurrent

import org.scalatest.{Matchers, FlatSpec}

import scalaz.std.AllInstances._

trait CombinatorBehaviors extends Matchers { this: FlatSpec =>
  private def delayedInt(value: Int, delay: Int) = TestProcesses.toProcess( {
    Thread.sleep(delay)
    value
  })
  val range = 1 to 1000

  def nonErrorCheck(combinator: Combinator): Unit = {
    val processList = range map(n => delayedInt(n, (n%5)*20)) toList
    val correctResult = range.sum

    it should "sum correctly given non-faulting subTasks" in {
      val result = combinator.sum(processList).run.run fold(_=>false,(_==correctResult))

      result shouldBe true
    }
  }
  def errorCheck(combinator: Combinator): Unit = {
    def isError(n: Int) = (n > 5)
    val badProcessList = (range map ( x => x match {
      case n if (!isError(n)) => delayedInt(n, (n%5)*20)
      case n => TestProcesses.fault[Int](s"Failed on n=$n")
    })) toList

    it should "produce a fault given faulting subTasks" in {
      val result = combinator.sum(badProcessList).run.run fold(_=>true,_=>false)

      result shouldBe true
    }
  }
}

class TestCombinators extends FlatSpec with CombinatorBehaviors {
  val combinators = List[Combinator](
    SequentialCombinator,
    SimpleForkCombinator,
    NondeterminismCombinator,
    ParallelApplicativeCombinator,
    AkkaCombinator(50)
  )

  for(combinator <- combinators) {
    combinator.getClass.getSimpleName should behave like nonErrorCheck(combinator)

    it should behave like errorCheck(combinator)
  }
}
