package org.dmpp.debugger

import java.awt._
import javax.swing._
import java.awt.event._
import java.awt.image.BufferedImage

import org.dmpp.amiga._

case class ScreenRect(x: Int, y: Int, width: Int, height: Int)

object PlayfieldCanvas {
  val ScreenColor = new Color(0x33, 0x33, 0x33)

  // A gradient created by hand, to visualise the trail of the video beam
  val Gray = Array(new Color(0xee, 0xee, 0xee),
                   new Color(0xdd, 0xdd, 0xdd),
                   new Color(0xcc, 0xcc, 0xcc),
                   new Color(0xbb, 0xbb, 0xbb),
                   new Color(0xaa, 0xaa, 0xaa),
                   new Color(0x99, 0x99, 0x99),
                   new Color(0x88, 0x88, 0x88),
                   new Color(0x77, 0x77, 0x77),
                   new Color(0x66, 0x66, 0x66),
                   new Color(0x55, 0x55, 0x55),
                   new Color(0x44, 0x44, 0x44),
                   new Color(0x33, 0x33, 0x33),
                   new Color(0x22, 0x22, 0x22)
                 )
  val Red = new Color(0xff, 0x00, 0x00, 0x80)
  val Blue = new Color(0x00, 0x00, 0xff, 0x80)
  val ViewFont = new Font("Monospaced", Font.PLAIN, 12)
  val TinyFont = new Font("Monospaced", Font.PLAIN, 10)
  val LoresPixelsPerCycle = 2
  val HiresFactor = 2
}

case class InfoLabel(x: Int, y: Int, color: Color, text: String) {
  def paint(g: Graphics) {
    g.setColor(color)
    g.fillRect(x, y, 20, 10)
    g.drawString(text, x + 25, y + 10)
  }
}
class InfoPanel(x: Int, y: Int) {
  import PlayfieldCanvas._
  val labels = Array(InfoLabel(x,       y, Color.BLUE,   "Vert. Blank"),
                     InfoLabel(x + 120, y, Color.CYAN,   "Horz. Blank"),
                     InfoLabel(x + 240, y, Color.YELLOW, "Disp. Window"),
                     InfoLabel(x + 360, y, Color.RED,    "Bitmap DMA")
                     )

  def paint(g: Graphics) {
    labels.foreach(_.paint(g))
  }
}

class PlayfieldCanvas(video: Video)
extends JComponent {
  import PlayfieldCanvas._

  def videoStandard = video.videoStandard
  def beam = video.videoBeam
  def playfield = video.playfield

  def playfieldLeft = playfield.diwstrt & 0xff
  def playfieldTop  = (playfield.diwstrt >>> 8) & 0xff 
  def playfieldRight = (playfield.diwstop & 0xff) | 0x100
  // TODO: positions > 0xff
  def playfieldBottom = ((playfield.diwstop >>> 8) & 0xff)
  def playfieldWidth  = playfieldRight - playfieldLeft
  def playfieldHeight = playfieldBottom - playfieldTop

  def ddfLeft = playfield.ddfstrt & 0xff
  def ddfRight = playfield.ddfstop & 0xff
  def ddfWidth = (ddfRight - ddfLeft)

  val dmaViewHeight = 80
  val screen = ScreenRect(30, 35,
                          videoStandard.CpuCyclesPerScanline * HiresFactor,
                          videoStandard.LinesTotal * HiresFactor)
  def canvasWidth  = screen.width + 60
  def canvasHeight = screen.height + dmaViewHeight + 30

  val backBuffer = new BufferedImage(canvasWidth, canvasHeight,
                                     BufferedImage.TYPE_INT_RGB)

  this.setPreferredSize(new Dimension(canvasWidth, canvasHeight))

  val hblankStart = 0x0f * LoresPixelsPerCycle * HiresFactor
  val hblankWidth = (0x35 - 0x0f) * LoresPixelsPerCycle * HiresFactor
  val beamInfoX = 10
  val beamInfoY = 10

  val infoPanel = new InfoPanel(10, 17)
  val dmaSlots = new DmaSlotView(screen.x, screen.y + screen.height + 20,
                                 screen.width, 20, beam)

  var animationSpeed = 6

  val beamTimer = new Timer(20, new ActionListener {
      def actionPerformed(event: ActionEvent) {
        repaint()
        beam.advance(animationSpeed)
      }
    })

  def updateDisplay {
    invalidate
    repaint()
  }

  def startBeam { beamTimer.start }
  def stopBeam { beamTimer.stop }
  def singleStep {
    beam.advance(1)
    updateDisplay
  }

  override def paintComponent(g: Graphics) {
    paintElements(backBuffer.getGraphics)
    g.drawImage(backBuffer, 0, 0, null)
  }

  private def paintElements(g: Graphics) {
    clear(g)
    drawScreen(g)
    drawHorizontalBlankArea(g)
    drawVerticalBlankArea(g)
    drawDisplayWindow(g)
    drawBitmapDmaArea(g)
    drawSeparator(g, screen.y + screen.height + 2)
    drawBeam(g)
    drawBeamPositions(g)
    infoPanel.paint(g)
    dmaSlots.paint(g)
  }

  private def clear(g: Graphics) {
    // background
    g.setColor(Color.BLACK)
    g.fillRect(0, 0, canvasWidth, canvasHeight)
    g.setFont(ViewFont)
  }
  private def drawScreen(g: Graphics) {
    g.setColor(ScreenColor)
    g.fillRect(screen.x, screen.y, screen.width, screen.height)
  }
  private def drawHorizontalBlankArea(g: Graphics) {
    g.setColor(Color.CYAN)
    g.fillRect(screen.x + hblankStart, screen.y,
               hblankWidth, screen.height)
  }
  private def drawVerticalBlankArea(g: Graphics) {
    g.setColor(Blue)
    g.fillRect(screen.x, screen.y,
               screen.width, videoStandard.VbStop * HiresFactor)
  }
  private def drawDisplayWindow(g: Graphics) {
    g.setColor(Color.YELLOW)
    g.drawRect(screen.x + playfieldLeft * HiresFactor,
               screen.y + playfieldTop * HiresFactor,
               playfieldWidth * HiresFactor,
               playfieldHeight * HiresFactor)
  }
  private def drawBitmapDmaArea(g: Graphics) {
    // DDFSTRT/DDFSTOP
    g.setColor(Red)
    g.drawRect(screen.x + ddfLeft * LoresPixelsPerCycle * HiresFactor,
               screen.y + playfieldTop * HiresFactor,
               ddfWidth * LoresPixelsPerCycle * HiresFactor,
               playfieldHeight * HiresFactor)
  }
  private def drawBeam(g: Graphics) {
    val beamx = screen.x + beam.hpos * HiresFactor
    val beamy = screen.y + beam.vpos * HiresFactor
    g.drawRect(beamx, beamy, HiresFactor, HiresFactor)
    // Draw a small gradient in front of the position
    for (i <- 0 until Gray.length) {
      if (beam.hpos > i) {
        g.setColor(Gray(i))
        g.drawRect(beamx - (2 + i), beamy, HiresFactor, HiresFactor)
      }
    }
    g.setColor(Color.DARK_GRAY)
    g.fillRect(beamx, screen.y, 2, screen.height)
  }
  private def drawSeparator(g: Graphics, y: Int) {
    g.setColor(Color.WHITE)
    g.drawLine(0, y, canvasWidth, y)
  }
  private def drawBeamPositions(g: Graphics) {
    g.setColor(Color.WHITE)
    g.drawString(("video h: $%03x (%03d) v: $%03x (%03d) " +
                 "clock: $%02x | win w: %d h: %d |").format(beam.hpos, beam.hpos,
                                                            beam.vpos, beam.vpos,
                                                            beam.hpos / 2,
                                                            playfieldWidth,
                                                            playfieldHeight),
                 beamInfoX, beamInfoY)
  }
}

object DmaSlots {
  val NumSlotsMemoryRefresh = 4
  val NumSlotsDisk          = 3
  val NumSlotsAudio         = 4
  val NumSlotsSprite        = 8 * 2
  val NumSlotsUndef         = 2
  val NumSlotsBitmap        = 84

  val DiskDmaStart          = 0x08
  val AudioDmaStart         = 0x0e
  val SpriteDmaStart        = 0x16
  val UndefDmaStart         = 0x36
  val BitmapDmaStart        = 0x3a
}

class DmaSlotView(x: Int, y: Int, width: Int, height: Int, videoBeam: VideoBeam) {
  import PlayfieldCanvas._
  import DmaSlots._

  val DmaSlotWidth = 4

  def paint(g: Graphics) {
    g.setColor(Color.WHITE)
    g.drawString("DMA Time Slots", x - 15, y - 7)
    g.drawLine(x, y,          x + width, y)
    g.drawLine(x, y + height, x + width, y + height)
    drawTicks(g)
    paintMemoryRefreshDma(g)
    paintDiskDma(g)
    paintAudioDma(g)
    paintSpriteDma(g)
    paintUndefDma(g)
    paintBitmapDma(g)
    drawPointer(g)
  }
  private def drawTicks(g: Graphics) {
    val ty = y + height
    g.setFont(TinyFont)
    g.setColor(Color.WHITE)
    for (i <- 0 until width) {
      if (i % (8 * DmaSlotWidth) == 0) {
        val xi = x + i
        g.drawLine(xi, ty, xi, ty + 5)
        g.drawString("$%02x".format(i / DmaSlotWidth), xi - 6, ty + 14)
      }
    }
  }

  private def paintOddCycles(g: Graphics, numSlots: Int, startx: Int,
                              color: Color) {
    g.setColor(color)
    var lx = startx - DmaSlotWidth
    for (i <- 0 until numSlots) {
      g.fillRect(lx, y + 3, DmaSlotWidth, height - 6)
      lx += DmaSlotWidth * 2
    }
  }
  private def paintMemoryRefreshDma(g: Graphics) {
    paintOddCycles(g, NumSlotsMemoryRefresh, x, Color.DARK_GRAY)
  }
  private def paintDiskDma(g: Graphics) {
    paintOddCycles(g, NumSlotsDisk,
                    x + DiskDmaStart * DmaSlotWidth,
                    Color.GRAY)
  }
  private def paintAudioDma(g: Graphics) {
    paintOddCycles(g, NumSlotsAudio,
                    x + AudioDmaStart * DmaSlotWidth,
                    Color.MAGENTA)
  }
  private def paintSpriteDma(g: Graphics) {
    paintOddCycles(g, NumSlotsSprite,
                    x + SpriteDmaStart * DmaSlotWidth,
                    Color.GREEN)
  }
  private def paintUndefDma(g: Graphics) {
    paintOddCycles(g, NumSlotsUndef, x + UndefDmaStart * DmaSlotWidth, Color.ORANGE)
  }
  private def paintBitmapDma(g: Graphics) {
    paintOddCycles(g, NumSlotsBitmap, x + BitmapDmaStart * DmaSlotWidth, Color.BLUE)
  }
  private def drawPointer(g: Graphics) {
    g.setColor(Color.WHITE)
    val slot = videoBeam.hpos / LoresPixelsPerCycle
    val xpos = x + 1 + (slot * DmaSlotWidth)
    g.fillRect(xpos, y - 2, 2, 1)
    g.fillRect(xpos - 1, y - 3, 4, 1)
  }
}
