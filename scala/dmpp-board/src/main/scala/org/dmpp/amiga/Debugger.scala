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

import scala.swing._
import scala.swing.event._

import javax.swing.BorderFactory
import javax.swing.border._
import java.awt.Font
import java.awt.Dimension
import java.awt.Color
import java.awt.GridBagConstraints
import org.mahatma68k.Cpu
import org.dmpp.cymus._

trait ExecutionContext {
  var instructionNum : Int = 1
}
// **********************************************************************
// ***** CPU view
// **********************************************************************

class CpuView(amiga: Amiga) extends BoxPanel(Orientation.Vertical) {
  // Status register: (System byte | User byte)
  // System byte: T0 T1  S  M  0 I2 I1 I0
  // User byte:    0  0  0  X  N  Z  V  C
  val SRLabels = Array("T0", "T1", "S", "M", "-", "I2", "I1", "I0",
                       "0", "0", "0", "X", "N", "Z", "V", "C")

  val textFont = new Font("Monospaced", Font.PLAIN, 11)
  preferredSize = new Dimension(220, 200)
  val aregLabels = new Array[Label](8)
  val dregLabels = new Array[Label](8)

  val srLabel = createLabel(" SR: 0000 XCVFZ")
  val pcLabel = createLabel(" PC: $000000")
  val sspLabel = createLabel("SSP: $000000")
  val uspLabel = createLabel("USP: $000000")
  contents += pcLabel
  contents += srLabel
  contents += sspLabel
  contents += uspLabel
  contents += new Label(" ")
  for (i <- 0 to 7) {
    val label = createLabel(" d%d: #$00".format(i))
    dregLabels(i) = label
    contents += label
  }
  contents += new Label(" ")
  for (i <- 0 to 7) {
    val label = createLabel(" a%d: #$00".format(i))
    aregLabels(i) = label
    contents += label
  }
  border = BorderFactory.createTitledBorder("MC68000")
  update

  def createLabel(name: String): Label = {
    val label = new Label(name)
    label.font = textFont
    label.horizontalAlignment = Alignment.Left
    label
  }

  def getSRText: String = {
    val sr = amiga.cpu.getSR
    val buffer = new StringBuilder
    for (i <- 0 to 15) {
      if (((sr >> i) & 1) == 1) buffer.append(SRLabels(15 - i))
      else buffer.append("-")
      if (i == 7) buffer.append(" ")
    }
    buffer.reverse.toString
  }

  def update = {
    srLabel.text  = " SR: %s".format(getSRText)
    pcLabel.text  = " PC: $%04x".format(amiga.cpu.getPC)
    sspLabel.text = "SSP: $%04x".format(amiga.cpu.getSSP)
    uspLabel.text = "USP: $%04x".format(amiga.cpu.getUSP)
    for (i <- 0 to 7) {
      dregLabels(i).text = " d%d: #$%02x"
          .format(i, amiga.cpu.getDataRegisterValue(i))
    }
    for (i <- 0 to 7) {
      aregLabels(i).text = " a%d: #$%02x"
          .format(i, amiga.cpu.getAddressRegisterValue(i))
    }
  }
}

// **********************************************************************
// ***** CIA view
// **********************************************************************

abstract class CiaView(cia: Cia8520, startAddress: Int)
extends GridBagPanel with CiaChangeListener {
  val RegNames = Array("pra", "prb", "ddra", "ddrb", "talo", "tahi",
                       "tblo", "tbhi", "todlo", "todmid", "todhi", "",
                       "sdr", "icr", "cra", "crb")
  val StdLabels = Array("7", "6", "5", "4", "3", "2", "1", "0")
  val labels = Array.ofDim[Label](16, 8)
  val c = new Constraints(new GridBagConstraints)
  val smallFont = new Font("Dialog", Font.PLAIN, 9)
  val lineBorder = BorderFactory.createLineBorder(Color.BLACK, 1)
  val emptyBorder = BorderFactory.createEmptyBorder(3,3,3,3)

  c.fill = GridBagPanel.Fill.Both
  for (row <- 0 to 15) {
    c.gridy = row
    val label = new Label("$%06x %s: ".format(startAddress + row * 256,
                                              RegNames(row)))
    label.font = smallFont
    label.preferredSize = new Dimension(80, 14)
    add(label, c)

    for (col <- 0 to 7) {
      labels(row)(col) = createCiaRegBitLabel(registerLabels(row)(col))
      add(labels(row)(col), c)
    }
  }
  cia.addListener(this)

  // Implement this
  def registerLabels(row : Int) : Array[String]

  def createCiaRegBitLabel(title : String) = {
    val comp = new Label(title)
    comp.font = smallFont
    comp.preferredSize = new Dimension(40, 14)
    comp.border = new CompoundBorder(lineBorder, emptyBorder)
    comp.horizontalAlignment = Alignment.Center
    comp
  }

  override def praOutput(value : Int) { }
  override def prbOutput(value : Int) { }
  override def ciaRegisterChanged(regnum : Int, value : Int) = {
    "CIA register changed: %d = value: %04x\n".format(regnum, value)
    var mask = value
    for (i <- 0 to 7) {
      if (((mask >> i) & 1) == 1) {
        labels(regnum)(7 - i).background = Color.ORANGE
        labels(regnum)(7 - i).opaque = true
      } else {
        labels(regnum)(7 - i).opaque = false
      }
    }
  }
}

class CiaAView(cia : Cia8520, startAddress: Int) extends CiaView(cia, startAddress) {
  var PRABits : Array[String] = null
  for (regnum <- 0 to 15) ciaRegisterChanged(regnum, cia.getRegister(regnum))
  def registerLabels(row : Int) : Array[String] = {
    if (row == 0) {
      if (PRABits == null)
        PRABits = Array("/FIR1", "/FIR0", "/RDY", "/TK0", "/WPRO", "/CHNG",
                        "/LED", "OVL")
      return PRABits
    }
    StdLabels
  }
}
class CiaBView(cia: Cia8520, startAddress: Int) extends CiaView(cia, startAddress) {
  var PRABits : Array[String] = null
  var PRBBits : Array[String] = null
  for (regnum <- 0 to 15) ciaRegisterChanged(regnum, cia.getRegister(regnum))
  def registerLabels(row : Int) : Array[String] = {
    if (row == 0) {
      if (PRABits == null)
        PRABits = Array("DTR", "/RTS", "/CD", "/CTS", "/DSR", "SEL",
                        "POUT", "BUSY")
      return PRABits
    }
    if (row == 1) {
      if (PRBBits == null)
        PRBBits = Array("/MTR", "/SEL3", "/SEL2", "/SEL1", "/SEL0",
                        "/SIDE", "DIR", "/STEP")
      return PRBBits
    }
    StdLabels
  }
}

// **********************************************************************
// ***** Code view
// **********************************************************************

class CodeView(amiga: Amiga, cpuView: CpuView, context: ExecutionContext)
extends BorderPanel {
  val textFont = new Font("Monospaced", Font.PLAIN, 11)
  val textarea = new TextArea
  val scrollpane = new ScrollPane(textarea)
  val stepButton = new Button(">")
  val nstepButton = new Button("Run n steps:")
  val nstepField = new TextField(8)
  val toolbar = new FlowPanel(FlowPanel.Alignment.Left)(stepButton,
                                                        nstepButton,
                                                        nstepField)

  border = BorderFactory.createTitledBorder("Code")
  textarea.font = textFont
  scrollpane.preferredSize = new Dimension(550, 100)
  add(toolbar, BorderPanel.Position.North)
  add(scrollpane, BorderPanel.Position.Center)
  listenTo(stepButton, nstepButton)
  reactions += {
    case ButtonClicked(`stepButton`) =>
      executeInstruction(true)
    case ButtonClicked(`nstepButton`) =>
      val steps : Int = Integer.parseInt(nstepField.text)
      println("running n steps: " + steps)
      for (i <- 1 until steps) {
        executeInstruction(false)
      }
      executeInstruction(true)
  }

  def executeInstruction(doOutput : Boolean) = {
    val buffer = new StringBuilder
    if (doOutput) buffer.append("%08d $%04x: ".format(context.instructionNum,
                                                      amiga.cpu.getPC()))
    val instr = amiga.cpu.nextInstruction
    if (doOutput) {
      buffer.append(instr.toString)
      buffer.append(" (cycles: " + instr.numCycles +")")
      buffer.append(" [$%04x]\n".format(amiga.cpu.currentInstrWord()))
      textarea.text += buffer.toString
    }
    instr.execute
    amiga.video.doCycles(instr.numCycles)
    context.instructionNum += 1
    if (doOutput) cpuView.update
  }

  def append(text : String) = {
    textarea.text += text
  }
}

// **********************************************************************
// ***** Custom Register view
// **********************************************************************

class CustomRegisterView(amiga: Amiga)
extends GridPanel(0, 9) with CustomChipChangeListener {
  val smallFont = new Font("Monospaced", Font.PLAIN, 10)
  val registerLabels = new Array[Label](amiga.customChipRegisters.length)
  
  var index = 0
  for (reg <- amiga.customChipRegisters) {
    val label = createRegisterControl(reg.name)
    contents += label
    registerLabels(index) = label
    index += 1
  }
  border = BorderFactory.createTitledBorder("Custom Chip Registers")
  preferredSize = new Dimension(200, 325)
  amiga.customSpace.addListener(this)

  def createRegisterControl(name: String): Label = {
    val comp = new Label("%8s $00000000".format(name))
    comp.font = smallFont
    comp
  }
  def customChipRegisterChanged(regnum: Int, value: Int) = {
    val text = "%8s $%08x".format(amiga.customChipRegisters(regnum).name,
                                  value)
    registerLabels(regnum).text = text    
  }
}

// **********************************************************************
// ***** Debug frame
// **********************************************************************

class DebugFrame(amiga: Amiga) extends MainFrame
with ExecutionContext {
  title = "Dream Machine Preservation Project - Debugger"
  val titleFont = new Font("Dialog", Font.PLAIN, 10)
  val verticalSplitPane = new SplitPane(Orientation.Horizontal)
  val mainSplitPane = new SplitPane(Orientation.Vertical)
  val mainSplitPane2 = new SplitPane(Orientation.Vertical)

  val cpuView = new CpuView(amiga)
  val codeView = new CodeView(amiga, cpuView, this)
  val customRegisterView = new CustomRegisterView(amiga)
  val ciaView = createCiaPanel

  cpuView.border.asInstanceOf[TitledBorder].setTitleFont(titleFont)
  codeView.border.asInstanceOf[TitledBorder].setTitleFont(titleFont)
  ciaView.border.asInstanceOf[TitledBorder].setTitleFont(titleFont)
  customRegisterView.border.asInstanceOf[TitledBorder]
      .setTitleFont(titleFont)

  mainSplitPane.dividerSize = 1
  mainSplitPane.leftComponent = cpuView
  mainSplitPane.rightComponent = mainSplitPane2
  mainSplitPane2.dividerSize = 1
  mainSplitPane2.leftComponent = ciaView
  mainSplitPane2.rightComponent = codeView

  verticalSplitPane.dividerSize = 1
  verticalSplitPane.topComponent = mainSplitPane
  verticalSplitPane.bottomComponent = customRegisterView

  contents = verticalSplitPane
  
  //addWindowListener(MyWindowListener)
  pack;

  def createCiaPanel = {
    new BoxPanel(Orientation.Vertical) {
      contents += new CiaAView(amiga.ciaA, 0xbfe001)
      //ciaPanel.add(Box.createVerticalStrut(2))
      contents += new CiaBView(amiga.ciaB, 0xbfd000)
      border = BorderFactory.createTitledBorder("CIA-8520 (A/B)")
    }
  }

/*
  //val NUM_INSTRUCTIONS  = 262268
  val NUM_INSTRUCTIONS  = 1
  // The strange Exec counter loop
  val SKIP1_START  = 4
  val SKIP1_END    = 262144
  // Initialization of Exec exception vectors - they all point to $fc05b4
  // (Guru Meditation)
  val SKIP2_START  = 262168
  val SKIP2_END    = 262256
  def notIn(i : Int, from : Int, to : Int) : Boolean = i < from || i > to
  def doOutput(i : Int) : Boolean = {
    notIn(i, SKIP1_START, SKIP1_END) && notIn(i, SKIP2_START, SKIP2_END)
  }

  def controlAmiga = {
    val cpu = amiga.cpu
  
    val startTime = System.currentTimeMillis
    var buffer = new StringBuilder
    for (i <- 1 to NUM_INSTRUCTIONS) {
      if (doOutput(i))
        buffer.append("%08d $%04x: ".format(instructionNum,
                                            cpu.getPC()))
      val instr = cpu.nextInstruction
      if (doOutput(i)) {
        buffer.append(instr.toString)
        buffer.append(" (cycles: " + instr.numCycles +")")
        buffer.append(" [$%04x]\n".format(cpu.currentInstrWord()))
      }
      instr.execute
    }
    instructionNum = NUM_INSTRUCTIONS + 1
    cpuView.update
    codeView.append(buffer.toString)
    val endTime = System.currentTimeMillis
    println("elapsed time: " + (endTime - startTime) + " ms.")
  }

  object MyWindowListener extends WindowAdapter {
    override def windowOpened(event : WindowEvent) = {
      new Thread(new Runnable {
        def run = controlAmiga
      }).start
    }
  }*/
}
