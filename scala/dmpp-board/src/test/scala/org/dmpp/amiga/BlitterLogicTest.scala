package org.dmpp.amiga

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/*
 * This test tests the principal arrangement of the Blitter logic functions.
 * Since the code is generated, we do not need to test this much more
 */
@RunWith(classOf[JUnitRunner])
class BlitterLogicSpec extends FlatSpec with Matchers {
  "org.dmpp.amiga.BlitterLogic" should "combine logic" in {
    BlitterLogic.lf_0x00(0x01, 0x02, 0x03)       should be (0)
    BlitterLogic.lf_0x03(0xf0f0, 0x0ff0, 0x1234) should be (0x000f)
    BlitterLogic.lf_0xff(0x01, 0x02, 0x03)       should be (0xffff)
  }
}
