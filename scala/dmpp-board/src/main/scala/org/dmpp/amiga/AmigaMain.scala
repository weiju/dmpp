/**
 * Created on September 27, 2009
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
import java.io._

import scala.swing._
import java.awt.Dimension
import java.awt.event._

// User stack pointer seems to be initialized with $1986
object AmigaMain extends SwingApplication {
  var amiga = new Amiga
  val UseDebugger = false
  val NumInstructions = 1265454 // UAE triggers a blitter done interrupt here
/*
  val NUM_INSTRUCTIONS = 1265453 // currently working
  val NUM_INSTRUCTIONS = 1265600 // From here, beam movement is expected
  val NUM_INSTRUCTIONS = 1266500 // beam synchronization loop end
  val NUM_INSTRUCTIONS = 1277480 // CIA timer and TOD setup
  val NUM_INSTRUCTIONS = 1279000 // Audio system setup
  val NUM_INSTRUCTIONS = 1283970 // First vertical blank interrupt
  val NUM_INSTRUCTIONS = 1284150 // End of vertical blank interrupt
  val NUM_INSTRUCTIONS = 1288000 // End of vertical blank interrupt
  val NUM_INSTRUCTIONS = 2690000 // This is the target number !!!
*/
  val FastSkip = 1 until (NumInstructions - 8)

  def doOutput(i : Int) : Boolean = {
    if (FastSkip contains i) return false
    true
  }
  /* Title lines for assembly instruction line numbers */
  val LineTitles = Map(
    262147 -> "******* END OF INITIAL WAIT LOOP *********",
    262164 -> "******* INIT EXCEPTION VECTORS *********",
    262270 -> "******* INIT EXEC_BASE *********",
    262276 -> "******* CHECK EXPANSION RAM *********",
    262296 -> "******* CHECK CHIP MEMORY *********",
    263331 -> "******* CLEAR CHIP MEMORY *********",
    525389 -> "******* CHIP MEMORY CLEARED *********",
    525684 -> "******* INITIALIZE EXEC LISTS *********",
    525685 -> "******* INSTALL TRAP HANDLERS *********",
    525870 -> "******* PROCESS REL VECTOR INIT TABLE *********",
    526718 -> "******* ADD CHIPMEM TO FREE LIST *********",
    526774 -> "******* Add Exec to Library List *********",
    530030 -> "******* EXIT SUPERVISOR MODE *********",
    1256991 -> "******* Switch off LED *********",
    1260399 -> "******* Write CIA-B, Port A DTR/RTS *********",
    1261114 -> "******* Initializing disk drives *********",
    1261148 -> "******* Setting ICR 1 *********",
    1261181 -> "******* Setting ICR 2 *********",
    // this only works if no external floppies are configured
    1261207 -> "******* Checking Floppy 1 *********",
    1261222 -> "******* Floppy ID START 1 *********",
    1261232 -> "******* Check Floppy Ready flag 1 *********",
    1261700 -> "******* End of ext floppy check *********"
  )
  // Title lines for Kickstart addresses
  val PcTitles = Map(
    0x5ec    -> "******* Permit() *********",
    0xfc03cc -> "******* SETUP EXCEPTION VECTORS FOR NORMAL OPERATION *********",
    0xfc0422 -> "******* ENABLE DMA AND INTERRUPTS *********",
    0xfc08aa -> "******* Supervisor(code) *********",
    0xfc0900 -> "******* ROM SCANNER *********",
    0xfc0948 -> "******* SCAN FOR ROM TAGS *********",
    0xfc091c -> "******* InitCode() *********",
    0xfc0af0 -> "******* END OF TABLE, STORE RESULTS *********",
    0xfc0b28 -> "******* InitResident(resident, seglist) *********",
    0xfc0bc8 -> "******* InitStruct(initTable, memory, size) *********",
    0xfc0e86 -> "******* Schedule() *********",
    0xfc125c -> "******* SETUP EXEC INTERRUPT HANDLERS *********",
    0xfc12c8 -> "******* SET ADDRESS OF INTERRUPT HANDLER *********",
    0xfc1498 -> "******* SumLibrary(a1) *********",
    0xfc14ec -> "******* MakeLibrary(vectors, structure, init...) *********",
    0xfc1576 -> "******* d0 = MakeFunctions(a1, a2) *********",
    0xfc15d8 -> "******* AddHead(list, node) *********",
    0xfc1600 -> "******* Remove(node) *********",
    0xfc1634 -> "******* Enqueue() *********",
    0xfc165a -> "******* FindName(start, name) *********",
    0xfc169c -> "******* Allocate() *********",
    0xfc1704 -> "******* Deallocate() *********",
    0xfc1794 -> "******* AllocMem() *********",
    0xfc191e -> "******* memlist = AllocEntry() *********",
    0xfc19ea -> "******* error = AddMemList() *********",
    0xfc1c48 -> "******* AddTask(task, initialPC, finalPC) *********",
    0xfc21f8 -> "******* RawIOInit() *********",
    0xfc22fa -> "******* INITIALIZE ROM-WACK DEBUGGER *********",
    0xfc2472 -> "******* INITIALIZE ROM-WACK DATA *********"
  )

  def printLineTitle(lineno : Int) {
    if (LineTitles contains lineno) println(LineTitles(lineno))
  }

  def printPcTitle(pc : Int) {
    if (PcTitles contains pc) println(PcTitles(pc))
  }

  def initAmiga(romfile : String) {
    // Amiga Boot ROM address
    println("Amiga Preservation Project Version 1.0")
    amiga.init(romfile)
  }

  def runExec {
    val cpu = amiga.cpu
    var cycles : Long = 0
    val startTime = System.currentTimeMillis
    for (i <- 1 to NumInstructions) {
       // for breaking the program at some defined pc
      if (cpu.getPC == 0xfc0900 && i > 530635) {
        printf("EXIT AT LINE %d\n", i)
        System.exit(0)
      }
       try {
         val printOutput = doOutput(i)
         //amiga.chipmem.DEBUG = printOutput || i > 525383
         //amiga.chipmem.DEBUG = printOutput || i > 1257000
         printLineTitle(i)
         //printPcTitle(cpu.getPC)
         amiga.dmaController.doDmaWithStolenCpuCycles
         if (printOutput) printf("\n%d - $%04x: ", i, cpu.getPC)
         val instr = cpu.nextInstruction
         if (printOutput) {
           print(instr)
           print(" (# cycles: " + instr.numCycles +")")
           printf(" [$%04x]\n", cpu.currentInstrWord)
         }
         instr.execute
         cycles += instr.numCycles
         amiga.doCycles(instr.numCycles)

         if (printOutput) {
           printf("vpos: %02x hpos: %02x ", amiga.video.vpos,
                          (amiga.video.hpos >>> 1))
           print(cpu.getState)
           println("---------------------------------------------------------")
         }
       } catch {
         case ex : Exception =>
           ex.printStackTrace
           printf("Exception thrown at line %d, pc: %04x\n", i, cpu.getPC - 2)
           System.exit(0)
       }
    }
    val endTime = System.currentTimeMillis
    printf("elapsed time: %d ms, # cycles: %d.", (endTime - startTime), cycles)
  }

  // The Power LED
  var powerled = new Label("Power") {
    foreground = java.awt.Color.RED
  }

  def connectPowerLED {
    // Initialize listeners to receive messages from system components
    amiga.ciaA.addListener(new CiaChangeListener {
      override def praOutput(value : Int) = {
        if ((value & 0x02) == 0x02) {
          powerled.foreground = java.awt.Color.RED
        } else {
          powerled.foreground = java.awt.Color.BLACK
        }
      }
      override def prbOutput(value : Int) = { }
      override def ciaRegisterChanged(regnum: Int, value: Int) { }
    })
  }
  def openFrame {
    val frame = new MainFrame {
      val OVERSCAN_WIDTH       = 736
      val OVERSCAN_HEIGHT_NTSC = 482
      val OVERSCAN_HEIGHT_PAL  = 566

      title = "Dream Machine Preservation Project Version 0.01"
      var mainpanel = new BoxPanel(Orientation.Vertical)

      // Main display area, NTSC mode for now
      val _mainview = new javax.swing.JComponent {
        override def paintComponent(g: java.awt.Graphics) = {
          // TODO: Here comes the interesting stuff
          g.setColor(java.awt.Color.BLACK)
          g.fillRect(0, 0, OVERSCAN_WIDTH, OVERSCAN_HEIGHT_NTSC)
        }
      }
      val mainview = Component.wrap(_mainview);
      mainview.preferredSize = new Dimension(OVERSCAN_WIDTH,
                                             OVERSCAN_HEIGHT_NTSC)
      mainpanel.contents += mainview

      // Control panel
      var controlPanel =
        new FlowPanel(scala.swing.FlowPanel.Alignment.Left)(powerled)
      mainpanel.contents += controlPanel
      contents = mainpanel

      // events
      connectPowerLED
      peer.addWindowListener(new WindowAdapter {
        override def windowOpened(e: WindowEvent) = {
          println("COMPONENT_SHOWN()")
          // run Amiga in separate thread
          val execThread = new Thread {
            override def run = runExec
          }
          execThread.start
        }
      })
    }
    frame.visible = true
  }

  def isMacOsX = System.getProperty("mrj.version") != null

  def setMacOsXProperties {
    if (isMacOsX) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode",
          "false");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          "DMPP");
    }
  }

  // main method
  def startup(args : Array[String]) {
    setMacOsXProperties
    val romfile = args(0)
    printf("ROM file: %s\n", romfile)
    initAmiga(romfile)
    val useDebugger =
      "true" == System.getProperty("USE_DEBUGGER", String.valueOf(UseDebugger))
    printf("USE_DEBUGGER: %b\n", useDebugger)
    openFrame
    if (useDebugger) {
      val debugger = new DebugFrame(amiga)
      debugger.visible = true
    }
  }
}
