package scalera.macros

import scala.reflect.macros._

object Macros {

  def entityImpl(
                  c: whitebox.Context)(
                  name: c.Expr[String])(
                  attributes: c.Expr[(String, Any)]*) = {
    import c.universe._
    c.Expr[Any]( q"""new {
      ${classExpr(c)(name)(attributes:_*)}
      }""")
  }

  def classExpr(
                 c: whitebox.Context)(
                 name: c.Expr[String])(
                 attributes: c.Expr[(String, Any)]*) = {
    import c.universe._
    val members =
      attributes.map{ att =>
        c.eval(c.Expr[(String,Any)](c.untypecheck(att.tree))) match {
          case (attName,value) =>
            ValDef(
              Modifiers(Flag.CASEACCESSOR | Flag.PARAMACCESSOR),
              TermName(attName),
              Ident(TypeName("Any")),//TODO Infer type from value.
              EmptyTree)
        }
      }.toList
    ClassDef(
      Modifiers(Flag.CASE),
      TypeName(c.eval(name)),
      List(),
      Template(
        List(
          Select(Ident(TermName("scala")), TypeName("Product")),
          Select(Ident(TermName("scala")), TypeName("Serializable"))),
        noSelfType,
        members :::
        List(
          DefDef(
            Modifiers(),
            termNames.CONSTRUCTOR,
            List(),
            List(members),
            TypeTree(),
            Block(List(pendingSuperCall), Literal(Constant(())))))))
  }

}