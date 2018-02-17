package pcc.core

package object static {
  type State = pcc.core.State
  type SrcCtx = forge.SrcCtx
  type Config = pcc.core.Config
  type Issue  = pcc.core.Issue

  type Type[T] = pcc.core.Type[T]
  type Sym[T] = pcc.core.Sym[T]
  type Def[+A,+B] = pcc.core.Def[A,B]
  lazy val Def = pcc.core.Def

  type Top[T] = pcc.core.Top[T]

  type Op[T] = pcc.core.Op[T]
  lazy val Op = pcc.core.Op

  type Block[T] = pcc.core.Block[T]
  lazy val Block = pcc.core.Block

  type BlockOptions = pcc.core.BlockOptions
  lazy val BlockOptions = pcc.core.BlockOptions

  lazy val Stm = pcc.core.Stm

  lazy val rewrites = pcc.core.rewrites
  lazy val flows = pcc.core.flows
}
