package org.dmpp.amiga

import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}
import org.mahatma68k.AddressSpace

/**
 * A test for Copper functionality.
 */
class MockMemory extends AddressSpace {
  def start = 0
  def size  = 10000000
  def readByte(address: Int)  = 0
  def readShort(address: Int) = 0
  def readLong(address: Int)  = 0
  def writeByte(address: Int, value: Int) { }
  def writeShort(address: Int, value: Int) { }
  def writeLong(address: Int, value: Int) { }        
}

class CopperTest extends JUnit4(BlitterLogicSpec)
object CopperSpec extends Specification {
  val NoCyclesUsed = 0

  var copper: Copper = null
  "Copper" should {
    doBefore {
      copper = new Copper
      val mockMemory = new MockMemory
      copper.addressSpace = mockMemory
    }
    "have a valid initial state" in {
      copper.addressSpace must notBeNull
      copper.enabled must beFalse
      copper.waiting must beFalse
    }
    "do nothing when disabled" in {
      copper.enabled = false
      copper.doDma must_== NoCyclesUsed
    }
  }
}
