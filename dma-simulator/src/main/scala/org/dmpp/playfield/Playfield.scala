package org.dmpp.playfield

import org.dmpp.amiga._

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
