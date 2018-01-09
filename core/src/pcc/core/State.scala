package pcc.core

import java.io.PrintStream

import pcc.util.NullOutputStream

import scala.collection.mutable

class State {
  /** Config **/
  val config: Config = new Config

  /** Symbol IDs **/
  private var id: Int = -1
  def maxId: Int = id-1 // Inclusive
  def nextId(): Int = { id += 1; id }

  /** List of effectful statements in the current scope **/
  var context: Seq[Sym[_]] = _

  /** Alias caches **/
  val shallowAliasCache = new mutable.HashMap[Sym[_], Set[Sym[_]]]
  val deepAliasCache = new mutable.HashMap[Sym[_], Set[Sym[_]]]
  val aliasCache = new mutable.HashMap[Sym[_], Set[Sym[_]]]

  /** Definition cache used for CSE **/
  var defCache: Map[Op[_], Sym[_]] = Map.empty

  /** Graph Metadata **/
  val metadata:  GraphMetadata = new GraphMetadata
  val globals: GlobalMetadata = new GlobalMetadata

  /** Compiler passes **/
  var pass: Int = 0
  def paddedPass: String = paddedPass(pass)
  def paddedPass(pass: Int): String = { val p = pass.toString; "0"*(4 - p.length) + p }

  /** Logging / Streams **/
  var logTab: Int = 0
  var genTab: Int = 0
  var out: PrintStream = Console.out
  var log: PrintStream = new PrintStream(new NullOutputStream)
  var gen: PrintStream = new PrintStream(new NullOutputStream)
  val streams = new mutable.HashMap[String, PrintStream]

  /** Infos **/
  var infos: Int = 0
  def hadInfos: Boolean = infos > 0
  def logInfo(): Unit = { infos += 1 }

  /** Warnings **/
  var warnings: Int = 0
  def hadWarnings: Boolean = warnings > 0
  def logWarning(): Unit = { warnings += 1 }

  /** Errors **/
  var errors: Int = 0
  def hadErrors: Boolean = errors > 0
  def logError(): Unit = { errors += 1 }

  /** Bugs **/
  var bugs: Int = 0
  def hadBugs: Boolean = bugs > 0
  def logBug(): Unit = { bugs += 1 }

  def reset(): Unit = {
    config.reset()
    id = -1
    context = Nil
    shallowAliasCache.clear()
    deepAliasCache.clear()
    aliasCache.clear()
    defCache = Map.empty
    metadata.reset()
    globals.reset()
    pass = 1
    logTab = 0
    genTab = 0
    log = new PrintStream(new NullOutputStream)
    gen = new PrintStream(new NullOutputStream)
    streams.clear()
    infos = 0
    warnings = 0
    errors = 0
    bugs = 0
  }

  def copyTo(target: State): Unit = {
    this.config.copyTo(target.config)
    target.id = this.id
    target.context = this.context
    target.shallowAliasCache ++= this.shallowAliasCache
    target.deepAliasCache ++= this.deepAliasCache
    target.aliasCache ++= this.aliasCache
    target.defCache = this.defCache
    metadata.copyTo(target.metadata)
    globals.copyTo(target.globals)
    target.pass = this.pass
    target.logTab = this.logTab
    target.genTab = this.genTab
    target.log = this.log
    target.gen = this.gen
    target.streams ++= this.streams
    target.infos = this.infos
    target.warnings = this.warnings
    target.errors = this.errors
    target.bugs = this.bugs
  }
}