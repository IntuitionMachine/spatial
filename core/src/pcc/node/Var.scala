package pcc.node

import forge._
import pcc.core._
import pcc.data.Effects
import pcc.lang._

@op case class NewVar[T:Type](init: Option[T])(implicit val tp: Type[Var[T]]) extends Alloc[Var[T]] {
  override def aliases  = Nil
  override def contains = syms(init)
  override def extracts = Nil
  override def effects  = Effects.Mutable
  override val debugOnly = true
}
@op case class ReadVar[T:Type](v: Var[T]) extends Primitive[T] {
  override def aliases = Nil
  override def contains = Nil
  override def extracts = syms(v)
  override val isStateless: Boolean = true
  override val debugOnly: Boolean = true
}
@op case class AssignVar[T:Type](v: Var[T], x: T) extends Primitive[Void] {
  override def aliases = Nil
  override def contains = syms(x)
  override def extracts = syms(v)
  override val debugOnly: Boolean = true
}
