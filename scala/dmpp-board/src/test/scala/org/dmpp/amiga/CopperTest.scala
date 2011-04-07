/**
 * Created on November 23, 2009
 * Copyright (c) 2009-2011, Wei-ju Wu
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of Wei-ju Wu nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY WEI-JU WU ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL WEI-JU WU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dmpp.amiga

import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.dmpp.common._

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

  def reset {
    writeLog = Nil
  }
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

class MockVideo extends Video(NTSC) {
}

/**
 * A test for Copper functionality.
 */
@RunWith(classOf[JUnitRunner])
class CopperSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  // defined here to avoid scope conflicts
  class MockChipBus extends Bus {
    def requestMemory(device: BusDevice, address: Int, numCycles: Int) = true
  }

  val NoCyclesUsed = 0

  val mockMemory = new CopperListMemory
  val mockVideo: MockVideo = new MockVideo
  val copper: Copper = new Copper(new MockChipBus)
  copper.addressSpace = mockMemory
  copper.video = mockVideo

  override def beforeEach {
    mockMemory.reset
  }

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
    copper.notifyVerticalBlank

    copper.pc should be (0x20000)
  }
  it should "jump to copper list 1 after COPJMP1 is written" in {
    copper.COP1LCL.value = 0x0000
    copper.COP1LCH.value = 0x0002
    copper.COPJMP1.value = 0x1234 // write anything to the strobe

    copper.pc should be (0x20000)
  }
  it should "jump to copper list 2 after COPJMP2 is written" in {
    copper.COP2LCL.value = 0x0000
    copper.COP2LCH.value = 0x0003
    copper.COPJMP2.value = 0x1234 // write anything to the strobe

    copper.pc should be (0x30000)
  }
  it should "execute a move instruction" in {
    // first copper instruction in the HRM: a move of #$02 into
    // $dff0e0 (BPL1PTH)
    addCopperListAndRestart(CopperList(0x20000, List(0x00e0, 0x0002)))
    copper.doDma               should be (Copper.NumMoveCycles)
    mockMemory.writeLog.length should be (1)
    mockMemory.writeLog(0)     should be ("#2.w -> $dff0e0")
  }
  it should "fail when moving to a protected location" in {
    // move #$02, $40
    addCopperListAndRestart(CopperList(0x20000, List(0x0040, 0x0002)))
    evaluating { copper.doDma } should produce [IllegalCopperAccessException]
    // move #$02, $7e
    addCopperListAndRestart(CopperList(0x20000, List(0x007e, 0x0002)))
    evaluating { copper.doDma } should produce [IllegalCopperAccessException]
  }
  it should "not fail when moving to a protected location and danger bit is set" in {
    copper.COPCON.value = 2 // set danger bit
    // move #$02, $40
    addCopperListAndRestart(CopperList(0x20000, List(0x0040, 0x0002)))
    copper.doDma               should be (Copper.NumMoveCycles)
    mockMemory.writeLog.length should be (1)
    mockMemory.writeLog(0)     should be ("#2.w -> $dff040")
  }
  it should "not fail when moving to an illegal location even if danger bit is set" in {
    copper.COPCON.value = 2 // set danger bit
    // move #$02, $38
    addCopperListAndRestart(CopperList(0x20000, List(0x0038, 0x0002)))
    evaluating { copper.doDma } should produce [IllegalCopperAccessException]
  }

  it should "execute a wait instruction" in {
    // another copper instruction in the HRM: wait for line 150
    addCopperListAndRestart(CopperList(0x20000, List(0x9601, 0xff00)))
    copper.doDma               should be (Copper.NumWaitCycles)
    mockMemory.writeLog.length should be (0)
    copper.waiting             should be (true)

    // subsequent dma cycles should do nothing
    copper.doDma               should be (Copper.NumWaitingCycles)

    mockVideo.videoBeam.vpos = 150
    copper.doDma               should be (Copper.NumWakeupCycles)
  }

  it should "execute a skip instruction unsuccessfully" in {
    addCopperListAndRestart(CopperList(0x20000,
                                       List(0x9601, 0xff01,   // skip v = 150
                                            0x0038, 0x0002,
                                            0x0042, 0x0004)))
    copper.doDma               should be (Copper.NumSkipCycles)
    copper.pc                  should equal (0x20004)
  }

  it should "execute a skip instruction successfully" in {
    addCopperListAndRestart(CopperList(0x20000,
                                       List(0x9601, 0xff01,   // skip v = 150
                                            0x0038, 0x0002,
                                            0x0042, 0x0004)))
    mockVideo.videoBeam.vpos = 151
    copper.doDma               should be (Copper.NumSkipCycles)
    copper.pc                  should equal (0x20008)
  }

  // TODO: What if a wait instruction is executed when the position is already
  // reached ??? should we be waiting or simply continue ?
  private def addCopperListAndRestart(copperList: CopperList) {
    mockMemory.addCopperList(copperList)
    copper.COP1LCL.value = 0x0000
    copper.COP1LCH.value = 0x0002
    copper.notifyVerticalBlank
    copper.enabled = true
  }
}
