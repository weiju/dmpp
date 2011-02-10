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
package org.dmpp.cymus;
import org.specs._
import org.specs.matcher._
import org.specs.runner.{ConsoleRunner, JUnit4}

class CiaTest extends JUnit4(CiaSpec)
object CiaSpecRunner extends ConsoleRunner(CiaSpec)

object CiaSpec extends Specification with xUnit {
  "CIA 8520" should {
    // Tests setting the registers of the general purpose ports.
    "set a port register" in {
      val cia = new Cia8520("CIA Test");
      cia.portAPins = 0x06 // %00000110
      cia.setRegister(AbstractCia.DDRA, 3) // %00000011
      cia.setRegister(AbstractCia.PRA,  1) // %00000001
      
      assertEquals(5, cia.getRegister(AbstractCia.PRA))
      assertEquals(0x05, cia.portAPins)
      assertEquals(3, cia.getRegister(AbstractCia.DDRA))
    }
  }
}
