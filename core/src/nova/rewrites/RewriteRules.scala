package nova.rewrites

import forge.tags._
import nova.core._
import nova.node._
import nova.lang._

// FIXME: It's a little too easy to write something horribly broken here
trait RewriteRules {
  @rewrite def fixdiv(x: FixDiv[_]) = {case FixDiv(a:Fix[_],Const(1)) => a }

  @rewrite def fixadd(x: FixAdd[_]) = {
    case FixAdd(a:Fix[_],Const(0)) => a
    case FixAdd(Const(0),a:Fix[_]) => a
  }
  @rewrite def fixsub(x: FixSub[_]) = {
    case FixSub(a:Fix[_],Const(0)) => a
    //case op@FixSub(Const(0),a:Fix[_]) => op.tR
  }
  /*@rewrite def fixmul(a: FixMul[_]) = {
    case FixMul(_,z@Const(0)) => z
    case FixMul(z@Const(0),_) => z
  }*/
}