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

object Video {
  val NumLines_PAL   = 312
  val MinVStart_PAL  =  25
  val NumLines_NTSC  = 262
  val MinVStart_NTSC =  20

  // 455 clock cycles per scanline, both on PAL and NTSC
  // DMA clock cycles are half of that and only reach until 228 ($E4),
  // but it's only a matter of a right shift to convert it
  // VHPOSR's values are measured in DMA clocks, so they will fit in
  // the 8 bit reserved for it
  val CpuCyclesPerScanline = 455
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
class Video(interruptController: InterruptController) {
  import Video._

  var hpos = 0
  var vpos = 0
  var minVStart = MinVStart_NTSC
  var numTotalScanLines = NumLines_NTSC

  // This funny expression ensures that even line return 227 and odd lines
  // return 228 clocks
  def hclocks = (hpos >>> 1) + (vpos & 0x000000001)

  // Display mode
  var hiresMode            = false
  var bitplaneUseCode      = 0
  var holdAndModifyMode    = false
  var doublePlayfieldMode  = false
  var compositeColorEnable = false
  // might not belong here, but is in BPLCON0, so for now, we are fine
  // we do not support genlocks anyways
  var genlockAudioEnable   = false
  var lightpenEnable       = false
  var interlaceMode        = false
  var externalResync       = false
  // playfield settings
  var playfield1HScrollCode  = 0
  var playfield2HScrollCode  = 0
  var playfield2Priority     = false
  var playfield1PriorityCode = 0
  var playfield2PriorityCode = 0
  var bpl1Mod = 0
  var bpl2Mod = 0
  
  var copper: Copper = null

  def doCycles(numCycles: Int) = {
    hpos += numCycles
    if (hpos > CpuCyclesPerScanline) {
      vpos += 1
      if (vpos == minVStart) { // should be diwstart
        // no vertical blank
      } else if (vpos == numTotalScanLines) {
        // vertical blank
        vpos = 0
        interruptController.intreq.value = 1 << 5
        copper.restartOnVerticalBlank
        System.out.println("VERTICAL BLANK !!!!");
      }
      hpos = 0
    }
  }

  def bplcon0 = 0
  def bplcon0_=(value : Int) {
    hiresMode            = (value & 0x8000) == 0x8000
    bitplaneUseCode      = (value >> 12) & 0x07
    holdAndModifyMode    = (value & 0x800) == 0x800
    doublePlayfieldMode  = (value & 0x400) == 0x400
    compositeColorEnable = (value & 0x200) == 0x200
    genlockAudioEnable   = (value & 0x100) == 0x100
    lightpenEnable       = (value & 0x08)  == 0x08
    interlaceMode        = (value & 0x04)  == 0x04
    externalResync       = (value & 0x02)  == 0x02
    printf("[BPLCON0: HIRES = %s BPU = %d, HAM = %s, DPF = %s, COMP = %s, " +
         "GAUD = %s, LPEN = %s, LACE = %s, ERSY = %s]\n", bool2Str(hiresMode),
         bitplaneUseCode, bool2Str(holdAndModifyMode),
         bool2Str(doublePlayfieldMode), bool2Str(compositeColorEnable),
         bool2Str(genlockAudioEnable), bool2Str(lightpenEnable),
         bool2Str(interlaceMode), bool2Str(externalResync))
  }

  def bplcon1 = 0
  def bplcon1_=(value : Int) {
    playfield2HScrollCode = (value >> 4) & 0x0f
    playfield1HScrollCode = value & 0x0f
    printf("[BPLCON1: PF1H = %02x, PF2H = %02x]\n", playfield1HScrollCode,
           playfield2HScrollCode)
  }
  
  def bplcon2 = 0
  def bplcon2_=(value : Int) {
    playfield2Priority = (value & 0x40) == 0x40
    playfield2PriorityCode = (value >> 3) & 0x07
    playfield1PriorityCode = value & 0x07
    printf("[BPLCON2: PF2_PRIO = %s, PF1P = %d, PF2P = %d]\n",
           bool2Str(playfield2Priority), playfield1PriorityCode,
           playfield2PriorityCode)
  }

  def bplcon3  = 0
  def bplcon3_=(value: Int)  = {
    // TODO: This is a new ECS register, ignore it for now
    printf("[TODO] Setting BPLCON3 to value: %02x\n", value)
  }

  def diwstrt = 0
  def diwstrt_=(value: Int) {
    val vertical = (value >>> 8) & 0xff
    val horizontal = value & 0xff
    // TODO assign to something
    printf("[DIWSTRT: VERT = %d HORZ = %d]\n", vertical, horizontal)
  }

  def diwstop = 0
  def diwstop_=(value: Int) {
    val vertical = (value >>> 8) & 0xff
    val horizontal = value & 0xff
    // TODO assign to something
    printf("[DIWSTOP: VERT = %d HORZ = %d]\n", vertical, horizontal)
  }

  def ddfstrt = 0
  def ddfstrt_=(value: Int) {
    val horizontal = value & 0xfc
    // TODO assign to something
    printf("[DDFSTRT: HORZ = %d]\n", horizontal)
  }

  def ddfstop = 0
  def ddfstop_=(value: Int) {
    val horizontal = value & 0xfc
    // TODO assign to something
    printf("[DDFSTOP: HORZ = %d]\n", horizontal)
  }

  private def bool2Str(flag: Boolean) = if (flag) "on" else "off"
}
