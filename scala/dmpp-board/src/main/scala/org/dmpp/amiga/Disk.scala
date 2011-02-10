/**
 * Created on November 2, 2009
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

import org.dmpp.cymus._

class FloppyDrive { 
  var motorOn      = false
  var diskInserted = false
  var side         =  0 // 0 = upper, 1 = lower
  var direction    = -1 // -1 = towards center, 1 = towards track 0 (outside)
  var currentTrack =  0 // tracks 0-79, 0 is outside

  def step = {
    currentTrack += direction
    // do not step off
    if (currentTrack < 0)  currentTrack = 0
    if (currentTrack > 79) currentTrack = 79
    println("stepped to track " + currentTrack)
  }
}

class FloppyController {
  var selectedDrive = 0 // 0 = internal drive
  val internalDrive = new FloppyDrive
  
  def connect(ciaA: Cia8520, ciaB: Cia8520) = {
    ciaA.addListener(new CiaChangeListener {
      override def praOutput(value: Int) {
        if ((value & 0x3c) != 0)
          println("Floppy controller, port A: %d\n", value)
      }
      override def prbOutput(value: Int) { }
      override def ciaRegisterChanged(regnum: Int, value: Int) { }
    })
    ciaB.addListener(new CiaChangeListener {
      override def praOutput(value: Int) { }
      override def prbOutput(value: Int) {
        println("Floppy controller, port B: " + value)
      }
      override def ciaRegisterChanged(regnum: Int, value: Int) { }
    })
  }
}

