/**
 * Created on November 14, 2009
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

import org.dmpp.cpu.ClockedDevice

/**
 * Super class for the CIA-8520 and CIA-6526.
 * The only difference is the TOD clock (24 bit counter in 8520
 * vs 4 * 8 bit BCD in 6526), so the TOD part is left abstract.
 * Register setup also has to be done in the sub class.
 */
object AbstractCia {
  val PRA  = 0
  val PRB  = 1
  val DDRA = 2
  val DDRB = 3
  val CRA  = 14
  val CRB  = 15
}

abstract class AbstractCia(aLabel: String) extends ClockedDevice {

  // **********************************************************************
  // ***** CIA COMPONENTS
  // **********************************************************************
  trait CiaRegister {
    def value: Int
    def value_=(aValue: Int)
  }

  /** 8 bit port to implement port A and B */
  protected class Port {
    private var _ddr  = 0

    /**
     * Direct access to the port pins signals. These are used in two ways:
     * 1. Peripherals set can access these through the CIA's portX methods.
     * 2. Timer A and B can call set pins directly if the crb bit is set.
     */
    var pins = -1

    def reset {
      pins = -1
      _ddr  = 0
    }
    def pr = {
      new CiaRegister {
        def value = pins
        def value_=(aValue: Int) { updatepins(aValue) }
      }
    }
    def ddr = {
      new CiaRegister {
        def value = _ddr
        def value_=(aValue: Int) { _ddr = aValue }
      }
    }

    /**
     * Combines DDRx, PRx and the current pins signals into a new pins signal
     * combination.
     * Only output bits are affected, input bits have no effect on the result.
     */
    private def updatepins(prValue: Int) {
      var maskbit = 0
      var newpins = pins
      for (i <- 0 until 8) {
        maskbit = 1 << i
        if ((_ddr & maskbit) == maskbit) {
          if ((prValue & maskbit) == maskbit) newpins |= maskbit
          else newpins &= ~maskbit
        }
      }
      pins = newpins
    }
  }

  /** Serial port */
  class SerialPort {
    private var buffer = 0 // 8 bit shift register
    def sdr = {
      new CiaRegister {
        def value = buffer
        def value_=(aValue: Int) { buffer = aValue }
      }
    }
  }

  /** 16 bit timer */
  abstract class AbstractTimer {
    var latch   = 65535
    var count   = 0
    var running = false
    var pbon    = false 
    var outmode = false
    var runmode = false

    def reset {
      count = 0
      latch = 65535 // set all 16 bits to 1
    }

    protected def flags2Control = {
      var result = 0;
      if (running) result |= 0x01
      if (pbon)    result |= 0x02
      if (outmode) result |= 0x04
      if (runmode) result |= 0x08
      result
    }
    protected def control2Flags(control: Int) {
      running  = (control & 0x01) == 0x01
      pbon     = (control & 0x02) == 0x02
      outmode  = (control & 0x04) == 0x04
      runmode  = (control & 0x08) == 0x08
      // Force Load
      if ((control & 0x10) == 0x10) count = latch
    }
    protected def handleUnderflow {
      // TODO: actions according to CRx
      println("TIMER UNDERFLOW !!")
      count = latch
    }

    override def toString = {
      "latch = %d count = %d run = %b pbon = %b " +
      "outmode = %b runmode = %b".format(latch, count, running,
                           pbon, outmode, runmode)
    }

    def lo = {
      new CiaRegister {
        def value = count & 0xff
        def value_=(aValue: Int) {
          latch = (latch & 0xff00) | (aValue & 0xff)
        }
      }
    }
    def hi = {
      new CiaRegister {
        def value = (count >>> 8) & 0xff
        def value_=(aValue: Int) {
          latch = (latch & 0xff) | ((value << 8) & 0xff00)
          if (!running) count = latch
        }
      }
    }
    def cr = {
      new CiaRegister {
        def value = flags2Control
        def value_=(aValue: Int) { control2Flags(aValue) }
      }
    }
    def pulseCnt(n: Int) {
      // TODO
    }
    def pulse02(n: Int) {
      if (running && count02Pulses) {
        count -= n
        if (count < 0) handleUnderflow
      }
    }
    protected def count02Pulses: Boolean
  }

  class TimerA extends AbstractTimer {
    var inmode = false
    var spmode = false
    var todin  = false
    override def reset {
      super.reset
      inmode = false
      spmode = false
      todin  = false
    }
    override protected def flags2Control = {
      var result = super.flags2Control
      if (inmode) result |= 0x20
      if (spmode) result |= 0x40
      if (todin)  result |= 0x80
      result
    }
    override protected def control2Flags(control: Int) {
      super.control2Flags(control)
      inmode = (control & 0x20) == 0x20
      spmode = (control & 0x40) == 0x40
      todin  = (control & 0x80) == 0x80
    }
    override protected def handleUnderflow {
      super.handleUnderflow
      // TODO: if cra bit set, set port B bit accordingly
    }
    override def toString = {
      "%s inmode = %b spmode = %b todin = %b".format(
        super.toString(), inmode, spmode, todin)
    }
    protected def count02Pulses = !inmode
  }

  class TimerB extends AbstractTimer {
    var inmode1 = false
    var inmode2 = false
    var alarm   = false
    override def reset {
      super.reset
      inmode1 = false
      inmode2 = false
      alarm = false
    }
    override protected def flags2Control = {
      var result = super.flags2Control
      if (inmode1) result |= 0x20
      if (inmode2) result |= 0x40
      if (alarm)   result |= 0x80
      result
    }
    override protected def control2Flags(control: Int) {
      super.control2Flags(control)
      inmode1 = (control & 0x20) == 0x20
      inmode2 = (control & 0x40) == 0x40
      alarm   = (control & 0x80) == 0x80
    }
    override protected def handleUnderflow() {
      super.handleUnderflow
      // TODO: if crb bit set, set port B bit accordingly
    }
    override def toString = {
      "%s inmode1 = %b inmode2 = %b alarm = %b".format(
                           super.toString(), inmode1, inmode2, alarm)
    }
    protected def count02Pulses = !inmode1 && !inmode2
  }

  // **********************************************************************
  // ***** CIA STATE
  // **********************************************************************

  // this is just a tag to mark CIA's in a system with more than one CIA
  private var label = aLabel

  // Setup the CIA's functional components. Each component exposes the
  // registers that belong to it. We just stuff them into an array
  // to determine the register through polymorphism.

  // Interrupt Control Register
  protected def icr = new CiaRegister {
      private var icr = 0
      def value = icr
      def value_=(aValue: Int) { icr = aValue }
  };

  // The timer structures
  // Timer A can count CNT or 02
  protected val timerA = new TimerA
  // Timer B can count CNT, 02, Timer A underflow or
  // Timer A underflow + CNT high
  protected val timerB = new TimerB

  // these port variables model the buffers for the port pins of the CIA.
  // Only the low 8 bits are used
  protected val portA      = new Port
  protected val portB      = new Port
  protected val serialPort = new SerialPort

  private var listeners  : List[CiaChangeListener] = Nil
  private var irqListener: IrqListener = null

  protected def getReg(regnum: Int): CiaRegister
  protected def getRegName(regnum: Int): String

  // **********************************************************************
  // ***** PUBLIC INTERFACE
  // **********************************************************************

  /**
   * Adds a CiaChangeListener to the list of listeners.
   * @param l CiaChangeListener
   */
  def addListener(l: CiaChangeListener) { listeners ::= l }

  /**
   * Sets the IrqListener. This is called when an interrupt is generated
   * in the CIA (IRQ 6). There can be only one such listener.
   * @param l IrqListener
   */
  def setIrqListener(l: IrqListener) { irqListener = l }

  /**
   * Resets the CIA chip, its registers and internal components. In the
   * real chip, this is done through the /RES pin.
   */
  def reset {    
    portA.reset
    portB.reset
    timerA.reset
    timerB.reset
  }

  // **********************************************************************
  // ***** PINS
  // **********************************************************************
  // Status of the port buffers
  /**
   * Returns the status of the port A pins.
   * @return port A pins
   */
  def portAPins = portA.pins

  /**
   * Sets the status of the port A pins.
   * @param value new value of port A pins
   */
  def portAPins_=(value: Int) { portA.pins = value }

  /**
   * Returns the status of the port B pins.
   * @return port B pins
   */
  def portBPins = portB.pins

  /**
   * Sets the status of the port B pins.
   * @param value new value of port B pins
   */
  def portBPins_=(value: Int) { portB.pins = value }

  // CLOCK
  /**
   * Sends n pulses to the 02 pin.
   * Depending on their state, the internal timers might respond.
   * @param n number of pulses to send
   */
  protected def pulse02(n: Int) {
    timerA.pulse02(n)
    timerB.pulse02(n)
  }

  /**
   * Sends n pulses to the CNT pin.
   * Depending on their state, the internal timers might respond.
   * @param n number of pulses to send
   */
  protected def pulseCnt(n: Int) {
    timerA.pulseCnt(n)
    timerB.pulseCnt(n)
  }

  /**
   * Sends n pulses to the TOD pin.
   * @param n number of pulses to send
   */
  protected def pulseTod(n: Int)

  /**
   * Sends n ticks to TOD, 02 and CNT. We might actually not need to handle
   * separate signals.
   * @param numTicks number of ticks sent
   */
  def receiveTicks(numTicks: Int) {
    pulse02(numTicks)
    pulseCnt(numTicks)
    pulseTod(numTicks)
  }

  // **********************************************************************
  // ***** REGISTER ACCESS
  // **********************************************************************
  /**
   * Returns the contents of the specified register. See CIA specification
   * for details.
   * @param regnum register number
   * @return contents of the register according to specification
   */
  def getRegister(regnum: Int) = {
    printf("[%s] read CIA register: %s = %02x\n", label,
           getRegName(regnum), getReg(regnum).value)
    getReg(regnum).value
  }

  /**
   * Sets the contents of the specified register. See CIA specification
   * for details.
   * @param regnum register number
   * @param value the value to set to the register
   */
  def setRegister(regnum: Int, aValue: Int) {
    printf("[%s], write CIA register: %d [ %s ] = %d\n", label,
           regnum, getRegName(regnum), aValue)
    for (l <- listeners) l.ciaRegisterChanged(regnum, aValue)
    getReg(regnum).value = aValue
    if (regnum == AbstractCia.PRA) {
      for (l <- listeners) l.praOutput(portA.pins)
    } else if (regnum == AbstractCia.PRB) {
      for (l <- listeners) l.prbOutput(portB.pins)
    }
    if (regnum == AbstractCia.CRA) println(timerA.toString())
    if (regnum == AbstractCia.CRB) println(timerB.toString())
  }
}
