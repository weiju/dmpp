/**
 * Created on November 22, 2009
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

import org.mahatma68k._
import org.mahatma68k.InterruptAcknowledge.VectorType

/**
 * The InterruptController holds the enable and request masks.
 */
class InterruptController extends InterruptAcknowledge
with VerticalBlankListener {

  var intena = 0
  var intreq = 0
  var cpu: Cpu = null

  val INTENA = new CustomChipWriteRegister("INTENA") {
    def value_=(value: Int) { intena = value }
  }
  val INTENAR = new CustomChipReadRegister("INTENAR") {
    def value = intena
  }
  val INTREQ = new CustomChipWriteRegister("INTREQ") {
    def value_=(value: Int) {
      intreq = value
      val mask = 0x4000 | value;
      val enabled = (intena & mask) == mask; 
      println("Interrupt request: " + value + " enabled: " + enabled)
      if (enabled) {
        // do interrupt request
        println("DO INTERRUPT REQUEST")
        // FOR NOW, LEVEL 3 (Vertical Blank)
        cpu.makeInterruptRequest(3, InterruptController.this)
      }
    }
  }
  val INTREQR = new CustomChipReadRegister("INTREQR") {
    def value = intreq
  }
  def acknowledge(level: Int) = {
    VectorType.AUTOVECTOR
  }

  def notifyVerticalBlank {
    INTREQ.value = 1 << 5
  }
}
