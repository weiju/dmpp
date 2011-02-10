/**
 * Created on September 28, 2009
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
import java.util.ArrayList
import org.mahatma68k.AddressSpace
import org.dmpp.cymus.Cia8520

class IllegalAccessException extends Exception

/**
 * Mapping of the CIA to the Amiga address space.
 */
class CiaSpace extends AddressSpace {
  val ciaA = new Cia8520("CIA A")
  val ciaB = new Cia8520("CIA B")

  def start = 0xbf0000
  def size = 0x10000

  def readByte(address: Int) = {
    val cia = getCiaForAddress(address)
    cia.getRegister(getRegisterNumForAddress(address))
  }
  def readShort(address: Int): Int = {
    println("*ILLEGAL* - read short from CIA")
    throw new IllegalAccessException
  }
  def readLong(address: Int): Int = {
    println("*ILLEGAL* - read long from CIA")
    throw new IllegalAccessException
  }

  def writeByte(address: Int, value: Int) = {
    val cia = getCiaForAddress(address)
    cia.setRegister(getRegisterNumForAddress(address), value & 0xff)
  }
  def writeShort(address: Int, value: Int) = {
    println("*ILLEGAL* - Write short to CIA")
    throw new IllegalAccessException
  }
  def writeLong(address: Int, value: Int) = {
    println("*ILLEGAL* - Write long to CIA")
    throw new IllegalAccessException
  }

  def getRegisterNumForAddress(address: Int) = (address & 0x0f00) >> 8
  def getCiaForAddress(address : Int) = {
    val range   = address & 0x00fff000
    val regspec = address & 0x000000ff
    var cia: Cia8520 = null
    if (range == 0xbfd000 && regspec == 0) {
      cia = ciaB
    } else if (range == 0xbfe000 && regspec == 1) {
      cia = ciaA
    }
    if (cia == null) {
      printf("Could not map address %08x to CIA register\n", address)
    }
    cia
  }
}

