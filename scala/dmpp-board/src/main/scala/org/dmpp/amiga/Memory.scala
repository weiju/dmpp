/**
 * Created on November 26, 2009
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

import java.io._
import java.nio.ByteBuffer
import org.dmpp.common.AddressSpace

/**
 * Memory that is used for read-only access, e.g. Kickstart ROM
 */
object ReadOnlyMemory {
  val RomSize          = 0x40000
  val ModuloMask       = RomSize - 1
}

class ReadOnlyMemory(buffer: ByteBuffer, offset: Int, memsize: Int)
extends AddressSpace {
  import ReadOnlyMemory._

  def start = offset
  def size = RomSize

  private def mapAddress(address: Int) = address & ModuloMask

  def readByte(address: Int) = {
    buffer.get(mapAddress(address)).asInstanceOf[Int]
  }
  def readShort(address: Int) = {
    buffer.getShort(mapAddress(address)).asInstanceOf[Int]
  }
  def readLong(address: Int) = {
    buffer.getInt(mapAddress(address))
  }
  def writeByte(address: Int, value: Int) { }
  def writeShort(address: Int, value: Int) { }
  def writeLong(address: Int, value: Int) { }
}

/**
 * All unused slots of the address space are filled with dummy memory.
 */
class DummyAddressSpace(tag: String, retval: Int) extends AddressSpace {
  var debug = true
  def start = 0
  def size = 0

  /** Default constructor. */
  def this(tag: String) = this(tag, 0)

  def readByte(address: Int) = {
    if (debug) printf("reading B from %s, address: $%04x !!\n", tag, address)
    retval & 0xff
  }
  def readShort(address: Int) = {
    if (debug) printf("reading W from %s, address: $%04x !!\n", tag, address)
    retval & 0xffff
  }
  def readLong(address: Int) = {
    if (debug) printf("reading L from %s, address: $%04x !!\n", tag, address)
    retval
  }
  def writeByte(address: Int, value: Int) {
    if (debug)
      printf("writing B to %s, address: $%04x = #$%04x !!\n", tag, address, value)
  }
  def writeShort(address: Int, value: Int) {
    if (debug)
      printf("writing W to %s, address: $%04x = #$%04x !!\n", tag, address, value)
  }
  def writeLong(address: Int, value: Int) {
    if (debug)
      printf("writing L to %s, address: $%04x = #$%04x !!\n", tag, address, value)
  }
}

/**
 */
class RandomAccessMemory(offset: Int, memsize: Int) extends AddressSpace {
  val byteArray  = new Array[Byte](memsize)
  val mem        = ByteBuffer.wrap(byteArray)
  val moduloMask = memsize - 1
  var debug      = false

  def start      = offset
  def size       = memsize

  private def indexOfAddress(address: Int) = (address - offset) & moduloMask

  def readByte(address: Int) = {
    if (debug)
      printf("reading B from RAM address: $%04x = #$%04x\n", address,
              mem.get(indexOfAddress(address)).asInstanceOf[Int])
    mem.get(indexOfAddress(address)).asInstanceOf[Int]
  }
  def readShort(address: Int) = {
    if (debug)
      printf("reading W from RAM address: $%04x = #$%04x\n", address,
              mem.getShort(indexOfAddress(address)).asInstanceOf[Int])
    mem.getShort(indexOfAddress(address)).asInstanceOf[Int]
  }
  def readLong(address: Int) = {
    if (debug)
      printf("reading L from RAM address: $%04x = #$%04x\n", address,
              mem.getInt(indexOfAddress(address)))
    mem.getInt(indexOfAddress(address))
  }
  def writeByte(address: Int, value: Int) {
    if (debug) printf("writing B to RAM address: %04x = %04x !!\n", address, value)
    mem.put(indexOfAddress(address), value.asInstanceOf[Byte])
  }
  def writeShort(address: Int, value: Int) {
    if (debug) printf("writing W to RAM address: $%04x = %04x !!\n", address, value)
    mem.putShort(indexOfAddress(address), value.asInstanceOf[Short])
  }
  def writeLong(address: Int, value: Int) {
    if (debug) printf("writing L to RAM address: $%04x = %04x !!\n", address, value)
    mem.putInt(indexOfAddress(address), value)
  }
}

/**
 * Memory factory, might not be needed anymore. Creates the Kickstart ROM
 * from a File.
 */
object MemoryFactory {
  def readKickstartFromFile(file: File, offset: Int): ReadOnlyMemory = {
    var fis : FileInputStream = null
    try {
      fis = new FileInputStream(file)
      val memsize = file.length.asInstanceOf[Int]
      val byteArray = new Array[Byte](memsize)
      fis.read(byteArray)
      val mem = ByteBuffer.wrap(byteArray)
      new ReadOnlyMemory(mem, offset, memsize)
    } catch {
      case e => e.printStackTrace
      null
    } finally {
      if (fis != null) fis.close
    }
  }
}
