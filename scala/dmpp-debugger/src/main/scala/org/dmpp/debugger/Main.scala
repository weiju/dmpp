/**
 * Created on March 10, 2011
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
package org.dmpp.debugger

import javax.swing._
import java.awt._
import java.awt.event._
import javax.swing.event._
import javax.swing.border._

import org.dmpp.amiga._

object Main {
  import PlayfieldConstants._

  val diwStrtField = new JTextField(5)
  val diwStopField = new JTextField(5)
  val ddfStrtField = new JTextField(5)
  val ddfStopField = new JTextField(5)

  def createStepperPanel(canvas: PlayfieldCanvas) = {
    val stepperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
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

    stepperPanel.add(stopBeamButton)
    stepperPanel.add(startBeamButton)
    stepperPanel.add(singleStepButton)
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
    stepperPanel.add(new JLabel("Speed: "))
    stepperPanel.add(slider)
    stepperPanel.add(sliderLabel)
    slider.addChangeListener(new ChangeListener {
      def stateChanged(e: ChangeEvent) {
        canvas.animationSpeed = slider.getValue
        sliderLabel.setText("%d".format(slider.getValue))
      }
    })
    stepperPanel
  }
  def createPlayfieldSettingsPanel(video: Video, canvas: PlayfieldCanvas) = {
    val settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
    settingsPanel.setBorder(new TitledBorder("Playfield Settings"))
    val inputPanel1 = new JPanel(new GridLayout(2, 3))
    settingsPanel.add(inputPanel1)
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


    updateButton.addActionListener(new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        video.DIWSTRT.value = parseField(diwStrtField, "DIWSTRT")
        video.DIWSTOP.value = parseField(diwStopField, "DIWSTOP")
        video.DDFSTRT.value = parseField(ddfStrtField, "DDFSTRT")
        video.DDFSTOP.value = parseField(ddfStopField, "DDFSTOP")
        canvas.updateDisplay
      }
    })
    settingsPanel
  }

  def main(args: Array[String]) {
    if (args.length == 0) {
      println("Please provide path to Kickstart ROM file")
      System.exit(0)
    }
    val amiga = createAmiga(args(0))

    val frame = new JFrame("DMPP Debugger")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    val tabbedPane = new JTabbedPane
    frame.getContentPane.add(tabbedPane, BorderLayout.CENTER)
    val copperView = new CopperView(amiga.copper)
    frame.getContentPane.add(copperView, BorderLayout.WEST)

    tabbedPane.add(createVideoPanel(amiga.video), "Video")
    tabbedPane.add(createCustomChipPanel(amiga), "Custom Chips")

    frame.pack
    frame.setVisible(true)
  }

  def createAmiga(pathToKickRom: String) = {
    val amiga = new Amiga
    amiga.init(pathToKickRom)
    amiga.video.DIWSTRT.value = DIWSTRT_Standard
    amiga.video.DIWSTOP.value = DIWSTOP_Standard
    amiga.video.DDFSTRT.value = DDFSTRT_StandardLores
    amiga.video.DDFSTOP.value = DDFSTOP_StandardLores
    amiga
  }

  def createVideoPanel(video: Video) = {
    val mainPanel = new JPanel(new BorderLayout)
    val canvas = new PlayfieldCanvas(video)

    mainPanel.add(canvas, BorderLayout.CENTER)
    mainPanel.add(createPlayfieldSettingsPanel(video, canvas),
                  BorderLayout.NORTH)
    mainPanel.add(createStepperPanel(canvas), BorderLayout.SOUTH)
    mainPanel
  }

  def createCustomChipPanel(amiga: Amiga) = {
    val mainPanel = new JPanel(new BorderLayout)
    val ciaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
    mainPanel.add(ciaPanel, BorderLayout.SOUTH)
    val ciaAPanel = new CiaAPanel(amiga.ciaA, 0xbfe001)
    val ciaBPanel = new CiaBPanel(amiga.ciaB, 0xbfd000)
    ciaPanel.add(ciaAPanel)
    ciaPanel.add(ciaBPanel)

    val customRegisterPanel = new CustomRegisterView(amiga)
    mainPanel.add(customRegisterPanel, BorderLayout.CENTER)
    mainPanel
  }

  private def parseField(textfield: JTextField, fieldname: String) = {
    Integer.parseInt(textfield.getText, 16)
  }

  private def getIcon(filename: String) = {
    new ImageIcon(getClass.getResource("/" + filename))
  }
}
