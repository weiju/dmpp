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

import java.io._
import java.nio._

import org.dmpp.common.AddressSpace

object KickstartROM {
  val ROMStart         = 0xfc0000
  val ROMSize          = 0x40000
  val ModuloMask       = ROMSize - 1
}

class KickstartROM(bytes: ByteBuffer) extends AddressSpace {
  import KickstartROM._
  def start = ROMStart
  def size  = ROMSize
  def readByte(address: Int) = bytes.get(address - ROMStart).asInstanceOf[Int]
  def readShort(address: Int) = bytes.getShort(address - ROMStart).asInstanceOf[Int]
  def readLong(address: Int) = bytes.getInt(address - ROMStart)
  def writeByte(address: Int, value: Int) { }
  def writeShort(address: Int, value: Int) { }
  def writeLong(address: Int, value: Int) { }
  def execBase = readLong(ROMStart + 4)
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
  def currentInstructionWord = addressSpace.readShort(ip)

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
    if (args.length > 0) {
      val file = new File(args(0))
      val dataBytes = new Array[Byte](file.length.asInstanceOf[Int])
      val is = new FileInputStream(file)
      is.read(dataBytes)
      is.close
      val kickrom = new KickstartROM(ByteBuffer.wrap(dataBytes))
      val execBase = kickrom.execBase
      printf("Exec base: %04x\n", kickrom.execBase)
      val cpu = new Cpu(kickrom)
      cpu.ip = kickrom.execBase
      printf("1st instruction word: %04x\n", cpu.currentInstructionWord)
      val opcodes = InstructionSet.createOpcodes(cpu)
    } else {
      println("Please provide path to Kickstart ROM")
    }
  }
}
