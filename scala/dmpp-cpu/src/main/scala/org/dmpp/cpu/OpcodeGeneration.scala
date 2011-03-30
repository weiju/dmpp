/**
 * Created on March 27, 2011
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
package org.dmpp.cpu

/*
 * This file solely deals with associating the Flyweight instruction
 * objects with the possible opcodes. To keep the bytecode size smaller,
 * we generate the opcode table each time a Cpu object is created,
 * making the Cpu the heaviest object in the DMPP system.
 *
 * We use strings and characters to have a more readable interpretation
 * of opcode-instruction association.
 *
 * Effective addressing modes:
 *
 * an = address register direct
 * dn = data register direct
 * ai = address register indirect
 * ap = ARI with post-increment
 * ar = ARI with pre-decrement
 * ad = ARI with displacement
 * ax = ARI with index
 * aw = absolute short
 * al = absolute long
 * pd = program counter indirect with displacement
 * px = program counter indirect with index
 * im = immediate

 * Possible sub patterns in a pattern string:
 * aaa = address register
 * ddd = data register
 * DDDDDDDD = 8-bit displacement
 * eeeeee = effective address std
 * EEEEEE = effective address rev (move.<size> <src>, <dest>)
 * mmm = opmode (can be 1, 2 or a), specified by arithmetic instructions
 * ss  = size
 * vvv = data value encoded in instruction
 * fff = effective address register number (data register)
 * FFF = effective address register number (address register)
 * tttt = trap vector
 */

/**
 * By providing permutations of their bit combinations, sub patterns
 * can generate a number of values based on a base value by ORing that
 * bit combination into its designated position
 */
trait SubPattern {
  def generateCombinations(baseValue: Int): List[Int]
}

/**
 * This pattern handles the register position ----rrr---------
 */
case object RegisterPattern extends SubPattern {
  def generateCombinations(baseValue: Int, context: ExecutionContext) = {
    var result: List[(Int, ExecutionContext)] = Nil
    for (i <- 0 to 7) {
      result ::= ((baseValue | (i << 9)), context.cloneWithRegnum(i))
    }
    result.reverse
  }
  def generateCombinations(baseValue: Int) = {
    var result: List[Int] = Nil
    for (i <- 0 to 7) result ::= (baseValue | (i << 9))
    result.reverse
  }
}

/**
 * This pattern handles the effective address position ----------eeeeee
 * Note that this pattern comes in the variation ----EEEEEE------ which
 * has eamode and register fields reversed and only is used in the
 * general form of the move instruction.
 */
abstract class EaModePattern extends SubPattern {
  val modeValue: Int
  def generateCombinations(baseValue: Int) = {
    var result: List[Int] = Nil
    val baseValue2 = (baseValue | (modeValue << 3))
    for (i <- 0 to 7) result ::= (baseValue2 | i)
    result.reverse
  }
  def generateReverseCombinations: List[Int] = Nil // TODO
}

case object DataRegisterDirectPattern extends EaModePattern {
  val modeValue = 0x00 // %000
}

case object AddressRegisterDirectPattern extends EaModePattern {
  val modeValue = 0x01 // %000
}

case object AddressRegisterIndirectPattern extends EaModePattern {
  val modeValue = 0x02 // %010
}
case object ARIPostIncPattern extends EaModePattern {
  val modeValue = 0x03 // %011
}
case object ARIPreDecPattern extends EaModePattern {
  val modeValue = 0x04 // %100
}
case object ARIDisplacementPattern extends EaModePattern {
  val modeValue = 0x05 // %101
}
case object ARIIndexPattern extends EaModePattern {
  val modeValue = 0x06 // %110
}
case object AbsoluteShortPattern extends EaModePattern {
  val modeValue = 0x07 // %111
  override def generateCombinations(baseValue: Int) = {
    List(baseValue | 0x38) // %111000
  }
}
case object AbsoluteLongPattern extends EaModePattern {
  val modeValue = 0x07 // %111
  override def generateCombinations(baseValue: Int) = {
    List(baseValue | 0x39) // %111001
  }
}
case object PCIndirectDisplacementPattern extends EaModePattern {
  val modeValue = 0x07 // %111
  override def generateCombinations(baseValue: Int) = {
    List(baseValue | 0x3a) // %111010
  }
}
case object PCIndirectIndexPattern extends EaModePattern {
  val modeValue = 0x07 // %111
  override def generateCombinations(baseValue: Int) = {
    List(baseValue | 0x3b) // %111011
  }
}
case object ImmediatePattern extends EaModePattern {
  val modeValue = 0x07 // %111
  override def generateCombinations(baseValue: Int) = {
    List(baseValue | 0x3c) // %111100
  }
}

/**
 * Helper constants to support opcode generation
 */
object InstrDef {
  /**
   * Amount of positions to shift when a certain letter is encountered.
   */
  val PatternShift = Map(
    'a' -> 3, 'd' -> 3, 'e' -> 6, 'E' -> 6, 'm' -> 3, 's' -> 2,
    'v' -> 3, 'D' -> 8, 'f' -> 3, 'F' -> 3, 't' -> 4, '0' -> 1, '1' -> 1
  )

  val EaModePatternMap = Map("dn" -> DataRegisterDirectPattern,
                             "an" -> AddressRegisterDirectPattern,
                             "ai" -> AddressRegisterIndirectPattern,
                             "ap" -> ARIPostIncPattern,
                             "ar" -> ARIPreDecPattern,
                             "ad" -> ARIDisplacementPattern,
                             "ax" -> ARIIndexPattern,
                             "aw" -> AbsoluteShortPattern,
                             "al" -> AbsoluteLongPattern,
                             "pd" -> PCIndirectDisplacementPattern,
                             "px" -> PCIndirectIndexPattern,
                             "im" -> ImmediatePattern)
}
case class InstrDef(mnemonic  : String,
                    pattern   : String,
                    sizes     : List[String], // (opmode1|opmode2|b|w|l|none)*
                    eaModes   : List[String]) {
  import InstrDef._

  /**
   * Binary base value for this instruction based on the pattern.
   * Final values are calculated by substituting addressing modes and
   * value combinations
   */
  lazy val baseValue = {
    var result = 0
    var i = 0
    while (i <  pattern.length) {
      val c = pattern.charAt(i)
      result <<= 1
      if (c == '1') result |= 1
      val shift = PatternShift(c)
      i += shift
      result <<= shift - 1
    }
    result
  }
  /**
   * Make a list of pattern characters in the order as they occur in the
   * pattern string.
   */
  lazy val varpatterns: List[Char] = {
    var result: List[Char] = Nil
    var i = 0
    while (i <  pattern.length) {
      val c = pattern.charAt(i)
      if (c != '0' && c != '1') result = c :: result
      i += 1 + PatternShift(c)
    }
    result.reverse
  }

  def setToOpcodeArray(opcodes: Array[Opcode]) {
    println("setToOpcodeArray()")
    printf("BASE VALUE: %02x\n", baseValue)
    var indices = List(baseValue)
    for (varpattern <- varpatterns) {
      printf("VARPATTERN: %c\n", varpattern)
      indices =
        indices.flatMap(value => generateCombinationsFor(varpattern, eaModes,
                                                         value))
    }
    println(indices)
  }
  // TODO
  // it would be cool if this would instead return an (index, context)
  // list, so we do not need to decode the eamode, size and register numbers
  private def generateCombinationsFor(varpattern: Char,
                                      eaModes: List[String],
                                      baseValue: Int): List[Int] = {
    List(baseValue)
  }
}

/**
 * Instruction set definitions.
 */
object InstructionSet {
  // the two-address move is not included to avoid specifying an output
  // specification only for one instruction this list on and keep InstrDef simple
  val InstrDefs = Array(
    InstrDef(mnemonic = "adda", pattern = "1101aaammmeeeeee", sizes = List("opmodea"),
             eaModes  = List("dn","an","ai","ap","ar","ad","ax","aw","al","pd","px","im")),
    InstrDef(mnemonic = "lea", pattern = "0100aaa111eeeeee", sizes = List("l"),
             eaModes  = List("ai","ad","ax","aw","al","pd","px"))
  )
}
