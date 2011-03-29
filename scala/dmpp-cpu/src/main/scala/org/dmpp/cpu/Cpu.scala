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

trait AddressSpace {
  def uint16At(address: Int): Int
}

/**
 * Effective address mode trait. Effective address modes know how to pull
 * their values from the system.
 * an effective address takes 6 bit usually the 6 least significant positions
 * where effective address = mmmrrr (m = mode, r = register number)
 */
abstract class EffectiveAddressMode {
  type Address = Int
  def value(regnum: Int, size: Int): Int
  def memoryRequest(size: Int): Option[(Address, Int)]

  def bValue(regnum: Int) = value(regnum, 1)
  def wValue(regnum: Int) = value(regnum, 2)
  def lValue(regnum: Int) = value(regnum, 4)

  def bMemoryRequest = memoryRequest(1)
  def wMemoryRequest = memoryRequest(2)
  def lMemoryRequest = memoryRequest(4)
}

class Cpu(addressSpace: AddressSpace) {
  var ip       = 0
  var userMode = true
  val d        = Array(0, 0, 0, 0, 0, 0, 0, 0)
  val a        = Array(0, 0, 0, 0, 0, 0, 0, 0)
  val Instructions = Map(
    "lea" -> new LeaInstruction(this)
  )
  val opcodes = new Array[Opcode](65535)
  def currentInstructionWord = addressSpace.uint16At(ip)

  init

  def init {
    initOpcodes
  }

  private def initOpcodes {
    for (instrDef <- InstructionSet.InstrDefs) {
      instrDef.setToOpcodeArray(opcodes)
    }
  }

  // Addressing modes are defined on the CPU object so they can be used
  // to efficiently retrieve values
  val AddressRegisterDirect = new EffectiveAddressMode {
    def value(regnum: Int, size: Int) = a(regnum)
    def memoryRequest(size: Int) = None
  }
  val DataRegisterDirect = new EffectiveAddressMode {
    def value(regnum: Int, size: Int): Int = d(regnum)
    def memoryRequest(size: Int) = None
  }
}

object Main {

  def main(args: Array[String]) {
    println("Mahatma 68k CPU Emulator")
    val cpu = new Cpu(null)
  }
}
