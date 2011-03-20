/**
 * Created on March 19, 2011
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

object PlayfieldConstants {
  val DIWSTRT_Standard      = 0x2c81 // 0xvvhh
  val DIWSTOP_Standard      = 0xf4c1 // 0xvvhh

  val DDFSTRT_StandardLores = 0x38
  val DDFSTRT_StandardHires = 0x3c
  val DDFSTRT_LimitLores    = 0x18
  val DDFSTRT_LimitHires    = 0x18

  val DDFSTOP_StandardLores = 0xd0
  val DDFSTOP_StandardHires = 0xd4
  val DDFSTOP_LimitLores    = 0xd8
  val DDFSTOP_LimitHires    = 0xd8
}

/**
 * An implementation of the Amiga Playfield hardware.
 * @constructor create an instance of a Playfield component
 * @param video the Video object
 */
class PlayfieldSystem(video: Video) {
  var ddfstrt  = 0
  var ddfstop  = 0
  var diwstrt  = 0
  var diwstop  = 0
  var bplcon0  = 0
  var bplcon1  = 0
  var bplcon2  = 0
  var bplcon3  = 0
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
}
