package chess

import Pos._

class ClockTest extends ChessTest {

  "play with a clock" should {
    val clock = Clock(5 * 60 * 1000, 0)
    val game = makeGame withClock clock.start
    "new game" in {
      game.clock map { _.color } must_== Some(White)
    }
    "one move played" in {
      game.playMoves(E2 -> E4) must beSuccess.like {
        case g: Game => g.clock map { _.color } must_== Some(Black)
      }
    }
  }
  "create a clock" should {
    "with time" in {
      Clock(60, 10).limitSeconds must_== 60
    }
    "with increment" in {
      Clock(60, 10).incrementSeconds must_== 10
    }
    "with few time" in {
      Clock(0, 10).limitSeconds must_== 0
    }
    "with 30 seconds" in {
      Clock(30, 0).limitInMinutes must_== 0.5
    }
  }
  "lag compensation" should {
    def durOf(lag: Float) = MoveMetrics(Some(Centis((100 * lag).toInt)))
    def clockStep(wait: Float, lag: Float): Double = {
      val clock = Clock(60, 0).start.step()
      // TODO: we should stub Clock::now instead of sleeping.
      Thread sleep ((wait + lag) * 1000).toInt
      (clock step durOf(lag) remainingTime Black) toSeconds
    }
    def clockStart(lag: Float): Double = {
      val clock = Clock(60, 0).start.step()
      (clock step durOf(lag) remainingTime White) toSeconds
    }
    val delta = 0.2
    val maxLag = ClockPlayer.maxLagComp.toSeconds
    "premove, no lag" in {
      clockStep(0, 0) must beCloseTo(60, delta)
    }
    "premove, small lag" in {
      clockStep(0, 0.2f) must beCloseTo(60, delta)
    }
    "premove, big lag" in {
      clockStep(0, 2f) must beCloseTo(59, delta)
    }
    "1s move, no lag" in {
      clockStep(1f, 0) must beCloseTo(59, delta)
    }
    "1s move, small lag" in {
      clockStep(1f, 0.2f) must beCloseTo(59, delta)
    }
    "1s move, big lag" in {
      clockStep(1f, 2f) must beCloseTo(58, delta)
    }
    "start, no lag" in {
      clockStart(0) must beCloseTo(60, delta)
    }
    "start, small lag" in {
      clockStart(0.2f) must beCloseTo(60, delta)
    }
    "start, big lag" in {
      clockStart(maxLag * 5) must beCloseTo(60, delta)
    }
  }
}
