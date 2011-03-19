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
import org.mahatma68k.AddressSpace

/**
 * The Copper class implements the Copper coprocessor of the Amiga system.
 * It is implemented as a DmaChannel to provide a more generic interface.
 */
object Copper {
  val NumMoveCycles    = 4
  val NumSkipCycles    = 4
  val NumWaitCycles    = 6
  val NumWakeupCycles  = 1
  val NumWaitingCycles = 0
}

class Copper extends DmaChannel {
  import Copper._

  case class CopperPosition(hp: Int, vp: Int, he: Int, ve: Int)

  private var cop1lc        = 0
  private var cop2lc        = 0
  private var ir1           = 0
  private var ir2           = 0
  private var _video: Video = null

  // wait status
  private var blitterFinishedDisable = false
  private var comparePos = 0
  private var compareMask = 0

  // public accessible state
  var addressSpace : AddressSpace = null
  var waiting : Boolean           = false
  var danger  : Boolean           = false
  var pc            = 0

  def video = _video
  def video_=(aVideo: Video) {
    _video = aVideo
    _video.copper = this
  }

  override def reset {
    super.reset
    waiting = false
    danger  = false
  }

  def restartOnVerticalBlank {
    pc      = cop1lc
    waiting = false
    enabled = false
  }
  
  private def positionReached: Boolean = {
    // merge the beam position into one word for fast comparison
    val currentPos =
      (((_video.vpos & 0xff) << 8) | (_video.hclocks & 0xff)) & compareMask
    currentPos >= comparePos
  }

  override def doDma: Int = {
    if (!enabled) return 0
    if (waiting && !positionReached) NumWaitingCycles
    else if (waiting && positionReached) {
      waiting = false
      NumWakeupCycles
    } else {
      fetchNextInstruction
      decideInstruction
    }
  }

  private def fetchNextInstruction {
    ir1 = addressSpace.readShort(pc)
    ir2 = addressSpace.readShort(pc + 2)
    pc += 4
  }

  private def decideInstruction: Int = {
    if ((ir1 & 0x01) == 0) move
    else skipOrWait
  }

  private def skipOrWait: Int = {
    // creating the compare mask works as following:
    // IR2 can only use bits 1-14 for masking, since Bit 0 is used to
    // differentiate between WAIT and skip and Bit 15 is the Blitter Finish Disable
    // Flag (BFD). The Copper can only see even positions anyways, but we set
    // Bit 15 in the comparison mask so it will not be on vertical positions
    // larger than 0x80
    compareMask = (ir2 & 0xffff) | 0x8000
    comparePos = ir1 & compareMask
    blitterFinishedDisable = (ir2 & 0x8000) == 0x8000
    val position = CopperPosition(hp = ir1 & 0xfe,
                                  vp = (ir1 >>> 8) & 0xff,
                                  he = ir2 & 0xfe,
                                  ve = (ir2 >>> 8) & 0x7f)

    if ((ir2 & 0x01) == 1) skip(position)
    else wait(position)
  }

  private def move: Int = {
    val address = (ir1 & 0x1fe) + 0xdff000
    printf("Copper: MOVE #$%02x, $%04x\n", ir2, address)
    addressSpace.writeShort(address, ir2)
    NumMoveCycles
  }

  private def skip(pos: CopperPosition): Int = {
    printf("Copper: SKIP HP=%d VP=%d HE=%d VE=%d BFD=%b\n",
           pos.hp, pos.vp, pos.he, pos.ve, blitterFinishedDisable)
    if (positionReached) pc += 4
    NumSkipCycles
  }

  private def wait(pos: CopperPosition): Int = {
    printf("Copper: WAIT HP=%d VP=%d HE=%d VE=%d BFD=%b\n",
           pos.hp, pos.vp, pos.he, pos.ve, blitterFinishedDisable)
    waiting = true
    NumWaitCycles
  }
  val COP1LCH = new CustomChipWriteRegister("COP1LCH") {
    def value_=(aValue: Int) {
      cop1lc = (cop1lc & 0x0000ffff) | (aValue << 16)
    }
  }
  val COP1LCL = new CustomChipWriteRegister("COP1LCL") {
    def value_=(aValue: Int) {
      cop1lc = (cop1lc & 0xffff0000) | aValue
      printf("COP1LC is now: %04x\n", cop1lc)
    }
  }

  val COP2LCH = new CustomChipWriteRegister("COP2LCH") {
    def value_=(aValue: Int) {
      cop2lc = (cop2lc & 0x0000ffff) | (aValue << 16)
    }
  }
  val COP2LCL = new CustomChipWriteRegister("COP2LCL") {
    def value_=(aValue: Int) {
      cop2lc = (cop2lc & 0xffff0000) | aValue
      printf("COP2LC is now: %04x\n", cop2lc)
    }
  }

  val COPJMP1 = new CustomChipStrobeRegister("COPJMP1") {
    def value_=(aValue: Int) {
      pc = cop1lc
      printf("Strobed COPJMP1, pc is now: %04x\n", pc)
    }
  }

  val COPJMP2 = new CustomChipStrobeRegister("COPJMP2") {
    def value_=(aValue: Int) { pc = cop2lc }
  }

  val COPCON = new CustomChipWriteRegister("COPCON") {
    /**
     * COPCON only has one supported bit: the DANGER bit.
     * we simply map that to a boolean
     */
    def value_=(aValue: Int) { danger = (aValue & 0x02) == 0x02 }
  }
  // We do not implement COPINS. It is not clear if it is ever used. For now,
  // we will throw an exception on access
  val COPINS = BogusRegister("COPINS")
}
