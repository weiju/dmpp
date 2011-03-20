package org.dmpp.debugger

import javax.swing._
import java.awt._
import java.awt.event._
import javax.swing.event._

import org.dmpp.amiga._

class CustomRegisterView(amiga: Amiga)
extends JPanel(new BorderLayout) with CustomChipChangeListener {

  val labelPanel = new JPanel
  val gridLayout = new GridLayout(0, 8, 0, 0)
  labelPanel.setLayout(gridLayout)
  val scrollPane = new JScrollPane(labelPanel)
  add(scrollPane, BorderLayout.CENTER)

  val smallFont = new Font("Monospaced", Font.PLAIN, 10)
  val registerLabels = new Array[JLabel](amiga.customChipRegisters.length)
  
  var index = 0
  for (reg <- amiga.customChipRegisters) {
    val label = createRegisterControl(reg.name)
    labelPanel.add(label)
    registerLabels(index) = label
    index += 1
  }
  setBorder(BorderFactory.createTitledBorder("Custom Chip Registers"))
  amiga.customSpace.addListener(this)

  private def createRegisterControl(name: String) = {
    val comp = new JLabel("%8s $00000000".format(name))
    comp.setFont(smallFont)
    comp
  }
  def customChipRegisterChanged(regnum: Int, value: Int) = {
    val text = "%8s $%08x".format(amiga.customChipRegisters(regnum).name,
                                  value)
    registerLabels(regnum).setText(text)    
  }
}
