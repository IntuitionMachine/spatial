package pcc.helpers

import pcc.core._
import pcc.lang._

trait HelpersIR {
  implicit class SymUtils[A](x: Sym[A]) {
    def isNum:  Boolean = x.isInstanceOf[Num[_]]
    def isBits: Boolean = x.isInstanceOf[Bits[_]]
    def isVoid: Boolean = x.isInstanceOf[Void]

    def nestedInputs: Set[Sym[_]] = {
      x.dataInputs.toSet ++ x.op.map{o =>
        val outs = o.blocks.flatMap(_.nestedStms)
        val used = outs.flatMap{s => s.nestedInputs }
        used diff outs
      }.getOrElse(Set.empty)
    }
  }

  implicit class ParamHelpers(x: I32) {
    def toInt: Int = x.c.map(_.toInt).getOrElse{throw new Exception(s"Cannot convert symbol $x to a constant")}
  }
}
