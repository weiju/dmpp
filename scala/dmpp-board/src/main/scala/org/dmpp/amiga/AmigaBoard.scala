/**
 * Created on October 6, 2009
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
import org.mahatma68k._
import org.dmpp.cymus._
import java.io.File

/**
 * This file defines the integration of the system components on an Amiga
 * main board.
 * All memory sizes need to be powers of two because the default behaviour of
 * a memory object is to locical-and the access address with the size to
 * emulate the incomplete address decoding scheme of the components.
 */

/**
 * For now, this is the configuration of an Amiga computer.
 * Memory map:
 * - custom chips: 0xc00000-0xdfffff
 * - CIA:          0xa00000-0xbfffff
 * - Autoconfig:   0x200000-0x9fffff (w/fast ram, we start later) -> nothing
 * - Kickmem:      0xfc0000-0xffffff 256 K
 * - UAE defines rom tag area, but Exec accesses $f00000-$f7ffff:
 * - 0xf00000-0xf0ffff how does that work ?
 * - what about the area 0xf10000-f7ffff ??? why is that scanned ?
 * - autoconf mem:  0xe80000-0xe8ffff
 * optional:
 * - slow mem:     0xc00000-0xdbffff
 * - rt clock:     0xdc0000-0xdcffff
 * we currently use following simplifications:
 * - romtag memory responds with 0's
 * - autoconf mem responds with 0's  
 */
object Amiga {
  val CIAStart         = 0xbf0000
  val CIASize          = 0x10000

  // 512 Megabytes of Chip memory for now
  val SizeChipMemory   = 0x80000

  // This is true for 256K ROM, 512K ROM will need to expand
  // into the area from $fc0000-$ffffff
  val ROMStart         = 0xfc0000
  val Kick13ExecStart  = 0xfc00d2

  // Since the standard Motorola 68000 has an address space of 24 Bit,
  // there are 256 address banks, with the highest address being
  // occupied by the Kickstart ROM. An address is mapped to a bank by
  // shifting it 4 bytes to the right. 
  val AddressBanks     = 0x100
}

class Amiga extends AddressSpace {

  import Amiga._
  // Peripherals, listed first, so we can connect them to the chips
  // in their constructor.
  val floppyController    = new FloppyController
  val interruptController = new InterruptController
  val dmaController       = new DmaController
  val video               = new Video(interruptController)
  val addressMap          = new Array[AddressSpace](AddressBanks)

  // It is useful to be able to access CIA, custom chips and chip memory
  // directly, so we expose them here
  val cpu                 = new Cpu
  val copper              = new Copper
  val blitter             = new Blitter
  val ciaSpace            = new CiaSpace
  val customSpace         = new CustomAddressSpace(interruptController,
                                                   dmaController,
                                                   video,
                                                   copper,
                                                   blitter)
  val chipmem             = new RandomAccessMemory(0, SizeChipMemory)
  val dummymem            = new DummyAddressSpace("Dummy")

  // $e80000 is Autoconfig, UAE returns all 11111's here
  val autoConf = new DummyAddressSpace("[AutoConf]", 0xffffff)

  autoConf.debug = false
  val romtagMem = new DummyAddressSpace("[RomTagArea]")
  romtagMem.debug = false

  def ciaA = ciaSpace.ciaA
  def ciaB = ciaSpace.ciaB
  def customChipRegisters = customSpace.registers
  var kickrom : ReadOnlyMemory = null
  var kickromOverlayMode = false

  def init(filename : String) = {
    interruptController.cpu = cpu
    dmaController.amiga = this
    // initialize global address space
    for (i <- 0 to AddressBanks - 1) addressMap(i) = dummymem
    kickrom = MemoryFactory.readKickstartFromFile(new File(filename),
                                                  ROMStart)
    println("MAPPING KICKSTART ROM...")
    mapAddressSpaceToRange(kickrom, 0xfc, 0xff)
    mapAddressSpaceToRange(romtagMem, 0xf0, 0xf7)
    println("MAPPING CIA SPACE...")
    addAddressSpace(ciaSpace)
    println("MAPPING AUTOCONFIG SPACE...")
    mapAddressSpaceToRange(autoConf, 0xe8, 0xef)

    // the currenly 512KB of chip memory are mapped within the address
    // range of $000000-$0fffff, so it responds within the first MB of
    // the address space.
    println("MAPPING CHIP MEMORY...")
    mapAddressSpaceToRange(chipmem, 0x00, 0x0f)

    // map custom chip space at $C00000-$DFFFFF. Exec tests this area
    // for expansion memory (slow mem). If the expansion is absent, the
    // custom chips will respond here
    println("MAPPING CUSTOM CHIP REGISTERS...")
    mapAddressSpaceToRange(customSpace, 0xc0, 0xc0 + 0x1f)

    cpu.setAddressSpace(this)
    initCIAs
    cpu.setPC(readLong(0x04)) // System start address
    printf("Initial long word of Kickstart: %08x\n", readLong(0x00))

    floppyController.connect(ciaA, ciaB)
    
    // initialize Copper
    copper.video        = video
    copper.addressSpace = this
  }

  def initCIAs() {
    import org.dmpp.cymus.AbstractCia._

    // Initialize listeners to receive messages from system components
    ciaA.addListener(new CiaChangeListener {
      private def overlayBitSet(value : Int) = (value & 0x01) == 1
      private def overlayKickstart = {
        println("OVERLAY KICKSTART")
        mapAddressSpaceToRange(kickrom, 0, (kickrom.size - 1) >> 16)
        kickromOverlayMode = true
      }
      private def overlayRAM = {
        println("OVERLAY CHIPRAM")
        mapAddressSpaceToRange(chipmem, 0, (kickrom.size - 1) >> 16)
        kickromOverlayMode = false
      }

      override def praOutput(value: Int) = {
        printf("CIA A PRA output = value: %04x\n", value)
        // Check overlay bit and remap if necessary
        if (!kickromOverlayMode && overlayBitSet(value)) {
          overlayKickstart
        } else if (kickromOverlayMode && !overlayBitSet(value)) {
          overlayRAM
        }
      }
      override def prbOutput(value: Int) = {
        printf("CIA A PRB output = value: %04x\n", value)
      }
      override def ciaRegisterChanged(regnum: Int, value: Int) { }
    })
    ciaB.addListener(new CiaChangeListener {      
      override def praOutput(value: Int) = {
        printf("CIA B PRA output = value: %04x\n", value)
      }
      override def prbOutput(value: Int) = {
        printf("CIA B PRB output = value: %04x\n", value)
      }
      override def ciaRegisterChanged(regnum: Int, value: Int) { }
    })
    ciaA.setIrqListener(new IrqListener {
      def irqRequested {
        // delegate to interrupt controller for CIA-A (Bit 3)
        interruptController.intreq.value = 1 << 3
      }
    })
    ciaB.setIrqListener(new IrqListener {
      def irqRequested {
        // delegate to interrupt controller for CIA-B (Bit 13)
        interruptController.intreq.value = 1 << 13
      }
    })
    // Initial component states
    ciaA.portAPins = 0x20 // set /RDY 
    ciaA.setRegister(DDRA, 3) // intitial DDRA-A state
    ciaA.setRegister(PRA, 0x01) // set OVL
    ciaB.setRegister(DDRA, 0xff)
    ciaB.setRegister(DDRB, 0xff)
  }

  def mapAddressSpaceToRange(addressSpace: AddressSpace, startIndex: Int,
                             endIndex: Int) = {
    for (i <- startIndex to endIndex) addressMap(i) = addressSpace
  }
  def addAddressSpace(addressSpace : AddressSpace) = {
    mapAddressSpaceToRange(addressSpace, addressSpace.start >> 16,
                           (addressSpace.start + addressSpace.size - 1) >> 16)
  }

  // AddressSpace interface
  def getAddressSpace(address: Int) = addressMap(address >> 16)

  val start = 0
  val size = 0
  def readByte(address: Int) = {
    getAddressSpace(address).readByte(address)
  }
  def readShort(address: Int) = {
    getAddressSpace(address).readShort(address)
  }
  def readLong(address: Int) = {
    getAddressSpace(address).readLong(address)
  }
  def writeByte(address: Int, value: Int) = {
    getAddressSpace(address).writeByte(address, value)
  }
  def writeShort(address: Int, value: Int) = {
    getAddressSpace(address).writeShort(address, value)
  }
  def writeLong(address: Int, value: Int) = {
    getAddressSpace(address).writeLong(address, value)
  }
  /**
   * Increment the system's clock, excluding the CPU.
   */
  def doCycles(numCycles: Int) {
    video.doCycles(numCycles)
    // Until we really understand what happens, pulse each CIA counter pin
    // separately
    ciaA.pulseAll(numCycles)
    ciaB.pulseAll(numCycles)
    dmaController.doDmaWithCpu(numCycles)
  }
}
