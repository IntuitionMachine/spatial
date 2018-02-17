package forge

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

case class utils[Ctx <: blackbox.Context](ctx: Ctx) {
  import ctx.universe._

  // Fix for bug where <caseaccessor> gets added to (private) implicit fields
  def fieldsFix(fields: List[ValDef]): List[ValDef] = fields.map{
    case ValDef(mods,name,tp,rhs) if mods.hasFlag(Flag.CASEACCESSOR) && mods.hasFlag(Flag.IMPLICIT) && mods.hasFlag(Flag.SYNTHETIC) =>
      val flags = Modifiers(Flag.SYNTHETIC | Flag.IMPLICIT | Flag.PARAMACCESSOR)
      ValDef(flags, name, tp, rhs)
    case v => v
  }

  def makeTypeName(tp: TypeDef): Tree = {
    val TypeDef(_,TypeName(name),targs,_) = tp
    makeType(name, targs)
  }

  def makeType(name: String, targs: List[TypeDef]): Tree = {
    val init = Ident(TypeName(name))
    if (targs.isEmpty) init else AppliedTypeTree(init, targs.map(makeTypeName))
  }

  def makeDefCall(name: String, targs: List[TypeDef], argss: List[List[Tree]]): Tree = {
    val call = Ident(TermName(name))
    val fullCall = if (targs.isEmpty) call else {
      TypeApply(call, targs.map(makeTypeName))
    }
    argss.foldLeft(fullCall){(call,args) => Apply(call,args) }
  }

  def makeType(name: String): Tree = makeType(name, Nil)

  def injectClassMethod(
    cls: ClassDef,
    errorIfExists: Boolean,
    method: (String, Tree) => Tree
  ): ClassDef = {
    val ClassDef(mods,TypeName(name),tparams,Template(parents,self,_)) = cls
    val (fieldsX, methods) = cls.fieldsAndMethods

    val fields = fieldsFix(fieldsX)
    val body = fields ++ methods

    val fieldNames = fields.map(_.name)
    val methodNames = methods.map(_.name)
    val names = fieldNames ++ methodNames
    val tp = makeType(name,tparams)
    val newMethod = method(name,tp)

    val methodName = newMethod match {
      case d: DefDef => d.name
      case _ =>
        ctx.abort(ctx.enclosingPosition, "Inject method did not return a def.")
    }
    if (!names.contains(methodName)) {
      ClassDef(mods,TypeName(name),tparams,Template(parents,self,body :+ newMethod))
    }
    else if (errorIfExists) {
      ctx.error(ctx.enclosingPosition, s"Could not inject method $methodName to class - method already defined")
      cls
    }
    else cls
  }

  def isWildcardType(tp: Tree, str: String): Boolean = tp match {
    case ExistentialTypeTree(AppliedTypeTree(Ident(TypeName(`str`)), List(Ident(TypeName(arg)))), _) => arg.startsWith("_$")
    case _ => false
  }

  def modifyClassFields(
    cls: ClassDef,
    func: ValDef => ValDef
  ): ClassDef = {
    val ClassDef(mods,TypeName(name),tparams,Template(parents,self,_)) = cls
    val (fieldsX,methods) = cls.fieldsAndMethods
    val fields = fieldsFix(fieldsX)
    val fields2 = fields.map(func)
    val body2 = fields2 ++ methods
    ClassDef(mods,TypeName(name),tparams,Template(parents,self,body2))
  }

  implicit class ValDefOps(v: ValDef) {
    def tp: Option[Tree] = {
      val ValDef(_,_,tp,_) = v
      if (tp == EmptyTree) None else Some(tp)
    }
    def asVar: ValDef = {
      val ValDef(mods,name,tp,rhs) = v
      ValDef(Modifiers(mods.flags | Flag.MUTABLE),name,tp,rhs)
    }
  }

  implicit class DefDefOps(df: DefDef) {
    def paramss: List[List[ValDef]] = {
      val DefDef(_,_,_,pss,_,_) = df
      pss
    }
  }

  implicit class ClassOps(cls: ClassDef) {
    val ClassDef(mods,TypeName(nameStr),tparams, impl @ Template(parents,selfType,body)) = cls

    def injectMethod(method: (String, Tree) => Tree): ClassDef = {
      injectClassMethod(cls, errorIfExists = false, method)
    }
//    def optionalInjectMethod(method: (String,Tree) => Tree): ClassDef = {
//      injectClassMethod(cls, errorIfExists = false, method)
//    }

    def fieldsAndMethods: (List[ValDef],List[DefDef]) = {
      val fields  = body.collect{case x: ValDef => x }
      val methods = body.collect{case x: DefDef => x }
      (fields,methods)
    }

    def fields: List[ValDef]  = fieldsAndMethods._1
    def methods: List[DefDef] = fieldsAndMethods._2

    def modifyFields(func: ValDef => ValDef): ClassDef = {
      modifyClassFields(cls,func)
    }
    def withVarParams: ClassDef = {
      val params = constructorArgs.head.map(_.name)
      cls.modifyFields{
        case field if params.contains(field.name) => field.asVar
        case field => field
      }
    }

    def typeArgs: List[Tree] = tparams.map{tp => Ident(tp.name)}

    def constructor: Option[DefDef] = methods.find{_.name == termNames.CONSTRUCTOR}
    def constructorArgs: List[List[ValDef]] = constructor.map{d =>  d.paramss }.getOrElse(Nil)

    def callConstructor(args: Tree*): Tree = {
      makeDefCall(nameStr,tparams,List(args.toList))
    }

    def asCaseClass: ClassDef = {
      ClassDef(Modifiers(mods.flags | Flag.CASE),cls.name,tparams,impl)
    }
  }

}
