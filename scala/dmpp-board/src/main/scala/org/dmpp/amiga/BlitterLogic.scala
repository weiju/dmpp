package org.dmpp.amiga

object BlitterLogic {
  def lf_0x00(a : Int, b : Int, c: Int) : Int =  0
  def lf_0x01(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c) & 0xffff
  }
  def lf_0x02(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c) & 0xffff
  }
  def lf_0x03(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b) & 0xffff
  }
  def lf_0x04(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c) & 0xffff
  }
  def lf_0x05(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~c) & 0xffff
  }
  def lf_0x06(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x07(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x08(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c) & 0xffff
  }
  def lf_0x09(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x0a(a : Int, b : Int, c: Int) : Int =  {
    (~a & c) & 0xffff
  }
  def lf_0x0b(a : Int, b : Int, c: Int) : Int =  {
    (~a & c | ~a & ~b) & 0xffff
  }
  def lf_0x0c(a : Int, b : Int, c: Int) : Int =  {
    (~a & b) & 0xffff
  }
  def lf_0x0d(a : Int, b : Int, c: Int) : Int =  {
    (~a & b | ~a & ~c) & 0xffff
  }
  def lf_0x0e(a : Int, b : Int, c: Int) : Int =  {
    (~a & b | ~a & c) & 0xffff
  }
  def lf_0x0f(a : Int, b : Int, c: Int) : Int =  {
    (~a) & 0xffff
  }
  def lf_0x10(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c) & 0xffff
  }
  def lf_0x11(a : Int, b : Int, c: Int) : Int =  {
    (~b & ~c) & 0xffff
  }
  def lf_0x12(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x13(a : Int, b : Int, c: Int) : Int =  {
    (~b & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x14(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & b & ~c) & 0xffff
  }
  def lf_0x15(a : Int, b : Int, c: Int) : Int =  {
    (~b & ~c | ~a & ~c) & 0xffff
  }
  def lf_0x16(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x17(a : Int, b : Int, c: Int) : Int =  {
    (~b & ~c | ~a & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x18(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & b & c) & 0xffff
  }
  def lf_0x19(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | ~b & ~c) & 0xffff
  }
  def lf_0x1a(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & c) & 0xffff
  }
  def lf_0x1b(a : Int, b : Int, c: Int) : Int =  {
    (~a & c | ~b & ~c) & 0xffff
  }
  def lf_0x1c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & b) & 0xffff
  }
  def lf_0x1d(a : Int, b : Int, c: Int) : Int =  {
    (~a & b | ~b & ~c) & 0xffff
  }
  def lf_0x1e(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | ~a & b | ~a & c) & 0xffff
  }
  def lf_0x1f(a : Int, b : Int, c: Int) : Int =  {
    (~b & ~c | ~a) & 0xffff
  }
  def lf_0x20(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c) & 0xffff
  }
  def lf_0x21(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x22(a : Int, b : Int, c: Int) : Int =  {
    (~b & c) & 0xffff
  }
  def lf_0x23(a : Int, b : Int, c: Int) : Int =  {
    (~b & c | ~a & ~b) & 0xffff
  }
  def lf_0x24(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & b & ~c) & 0xffff
  }
  def lf_0x25(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & ~c) & 0xffff
  }
  def lf_0x26(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | ~b & c) & 0xffff
  }
  def lf_0x27(a : Int, b : Int, c: Int) : Int =  {
    (~b & c | ~a & ~c) & 0xffff
  }
  def lf_0x28(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & b & c) & 0xffff
  }
  def lf_0x29(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x2a(a : Int, b : Int, c: Int) : Int =  {
    (~b & c | ~a & c) & 0xffff
  }
  def lf_0x2b(a : Int, b : Int, c: Int) : Int =  {
    (~b & c | ~a & c | ~a & ~b) & 0xffff
  }
  def lf_0x2c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & b) & 0xffff
  }
  def lf_0x2d(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | ~a & b | ~a & ~c) & 0xffff
  }
  def lf_0x2e(a : Int, b : Int, c: Int) : Int =  {
    (~a & b | ~b & c) & 0xffff
  }
  def lf_0x2f(a : Int, b : Int, c: Int) : Int =  {
    (~b & c | ~a) & 0xffff
  }
  def lf_0x30(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b) & 0xffff
  }
  def lf_0x31(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~b & ~c) & 0xffff
  }
  def lf_0x32(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~b & c) & 0xffff
  }
  def lf_0x33(a : Int, b : Int, c: Int) : Int =  {
    (~b) & 0xffff
  }
  def lf_0x34(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | a & ~b) & 0xffff
  }
  def lf_0x35(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & ~c) & 0xffff
  }
  def lf_0x36(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | a & ~b | ~b & c) & 0xffff
  }
  def lf_0x37(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~c | ~b) & 0xffff
  }
  def lf_0x38(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | a & ~b) & 0xffff
  }
  def lf_0x39(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | a & ~b | ~b & ~c) & 0xffff
  }
  def lf_0x3a(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & c) & 0xffff
  }
  def lf_0x3b(a : Int, b : Int, c: Int) : Int =  {
    (~a & c | ~b) & 0xffff
  }
  def lf_0x3c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b) & 0xffff
  }
  def lf_0x3d(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b | ~b & ~c) & 0xffff
  }
  def lf_0x3e(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b | ~b & c) & 0xffff
  }
  def lf_0x3f(a : Int, b : Int, c: Int) : Int =  {
    (~b | ~a) & 0xffff
  }
  def lf_0x40(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c) & 0xffff
  }
  def lf_0x41(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x42(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x43(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x44(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c) & 0xffff
  }
  def lf_0x45(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~a & ~c) & 0xffff
  }
  def lf_0x46(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | b & ~c) & 0xffff
  }
  def lf_0x47(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x48(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & b & c) & 0xffff
  }
  def lf_0x49(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x4a(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & c) & 0xffff
  }
  def lf_0x4b(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~a & c | ~a & ~b) & 0xffff
  }
  def lf_0x4c(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~a & b) & 0xffff
  }
  def lf_0x4d(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~a & b | ~a & ~c) & 0xffff
  }
  def lf_0x4e(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~a & c) & 0xffff
  }
  def lf_0x4f(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~a) & 0xffff
  }
  def lf_0x50(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c) & 0xffff
  }
  def lf_0x51(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~b & ~c) & 0xffff
  }
  def lf_0x52(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | a & ~c) & 0xffff
  }
  def lf_0x53(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x54(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | b & ~c) & 0xffff
  }
  def lf_0x55(a : Int, b : Int, c: Int) : Int =  {
    (~c) & 0xffff
  }
  def lf_0x56(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | a & ~c | b & ~c) & 0xffff
  }
  def lf_0x57(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b | ~c) & 0xffff
  }
  def lf_0x58(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | a & ~c) & 0xffff
  }
  def lf_0x59(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | a & ~c | ~b & ~c) & 0xffff
  }
  def lf_0x5a(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c) & 0xffff
  }
  def lf_0x5b(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c | ~b & ~c) & 0xffff
  }
  def lf_0x5c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & b) & 0xffff
  }
  def lf_0x5d(a : Int, b : Int, c: Int) : Int =  {
    (~a & b | ~c) & 0xffff
  }
  def lf_0x5e(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c | b & ~c) & 0xffff
  }
  def lf_0x5f(a : Int, b : Int, c: Int) : Int =  {
    (~c | ~a) & 0xffff
  }
  def lf_0x60(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | a & ~b & c) & 0xffff
  }
  def lf_0x61(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | a & ~b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x62(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~b & c) & 0xffff
  }
  def lf_0x63(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~b & c | ~a & ~b) & 0xffff
  }
  def lf_0x64(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | b & ~c) & 0xffff
  }
  def lf_0x65(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | b & ~c | ~a & ~c) & 0xffff
  }
  def lf_0x66(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c) & 0xffff
  }
  def lf_0x67(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c | ~a & ~c) & 0xffff
  }
  def lf_0x68(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | a & ~b & c | ~a & b & c) & 0xffff
  }
  def lf_0x69(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | a & ~b & c | ~a & b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x6a(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~b & c | ~a & c) & 0xffff
  }
  def lf_0x6b(a : Int, b : Int, c: Int) : Int =  {
    (a & b & ~c | ~b & c | ~a & c | ~a & ~b) & 0xffff
  }
  def lf_0x6c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | b & ~c | ~a & b) & 0xffff
  }
  def lf_0x6d(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & c | b & ~c | ~a & b | ~a & ~c) & 0xffff
  }
  def lf_0x6e(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c | ~a & b) & 0xffff
  }
  def lf_0x6f(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c | ~a) & 0xffff
  }
  def lf_0x70(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | a & ~b) & 0xffff
  }
  def lf_0x71(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | a & ~b | ~b & ~c) & 0xffff
  }
  def lf_0x72(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~b & c) & 0xffff
  }
  def lf_0x73(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~b) & 0xffff
  }
  def lf_0x74(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | b & ~c) & 0xffff
  }
  def lf_0x75(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~c) & 0xffff
  }
  def lf_0x76(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c | a & ~c) & 0xffff
  }
  def lf_0x77(a : Int, b : Int, c: Int) : Int =  {
    (~c | ~b) & 0xffff
  }
  def lf_0x78(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | a & ~c | a & ~b) & 0xffff
  }
  def lf_0x79(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & c | a & ~c | a & ~b | ~b & ~c) & 0xffff
  }
  def lf_0x7a(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c | a & ~b) & 0xffff
  }
  def lf_0x7b(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c | ~b) & 0xffff
  }
  def lf_0x7c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b | a & ~c) & 0xffff
  }
  def lf_0x7d(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b | ~c) & 0xffff
  }
  def lf_0x7e(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~b & c | ~a & b) & 0xffff
  }
  def lf_0x7f(a : Int, b : Int, c: Int) : Int =  {
    (~c | ~b | ~a) & 0xffff
  }
  def lf_0x80(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c) & 0xffff
  }
  def lf_0x81(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & ~b & ~c) & 0xffff
  }
  def lf_0x82(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & ~b & c) & 0xffff
  }
  def lf_0x83(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & ~b) & 0xffff
  }
  def lf_0x84(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & b & ~c) & 0xffff
  }
  def lf_0x85(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & ~c) & 0xffff
  }
  def lf_0x86(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x87(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~a & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x88(a : Int, b : Int, c: Int) : Int =  {
    (b & c) & 0xffff
  }
  def lf_0x89(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | b & c) & 0xffff
  }
  def lf_0x8a(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~a & c) & 0xffff
  }
  def lf_0x8b(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~a & ~b) & 0xffff
  }
  def lf_0x8c(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~a & b) & 0xffff
  }
  def lf_0x8d(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~a & ~c) & 0xffff
  }
  def lf_0x8e(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~a & b | ~a & c) & 0xffff
  }
  def lf_0x8f(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~a) & 0xffff
  }
  def lf_0x90(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | a & ~b & ~c) & 0xffff
  }
  def lf_0x91(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~b & ~c) & 0xffff
  }
  def lf_0x92(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | a & ~b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x93(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~b & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x94(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | a & ~b & ~c | ~a & b & ~c) & 0xffff
  }
  def lf_0x95(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~b & ~c | ~a & ~c) & 0xffff
  }
  def lf_0x96(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | a & ~b & ~c | ~a & b & ~c | ~a & ~b & c) & 0xffff
  }
  def lf_0x97(a : Int, b : Int, c: Int) : Int =  {
    (a & b & c | ~b & ~c | ~a & ~c | ~a & ~b) & 0xffff
  }
  def lf_0x98(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | b & c) & 0xffff
  }
  def lf_0x99(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c) & 0xffff
  }
  def lf_0x9a(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | b & c | ~a & c) & 0xffff
  }
  def lf_0x9b(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c | ~a & c) & 0xffff
  }
  def lf_0x9c(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | b & c | ~a & b) & 0xffff
  }
  def lf_0x9d(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c | ~a & b) & 0xffff
  }
  def lf_0x9e(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b & ~c | b & c | ~a & b | ~a & c) & 0xffff
  }
  def lf_0x9f(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c | ~a) & 0xffff
  }
  def lf_0xa0(a : Int, b : Int, c: Int) : Int =  {
    (a & c) & 0xffff
  }
  def lf_0xa1(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | a & c) & 0xffff
  }
  def lf_0xa2(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~b & c) & 0xffff
  }
  def lf_0xa3(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~b) & 0xffff
  }
  def lf_0xa4(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | a & c) & 0xffff
  }
  def lf_0xa5(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c) & 0xffff
  }
  def lf_0xa6(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | a & c | ~b & c) & 0xffff
  }
  def lf_0xa7(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c | ~b & c) & 0xffff
  }
  def lf_0xa8(a : Int, b : Int, c: Int) : Int =  {
    (a & c | b & c) & 0xffff
  }
  def lf_0xa9(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | a & c | b & c) & 0xffff
  }
  def lf_0xaa(a : Int, b : Int, c: Int) : Int =  {
    (c) & 0xffff
  }
  def lf_0xab(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b | c) & 0xffff
  }
  def lf_0xac(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & b) & 0xffff
  }
  def lf_0xad(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c | b & c) & 0xffff
  }
  def lf_0xae(a : Int, b : Int, c: Int) : Int =  {
    (~a & b | c) & 0xffff
  }
  def lf_0xaf(a : Int, b : Int, c: Int) : Int =  {
    (c | ~a) & 0xffff
  }
  def lf_0xb0(a : Int, b : Int, c: Int) : Int =  {
    (a & c | a & ~b) & 0xffff
  }
  def lf_0xb1(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~b & ~c) & 0xffff
  }
  def lf_0xb2(a : Int, b : Int, c: Int) : Int =  {
    (a & c | a & ~b | ~b & c) & 0xffff
  }
  def lf_0xb3(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~b) & 0xffff
  }
  def lf_0xb4(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | a & c | a & ~b) & 0xffff
  }
  def lf_0xb5(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c | a & ~b) & 0xffff
  }
  def lf_0xb6(a : Int, b : Int, c: Int) : Int =  {
    (~a & b & ~c | a & c | a & ~b | ~b & c) & 0xffff
  }
  def lf_0xb7(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c | ~b) & 0xffff
  }
  def lf_0xb8(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | b & c) & 0xffff
  }
  def lf_0xb9(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c | a & c) & 0xffff
  }
  def lf_0xba(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | c) & 0xffff
  }
  def lf_0xbb(a : Int, b : Int, c: Int) : Int =  {
    (c | ~b) & 0xffff
  }
  def lf_0xbc(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b | a & c) & 0xffff
  }
  def lf_0xbd(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & b | ~b & ~c) & 0xffff
  }
  def lf_0xbe(a : Int, b : Int, c: Int) : Int =  {
    (a & ~b | ~a & b | c) & 0xffff
  }
  def lf_0xbf(a : Int, b : Int, c: Int) : Int =  {
    (c | ~b | ~a) & 0xffff
  }
  def lf_0xc0(a : Int, b : Int, c: Int) : Int =  {
    (a & b) & 0xffff
  }
  def lf_0xc1(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | a & b) & 0xffff
  }
  def lf_0xc2(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | a & b) & 0xffff
  }
  def lf_0xc3(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b) & 0xffff
  }
  def lf_0xc4(a : Int, b : Int, c: Int) : Int =  {
    (a & b | b & ~c) & 0xffff
  }
  def lf_0xc5(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~c) & 0xffff
  }
  def lf_0xc6(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | a & b | b & ~c) & 0xffff
  }
  def lf_0xc7(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b | b & ~c) & 0xffff
  }
  def lf_0xc8(a : Int, b : Int, c: Int) : Int =  {
    (a & b | b & c) & 0xffff
  }
  def lf_0xc9(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | a & b | b & c) & 0xffff
  }
  def lf_0xca(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & c) & 0xffff
  }
  def lf_0xcb(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b | b & c) & 0xffff
  }
  def lf_0xcc(a : Int, b : Int, c: Int) : Int =  {
    (b) & 0xffff
  }
  def lf_0xcd(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~c | b) & 0xffff
  }
  def lf_0xce(a : Int, b : Int, c: Int) : Int =  {
    (~a & c | b) & 0xffff
  }
  def lf_0xcf(a : Int, b : Int, c: Int) : Int =  {
    (b | ~a) & 0xffff
  }
  def lf_0xd0(a : Int, b : Int, c: Int) : Int =  {
    (a & b | a & ~c) & 0xffff
  }
  def lf_0xd1(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~b & ~c) & 0xffff
  }
  def lf_0xd2(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | a & b | a & ~c) & 0xffff
  }
  def lf_0xd3(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b | a & ~c) & 0xffff
  }
  def lf_0xd4(a : Int, b : Int, c: Int) : Int =  {
    (a & b | a & ~c | b & ~c) & 0xffff
  }
  def lf_0xd5(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~c) & 0xffff
  }
  def lf_0xd6(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & c | a & b | a & ~c | b & ~c) & 0xffff
  }
  def lf_0xd7(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b | ~c) & 0xffff
  }
  def lf_0xd8(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | b & c) & 0xffff
  }
  def lf_0xd9(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c | a & b) & 0xffff
  }
  def lf_0xda(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c | a & b) & 0xffff
  }
  def lf_0xdb(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & c | ~b & ~c) & 0xffff
  }
  def lf_0xdc(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | b) & 0xffff
  }
  def lf_0xdd(a : Int, b : Int, c: Int) : Int =  {
    (b | ~c) & 0xffff
  }
  def lf_0xde(a : Int, b : Int, c: Int) : Int =  {
    (a & ~c | ~a & c | b) & 0xffff
  }
  def lf_0xdf(a : Int, b : Int, c: Int) : Int =  {
    (b | ~c | ~a) & 0xffff
  }
  def lf_0xe0(a : Int, b : Int, c: Int) : Int =  {
    (a & b | a & c) & 0xffff
  }
  def lf_0xe1(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | a & b | a & c) & 0xffff
  }
  def lf_0xe2(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~b & c) & 0xffff
  }
  def lf_0xe3(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b | a & c) & 0xffff
  }
  def lf_0xe4(a : Int, b : Int, c: Int) : Int =  {
    (a & c | b & ~c) & 0xffff
  }
  def lf_0xe5(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c | a & b) & 0xffff
  }
  def lf_0xe6(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c | a & b) & 0xffff
  }
  def lf_0xe7(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~b & c | ~a & ~c) & 0xffff
  }
  def lf_0xe8(a : Int, b : Int, c: Int) : Int =  {
    (a & b | a & c | b & c) & 0xffff
  }
  def lf_0xe9(a : Int, b : Int, c: Int) : Int =  {
    (~a & ~b & ~c | a & b | a & c | b & c) & 0xffff
  }
  def lf_0xea(a : Int, b : Int, c: Int) : Int =  {
    (a & b | c) & 0xffff
  }
  def lf_0xeb(a : Int, b : Int, c: Int) : Int =  {
    (a & b | ~a & ~b | c) & 0xffff
  }
  def lf_0xec(a : Int, b : Int, c: Int) : Int =  {
    (a & c | b) & 0xffff
  }
  def lf_0xed(a : Int, b : Int, c: Int) : Int =  {
    (a & c | ~a & ~c | b) & 0xffff
  }
  def lf_0xee(a : Int, b : Int, c: Int) : Int =  {
    (b | c) & 0xffff
  }
  def lf_0xef(a : Int, b : Int, c: Int) : Int =  {
    (b | c | ~a) & 0xffff
  }
  def lf_0xf0(a : Int, b : Int, c: Int) : Int =  {
    (a) & 0xffff
  }
  def lf_0xf1(a : Int, b : Int, c: Int) : Int =  {
    (~b & ~c | a) & 0xffff
  }
  def lf_0xf2(a : Int, b : Int, c: Int) : Int =  {
    (~b & c | a) & 0xffff
  }
  def lf_0xf3(a : Int, b : Int, c: Int) : Int =  {
    (a | ~b) & 0xffff
  }
  def lf_0xf4(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | a) & 0xffff
  }
  def lf_0xf5(a : Int, b : Int, c: Int) : Int =  {
    (a | ~c) & 0xffff
  }
  def lf_0xf6(a : Int, b : Int, c: Int) : Int =  {
    (b & ~c | ~b & c | a) & 0xffff
  }
  def lf_0xf7(a : Int, b : Int, c: Int) : Int =  {
    (a | ~c | ~b) & 0xffff
  }
  def lf_0xf8(a : Int, b : Int, c: Int) : Int =  {
    (b & c | a) & 0xffff
  }
  def lf_0xf9(a : Int, b : Int, c: Int) : Int =  {
    (b & c | ~b & ~c | a) & 0xffff
  }
  def lf_0xfa(a : Int, b : Int, c: Int) : Int =  {
    (a | c) & 0xffff
  }
  def lf_0xfb(a : Int, b : Int, c: Int) : Int =  {
    (a | c | ~b) & 0xffff
  }
  def lf_0xfc(a : Int, b : Int, c: Int) : Int =  {
    (a | b) & 0xffff
  }
  def lf_0xfd(a : Int, b : Int, c: Int) : Int =  {
    (a | b | ~c) & 0xffff
  }
  def lf_0xfe(a : Int, b : Int, c: Int) : Int =  {
    (a | b | c) & 0xffff
  }
  def lf_0xff(a : Int, b : Int, c: Int) : Int =  0xffff
}
