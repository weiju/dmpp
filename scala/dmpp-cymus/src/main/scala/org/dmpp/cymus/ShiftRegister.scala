/**
 * Created on November 16, 2009
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
package org.dmpp.cymus

/**
 * An n-bit shift register implementation that serves several purposes in the
 * CIA implementation:
 * 1. implement the serial shift register (sdr)
 * 2. Acts as a pulse buffer/extender for the CNT and 02 signals
 * The register is implemented as a ring, so the size is static. The semantics are
 * like a FIFO queue.
 */
class ShiftRegister(n: Int) {
  private var bits: Array[Boolean] = new Array[Boolean](n)
  private var start: Int           = 0
  private var length: Int          = 0

  def empty = length == 0
  def enqueue(signal: Boolean) {
    printf("enqueue(), length = %d bits.length = %d\n", length,  bits.length)
    if (length == bits.length) {
      throw new IndexOutOfBoundsException("buffer is full")
    }
    bits((start + length) % bits.length) = signal
    length += 1
    printf("length is now: %d\n", length)
  }
  def dequeue: Boolean = {
    if (length > 0) length -= 1
    val result = bits(start)
    start = (start + 1) % bits.length
    printf("start is now: %d, length is now: %d\n", start, length)
    result
  }
}
