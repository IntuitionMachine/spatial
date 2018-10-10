package spatial.codegen.chiselgen

import argon._
import argon.node._
import spatial.lang._
import spatial.node._
import spatial.metadata.control._
import spatial.metadata.retiming._

trait ChiselGenDebug extends ChiselGenCommon {

  override protected def gen(lhs: Sym[_], rhs: Op[_]): Unit = rhs match {
	case FixToText(_)  =>
    case TextConcat(_) => 
    case PrintIf(_,_) => 
    case BitToText(_) => 
    case GenericToText(_) =>
    case VarNew(_) => 
    case VarRead(_) => 
    case VarAssign(_,_) => 

    case ExitIf(en) => 
    	val ens = if (en.isEmpty) "true.B" else en.map(quote).mkString("&")
	    emit(s"breakpoints(${earlyExits.length}) := ${ens} & (${quote(lhs.parent.s.get)}.datapathEn).D(${lhs.fullDelay})")
	    earlyExits = earlyExits :+ lhs

    case AssertIf(en,cond,_) => 
    	if (inHw) {
	    	val ens = if (en.isEmpty) "true.B" else en.map(quote).mkString("&")
	        emit(s"breakpoints(${earlyExits.length}) := ${ens} & (${quote(lhs.parent.s.get)}.datapathEn).D(${lhs.fullDelay}) & ~${quote(cond)}")
	        earlyExits = earlyExits :+ lhs
	    }

    case BreakpointIf(en) => 
    	val ens = if (en.isEmpty) "true.B" else en.map(quote).mkString("&")
        emit(s"breakpoints(${earlyExits.length}) := ${ens} & (${quote(lhs.parent.s.get)}.datapathEn).D(${lhs.fullDelay})")
        earlyExits = earlyExits :+ lhs

	case _ => super.gen(lhs, rhs)
  }
}