/**
 * Created on March 30, 2011
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

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.dmpp.common._

object MockChipBus extends Bus {
  def requestMemory(device: BusDevice, address: Int, numCycles: Int) = false
}

@RunWith(classOf[JUnitRunner])
class CpuBusSpec extends FlatSpec with ShouldMatchers {

  val cpuBus = new CpuBus(MockChipBus)

  "CpuBus" should "return immediately on ROM request" in {
    cpuBus.requestMemory(null, 0xfc0040, 2) should be (true)
  }
  it should "delegate request to chip bus for access to chip registers" in {
    cpuBus.requestMemory(null, 0xdf0000, 2) should be (false)
    cpuBus.requestMemory(null, 0xdfffff, 2) should be (false)
  }
  it should "delegate request to chip bus for access to chip RAM" in {
    cpuBus.requestMemory(null, 0x000000, 2) should be (false)
    cpuBus.requestMemory(null, 0x1fffff, 2) should be (false)
  }
}
