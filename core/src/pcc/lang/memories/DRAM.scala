package pcc.lang
package memories

import forge.api
import pcc.core._
import pcc.node._

import scala.collection.mutable

case class DRAM[A](eid: Int, tA: Bits[A]) extends RemoteMem[A,DRAM](eid) {
  type AI = tA.I
  override type I = Array[AI]

  override def fresh(id: Int): DRAM[A] = new DRAM[A](id,tA)
  override def stagedClass: Class[DRAM[A]] = classOf[DRAM[A]]
  override def typeArguments: List[Sym[_]] = List(tA)
}
object DRAM {
  private def apply[A](eid: Int, tA: Bits[A]): DRAM[A] = new DRAM[A](eid,tA)

  private lazy val types = new mutable.HashMap[Bits[_],DRAM[_]]()
  implicit def tp[A:Bits]: DRAM[A] = types.getOrElseUpdate(bits[A], new DRAM[A](-1,bits[A])).asInstanceOf[DRAM[A]]

  @api def apply[A:Bits](dims: I32*): DRAM[A] = stage(DRAMNew(dims))
}