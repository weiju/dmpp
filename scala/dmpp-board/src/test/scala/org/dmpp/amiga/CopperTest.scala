package org.dmpp.amiga

import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}
import org.mahatma68k.AddressSpace

case class CopperList(address: Int, words: List[Int]) {
  def contains(anAddress: Int) = {
    anAddress >= address && anAddress < address + words.length * 2
  }
  def readShort(anAddress: Int) = words((anAddress - address) / 2)
}

/**
 * A custom mock memory class which simulates mockup copper lists
 * and logs writes that the copper performs.
 */
class CopperListMemory extends AddressSpace {

  private var copperLists: List[CopperList] = Nil
  var writeLog : List[String] = Nil

  def start = 0
  def size  = 10000000
  def readByte(address: Int)  = 0
  def readShort(address: Int): Int = {
    for (copperList <- copperLists) {
      if (copperList.contains(address)) return copperList.readShort(address)
    }
    0
  }
  def readLong(address: Int)  = 0
  def writeByte(address: Int, value: Int) { }
  def writeShort(address: Int, value: Int) {
    addLog("#%d.w -> $%04x".format(value, address))
  }
  private def addLog(log: String) = {
    writeLog ::= log
    writeLog reverse
  }
  def writeLong(address: Int, value: Int) { }

  def addCopperList(copperList: CopperList) {
    copperLists ::= copperList
  }
}

/**
 * A test for Copper functionality.
 */
class CopperTest extends JUnit4(BlitterLogicSpec)
object CopperSpec extends Specification {
  val NoCyclesUsed = 0

  val mockMemory = new CopperListMemory
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
    "will execute a move instruction" in {
      // first copper instruction in the HRM: a move of #$02 into
      // $dff0e0 (BPL1PTH)
      val copperList = CopperList(0x20000, List(0x00e0, 0x0002))
      addCopperListAndRestart(copperList)

      copper.doDma must_== Copper.NumMoveCycles
      mockMemory.writeLog.length must_== 1
      mockMemory.writeLog(0) must_== "#2.w -> $dff0e0"
    }
  }

  private def addCopperListAndRestart(copperList: CopperList) {
    mockMemory.addCopperList(copperList)
    copper.cop1lcl.value = 0x0000
    copper.cop1lch.value = 0x0002
    copper.restartOnVerticalBlank
    copper.enabled = true
  }
}
