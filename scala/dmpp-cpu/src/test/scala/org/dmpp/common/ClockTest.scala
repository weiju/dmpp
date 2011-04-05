/**
 * Created on March 31, 2011
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
package org.dmpp.common

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class MockClockedDevice extends ClockedDevice {
  var tickCount = 0

  def receiveTicks(numTicks: Int) {
    tickCount += numTicks
  }
}

@RunWith(classOf[JUnitRunner])
class ClockSpec extends FlatSpec with ShouldMatchers {

  "DefaultClock" should "send ticks to its connected devices" in {
    val clockedDevice = new MockClockedDevice
    val clock = new DefaultClock
    clock.connectDevice(clockedDevice)
    clock.performTicks(2, null)
    
    clockedDevice.tickCount should be (2)
  }

  "DefaultClock" should "exclude the excluded device" in {
    val clockedDevice1 = new MockClockedDevice
    val excludedDevice = new MockClockedDevice
    val clock = new DefaultClock
    clock.connectDevice(clockedDevice1)
    clock.connectDevice(excludedDevice)
    clock.performTicks(2, excludedDevice)
    
    clockedDevice1.tickCount should be (2)
    excludedDevice.tickCount should be (0)
  }

  "ClockDivider" should "not notify the connected device" in {
    val clock = new DefaultClock
    val clockDivider = new ClockDivider(2)
    val dividedClockDevice = new MockClockedDevice
    clock.connectDevice(clockDivider)
    clockDivider.connectDevice(dividedClockDevice)

    clock.performTicks(1, null)
    dividedClockDevice.tickCount should be (0)
  }
  it should "notify the connected device with divided tick number" in {
    val clock = new DefaultClock
    val clockDivider = new ClockDivider(2)
    val dividedClockDevice = new MockClockedDevice
    clock.connectDevice(clockDivider)
    clockDivider.connectDevice(dividedClockDevice)

    clock.performTicks(5, null)
    dividedClockDevice.tickCount should be (2)
  }
}
