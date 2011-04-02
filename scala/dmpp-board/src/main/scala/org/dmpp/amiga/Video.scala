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

import org.dmpp.cpu._

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

/**
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
 * @constructor creates a VideoBeam object
 * @param videoStandard PAL or NTSC
 * @param notifyVerticalBlank a method to be called on the start of vertical
 *        blanking
 */
class VideoBeam(videoStandard: VideoStandard, notifyVerticalBlank: () => Unit) {

  var hpos           = 0
  var vpos           = 0

  /**
   * This funny expression ensures that even line return 227 and odd lines
   * return 228 clocks.
   * @return the current horizontal position in color clocks 
   */
  def hclocks = (hpos >>> 1) + (vpos & 0x000000001)

  /**
   * Advance the video beam by a certain number of Lores pixels.
   * @param number of Lores pixels to advance
   */
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

/**
 * The Video object acts as a an interface component for the PlayfieldSystem,
 * the SpriteSystem and the VideoBeam.
 * @constructor creates a new Video object
 * @param videoStandard either PAL or NTSC
 * @param interruptController a reference to the interrupt controller
 */
class Video(val videoStandard: VideoStandard,
            interruptController: InterruptController)
extends ClockedDevice {

  /**
   * Color register.
   * @constructor creates a color register
   * @param n color register number
   */
  class ColorRegister(n: Int) extends CustomChipWriteRegister("COLOR%02d".format(n)) {
    def value_=(value: Int) {
      color(n) = value
    }
  }

  val videoBeam = new VideoBeam(videoStandard, notifyVerticalBlank _)
  val playfield = new PlayfieldSystem(this)
  var copper: Copper = null

  // Color registers are shared between sprites and playfields, so they are
  // placed in the Video object
  val color = new Array[Int](32)

  // **********************************************************************
  // ***** Registers
  // **********************************************************************
  val DIWSTRT = new CustomChipWriteRegister("DIWSTRT") {
    def value_=(aValue: Int) { playfield.diwstrt = aValue }
  }
  val DIWSTOP = new CustomChipWriteRegister("DIWSTOP") {
    def value_=(aValue: Int) { playfield.diwstop = aValue }
  }
  val DDFSTRT = new CustomChipWriteRegister("DDFSTRT") {
    def value_=(aValue: Int) { playfield.ddfstrt = aValue }
  }
  val DDFSTOP = new CustomChipWriteRegister("DDFSTOP") {
    def value_=(aValue: Int) { playfield.ddfstop = aValue }
  }
  val BPLCON0 = new CustomChipWriteRegister("BPLCON0") {
    def value_=(aValue: Int) { playfield.bplcon0 = aValue }
  }
  val BPLCON1 = new CustomChipWriteRegister("BPLCON1") {
    def value_=(aValue: Int) { playfield.bplcon1 = aValue }
  }
  val BPLCON2 = new CustomChipWriteRegister("BPLCON2") {
    def value_=(aValue: Int) { playfield.bplcon2 = aValue }
  }
  val BPLCON3 = new CustomChipWriteRegister("BPLCON3") {
    def value_=(aValue: Int) { playfield.bplcon3 = aValue }
  }
  val BPL1MOD = new CustomChipWriteRegister("BPL1MOD") {
    def value_=(aValue: Int) { playfield.bpl1mod = aValue }
  }
  val BPL2MOD = new CustomChipWriteRegister("BPL2MOD") {
    def value_=(aValue: Int) { playfield.bpl2mod = aValue }
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
  val COLOR = new Array[ColorRegister](32)
  for (i <- 0 until 32) COLOR(i) = new ColorRegister(i)

  // **********************************************************************
  // ***** Methods
  // **********************************************************************
  def hpos      = videoBeam.hpos
  def vpos      = videoBeam.vpos
  def hclocks   = videoBeam.hclocks

  def receiveTicks(numTicks: Int) = videoBeam.advance(numTicks)

  /**
   * This method is called by the video beam on each start of the vertical
   * blanking phase.
   */
  private def notifyVerticalBlank {
    println("VERTICAL BLANK !!!!");
    if (interruptController != null) interruptController.INTREQ.value = 1 << 5
    if (copper != null) copper.restartOnVerticalBlank
  }
}
