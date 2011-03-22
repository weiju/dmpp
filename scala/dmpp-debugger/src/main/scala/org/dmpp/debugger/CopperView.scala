package org.dmpp.debugger

import javax.swing._
import javax.swing.border._
import java.awt._
import java.awt.event._

import org.dmpp.amiga._

class CopperView(copper: Copper) extends JPanel {
  val smallFont = new Font(Font.MONOSPACED, Font.PLAIN, 10)
  private def createLabel(text: String) = {
    val label = new JLabel(text)
    label.setFont(smallFont)
    label
  }

  val stateLabel              = createLabel("Disabled")
  val pcLabel                 = createLabel("$%06x".format(0))
  val waitPosLabel            = createLabel("---")
  val currentInstructionLabel = createLabel("---")
  val dangerLabel             = createLabel("False")

  currentInstructionLabel.setPreferredSize(new Dimension(150, 10))

  val grid = new JPanel
  add(grid)
  val gridbag = new GridBagLayout
  val c = new GridBagConstraints
  grid.setLayout(gridbag)
  grid.setBorder(new TitledBorder("Copper"))
  c.fill = GridBagConstraints.BOTH
  c.gridy = 0

  grid.add(createLabel("State: "), c)
  grid.add(stateLabel, c)

  c.gridy = 1
  grid.add(createLabel("Wait pos: "), c)
  grid.add(waitPosLabel, c)

  c.gridy = 2
  grid.add(createLabel("PC: "), c)
  grid.add(pcLabel, c)

  c.gridy = 3
  grid.add(createLabel("Current Instr.: "), c)
  grid.add(currentInstructionLabel, c)

  c.gridy = 4
  grid.add(createLabel("Danger bit set: "), c)
  grid.add(dangerLabel, c)

  retrieveCopperState

  private def retrieveCopperState {
    pcLabel.setText("$%06x".format(copper.pc))
    // TODO: Wait position
    if (!copper.enabled) {
      stateLabel.setText("Disabled")      
      dangerLabel.setText("---")
    } else {
      if (copper.waiting) stateLabel.setText("Waiting")
      else stateLabel.setText("Running")
      // TODO: current instruction
      dangerLabel.setText(if (copper.danger) "True" else "False")
    }
  }
}
