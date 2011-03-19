package org.dmpp.debugger

import javax.swing._
import javax.swing.border._
import java.awt._
import java.awt.event._

abstract class CiaPanel(startAddress: Int) extends JPanel {
  
  val RegisterNames = Array("pra", "prb", "ddra", "ddrb", "talo", "tahi",
                            "tblo", "tbhi", "todlo", "todmid", "todhi", "-",
                            "sdr", "icr", "cra", "crb")
  val BitNums       = Array("7", "6", "5", "4", "3", "2", "1", "0")
  val labels = Array.ofDim[JLabel](16, 8)
  val c = new GridBagConstraints
  val smallFont = new Font(Font.MONOSPACED, Font.PLAIN, 10)
  val lineBorder = BorderFactory.createLineBorder(Color.BLACK, 1)
  val emptyBorder = BorderFactory.createEmptyBorder(3,3,3,3)

  val gridbag = new GridBagLayout
  setLayout(gridbag)
  c.fill = GridBagConstraints.BOTH
  for (row <- 0 to 15) {
    c.gridy = row
    //c.gridx = 0
    val label = new JLabel("$%06x %s: ".format(startAddress + row * 256,
                                               RegisterNames(row)))
    label.setFont(smallFont)
    label.setPreferredSize(new Dimension(80, 14))
    add(label, c)

    for (col <- 0 to 7) {
      labels(row)(col) = createCiaRegBitLabel(registerLabels(row)(col))
      add(labels(row)(col), c)
    }
  }
  //cia.addListener(this)

  // Implement this
  def registerLabels(row : Int) : Array[String]

  def createCiaRegBitLabel(title : String) = {
    val comp = new JLabel(title)
    comp.setFont(smallFont)
    comp.setPreferredSize(new Dimension(40, 14))
    comp.setBorder(new CompoundBorder(lineBorder, emptyBorder))
    //comp.horizontalAlignment = Alignment.Center
    comp
  }
}

object CiaAPanel {
  val PRABits = Array("/FIR1", "/FIR0", "/RDY", "/TK0", "/WPRO", "/CHNG",
                      "/LED", "OVL")
}
class CiaAPanel(startAddress: Int) extends CiaPanel(startAddress) {

  def registerLabels(row: Int) = if (row == 0) CiaAPanel.PRABits else BitNums
}

object CiaBPanel {
  val PRABits = Array("DTR", "/RTS", "/CD", "/CTS", "/DSR", "SEL",
                      "POUT", "BUSY")
  val PRBBits = Array("/MTR", "/SEL3", "/SEL2", "/SEL1", "/SEL0",
                      "/SIDE", "DIR", "/STEP")
}
class CiaBPanel(startAddress: Int) extends CiaPanel(startAddress) {
  def registerLabels(row: Int) = if (row == 0) CiaBPanel.PRABits
                                 else if (row == 1) CiaBPanel.PRBBits
                                 else BitNums
}
