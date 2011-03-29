/**
 * Created on March 28, 2011
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

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SubPatternSpec extends FlatSpec with ShouldMatchers {

  "RegisterPattern" should "generate combinations" in {
    val context = ExecutionContext(0, null, 0, 0)
    val combinations = RegisterPattern.generateCombinations(0x41c0, context)
    combinations.length should be (8)
    combinations(0)._1 should be (0x41c0)
    combinations(1)._1 should be (0x43c0)
    combinations(7)._1 should be (0x4fc0)

    combinations(0)._2.regnum should be (0)
    combinations(1)._2.regnum should be (1)
    combinations(7)._2.regnum should be (7)
  }
  "DataRegisterDirectPattern" should "generate combinations" in {
    val combinations = DataRegisterDirectPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41c0)
    combinations(7) should be (0x41c7)
  }
  "AddressRegisterDirectPattern" should "generate combinations" in {
    val combinations = AddressRegisterDirectPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41c8)
    combinations(7) should be (0x41cf)
  }
  "AddressRegisterIndirectPattern" should "generate combinations" in {
    val combinations = AddressRegisterIndirectPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41d0)
    combinations(7) should be (0x41d7)
  }
  "ARIPostIncPattern" should "generate combinations" in {
    val combinations = ARIPostIncPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41d8)
    combinations(7) should be (0x41df)
  }
  "ARIPreDecPattern" should "generate combinations" in {
    val combinations = ARIPreDecPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41e0)
    combinations(7) should be (0x41e7)
  }
  "ARIDisplacementPattern" should "generate combinations" in {
    val combinations = ARIDisplacementPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41e8)
    combinations(7) should be (0x41ef)
  }
  "ARIIndexPattern" should "generate combinations" in {
    val combinations = ARIIndexPattern.generateCombinations(0x41c0)
    combinations.length should be (8)
    combinations(0) should be (0x41f0)
    combinations(7) should be (0x41f7)
  }
  "AbsoluteShortPattern" should "generate combinations" in {
    val combinations = AbsoluteShortPattern.generateCombinations(0x41c0)
    combinations.length should be (1)
    combinations(0) should be (0x41f8)
  }
  "AbsoluteLongPattern" should "generate combinations" in {
    val combinations = AbsoluteLongPattern.generateCombinations(0x41c0)
    combinations.length should be (1)
    combinations(0) should be (0x41f9)
  }
  "PCIndirectDisplacementPattern" should "generate combinations" in {
    val combinations = PCIndirectDisplacementPattern.generateCombinations(0x41c0)
    combinations.length should be (1)
    combinations(0) should be (0x41fa)
  }
  "PCIndirectIndexPattern" should "generate combinations" in {
    val combinations = PCIndirectIndexPattern.generateCombinations(0x41c0)
    combinations.length should be (1)
    combinations(0) should be (0x41fb)
  }
  "ImmediatePattern" should "generate combinations" in {
    val combinations = ImmediatePattern.generateCombinations(0x41c0)
    combinations.length should be (1)
    combinations(0) should be (0x41fc)
  }
}
