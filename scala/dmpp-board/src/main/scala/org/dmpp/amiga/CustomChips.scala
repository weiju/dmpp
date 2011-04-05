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
import org.dmpp.common.AddressSpace

trait ICustomChipReg {
  def name : String
  def value_=(value: Int)
  def value: Int
}

abstract class CustomChipWriteRegister(val name: String) extends ICustomChipReg {
  def value: Int = {
    throw new UnsupportedOperationException("%s is write-only".format(name))
  }
}
abstract class CustomChipReadRegister(val name: String) extends ICustomChipReg {
  def value_=(value: Int) {
    throw new UnsupportedOperationException("%s is read".format(name))
  }
}
abstract class CustomChipStrobeRegister(val name: String) extends ICustomChipReg {
  def value: Int = {
    throw new UnsupportedOperationException("%s illegal read from strobe".format(name))
  }
}
case class BogusRegister(val name: String) extends ICustomChipReg {
  def value_=(value: Int) {
    throw new UnsupportedOperationException("%s write not supported".format(name))
  }
  def value: Int = {
    throw new UnsupportedOperationException("%s read not supported".format(name))
  }
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
class CustomAddressSpace(interruptController: InterruptController,
                         dmaController: DmaController,
                         video: Video,
                         copper: Copper,
                         blitter: Blitter)
extends AddressSpace {
  val REGISTER_START = 0xdff000
  val NUM_REGISTERS  = 256 // this is actually the number of ECS registers
  
  val registers = Array[ICustomChipReg](
    new CustomChipRegister("BLTDDAT"),
    dmaController.DMACONR,
    video.VPOSR,                        video.VHPOSR,
    new CustomChipRegister("DSKDATR"),  new CustomChipRegister("JOY0DAT"),
    new CustomChipRegister("JOY1DAT"),  new CustomChipRegister("CLXDAT"),
    new CustomChipRegister("ADKCONR"),  new CustomChipRegister("POT0DAT"),
    new CustomChipRegister("POT1DAT"),  new CustomChipRegister("POTGOR"),
    new CustomChipRegister("SERDATR"),  new CustomChipRegister("DSKBYTR"),
    interruptController.INTENAR,
    interruptController.INTREQR,
    new CustomChipRegister("DSKPTH"),   new CustomChipRegister("DSKPTL"),
    new CustomChipRegister("DSKLEN"),   new CustomChipRegister("DSKDAT"),
    new CustomChipRegister("REFPTR"),   new CustomChipRegister("VPOSW"),
    new CustomChipRegister("VHPOSW"),   copper.COPCON,
    new CustomChipRegister("SERDAT"),   new CustomChipRegister("SERPER"),
    new CustomChipRegister("POTGO"),    new CustomChipRegister("JOYTEST"),
    new CustomChipRegister("STREQU"),   new CustomChipRegister("STRVBL"),
    new CustomChipRegister("STRHOR"),   new CustomChipRegister("STRLONG"),
    blitter.BLTCON0,                    blitter.BLTCON1,
    new CustomChipRegister("BLTAFWM"),  new CustomChipRegister("BLTALWM"),
    new CustomChipRegister("BLTCPTH"),  new CustomChipRegister("BLTCPTL"),
    new CustomChipRegister("BLTBPTH"),  new CustomChipRegister("BLTBPTL"),
    new CustomChipRegister("BLTAPTH"),  new CustomChipRegister("BLTAPTL"),
    new CustomChipRegister("BLTDPTH"),  new CustomChipRegister("BLTDPTL"),
    blitter.BLTSIZE,                    new CustomChipRegister("BLTCON0L"),
    new CustomChipRegister("BLTSIZV"),  new CustomChipRegister("BLTSIZH"),
    new CustomChipRegister("BLTCMOD"),  new CustomChipRegister("BLTBMOD"),
    new CustomChipRegister("BLTAMOD"),  new CustomChipRegister("BLTDMOD"),
    BogusRegister("UNDEF00"),           BogusRegister("UNDEF01"),
    BogusRegister("UNDEF02"),           BogusRegister("UNDEF03"),
    new CustomChipRegister("BLTCDAT"),  new CustomChipRegister("BLTBDAT"),
    new CustomChipRegister("BLTADAT"),  BogusRegister("UNDEF04"),
    new CustomChipRegister("SPRHDAT"),  BogusRegister("UNDEF05"),
    new CustomChipRegister("DENISEID"),
    new CustomChipRegister("DSKSYNC"), 
    copper.COP1LCH,                     copper.COP1LCL,
    copper.COP2LCH,                     copper.COP2LCL,
    copper.COPJMP1,                     copper.COPJMP2,
    copper.COPINS,
    video.DIWSTRT,                      video.DIWSTOP,
    video.DDFSTRT,                      video.DDFSTOP,
    dmaController.DMACON,
    new CustomChipRegister("CLXCON"),
    interruptController.INTENA,
    interruptController.INTREQ,
    new CustomChipRegister("ADKCON"),
    new CustomChipRegister("AUD0LCH"),  new CustomChipRegister("AUD0LCL"),
    new CustomChipRegister("AUD0LEN"),  new CustomChipRegister("AUD0PER"),
    new CustomChipRegister("AUD0VOL"),  new CustomChipRegister("AUD0DAT"),
    BogusRegister("UNDEF06"),           BogusRegister("UNDEF07"),
    new CustomChipRegister("AUD1LCH"),  new CustomChipRegister("AUD1LCL"),
    new CustomChipRegister("AUD1LEN"),  new CustomChipRegister("AUD1PER"),
    new CustomChipRegister("AUD1VOL"),  new CustomChipRegister("AUD1DAT"),
    BogusRegister("UNDEF08"),           BogusRegister("UNDEF09"),
    new CustomChipRegister("AUD2LCH"),  new CustomChipRegister("AUD2LCL"),
    new CustomChipRegister("AUD2LEN"),  new CustomChipRegister("AUD2PER"),
    new CustomChipRegister("AUD2VOL"),  new CustomChipRegister("AUD2DAT"),
    BogusRegister("UNDEF10"),           BogusRegister("UNDEF11"),
    new CustomChipRegister("AUD3LCH"),  new CustomChipRegister("AUD3LCL"),
    new CustomChipRegister("AUD3LEN"),  new CustomChipRegister("AUD3PER"),
    new CustomChipRegister("AUD3VOL"),  new CustomChipRegister("AUD3DAT"),
    BogusRegister("UNDEF12"),           BogusRegister("UNDEF13"),
    new CustomChipRegister("BPL1PTH"),  new CustomChipRegister("BPL1PTL"),
    new CustomChipRegister("BPL2PTH"),  new CustomChipRegister("BPL2PTL"),
    new CustomChipRegister("BPL3PTH"),  new CustomChipRegister("BPL3PTL"),
    new CustomChipRegister("BPL4PTH"),  new CustomChipRegister("BPL4PTL"),
    new CustomChipRegister("BPL5PTH"),  new CustomChipRegister("BPL5PTL"),
    new CustomChipRegister("BPL6PTH"),  new CustomChipRegister("BPL6PTL"),
    BogusRegister("UNDEF14"),           BogusRegister("UNDEF15"),
    BogusRegister("UNDEF16"),           BogusRegister("UNDEF17"),
    video.BPLCON0, video.BPLCON1,       video.BPLCON2, video.BPLCON3,
    video.BPL1MOD,                      video.BPL2MOD,
    BogusRegister("UNDEF18"),           BogusRegister("UNDEF19"),
    new CustomChipRegister("BPL1DAT"),  new CustomChipRegister("BPL2DAT"),
    new CustomChipRegister("BPL3DAT"),  new CustomChipRegister("BPL4DAT"),
    new CustomChipRegister("BPL5DAT"),  new CustomChipRegister("BPL6DAT"),
    BogusRegister("UNDEF20"),           BogusRegister("UNDEF21"),
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
    video.COLOR( 0), video.COLOR( 1),   video.COLOR( 2), video.COLOR( 3),
    video.COLOR( 4), video.COLOR( 5),   video.COLOR( 6), video.COLOR( 7),
    video.COLOR( 8), video.COLOR( 9),   video.COLOR(10), video.COLOR(11),
    video.COLOR(12), video.COLOR(13),   video.COLOR(14), video.COLOR(15),
    video.COLOR(16), video.COLOR(17),   video.COLOR(18), video.COLOR(19),
    video.COLOR(20), video.COLOR(21),   video.COLOR(22), video.COLOR(23),
    video.COLOR(24), video.COLOR(25),   video.COLOR(26), video.COLOR(27),
    video.COLOR(28), video.COLOR(29),   video.COLOR(30), video.COLOR(31),
    new CustomChipRegister("HTOTAL"),   new CustomChipRegister("HSSTOP"),
    new CustomChipRegister("HBSTRT"),   new CustomChipRegister("HBSTOP"),
    new CustomChipRegister("VTOTAL"),   new CustomChipRegister("VSSTOP"),
    new CustomChipRegister("VBSTRT"),   new CustomChipRegister("VBSTOP"),
    BogusRegister("RSRVD01"),           BogusRegister("RSRVD02"),
    BogusRegister("RSRVD03"),           BogusRegister("RSRVD04"),
    BogusRegister("RSRVD05"),           BogusRegister("RSRVD06"),
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

