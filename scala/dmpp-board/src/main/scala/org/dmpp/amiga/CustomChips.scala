/**
 * Created on October 1st, 2009
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
import org.mahatma68k.AddressSpace

trait ICustomChipReg {
  def name : String
  def value_=(value: Int)
  def value: Int
}

// This class should not implement ICustomChipReg and it only does so to
// act as a placeholder for now. Later, we have
// - pseudo read
// - early read
// - strobe
// - read
// - write
// - color
class CustomChipRegister(val name: String) extends ICustomChipReg {
  var value: Int = 0
}

class CustomChipRegisterR(val name: String, reg: ICustomChipReg)
extends ICustomChipReg {
  def value = reg.value
  def value_=(value: Int) { throw new Exception("setValue() not allowed") }
}
class CustomChipRegisterW(val name: String, reg: ICustomChipReg)
extends ICustomChipReg {
  def value_=(aValue: Int) {
    // set/clr
    if ((aValue & 0x8000) == 0x8000) { // set bit set
      reg.value |= (aValue & 0x7fff)
    } else {
      reg.value &= ~aValue
    }
    printf("REGISTER VALUE IS NOW: $%04x\n", reg.value & 0xffff)
  }
  def value = { throw new Exception("value() not allowed") }
}

// **********************************************************************
// **** VIDEO/PLAYFIELD REGISTERS
// **** TODO: Integrated them directly into the Video class
// **********************************************************************
/* VPOSR: Most significant bit of vertical beam position */
class VPosReader(video: Video) extends ICustomChipReg {
  def name = "VPOSR"
  def value_=(aValue: Int) { }
  def value = {
    val result = (video.vpos >> 8) & 0x01
    printf("VPOSR = %02x\n", result)
    result
  }
}
/*
 VHPOSR: vertical beam position, least significant bit in hi-byte
 horizontal beam position in lo-byte
*/
class VHPosReader(video: Video) extends ICustomChipReg {
  def name = "VHPOSR"
  def value_=(aValue: Int) { println("writing to VHPOSR not allowed") }
  def value = {
    val result = ((video.vpos & 0xff) << 8) | video.hclocks
    printf("VHPOSR = %02x (vpos = %d, hpos = %d)\n", result, video.vpos,
           video.hclocks)
    result
  }
}

abstract class VideoWriteRegister(val name: String, video: Video)
extends ICustomChipReg {
  def value = {
    throw new Exception("reading value from " + name +" not allowed")
  }
}
class BplCon0(video: Video) extends VideoWriteRegister("BPLCON0", video) {
  def value_=(aValue: Int) { video.bplcon0 = aValue }
}
class BplCon1(video: Video) extends VideoWriteRegister("BPLCON1", video) {
  def value_=(aValue: Int) { video.bplcon1 = aValue }
}
class BplCon2(video: Video) extends VideoWriteRegister("BPLCON2", video) {
  def value_=(aValue: Int) { video.bplcon2 = aValue }
}
class BplCon3(video: Video) extends VideoWriteRegister("BPLCON3", video) {
  def value_=(aValue: Int) { video.bplcon3 = aValue }
}
class Bpl1Mod(video: Video) extends VideoWriteRegister("BPL1MOD", video) {
  def value_=(aValue: Int) { video.bpl1mod = aValue }
}
class Bpl2Mod(video: Video) extends VideoWriteRegister("BPL2MOD", video) {
  def value_=(aValue: Int) { video.bpl2mod = aValue }
}
class DiwStrt(video: Video) extends VideoWriteRegister("DIWSTRT", video) {
  def value_=(aValue: Int) { video.diwstrt = aValue }
}
class DiwStop(video: Video) extends VideoWriteRegister("DIWSTOP", video) {
  def value_=(aValue: Int) { video.diwstop = aValue }
}
class DdfStrt(video: Video) extends VideoWriteRegister("DDFSTRT", video) {
  def value_=(aValue: Int) { video.ddfstrt = aValue }
}
class DdfStop(video: Video) extends VideoWriteRegister("DDFSTOP", video) {
  def value_=(aValue: Int) { video.ddfstop = aValue }
}

// **********************************************************************
// **** REGISTER ADDRESS SPACE
// **********************************************************************
trait CustomChipChangeListener {
  def customChipRegisterChanged(regnum: Int, value: Int)
}

/**
 * The address space for the custom chips. Register assignment per chip is
 * not corresponding to memory addresses, furthermore, a register can be
 * assigned to more than one chip. Instead, we regard all custom chips as
 * a single "super chip" and delegate some functionality which clearly
 * belongs to a subsystem (e.g. Copper, Blitter...)
 */
object CustomChipRegisters {
  val VPOSR  = 2
  val VHPOSR = 3
}
class CustomAddressSpace(interruptController: InterruptController,
                         dmaController: DmaController,
                         video: Video, copper: Copper, blitter: Blitter)
extends AddressSpace {
  val REGISTER_START = 0xdff000
  val NUM_REGISTERS  = 256 // this is actually the number of ECS registers
  
  val registers = Array[ICustomChipReg](
    new CustomChipRegister("BLTDDAT"), 
    new CustomChipRegisterR("DMACONR",  dmaController.dmacon),
    new VPosReader(video),              new VHPosReader(video),
    new CustomChipRegister("DSKDATR"),  new CustomChipRegister("JOY0DAT"),
    new CustomChipRegister("JOY1DAT"),  new CustomChipRegister("CLXDAT"),
    new CustomChipRegister("ADKCONR"),  new CustomChipRegister("POT0DAT"),
    new CustomChipRegister("POT1DAT"),  new CustomChipRegister("POTGOR"),
    new CustomChipRegister("SERDATR"),  new CustomChipRegister("DSKBYTR"),
    new CustomChipRegisterR("INTENAR",  interruptController.intena), 
    new CustomChipRegisterR("INTREQR",  interruptController.intreq),
    new CustomChipRegister("DSKPTH"),   new CustomChipRegister("DSKPTL"),
    new CustomChipRegister("DSKLEN"),   new CustomChipRegister("DSKDAT"),
    new CustomChipRegister("REFPTR"),   new CustomChipRegister("VPOSW"),
    new CustomChipRegister("VHPOSW"),   copper.copcon,
    new CustomChipRegister("SERDAT"),   new CustomChipRegister("SERPER"),
    new CustomChipRegister("POTGO"),    new CustomChipRegister("JOYTEST"),
    new CustomChipRegister("STREQU"),   new CustomChipRegister("STRVBL"),
    new CustomChipRegister("STRHOR"),   new CustomChipRegister("STRLONG"),
    blitter.bltcon0,                    blitter.bltcon1,
    new CustomChipRegister("BLTAFWM"),  new CustomChipRegister("BLTALWM"),
    new CustomChipRegister("BLTCPTH"),  new CustomChipRegister("BLTCPTL"),
    new CustomChipRegister("BLTBPTH"),  new CustomChipRegister("BLTBPTL"),
    new CustomChipRegister("BLTAPTH"),  new CustomChipRegister("BLTAPTL"),
    new CustomChipRegister("BLTDPTH"),  new CustomChipRegister("BLTDPTL"),
    blitter.bltsize,                    new CustomChipRegister("BLTCON0L"),
    new CustomChipRegister("BLTSIZV"),  new CustomChipRegister("BLTSIZH"),
    new CustomChipRegister("BLTCMOD"),  new CustomChipRegister("BLTBMOD"),
    new CustomChipRegister("BLTAMOD"),  new CustomChipRegister("BLTDMOD"),
    new CustomChipRegister("UNDEF00"),  new CustomChipRegister("UNDEF01"),
    new CustomChipRegister("UNDEF02"),  new CustomChipRegister("UNDEF03"),
    new CustomChipRegister("BLTCDAT"),  new CustomChipRegister("BLTBDAT"),
    new CustomChipRegister("BLTADAT"),  new CustomChipRegister("UNDEF04"),
    new CustomChipRegister("SPRHDAT"),  new CustomChipRegister("UNDEF05"),
    new CustomChipRegister("DENISEID"),
    new CustomChipRegister("DSKSYNC"), 
    copper.cop1lch,                     copper.cop1lcl,
    copper.cop2lch,                     copper.cop2lcl,
    copper.copjmp1,                     copper.copjmp2,
    copper.copins,
    new DiwStrt(video),                 new DiwStop(video),
    new DdfStrt(video),                 new DdfStop(video),
    new CustomChipRegisterW("DMACON",   dmaController.dmacon),
    new CustomChipRegister("CLXCON"),
    new CustomChipRegisterW("INTENA",   interruptController.intena),
    new CustomChipRegisterW("INTREQ",   interruptController.intreq),
    new CustomChipRegister("ADKCON"),
    new CustomChipRegister("AUD0LCH"),  new CustomChipRegister("AUD0LCL"),
    new CustomChipRegister("AUD0LEN"),  new CustomChipRegister("AUD0PER"),
    new CustomChipRegister("AUD0VOL"),  new CustomChipRegister("AUD0DAT"),
    new CustomChipRegister("UNDEF06"),  new CustomChipRegister("UNDEF07"),
    new CustomChipRegister("AUD1LCH"),  new CustomChipRegister("AUD1LCL"),
    new CustomChipRegister("AUD1LEN"),  new CustomChipRegister("AUD1PER"),
    new CustomChipRegister("AUD1VOL"),  new CustomChipRegister("AUD1DAT"),
    new CustomChipRegister("UNDEF08"),  new CustomChipRegister("UNDEF09"),
    new CustomChipRegister("AUD2LCH"),  new CustomChipRegister("AUD2LCL"),
    new CustomChipRegister("AUD2LEN"),  new CustomChipRegister("AUD2PER"),
    new CustomChipRegister("AUD2VOL"),  new CustomChipRegister("AUD2DAT"),
    new CustomChipRegister("UNDEF10"),  new CustomChipRegister("UNDEF11"),
    new CustomChipRegister("AUD3LCH"),  new CustomChipRegister("AUD3LCL"),
    new CustomChipRegister("AUD3LEN"),  new CustomChipRegister("AUD3PER"),
    new CustomChipRegister("AUD3VOL"),  new CustomChipRegister("AUD3DAT"),
    new CustomChipRegister("UNDEF12"),  new CustomChipRegister("UNDEF13"),
    new CustomChipRegister("BPL1PTH"),  new CustomChipRegister("BPL1PTL"),
    new CustomChipRegister("BPL2PTH"),  new CustomChipRegister("BPL2PTL"),
    new CustomChipRegister("BPL3PTH"),  new CustomChipRegister("BPL3PTL"),
    new CustomChipRegister("BPL4PTH"),  new CustomChipRegister("BPL4PTL"),
    new CustomChipRegister("BPL5PTH"),  new CustomChipRegister("BPL5PTL"),
    new CustomChipRegister("BPL6PTH"),  new CustomChipRegister("BPL6PTL"),
    new CustomChipRegister("UNDEF14"),  new CustomChipRegister("UNDEF15"),
    new CustomChipRegister("UNDEF16"),  new CustomChipRegister("UNDEF17"),
    new BplCon0(video),                 new BplCon1(video),
    new BplCon2(video),                 new BplCon3(video),
    new Bpl1Mod(video),                 new Bpl2Mod(video),
    new CustomChipRegister("UNDEF18"),  new CustomChipRegister("UNDEF19"),
    new CustomChipRegister("BPL1DAT"),  new CustomChipRegister("BPL2DAT"),
    new CustomChipRegister("BPL3DAT"),  new CustomChipRegister("BPL4DAT"),
    new CustomChipRegister("BPL5DAT"),  new CustomChipRegister("BPL6DAT"),
    new CustomChipRegister("UNDEF20"),  new CustomChipRegister("UNDEF21"),
    new CustomChipRegister("SPR0PTH"),  new CustomChipRegister("SPR0PTL"),
    new CustomChipRegister("SPR1PTH"),  new CustomChipRegister("SPR1PTL"),
    new CustomChipRegister("SPR2PTH"),  new CustomChipRegister("SPR2PTL"),
    new CustomChipRegister("SPR3PTH"),  new CustomChipRegister("SPR3PTL"),
    new CustomChipRegister("SPR4PTH"),  new CustomChipRegister("SPR4PTL"),
    new CustomChipRegister("SPR5PTH"),  new CustomChipRegister("SPR5PTL"),
    new CustomChipRegister("SPR6PTH"),  new CustomChipRegister("SPR6PTL"),
    new CustomChipRegister("SPR7PTH"),  new CustomChipRegister("SPR7PTL"),
    new CustomChipRegister("SPR0POS"),  new CustomChipRegister("SPR0CTL"),
    new CustomChipRegister("SPR0DATA"), new CustomChipRegister("SPR0DATB"),
    new CustomChipRegister("SPR1POS"),  new CustomChipRegister("SPR1CTL"),
    new CustomChipRegister("SPR1DATA"), new CustomChipRegister("SPR1DATB"),
    new CustomChipRegister("SPR2POS"),  new CustomChipRegister("SPR2CTL"),
    new CustomChipRegister("SPR2DATA"), new CustomChipRegister("SPR2DATB"),
    new CustomChipRegister("SPR3POS"),  new CustomChipRegister("SPR3CTL"),
    new CustomChipRegister("SPR3DATA"), new CustomChipRegister("SPR3DATB"),
    new CustomChipRegister("SPR4POS"),  new CustomChipRegister("SPR4CTL"),
    new CustomChipRegister("SPR4DATA"), new CustomChipRegister("SPR4DATB"),
    new CustomChipRegister("SPR5POS"),  new CustomChipRegister("SPR5CTL"),
    new CustomChipRegister("SPR5DATA"), new CustomChipRegister("SPR5DATB"),
    new CustomChipRegister("SPR6POS"),  new CustomChipRegister("SPR6CTL"),
    new CustomChipRegister("SPR6DATA"), new CustomChipRegister("SPR6DATB"),
    new CustomChipRegister("SPR7POS"),  new CustomChipRegister("SPR7CTL"),
    new CustomChipRegister("SPR7DATA"), new CustomChipRegister("SPR7DATB"),
    new CustomChipRegister("COLOR00"),  new CustomChipRegister("COLOR01"),
    new CustomChipRegister("COLOR02"),  new CustomChipRegister("COLOR03"),
    new CustomChipRegister("COLOR04"),  new CustomChipRegister("COLOR05"),
    new CustomChipRegister("COLOR06"),  new CustomChipRegister("COLOR07"),
    new CustomChipRegister("COLOR08"),  new CustomChipRegister("COLOR09"),
    new CustomChipRegister("COLOR10"),  new CustomChipRegister("COLOR11"),
    new CustomChipRegister("COLOR12"),  new CustomChipRegister("COLOR13"),
    new CustomChipRegister("COLOR14"),  new CustomChipRegister("COLOR15"),
    new CustomChipRegister("COLOR16"),  new CustomChipRegister("COLOR17"),
    new CustomChipRegister("COLOR18"),  new CustomChipRegister("COLOR19"),
    new CustomChipRegister("COLOR20"),  new CustomChipRegister("COLOR21"),
    new CustomChipRegister("COLOR22"),  new CustomChipRegister("COLOR23"),
    new CustomChipRegister("COLOR24"),  new CustomChipRegister("COLOR25"),
    new CustomChipRegister("COLOR26"),  new CustomChipRegister("COLOR27"),
    new CustomChipRegister("COLOR28"),  new CustomChipRegister("COLOR29"),
    new CustomChipRegister("COLOR30"),  new CustomChipRegister("COLOR31"),
    new CustomChipRegister("HTOTAL"),   new CustomChipRegister("HSSTOP"),
    new CustomChipRegister("HBSTRT"),   new CustomChipRegister("HBSTOP"),
    new CustomChipRegister("VTOTAL"),   new CustomChipRegister("VSSTOP"),
    new CustomChipRegister("VBSTRT"),   new CustomChipRegister("VBSTOP"),
    new CustomChipRegister("RSRVD01"),  new CustomChipRegister("RSRVD02"),
    new CustomChipRegister("RSRVD03"),  new CustomChipRegister("RSRVD04"),
    new CustomChipRegister("RSRVD05"),  new CustomChipRegister("RSRVD06"),
    new CustomChipRegister("BEAMCON0"), new CustomChipRegister("HSSTRT"),
    new CustomChipRegister("VSSTRT"),   new CustomChipRegister("HCENTER"),
    new CustomChipRegister("DIWHIGH")
  )
  var listeners = new scala.collection.mutable.HashSet[CustomChipChangeListener]
  def addListener(listener: CustomChipChangeListener) = {
    listeners += listener
  }

  def start = 0xdf0000
  def size  = 0x10000

  // **********************************************************************
  // ****** ADDRESS SPACE INTERFACE
  // **********************************************************************
  def readByte(address: Int): Int = {
    (readShort(address) >>> 8) & 0xff
  }
  def readShort(address: Int): Int = {
    val regnum = (address & 0x0fff) >> 1
    printf("Reading Custom chip register: %s\n", registers(regnum).name)
    registers(regnum).value
  }
  def readLong(address: Int): Int = {
    (readShort(address) << 16) | readShort(address + 2)
  }
  def writeByte(address: Int, value: Int) = {
    println("Illegal access: Can not write byte to custom chip register")
  }
  def writeShort(address: Int, value: Int) = {
    // incomplete address decoding - only use the low 3 Bytes of the address
    val regnum = (address & 0x0fff) >> 1
    printf("Writing to Custom chip register: %s = %04x\n", registers(regnum).name,
           value & 0xffff)
    registers(regnum).value = value & 0xffff
    listeners.foreach(l => l.customChipRegisterChanged(regnum, value))
  }
  def writeLong(address: Int, value: Int) = {
    writeShort(address, value >> 16)
    writeShort(address + 2, value & 0xffff)
  }
}

