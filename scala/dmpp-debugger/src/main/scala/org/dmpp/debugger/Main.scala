package org.dmpp.debugger

import javax.swing._
import java.awt._
import java.awt.event._
import javax.swing.event._
import javax.swing.border._

import org.dmpp.amiga._


object Playfield {
  val DIWSTRT_Standard      = 0x2c81 // 0xvvhh
  val DIWSTOP_Standard      = 0xf4c1 // 0xvvhh

  val DDFSTRT_StandardLores = 0x38
  val DDFSTRT_StandardHires = 0x3c
  val DDFSTRT_LimitLores    = 0x18
  val DDFSTRT_LimitHires    = 0x18

  val DDFSTOP_StandardLores = 0xd0
  val DDFSTOP_StandardHires = 0xd4
  val DDFSTOP_LimitLores    = 0xd8
  val DDFSTOP_LimitHires    = 0xd8
}

object Main {
  import Playfield._

  val diwStrtField = new JTextField(5)
  val diwStopField = new JTextField(5)
  val ddfStrtField = new JTextField(5)
  val ddfStopField = new JTextField(5)

  def createVideo = {
    val video = new Video(NTSC, null)
    video.DIWSTRT.value = DIWSTRT_Standard
    video.DIWSTOP.value = DIWSTOP_Standard
    video.DDFSTRT.value = DDFSTRT_StandardLores
    video.DDFSTOP.value = DDFSTOP_StandardLores
    video
  }

  def createMemoryPanel = {
    val mainPanel = new JPanel(new BorderLayout)
    val ciaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
    mainPanel.add(ciaPanel, BorderLayout.NORTH)
    val ciaAPanel = new CiaAPanel(0)
    val ciaBPanel = new CiaBPanel(0)
    ciaPanel.add(ciaAPanel)
    ciaPanel.add(ciaBPanel)
    mainPanel
  }

  def createVideoPanel(video: Video) = {
    val mainPanel = new JPanel(new BorderLayout)
    val canvas = new PlayfieldCanvas(video)
    mainPanel.add(canvas, BorderLayout.CENTER)

    val inputPanel1Flow = new JPanel(new FlowLayout(FlowLayout.LEFT))
    inputPanel1Flow.setBorder(new TitledBorder("Window Sizes"))
    val inputPanel1 = new JPanel(new GridLayout(2, 3))
    val inputPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT))
    //inputPanel2.setBorder(new TitledBorder("Beam Control"))
    inputPanel1Flow.add(inputPanel1)
    mainPanel.add(inputPanel1Flow, BorderLayout.NORTH)
    mainPanel.add(inputPanel2, BorderLayout.SOUTH)

    diwStrtField.setText("%04x".format(video.playfield.diwstrt))
    inputPanel1.add(new JLabel("DIWSTRT: "))
    inputPanel1.add(diwStrtField)

    diwStopField.setText("%04x".format(video.playfield.diwstop))
    inputPanel1.add(new JLabel("DIWSTOP: "))
    inputPanel1.add(diwStopField)

    val updateButton = new JButton("Update")
    inputPanel1.add(updateButton)

    ddfStrtField.setText("%02x".format(video.playfield.ddfstrt))
    inputPanel1.add(new JLabel("DDFSTRT: "))
    inputPanel1.add(ddfStrtField)

    ddfStopField.setText("%02x".format(video.playfield.ddfstop))
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
        video.DIWSTRT.value = parseField(diwStrtField, "DIWSTRT")
        video.DIWSTOP.value = parseField(diwStopField, "DIWSTOP")
        video.DDFSTRT.value = parseField(ddfStrtField, "DDFSTRT")
        video.DDFSTOP.value = parseField(ddfStopField, "DDFSTOP")
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
    mainPanel
  }

  def main(args: Array[String]) {
    val video = createVideo

    val frame = new JFrame("Amiga Screen and DMA Simulation")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    val tabbedPane = new JTabbedPane
    frame.getContentPane.add(tabbedPane, BorderLayout.CENTER)
    tabbedPane.add(createVideoPanel(video), "Video")
    tabbedPane.add(createMemoryPanel, "Memory")

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
