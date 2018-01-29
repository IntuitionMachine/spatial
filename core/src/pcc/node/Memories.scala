package pcc.node

import forge.op
import pcc.core._
import pcc.data._
import pcc.lang._

/** DRAM **/
@op case class DRAMNew[A:Bits](dims: Seq[I32]) extends Memory[DRAM[A]] {
  override def effects: Effects = Effects.Mutable
}

/** FIFO **/
@op case class FIFONew[A:Bits](depth: I32) extends Memory[FIFO[A]] {
  override def effects: Effects = Effects.Mutable
}

/** LIFO **/
@op case class LIFONew[A:Bits](depth: I32) extends Memory[LIFO[A]] {
  override def effects: Effects = Effects.Mutable
}

/** Reg **/
@op case class RegNew[T:Bits](reset: T) extends Memory[Reg[T]] {
  override def effects: Effects = Effects.Mutable
}

@op case class ArgInNew[T:Bits](init: T) extends Memory[Reg[T]]
@op case class ArgOutNew[T:Bits](init: T) extends Memory[Reg[T]] {
  override def effects: Effects = Effects.Mutable
}

@op case class RegWrite[T:Bits](reg: Reg[T], data: T, ens: Seq[Bit]) extends Writer(reg,data.asSym,Nil,ens)

@op case class RegRead[T:Bits](reg: Reg[T]) extends Reader(reg,Nil,Nil) {
  override val isStateless = true
  override def ens: Seq[Bit] = Nil
}

/** SRAM **/
@op case class SRAMNew[A:Bits](dims: Seq[I32]) extends Memory[SRAM[A]] {
  override def effects: Effects = Effects.Mutable
}
@op case class SRAMRead[A:Bits](sram: SRAM[A], addr: Seq[I32], ens: Seq[Bit]) extends Reader[A,A](sram,addr,ens)
@op case class SRAMWrite[A:Bits](sram: SRAM[A], data: A, addr: Seq[I32], ens: Seq[Bit]) extends Writer[A](sram,data.asSym,addr,ens)

@op case class SRAMDim(sram: SRAM[_], d: Int) extends Primitive[I32] {
  override val isStateless: Boolean = true
}
@op case class SRAMRank(sram: SRAM[_]) extends Primitive[I32] {
  override val isStateless: Boolean = true
}