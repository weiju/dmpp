/**
 * Created on Devember 4, 2009
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

/**
 * This is the component interface to the Blitter.
 */
class Blitter {
  // BLTCON0
  private var srcAShift      = 0
  private var function       = 0
  private var useA           = false
  private var useB           = false
  private var useC           = false
  private var useD           = false

  // BLTCON1
  private var srcBShift      = 0
  // not supported and probably not used anyways
  private var dOff           = false
  private var exclusiveFill  = false
  private var inclusiveFill  = false
  private var fillCarryInput = false
  private var descending     = false
  private var lineMode       = false

  private var blitsize = 0

  def doCycles(numCycles: Int) {
    // DO work
  }

  def printBltcon0 {
    printf("SRC A SHIFT=%d f=%d USEA=%b USEB=%b USEC=%b USED=%b\n",
           srcAShift, function, useA, useB, useC, useD)
  }

  def printBltcon1 {
    printf("SRC B SHIFT=%d DOFF=%b EFE=%b IFE=%b FC=%b DESC=%b LINE=%b\n",
           srcBShift, dOff, exclusiveFill, inclusiveFill, fillCarryInput,
           descending, lineMode)
  }

  def bltcon0 : ICustomChipReg = {
    new ICustomChipReg {
      def name = "BLTCON0"
      def value: Int = {
        throw new UnsupportedOperationException("can not read from " + name)
      }
      def value_=(aValue: Int) {
        srcAShift = (aValue >>> 24) & 0x0f
        function = aValue & 0xff
        useA = (aValue & 0x800) == 0x800
        useB = (aValue & 0x400) == 0x400
        useC = (aValue & 0x200) == 0x200
        useD = (aValue & 0x100) == 0x100
        printBltcon0
      }
    }
  }

  def bltcon1 : ICustomChipReg = {
    new ICustomChipReg {
      def name = "BLTCON1"
      def value : Int = {
        throw new UnsupportedOperationException("can not read from " + name)
      }
      def value_=(aValue: Int) {
        srcBShift = (aValue >>> 24) & 0x0f
        dOff  = (aValue & 0x80) == 0x80
        exclusiveFill   = (aValue & 0x10) == 0x10
        inclusiveFill   = (aValue & 0x08) == 0x08
        fillCarryInput  = (aValue & 0x04) == 0x04
        descending      = (aValue & 0x02) == 0x02
        lineMode        = (aValue & 0x01) == 0x01
        printBltcon1
      }
    }
  }

  def bltsize : ICustomChipReg = {
    new ICustomChipReg {
      def name = "BLTSIZE"
      def value : Int = {
        throw new UnsupportedOperationException("can not read from " + name)
      }
      def value_=(aValue: Int) {
        blitsize = aValue
        val h = (blitsize >>> 6) & 0x3ff
        val v = blitsize & 0x1f
        printf("STARTING BLITTER, H: %d V: %d\n", h, v)
      }
    }
  }
}
