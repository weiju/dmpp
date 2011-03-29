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

// this might be better a case class
case class ExecutionContext(regnum: Int = 0,
                            eamode: EffectiveAddressMode = null,
                            earegnum: Int = 0,
                            size: Int = 0) {

  /**
   * Creates a new object based on this one, with the specified regnum field set.
   * @param regnum the register number
   */
  def cloneWithRegnum(regnum: Int) = {
    ExecutionContext(regnum, eamode, earegnum, size)
  }
}

abstract class Instruction(cpu: Cpu) {
  /**
   * Register numbers in 68k instructions are typically specified at
   * ----rrr---------
   * @return the instruction's register number
   */
  def regnum = (cpu.currentInstructionWord >>> 9) & 0x07

  def execute(context: ExecutionContext): Unit
  def eaValueL(context: ExecutionContext) = {
    context.eamode.lValue(context.earegnum)
  }
}

case class Opcode(instruction: Instruction, context: ExecutionContext) {
  def execute = instruction.execute(context)
}

abstract class Disassembly(cpu: Cpu)
extends Instruction(cpu) {
  def execute(context: ExecutionContext) { }
}


class LeaInstruction(cpu: Cpu) extends Instruction(cpu) {
  def execute(context: ExecutionContext) {
    cpu.a(regnum) = eaValueL(context)
  }
}

class LeaDisassembly(cpu: Cpu) extends Disassembly(cpu) {
  def toString(context: ExecutionContext) = {
    "lea %s, a%02d".format(eaValueL(context).toString, regnum)
  }

}
