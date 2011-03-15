package org.dmpp.playfield

import javax.swing._
import java.awt._
import java.awt.event._
import javax.swing.event._
import javax.swing.border._

import org.dmpp.amiga._

object Main {
  val diwStrtField = new JTextField(5)
  val diwStopField = new JTextField(5)
  val ddfStrtField = new JTextField(5)
  val ddfStopField = new JTextField(5)

  def main(args: Array[String]) {
    val frame = new JFrame("Amiga Screen and DMA Simulation")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    val playfield = new Playfield()
    val canvas = new PlayfieldCanvas(NTSC, playfield)
    frame.getContentPane.add(canvas, BorderLayout.CENTER)

    val inputPanel1Flow = new JPanel(new FlowLayout(FlowLayout.LEFT))
    inputPanel1Flow.setBorder(new TitledBorder("Window Sizes"))
    val inputPanel1 = new JPanel(new GridLayout(2, 3))
    val inputPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT))
    //inputPanel2.setBorder(new TitledBorder("Beam Control"))
    inputPanel1Flow.add(inputPanel1)
    frame.getContentPane.add(inputPanel1Flow, BorderLayout.NORTH)
    frame.getContentPane.add(inputPanel2, BorderLayout.SOUTH)

    diwStrtField.setText("%04x".format(playfield.diwStart))
    inputPanel1.add(new JLabel("DIWSTRT: "))
    inputPanel1.add(diwStrtField)

    diwStopField.setText("%04x".format(playfield.diwStop))
    inputPanel1.add(new JLabel("DIWSTOP: "))
    inputPanel1.add(diwStopField)

    val updateButton = new JButton("Update")
    inputPanel1.add(updateButton)

    ddfStrtField.setText("%02x".format(playfield.ddfStart))
    inputPanel1.add(new JLabel("DDFSTRT: "))
    inputPanel1.add(ddfStrtField)

    ddfStopField.setText("%02x".format(playfield.ddfStop))
    inputPanel1.add(new JLabel("DDFSTOP: "))
    inputPanel1.add(ddfStopField)

    val startBeamButton = new JButton("")
    startBeamButton.setIcon(getIcon("play.png"))
    startBeamButton.setToolTipText("Start Beam")
    val stopBeamButton = new JButton("")
    stopBeamButton.setIcon(getIcon("stop.png"))
    stopBeamButton.setEnabled(false)
    startBeamButton.setToolTipText("Stop Beam")
    val singleStepButton = new JButton("")
    singleStepButton.setIcon(getIcon("singlestep.png"))
    singleStepButton.setToolTipText("Advance 1 cyle")

    inputPanel2.add(stopBeamButton)
    inputPanel2.add(startBeamButton)
    inputPanel2.add(singleStepButton)

    updateButton.addActionListener(new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        playfield.diwStart = parseField(diwStrtField, "DIWSTRT")
        playfield.diwStop = parseField(diwStopField, "DIWSTOP")
        playfield.ddfStart = parseField(ddfStrtField, "DDFSTRT")
        playfield.ddfStop = parseField(ddfStopField, "DDFSTOP")
        canvas.updateDisplay
      }
    })

    startBeamButton.addActionListener(new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        startBeamButton.setEnabled(false)
        singleStepButton.setEnabled(false)
        stopBeamButton.setEnabled(true)
        canvas.startBeam
      }
    })
    stopBeamButton.addActionListener(new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        stopBeamButton.setEnabled(false)
        startBeamButton.setEnabled(true)
        singleStepButton.setEnabled(true)
        canvas.stopBeam
      }
    })
    singleStepButton.addActionListener(new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        canvas.singleStep
      }
    })

    val slider = new JSlider(1, 100, canvas.animationSpeed)
    val sliderLabel = new JLabel("%d".format(canvas.animationSpeed))
    slider.setSnapToTicks(true)
    slider.setPaintLabels(true)
    slider.setPaintTicks(true)
    slider.setPaintTrack(true)
    inputPanel2.add(new JLabel("Speed: "))
    inputPanel2.add(slider)
    inputPanel2.add(sliderLabel)
    slider.addChangeListener(new ChangeListener {
      def stateChanged(e: ChangeEvent) {
        canvas.animationSpeed = slider.getValue
        sliderLabel.setText("%d".format(slider.getValue))
      }
    })

    frame.pack
    frame.setVisible(true)
  }

  private def parseField(textfield: JTextField, fieldname: String) = {
    Integer.parseInt(textfield.getText, 16)
  }

  private def getIcon(filename: String) = {
    new ImageIcon(getClass.getResource("/" + filename))
  }
}
