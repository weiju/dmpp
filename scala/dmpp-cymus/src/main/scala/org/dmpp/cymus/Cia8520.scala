/**
 * Created on November 12, 2009
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
package org.dmpp.cymus

/**
 * Constants used in the Cia implementation.
 */
object Cia8520 {
  val REGISTER_NAMES = Array(
    "pra",   "prb",    "ddra",  "ddrb",
    "talo",  "tahi",   "tblo",  "tbhi",
    "todlo", "todmid", "todhi", "cia_unused",
    "sdr",   "icr",    "cra",   "crb"
  )
}

/**
 * Implementation of the Complex Interface Adapter (CIA) 8520, as used in
 * the Amiga.
 * @constructor creates a new CIA 8520 instance
 * @param aLabel the name of this CIA chip in the system
 */
class Cia8520(aLabel: String) extends AbstractCia(aLabel) {
  // **********************************************************************
  // ***** 24 BIT TOD CLOCK
  // **********************************************************************
  class TimeOfDayClock {
    var running = false
    var latched = false
    var latch   = -1
    var count   = 0
    def reset {
      running = false
      latched = false
      count = 0
      latch = -1
    }
    override def toString = {
      "TOD: running = %b latched = %b latch = %d count = %d".format(
        running, latched, latch, count)
    }
    def lo = {
      new CiaRegister {
        def value = {
          val result = if (latched) latch & 0xff else count & 0xff
          latched = false
          //println("Unlatching the TOD clock.");
          result
        }
        def value_=(value: Int) {
          count = (count & 0xffff00) | (value & 0xff)
          running = true
        }
      }
    }
    def mid = {
      new CiaRegister {
        def value = {
          if (latched) (latch >>> 8) & 0xff else (count >>> 8) & 0xff
        }
        def value_=(aValue: Int) {
          count = (count & 0xff00ff) | ((aValue << 8) & 0xff00)
        }
      }
    }
    def hi = {
      new CiaRegister() {
        def value = {
          latched = true // why this is always true ?
          //println("Latching the TOD clock...");
          if (latched) (latch >>> 16) & 0xff else (count >>> 16)  & 0xff
        }
        def value_=(aValue: Int) {
          running = false
          count = (count & 0x00ffff) | ((value << 16) & 0xff0000)
        }
      }
    }
    // CIA-8520 has one unused register (it has only 3 TOD registers,
    // the CIA-6526 has four)
    def unused = {
      new CiaRegister {
        def value = 0
        def value_=(aValue: Int) { }
      }
    }
    def tick {
      if (running) {
        count += 1
        if (count > 0xffffff) {
          println("CIA TOD-24 OVERFLOW !!!")
          count &= 0xffffff
        }
      }
    }
  }

  // **********************************************************************
  // ***** CIA STATE
  // **********************************************************************
  private val tod = new TimeOfDayClock

  private val reg = Array(
    portA.pr,       portB.pr,  portA.ddr, portB.ddr,
    timerA.lo,      timerA.hi, timerB.lo, timerB.hi,
    tod.lo,         tod.mid,   tod.hi,    tod.unused,
    serialPort.sdr, icr,       timerA.cr, timerB.cr
  )

  /**
   * Returns the value of the specified register number.
   * @param regnum register number
   * @return register value
   */
  protected def getReg(regnum: Int) = reg(regnum)

  /**
   * Returns the CIA register name.
   * @param regnum register number
   * @return register name
   */
  protected def getRegName(regnum: Int) = Cia8520.REGISTER_NAMES(regnum)

  // **********************************************************************
  // ***** PUBLIC INTERFACE
  // **********************************************************************

  /**
   * Resets this CIA chip object.
   */
  override def reset {
    super.reset
    tod.reset
  }

  /**
   * Pulse the TOD clock with a single tick.
   */
  def todTick = tod.tick
}
