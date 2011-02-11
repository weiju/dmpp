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

  val mockMemory = new MockMemory
  var copper: Copper = null

  "Copper" should {
    doBefore {
      copper = new Copper
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
    "will be in a safe state after a reset" in {
      copper.enabled = true
      copper.waiting = true
      copper.danger  = true
      copper.reset
      copper.enabled must beFalse
      copper.waiting must beFalse
      copper.danger  must beFalse
    }
    "will be ready to run copper list 1 after verticalBlank" in {
      // point to address 0x20000, which is in chip mem
      copper.cop1lcl.value = 0x0000
      copper.cop1lch.value = 0x0002
      copper.restartOnVerticalBlank

      copper.pc must_== 0x20000
      copper.waiting must beFalse
      copper.enabled must beFalse
    }
  }
}
