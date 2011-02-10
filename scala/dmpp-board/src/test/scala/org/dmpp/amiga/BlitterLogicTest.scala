package org.dmpp.amiga

import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

/*
 * This test tests the principal arrangement of the Blitter logic functions.
 * Since the code is generated, we do not need to test this much more
 */
class BlitterLogicTest extends JUnit4(BlitterLogicSpec)
object BlitterLogicSpec extends Specification {
  "org.dmpp.amiga.BlitterLogic" should {
    "combine logic" in {
      BlitterLogic.lf_0x00(0x01, 0x02, 0x03)   must_== 0
      BlitterLogic.lf_0x03(0xf0f0, 0x0ff0, 0x1234) must_== 0x000f
      BlitterLogic.lf_0xff(0x01, 0x02, 0x03) must_== 0xffff
    }
  }
}
