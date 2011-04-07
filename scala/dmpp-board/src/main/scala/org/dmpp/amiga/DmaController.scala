/**
 * Created on November 25, 2009
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

import org.dmpp.common._

trait DmaChannel {
  def enabled: Boolean
  def enabled_=(flag: Boolean): Unit
  def reset: Unit
  def doDma: Int
}
class AbstractDmaChannel extends DmaChannel {
  var enabled = false

  /**
   * Perform DMA on this channel.
   * @return number of DMA cycles used
   */
  def doDma: Int = 0
  def reset {
    enabled = false
  }
}

class DmaController extends Bus {
  // Amiga DMA classification
  // DMA classes ordered by priority
  // disk comes first -> 3 cycles per scanline
  val disk     = new AbstractDmaChannel
  // audio comes next -> 4 cycles per scanline
  val audio3   = new AbstractDmaChannel
  val audio2   = new AbstractDmaChannel
  val audio1   = new AbstractDmaChannel
  val audio0   = new AbstractDmaChannel
  // sprite dma -> 16 cycles per scanline
  val sprite   = new AbstractDmaChannel
  // bitplane dma -> 80 + x cycles per scanline, depending on video setting
  val bitplane = new AbstractDmaChannel
  // these ones get the rest
  val blitter  = new AbstractDmaChannel
  val cpu      = new AbstractDmaChannel

  var blitterPriority                = false
  var masterEnable                   = false
  private var _amiga       : Amiga   = null // to advance the system clock
  private var copper       : Copper  = null
  private var _dmaChannels : Array[DmaChannel] = null
  
  def amiga = _amiga
  def amiga_=(anAmiga: Amiga) {
    _amiga = anAmiga
    copper = anAmiga.copper

    // DMA classes ordered by priority
    _dmaChannels = Array(audio0, audio1, audio2, audio3,
                         disk, sprite, blitter, copper, bitplane)
  }

  
  def doDmaWithStolenCpuCycles = {
    // Perform DMA until the CPU can get it
    var cycles = 0
    if (masterEnable) {
      // do Copper
      var copperCycles = 0
      do {
        copperCycles = amiga.copper.doDma
        cycles += copperCycles
      } while (copperCycles > 0)
    }
    // Make sure the rest of the system advances in time (except the CPU)
    amiga.doCycles(cycles)
    cycles
  }
  
  def doDmaWithCpu(numCycles: Int) {
    // EMPTY FOR NOW
  }
  
  // expose the control register here
  val DMACON = new CustomChipWriteRegister("DMACON") {
    def value_=(aValue: Int) {
      var mask = 0
      for (i <- 0 to 8) {
        mask = 1 << i
        _dmaChannels(i).enabled = (aValue & mask) == mask
      }
      masterEnable = ((aValue & 0x200) == 0x200)
      println("DMACON:" + toString)
    }
    override def toString = DmaController.this.toString
  }
  val DMACONR = new CustomChipReadRegister("DMACONR") {
    def value = {
      var result = 0
      for (i <- 0 to 8) {
        if (_dmaChannels(i).enabled) result |= (1 << i)
      }
      if (masterEnable) result |= 0x200
      result
    }
    override def toString = DmaController.this.toString
  }

  override def toString() = {
    "master=%b au3=%b au2=%b au1=%b au0=%b dsk=%b spr=%b blt=%b cop=%b " +
    "bpl=%b".format(
      masterEnable, audio3.enabled, audio2.enabled,
      audio1.enabled, audio0.enabled, disk.enabled,
      sprite.enabled, blitter.enabled,
      copper.enabled, bitplane.enabled)
  }

  // Chip bus interface
  def requestMemory(device: BusDevice, address: Int, numCycles: Int) = {
    true
  }
}
