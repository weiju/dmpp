package org.dmpp.amiga

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

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

class MockVideo extends Video(NTSC, null) {
}

/**
 * A test for Copper functionality.
 */
@RunWith(classOf[JUnitRunner])
class CopperSpec extends FlatSpec with ShouldMatchers {

  val NoCyclesUsed = 0

  val mockMemory = new CopperListMemory
  val mockVideo: MockVideo = new MockVideo
  val copper: Copper = new Copper
  copper.addressSpace = mockMemory
  copper.video = mockVideo

  "Copper" should "have a valid initial state" in {
    copper.addressSpace should not be (null)
    copper.enabled      should be (false)
    copper.waiting      should be (false)
  }
  it should "do nothing when disabled" in {
    copper.enabled = false
    copper.doDma should equal (NoCyclesUsed)
  }
  it should "be in a safe state after a reset" in {
    copper.enabled = true
    copper.waiting = true
    copper.danger  = true
    copper.reset
    copper.enabled should be (false)
    copper.waiting should be (false)
    copper.danger  should be (false)
  }
  it should "be ready to run copper list 1 after verticalBlank" in {
    // point to address 0x20000, which is in chip mem
    copper.COP1LCL.value = 0x0000
    copper.COP1LCH.value = 0x0002
    copper.restartOnVerticalBlank

    copper.pc should be (0x20000)
    copper.waiting should be (false)
    copper.enabled should be (false)
  }
  it should "execute a move instruction" in {
    // first copper instruction in the HRM: a move of #$02 into
    // $dff0e0 (BPL1PTH)
    val copperList = CopperList(0x20000, List(0x00e0, 0x0002))
    addCopperListAndRestart(copperList)

    copper.doDma               should be (Copper.NumMoveCycles)
    mockMemory.writeLog.length should be (1)
    mockMemory.writeLog(0)     should be ("#2.w -> $dff0e0")
  }

  it should "execute a wait instruction" in {
    // another copper instruction in the HRM: wait for line 150
    val copperList = CopperList(0x20000, List(0x9601, 0xff00))
    addCopperListAndRestart(copperList)

    copper.doDma               should be (Copper.NumWaitCycles)
    mockMemory.writeLog.length should be (0)
    copper.waiting             should be (true)

    // subsequent dma cycles should do nothing
    copper.doDma               should be (Copper.NumWaitingCycles)

    mockVideo.videoBeam.vpos = 150
    copper.doDma               should be (Copper.NumWakeupCycles)
  }

  // TODO: What if a wait instruction is executed when the position is already
  // reached ??? should we be waiting or simply continue ?
  private def addCopperListAndRestart(copperList: CopperList) {
    mockMemory.addCopperList(copperList)
    copper.COP1LCL.value = 0x0000
    copper.COP1LCH.value = 0x0002
    copper.restartOnVerticalBlank
    copper.enabled = true
  }
}
