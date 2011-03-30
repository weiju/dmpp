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

import scala.collection.mutable.HashMap

/**
 * Extrinsic instruction state. This state is provided as the arguments
 * encoded in an 68000 instruction.
 * @constructor creates an ExecutionContext
 * @param value encoded in register number field (can be address or data)
 * @param value encoded in eff. address register number field
 */
case class ExecutionContext(regnum: Int = 0,
                            earegnum: Int = 0) {

  /**
   * Creates a new object based on this one, with the specified regnum field set.
   * @param regnum the register number
   * @return new ExecutionContext with modified register number
   */
  def cloneWithRegnum(regnum: Int) = ExecutionContext(regnum, this.earegnum)

  /**
   * Creates a new object based on this one, with the specified earegnum field set.
   * @param regnum the register number
   * @return new ExecutionContext with modified register number
   */
  def cloneWithEaRegnum(earegnum: Int) = ExecutionContext(this.regnum, earegnum)
}

/**
 * Super class for operations based on a CPU.
 * @param cpu
 */
abstract class CpuStrategy(cpu: Cpu, size: Int, eamode: EffectiveAddressMode) {
  def eaValueL(context: ExecutionContext) = {
    eamode.lValue(context.earegnum)
  }
}

/**
 * Instruction objects implement the Flyweight pattern to save memory.
 * Intrinsic state is
 *
 * - Cpu reference
 * - instruction size
 * - effective address mode
 *
 * Extrinsic state is
 * - register number
 * - effective address register number
 */
abstract class Instruction(cpu: Cpu, size: Int,
                           eamode: EffectiveAddressMode)
extends CpuStrategy(cpu, size, eamode) {
  def execute(context: ExecutionContext): Unit
}

case class Opcode(instruction: Instruction, context: ExecutionContext) {
  def execute = instruction.execute(context)
}

class InstructionFactory(cpu: Cpu) {
  val addressModeMap = Map("an" -> cpu.AddressRegisterDirect,
                           "dn" -> cpu.DataRegisterDirect)
  val created = new HashMap[String, Instruction]

  private def makeKey(mnemonic: String, size: Int, eamodeName: String) = {
    "%s_%d_%s".format(mnemonic, size, eamodeName)
  }

  private def createInstruction(mnemonic: String, size: Int, eamodeName: String) = {
    mnemonic match {
      case "lea" =>
        new LeaInstruction(cpu, size, addressModeMap(eamodeName))
    }
  }
  def getInstruction(mnemonic: String, size: Int, eamodeName: String) = {
    val key = makeKey(mnemonic, size, eamodeName)
    if (!created.contains(key)) {
      created(key) = createInstruction(mnemonic, size, eamodeName)
    }
    created(key)
  }
}

class LeaInstruction(cpu: Cpu, size: Int, eamode: EffectiveAddressMode)
extends Instruction(cpu, size, eamode) {
  def execute(context: ExecutionContext) {
    cpu.a(context.regnum) = eaValueL(context)
  }
}
