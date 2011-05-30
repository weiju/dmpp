/**
 * Created on March 30, 2011
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
package org.dmpp.common

// **********************************************************************
// ***** Bus protocol definition
// ***** The Amiga differs from most other computers of its time
// ***** by having DMA devices which have to be synchronized on
// ***** the chip bus, this means the address space of the custom
// ***** chip registers and the chip RAM. On the chip bus, the DMA
// ***** access is prioritized, all DMA channels have a higher priority
// ***** level than the CPU which has to wait for the DMA controller
// ***** in Agnus to satisfy its requests.
// ***** DMPP implements synchronization by emulating a simple bus protocol.
// *****
// ***** There are two buses in DMPP:
// ***** 1. the processor bus, which always instantly acknowledges the
// *****    CPU's request when accessing non-chip address space
// ***** 2. the chip bus, which the DMA controller controls.
// *****
// ***** A BusDevice sends a memory request to the bus, requesting a
// ***** certain number of cycles and then switches into
// ***** waiting mode. The DMA controller stores the
// ***** request and wakes up the device when all requested cycles could
// ***** be allocated to it.
// *****
// ***** Only odd bus cycles actually need to be scheduled, there is
// ***** no conflict on the even cycles.
// ***** Bus devices are only the Copper, the Blitter and the CPU.
// ***** Memory refresh, disk, sound, and sprites take the even cycles anyways,
// ***** bitplane DMA will always take priority when odd cycles are
// ***** required.
// **********************************************************************

/**
 * BusDevice interface.
 */
trait BusDevice {
  /**
   * Notify a waiting device that its memory request was satisfied.
   */
  def memoryRequestAcknowledged: Unit
}

/**
 * Generic Bus interface. The CPU accesses memory by making bus requests.
 * Because we have DMA, memory accesses need to be serialized.
 */
trait Bus {
  /**
   * Sends a memory request.
   * @param device the requesting BusDevice
   * @param address the address of the request
   * @param numCycles the number of cycles requested
   */
  def requestMemory(device: BusDevice, address: Int, numCycles: Int): Unit
}
