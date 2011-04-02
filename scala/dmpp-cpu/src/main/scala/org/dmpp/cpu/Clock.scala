/**
 * Created on March 27, 2011
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
package org.dmpp.cpu

/**
 * Implements a system clock. The general idea is that clocked devices
 * are driven by the system clock, but control their state autonomously.
 * This model can support a variety of clocking schemes:
 * 
 * 1. We could emulate the clock tick-by-tick. Simple, but could be inefficient.
 * 2. Instead, we can let either the CPU drive the clock when it is certain
 *    that it can run uninterrupted and only when it has to wait, we use
 *    individual ticks. This is much more complicated, but leads to better
 *    performance.
 * 3. We could also realize differently clocked buses with this. Newer Amiga
 *    models employ higher CPU models by clocking them at a multiple of the
 *    Chip bus speed.
 *
 * Note that we can mostly increment the clock by two ticks, because we do not
 * need to synchronize even cycles, but we have to aware of the video beam
 * and to align clocking so that the even and odd slots always go to the
 * right device. Since every line has 455 clock ticks, this should be easy.
 */
trait Clock {
  def connectDevice(clockedDevice: ClockedDevice)
  def performTicks(numTicks: Int, except: ClockedDevice)
}

/**
 * Default clock implementation.
 * @constructor creates a DefaultClock instance
 */
class DefaultClock extends Clock {
  private var clockedDevices: List[ClockedDevice] = Nil

  /**
   * Connects a clocked device.
   * @param clockedDevice a clocked device to connect
   */
  def connectDevice(clockedDevice: ClockedDevice) {
    clockedDevices ::= clockedDevice
  }

  /**
   * Performs a certain number of clock tick. A device can be excluded from
   * receiving clock ticks. This is useful when it was the sender of the
   * ticks.
   * @param numTicks the number of ticks to perform
   * @param except the device to be excluded from receiving ticks
   */
  def performTicks(numTicks: Int, except: ClockedDevice = null) {
    for (device <- clockedDevices) {
      if (device != except) device.receiveTicks(numTicks)
    }
  }
}

/**
 * Interface of a clocked device.
 */
trait ClockedDevice {

  /**
   * Receive a certain amount of clock ticks.
   * @param numTicks number of ticks sent by the clock
   */
  def receiveTicks(numTicks: Int)
}

/**
 * A clock that can be added as a ClockDevice to another clock and divides
 * the number of ticks it received by a certain number of clock ticks.
 * @constructor creates a ClockDivider instance
 * @param divisionSize the division size
 */
class ClockDivider(divisionSize: Int) extends DefaultClock with ClockedDevice {
  var numTicksUnsent: Int = 0

  def receiveTicks(numTicks: Int) {
    numTicksUnsent += numTicks
    val numTicksToSend = numTicksUnsent / divisionSize
    numTicksUnsent = numTicksUnsent % divisionSize
    performTicks(numTicksToSend, null)
  }
}
