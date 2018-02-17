package pcc.data

import forge._
import pcc.core._
import pcc.traversal.transform.Transformer

import scala.collection.mutable

/** Shortcuts for metadata **/
@data object metadata {
  type Data = mutable.Map[Class[_],Metadata[_]]

  private def keyOf[M<:Metadata[M]:Manifest]: Class[M] = manifest[M].runtimeClass.asInstanceOf[Class[M]]

  def addAll(edge: Sym[_], data: Iterator[Metadata[_]]): Unit = data.foreach{m => edge.data += (m.key -> m) }
  def addOrRemoveAll(edge: Sym[_], data: Iterator[(Class[_],Option[Metadata[_]])]): Unit = data.foreach{
    case (key,Some(m)) => edge.data += (key -> m)
    case (key,None)    => edge.data.remove(key)
  }

  def add[M<:Metadata[M]:Manifest](edge: Sym[_], m: M): Unit = edge.data += (m.key -> m)
  def add[M<:Metadata[M]:Manifest](edge: Sym[_], m: Option[M]): Unit = m match {
    case Some(data) => edge.data += (data.key -> data)
    case None => edge.data.remove(keyOf[M])
  }
  def all(edge: Sym[_]): Iterator[(Class[_],Metadata[_])] = edge.data.iterator

  def clear[M<:Metadata[M]:Manifest](edge: Sym[_]): Unit = edge.data.remove(keyOf[M])

  def apply[M<:Metadata[M]:Manifest](edge: Sym[_]): Option[M] = edge.data.get(keyOf[M]).map(_.asInstanceOf[M])
}

@data object globals {
  def add[M<:Metadata[M]:Manifest](m: M): Unit = state.globals.add[M](m)
  def apply[M<:Metadata[M]:Manifest]: Option[M] = state.globals[M]
  def clear[M<:Metadata[M]:Manifest]: Unit = state.globals.clear[M]
  def mirrorAfterTransform(f: Transformer): Unit = state.globals.mirrorAfterTransform(f)
  def clearBeforeTransform(): Unit = state.globals.clearBeforeTransform()

  def foreach(func: (Class[_],Metadata[_]) => Unit): Unit = state.globals.foreach(func)
}
