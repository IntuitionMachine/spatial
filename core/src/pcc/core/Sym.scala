package pcc.core

import forge._
import pcc.lang.Text
import pcc.util.Tri._
import pcc.util.Types._
import pcc.util.escapeConst

abstract class Sym[A](eid: Int)(implicit ev: A<:<Sym[A]) extends Product { self =>
  type I
  @inline final protected def me: A = this.asInstanceOf[A]
  private var _rhs: Tri[I,Op[A]] = Nix
  private var isFixed: Boolean = true

  var name: Option[String] = None
  var ctx: SrcCtx = SrcCtx.empty
  var prevNames: Seq[(String,String)] = Nil

  final def fixed(): Unit = { isFixed = true }
  final def asBound(): A = { _rhs = Nix; me }
  final def asConst(c: Any): A = { _rhs = One(c.asInstanceOf[self.I]); isFixed = true; me }
  final def asParam(c: Any): A = { _rhs = One(c.asInstanceOf[self.I]); isFixed = false; me }
  final def asSymbol(rhs: Op[A]): A = { _rhs = Two(rhs); me }
  final def isConst: Boolean = _rhs.isOne && isFixed
  final def isParam: Boolean = _rhs.isOne && !isFixed
  final def isValue: Boolean = _rhs.isOne
  final def isBound: Boolean = id >= 0 && _rhs.isNix
  final def isSymbol: Boolean = _rhs.isTwo
  final def isType: Boolean   = id < 0
  final def rhs: Tri[I,Op[A]] = _rhs
  final def c: Option[I] = _rhs.getOne
  final def op: Option[Op[A]] = _rhs.getTwo
  final def id: Int = eid

  final def dataInputs: Seq[Sym[_]] = op.map(_.inputs).getOrElse(Nil)

  @inline final def viewAsSym(x: A): Sym[A] = ev(x)
  @inline final def asSym: Sym[A] = this

  override def toString: String = if (isType) this.typeName else _rhs match {
    case One(c) => s"${escapeConst(c)}"
    case Two(_) => s"x$id"
    case Nix    => s"b$id"
  }

  override def hashCode(): Int = c.map(_.hashCode()).getOrElse(id)
  override def equals(x: Any): Boolean = x match {
    case that: Sym[_] => this.id == that.id || (this.isValue && that.isValue && this.c == that.c)
    case _ => false
  }

  def fresh(id: Int): A
  def isPrimitive: Boolean
  def typeArguments: List[Sym[_]] = Nil
  def stagedClass: Class[A]
  def typeName: String = productPrefix + (if (typeArguments.isEmpty) "" else typeArguments.map(_.typeName).mkString("[", ",", "]"))
  def tp: A = fresh(-1)
  def mtyp[B]: B = this.tp.asInstanceOf[B]

  final def <:<(that: Sym[_]): Boolean = isSubtype(this.stagedClass, that.stagedClass)
  final def <:>(that: Sym[_]): Boolean = this <:< that && that <:< this

  @api def toText: Text = Text.textify(me)(this.tp,ctx,state)
}

object Lit {
  def unapply[A<:Sym[A]](x: A): Option[A#I] = if (x.isConst) x.c else None
}
object Const {
  def unapply(x: Any): Option[Any] = x match {
    case s: Sym[_] if s.isConst => s.c
    case _ => None
  }
}
object Param {
  def unapply(x: Any): Option[Any] = x match {
    case s: Sym[_] if s.isParam || s.isConst => s.c
    case _ => None
  }
}

