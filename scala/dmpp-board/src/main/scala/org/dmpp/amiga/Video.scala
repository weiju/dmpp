/**
 * Created on November 5, 2009
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

// This module captures one of the most important concepts in an Amiga
// emulation: Timing.
// Everything is based on a display screen which is either using NTSC
// or PAL format.
// As a reminder:
// - PAL has 312 (+ 313 = 625 interlace) scan lines/screen at 50 Hz
// - NTSC has 262 (+ 263 = 525 interlaced) scan lines/screen at 60 Hz
//
// Amiga timing is based on non-interlaced, lo-res mode
// (384 visible pixels horizontally in overscan mode). Note that
// Overscan mode displays a maximum of 384x280 (or 384x240) visible pixels
// in low resolution
// - on PAL, the lines from 0 to (24 + DIWSTRT[VSTART]) are the vertical
//   blanking area, meaning that the range below is 25 to 311 (286 lines)
//   => overshoot of 6 scan lines
// - on NTSC it is 0 to (19 + DIWSTRT[VSTART]), meaning that the visible
//   range on NTSC is 20 to 261 (241 lines)
//   => overshoot of 1 scan line
trait VideoStandard {
  // 455 clock cycles per scanline, both on PAL and NTSC
  // DMA clock cycles are half of that and only reach until 228 ($E4),
  // but it's only a matter of a right shift to convert it
  // VHPOSR's values are measured in DMA clocks, so they will fit in
  // the 8 bit reserved for it
  // The copper can see beam positions $00 to $E2 (0-226), which are available in both
  // PAL/NTSC, for a total of 227 positions. In addition, the Copper can only see even
  // positions. Each copper beam position unit equals 2 lores/4 hires pixels,
  // meaning a display has 227.5 * 2 = 455 lores pixels = 910 hires pixels
  // Horizontal blanking is from 0x0f (pixel 30) to 0x35 (pixel 106)
  val CpuCyclesPerScanline = 455 // = 227.5 color clocks

  def VbStart                   : Int
  def VbStop                    : Int
  def LinesTotal                : Int
  def DisplayableLines          : Int
  def MinVStart                 : Int
  def LinesTotalInterlace       : Int
  def DisplayableLinesInterlace : Int
}

object NTSC extends VideoStandard {
  val VbStart          = 0
  val VbStop           = 21
  val LinesTotal       = 262
  val DisplayableLines = 241 // LinesTotal - |VbStop - VbStart|
  val MinVStart        = 20
  val LinesTotalInterlace       = 524
  val DisplayableLinesInterlace = 483
}
object PAL extends VideoStandard {
  val VbStart          = 0
  val VbStop           = 29
  val LinesTotal       = 312 
  val DisplayableLines = 283 // LinesTotal - |VbStop - VbStart|
  val MinVStart        = 25
  val LinesTotalInterlace       = 625
  val DisplayableLinesInterlace = 567
}

class VideoBeam(videoStandard: VideoStandard,
                notifyVerticalBlank: () => Unit) {
  var hpos           = 0
  var vpos           = 0

  // This funny expression ensures that even line return 227 and odd lines
  // return 228 clocks
  def hclocks = (hpos >>> 1) + (vpos & 0x000000001)

  def advance(pixels: Int) {
    hpos += pixels
    if (hpos > videoStandard.CpuCyclesPerScanline) {
      vpos += 1
      hpos -= videoStandard.CpuCyclesPerScanline
      if (vpos >= videoStandard.LinesTotal) {
        vpos = 0
        notifyVerticalBlank() // note: the () must be added to call the method
      }
    }
  }
}

/*
 * Timing is absolutely crucial for a faithful Amiga emulation.
 * This is a class to capture the essence of Amiga timing:
 * Everything is tied to video beam. One color clock corresponds
 * to a clock cycle (two CPU cycles). What this class does is to count all
 * CPU cycles in the system and update the timing relevant variables
 * accordingly.
 * Timining-dependent components should query the VM-global instance of this
 * class to synchronize their work.
 * Increments usually come from the CPU, but could either come from DMA,
 * Copper or Blitter in case the CPU is locked out.
 *
 * Note: hpos and vpos measure the range of the PAL/NTSC range which is
 * from 0 to (455 - 1) (= 227.5 * 2) horizontally
 * and from 0 to (312 - 1) (PAL) or 262 (NTSC) vertically
 */
class Video(val videoStandard: VideoStandard,
            interruptController: InterruptController) {

  val videoBeam = new VideoBeam(videoStandard, notifyVerticalBlank _)
  def hpos      = videoBeam.hpos
  def vpos      = videoBeam.vpos
  def hclocks   = videoBeam.hclocks

  // color registers what we actually do here is to store a 24bit RGB
  // value here
  val color = new Array[Int](32)

  // playfield registers
  var ddfstrt  = 0
  var ddfstop  = 0
  var diwstrt  = 0
  var diwstop  = 0
  var bplcon0  = 0
  var bplcon1  = 0
  var bplcon2  = 0
  var bplcon3  = 0 // ECS register TODO
  var bpl1mod  = 0
  var bpl2mod  = 0

  // BPLCON0 derived values
  def hiresMode              = (bplcon0 & 0x8000) == 0x8000
  def holdAndModifyMode      = (bplcon0 & 0x800)  == 0x800
  def dualPlayfieldMode      = (bplcon0 & 0x400)  == 0x400
  def compositeColorEnable   = (bplcon0 & 0x200)  == 0x200
  def interlaceMode          = (bplcon0 & 0x04)   == 0x04
  def bitplaneUseCode        = (bplcon0 >> 12) & 0x07

  // BPLCON1 derived values
  def playfield2HScrollCode  = (bplcon1 >> 4) & 0x0f
  def playfield1HScrollCode  = bplcon1 & 0x0f

  // BPLCON2 derived values
  def playfield2Priority     = (bplcon2 & 0x40) == 0x40
  def playfield2PriorityCode = (bplcon2 >> 3) & 0x07
  def playfield1PriorityCode = bplcon2 & 0x07

  var copper: Copper = null

  def doCycles(numCycles: Int) = {
    videoBeam.advance(numCycles)
  }
  private def notifyVerticalBlank {
    println("VERTICAL BLANK !!!!");
    if (interruptController != null) interruptController.INTREQ.value = 1 << 5
    if (copper != null) copper.restartOnVerticalBlank
  }

  // registers
  val DIWSTRT = new CustomChipWriteRegister("DIWSTRT") {
    def value_=(aValue: Int) { diwstrt = aValue }
  }
  val DIWSTOP = new CustomChipWriteRegister("DIWSTOP") {
    def value_=(aValue: Int) { diwstop = aValue }
  }
  val DDFSTRT = new CustomChipWriteRegister("DDFSTRT") {
    def value_=(aValue: Int) { ddfstrt = aValue }
  }
  val DDFSTOP = new CustomChipWriteRegister("DDFSTOP") {
    def value_=(aValue: Int) { ddfstop = aValue }
  }
  val BPLCON0 = new CustomChipWriteRegister("BPLCON0") {
    def value_=(aValue: Int) { bplcon0 = aValue }
  }
  val BPLCON1 = new CustomChipWriteRegister("BPLCON1") {
    def value_=(aValue: Int) { bplcon1 = aValue }
  }
  val BPLCON2 = new CustomChipWriteRegister("BPLCON2") {
    def value_=(aValue: Int) { bplcon2 = aValue }
  }
  val BPLCON3 = new CustomChipWriteRegister("BPLCON3") {
    def value_=(aValue: Int) { bplcon3 = aValue }
  }
  val BPL1MOD = new CustomChipWriteRegister("BPL1MOD") {
    def value_=(aValue: Int) { bpl1mod = aValue }
  }
  val BPL2MOD = new CustomChipWriteRegister("BPL2MOD") {
    def value_=(aValue: Int) { bpl2mod = aValue }
  }
  val VPOSR = new CustomChipReadRegister("VPOSR") {
    def value = {
      val result = (vpos >> 8) & 0x01
      printf("VPOSR = %02x\n", result)
      result
    }
  }
  val VHPOSR = new CustomChipReadRegister("VHPOSR") {
    def value = {
      val result = ((vpos & 0xff) << 8) | hclocks
      printf("VHPOSR = %02x (vpos = %d, hpos = %d)\n", result, vpos,
             hclocks)
      result
    }
  }
}
