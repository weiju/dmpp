package org.dmpp.playfield

// Vertical
// LinesTotal are the number of vertical positions the Copper can see
// In interlace, this becomes one more on a long frame.
// So: 263 in NTSC, 313 in PAL

// Horizontal
// The copper can see beam positions $00 to $E2 (0-226), which are available in both
// PAL/NTSC, for a total of 227 positions. In addition, the Copper can only see even
// positions. Each copper beam position unit equals 2 lores/4 hires pixels,
// meaning a display has 227.5 * 2 = 455 lores pixels = 910 hires pixels
// Horizontal blanking is from 0x0f (pixel 30) to 0x35 (pixel 106)
trait VideoStandard {
  val PixelsPerLine = 455 // always (227.5 color clocks)

  def VbStart                   : Int
  def VbStop                    : Int
  def LinesTotal                : Int
  def DisplayableLines          : Int
  def LinesTotalInterlace       : Int
  def DisplayableLinesInterlace : Int
}

object NTSC extends VideoStandard {
  val VbStart          = 0
  val VbStop           = 21
  val LinesTotal       = 262
  val DisplayableLines = 241 // LinesTotal - |VbStop - VbStart|

  val LinesTotalInterlace       = 524
  val DisplayableLinesInterlace = 483
}
object PAL extends VideoStandard {
  val VbStart          = 0
  val VbStop           = 29
  val LinesTotal       = 312 
  val DisplayableLines = 283 // LinesTotal - |VbStop - VbStart|

  val LinesTotalInterlace       = 625
  val DisplayableLinesInterlace = 567
}

object Playfield {
  val DIWStartStandard = 0x2c81 // 0xvvhh
  val DIWStopStandard  = 0xf4c1 // 0xvvhh

  val DDFSTRTStandardLores = 0x38
  val DDFSTRTStandardHires = 0x3c
  val DDFSTRTLimitLores    = 0x18
  val DDFSTRTLimitHires    = 0x18

  val DDFSTOPStandardLores = 0xd0
  val DDFSTOPStandardHires = 0xd4
  val DDFSTOPLimitLores    = 0xd8
  val DDFSTOPLimitHires    = 0xd8
}
class Playfield(var diwStart: Int = Playfield.DIWStartStandard,
                var diwStop : Int = Playfield.DIWStopStandard,
                var ddfStart: Int = Playfield.DDFSTRTStandardLores,
                var ddfStop : Int = Playfield.DDFSTOPStandardLores) {
  def left = diwStart & 0xff
  def top  = (diwStart >>> 8) & 0xff
  def right = (diwStop & 0xff) | 0x100
  // TODO: positions > 0xff
  def bottom = ((diwStop >>> 8) & 0xff)
  def width  = right - left
  def height = bottom - top

  def ddfLeft = ddfStart & 0xff
  def ddfRight = ddfStop & 0xff
  def ddfWidth = (ddfRight - ddfLeft)
}

class VideoBeam(videoStandard: VideoStandard) {
  var hpos           = 0
  var vpos           = 0
  //var interlaceMode  = false
  //var shortFrame     = true

  //def linesOddFrame  = 262
  //def linesEvenFrame = 262

  def advance(pixels: Int) {
    hpos += pixels
    if (hpos > videoStandard.PixelsPerLine) {
      vpos += 1
      hpos -= videoStandard.PixelsPerLine
      if (vpos >= videoStandard.LinesTotal) vpos = 0
    }
  }
}

