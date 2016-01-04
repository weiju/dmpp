# Dream Machine Preservation Project

Author: Wei-ju Wu
Creation Date: Sometime in 2009
Description: Amiga research project

## About this project

This is a research project to figure out the inner workings of an Amiga computer.
One day this might become a real emulator.
Why would someone write an Amiga emulator when there are already Fellow and UAE,
one might ask. Well, these two emulators have a very strong Windows bias. I don't
use Windows except for testing.
Also, Fellow is implemented in assembly code, while UAE is written in C/C++.
I wanted to write this emulator in a higher and less verbose programming language,
which is also easier to understand than C/C++ or assembly code.

This currently happens to be Scala. It is in my opinion one of the best languages
to implement virtual machines in and it runs on the widely available Java Virtual
Machine. This might turn out to be a disadvantage in the future, but in my opinion,
using a higher programming language (and with less code) lets one focus more on
concepts than on implementation, so migrating to a different language/platform
should be easier.

## Building/Running (Scala 2.11.6/SBT 0.13.6)

```
cd scala
sbt
project dmpp-debugger
run <path-to-kickstart-rom-file>
```

## Challenges

Programming an Amiga emulator is more challenging than emulating many other systems
because it was one of the first systems to have dedicated coprocessors (Blitter, Copper)
which run concurrently to the CPU.
Synchronizing their operation and access to chip memory is tricky, one has to
keep in mind the priorities of the different DMA channels over each other.

## Status

Cpu:

 - Can run a significant portion of the Kickstart 1.3 68k code

Address space:

 - Kickstart ROM
 - Chip memory
 - Custom chip space
 - CIA space

CIA:

 - timer implementation

Copper:

 - implements MOVE

Debugger:

 - displays CPU state
 - displays Chip registers
 - displays CIA state
 - displays current code

Simulator:

 - visualize relationship between DMA cycles and beam position

## Derived Projects

This project spawned a couple of projects that were created in order to
provide custom tools:

 - Mahatma68k, a Motorola 68000 emulator written in Java
 - Logic Shrinker, a simplifier for digital logic using the Quine-McCluskey
   algorithm (Android and iPhone)
 - ADF Tools/Arr!Jay, an ADF management library and an ADF manager application

## Known issues:

 - setting a register has an immediate effect, e.g. when a strobe register is written.
   We might need to wait a couple of cycles in the future

