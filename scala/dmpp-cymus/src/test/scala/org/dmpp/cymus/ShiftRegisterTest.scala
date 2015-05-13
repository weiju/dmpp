/**
 * Created on November 16, 2009
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
package org.dmpp.cymus;

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ShiftRegisterSpec extends FlatSpec with Matchers {

  "ShiftRegister" should "be initialized" in {
    val reg = new ShiftRegister(4)
    reg.empty should be (true)
  }
  it should "enqueue and dequeue" in {
    val reg = new ShiftRegister(4)
    reg.enqueue(false)
    reg.enqueue(true)

    reg.empty   should be (false)
    reg.dequeue should be (false)
    reg.dequeue should be (true)
    reg.empty   should be (true)
  }
  it should "report a full queue" in {
    val reg = new ShiftRegister(2)
    reg.enqueue(false)
    reg.enqueue(true)

    an [IndexOutOfBoundsException] should be thrownBy { reg.enqueue(true) }
  }
  it should "wrap around slots" in {
    val reg = new ShiftRegister(2)
    // use the first slot normal
    reg.enqueue(true)
    reg.dequeue should be (true)

    // the second two slots should be wrapped
    reg.enqueue(false)
    reg.enqueue(true)
    reg.dequeue should be (false)
    reg.dequeue should be (true)
  }
}
