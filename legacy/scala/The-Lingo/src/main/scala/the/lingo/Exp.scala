/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo

import the.lingo.Value.Implicits._
import the.lingo.Showable.Implicits._

final object Exp {
  private[lingo] def consExp(tag: Value, xs: List[Value]): CoreWHNF =
    Tagged(Symbols.Exp, ListUtils.List(tag, ListUtils.ConsList(xs)))
}

private final object AsExpCached {

  private final object NotCached {
    def unapply(x: WHNF): Option[Exp] = x match {
      case x: Exp => Some(x)
      case _ => unapplyCore(x.toCore())
    }

    def unapplyCore(x: CoreWHNF): Option[Exp] = x match {
      case Tagged(AsSym(Symbols.Exp), AsCoreWHNF(Pair(AsSym(tag), ListUtils.ConsList(xs)))) =>
        (tag, xs) match {
          case (Symbols.Id, List(x)) => Some(Id(x))
          case (Symbols.Quote, List(x)) => Some(Quote(x))
          case (Symbols.Comment, List(comment, x)) => Some(Comment(comment, x))
          case (Symbols.Positioned, List(AsDebugStackPositionCached(pos), x)) => Some(Positioned(pos, x))
          case (Symbols.ApplyFunc, List(f, ListUtils.ConsList(xs))) => Some(ApplyFunc(f, xs))
          case (Symbols.ApplyMacro, List(f, ListUtils.ConsList(xs))) => Some(ApplyFunc(f, xs))
          case (Symbols.Builtin, List(AsSym(f), ListUtils.ConsList(xs))) => Some(Builtin(f, xs))
          case _ => None
        }
      case _ => None
    }
  }

  private val unapply_v = Value.cached_option_as(NotCached.unapply)

  def unapply(x: Value): Option[Exp] = unapply_v.apply(x)
}

sealed trait Exp extends FeaturedWHN_eval {
  private[lingo] def real_eval(context: Mapping, stack: DebugStack): Value

  final override def feature_eval(context: Mapping, stack: DebugStack) = this.real_eval(context, stack)
}

final case class Id(x: Value) extends Exp {
  override def impl_toCore() = Exp.consExp(Symbols.Id, List(x))

  private[lingo] override def real_eval(context: Mapping, stack: DebugStack) = context.get(x).getOrElse {
    CoreException(stack, Symbols.CoreExceptions.NoDefinition, context, this)
  }

  override def impl_show(implicit showContext: ShowContext): String = s"Id(${x.show})"
}

final case class Quote(x: Value) extends Exp {
  override def impl_toCore() = Exp.consExp(Symbols.Quote, List(x))

  private[lingo] override def real_eval(context: Mapping, stack: DebugStack) = x

  override def impl_show(implicit showContext: ShowContext): String = s"Quote(${x.show})"
}

final case class Comment(comment: Value, x: Value) extends Exp {
  override def impl_toCore() = Exp.consExp(Symbols.Comment, List(comment, x))

  private[lingo] override def real_eval(context: Mapping, stack: DebugStack) = x.eval(context, stack)

  override def impl_show(implicit showContext: ShowContext): String = s"Comment(${comment.show},${x.show})"
}

final private object WrapperedExp {
  def unapply(x: Value): Option[Exp] = x match {
    case AsExpCached(e) => e match {
      case Comment(_, x) => unapply(x)
      case Positioned(_, x) => unapply(x)
      case x => Some(x)
    }
    case _ => None
  }
}

final case class Positioned(pos: DebugStackPosition, x: Value) extends Exp {
  override def impl_toCore() =
    Exp.consExp(Symbols.Positioned, List(pos, x))

  private[lingo] override def real_eval(context: Mapping, stack: DebugStack) = x.eval(context, stack.push(pos))

  override def impl_show(implicit showContext: ShowContext): String = s"Positioned(${pos.show},${x.show})"
}

final case class ApplyFunc(f: Value, xs: List[Value]) extends Exp {
  private lazy val fAsId = f match {
    case WrapperedExp(Id(x)) => Some(x)
    case _ => None
  }

  override def impl_toCore() =
    Exp.consExp(Symbols.ApplyFunc, List(f, ListUtils.ConsList(xs)))

  private[lingo] override def real_eval(context: Mapping, rawStack: DebugStack) = {
    val stack = fAsId match {
      case Some(s) => rawStack.push(NamedPosition(s))
      case None => rawStack
    }
    f.eval(context, stack).app(xs.map((x: Value) => x.eval(context, stack)), stack)
  }

  override def impl_show(implicit showContext: ShowContext): String = s"ApplyFunc(${f.show},${xs.show})"
}

final case class ApplyMacro(f: Value, xs: List[Value]) extends Exp {
  override def impl_toCore() =
    Exp.consExp(Symbols.ApplyMacro, List(f, ListUtils.ConsList(xs)))

  private[lingo] override def real_eval(context: Mapping, stack: DebugStack) = f.eval(context, stack) match {
    case AsCoreWHNF(Tagged(AsSym(Symbols.Tags.Macro), ListUtils.ConsList(List(f)))) => f.app(context :: xs, stack)
    case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Macro, context, this)
  }

  override def impl_show(implicit showContext: ShowContext): String =
    s"ApplyMacro(${f.show},${xs.show})"
}

final case class Builtin(f: Sym, xs: List[Value]) extends Exp {
  override def impl_show(implicit showContext: ShowContext): String =
    s"Builtin(${f.show},${xs.show})"

  override def impl_toCore() =
    Exp.consExp(Symbols.Builtin, List(f, ListUtils.ConsList(xs)))

  private[lingo] override def real_eval(context: Mapping, stack: DebugStack) = {
    def evalIs[A](predicate: CoreWHNF => Boolean, x: Value): Value =
      ValueBoolean(predicate(x.eval(context, stack).reduce_rec_toCore()))

    def cons2[A <: WHNF](cons: (Value, Value) => A, x: Value, y: Value): Value =
      cons(x.eval(context, stack), y.eval(context, stack))

    def elim2[A <: WHNF](elim: CoreWHNF => Option[(Value, Value)], exception: Sym, v: Value, k: Value): Value =
      elim(v.eval(context, stack).reduce_rec_toCore()) match {
        case Some((x, y)) => k.eval(context, stack).app(List(x,y),stack)
        case None => CoreException(stack, exception, context, this)
      }

    (f, xs) match {
      case (Symbols.Builtins.IsPair, List(x)) => evalIs(
        _ match {
          case _: Pair => true
          case _ => false
        }, x)
      case (Symbols.Builtins.ConsPair, List(head, tail)) => cons2(Pair, head, tail)
      case (Symbols.Builtins.ElimPair, List(v, k)) =>
        elim2(_ match {
          case Pair(x, y) => Some(x, y)
          case _ => None
        }, Symbols.CoreExceptions.TypeMismatch_Pair, v, k)

      case (Symbols.Builtins.IsTagged, List(x)) => evalIs(
        _ match {
          case _: Tagged => true
          case _ => false
        }, x)
      case (Symbols.Builtins.ConsTagged, List(tag, xs)) => cons2(Tagged(_, _), tag, xs)
      case (Symbols.Builtins.ElimTagged, List(v, k)) =>
        elim2(_ match {
          case Tagged(x, y) => Some(x, y)
          case _ => None
        }, Symbols.CoreExceptions.TypeMismatch_Tagged, v, k)

      case (Symbols.Builtins.IsException, List(x)) => evalIs(
        _ match {
          case _: ValueException => true
          case _ => false
        }, x)
      case (Symbols.Builtins.ConsException, List(tag, xs)) => cons2(ValueException, tag, xs)
      case (Symbols.Builtins.ElimException, List(v, k)) =>
        elim2(_ match {
          case ValueException(x, y) => Some(x, y)
          case _ => None
        }, Symbols.CoreExceptions.TypeMismatch_Exception, v, k)

      case (Symbols.Builtins.IsSymbol, List(x)) => evalIs(
        _ match {
          case _: Sym => true
          case _ => false
        }, x)
      case (Symbols.Builtins.SymbolToString, List(x)) => x.eval(context, stack) match {
        case AsCoreWHNF(Sym(x)) => ValueString(x.toString())
        case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Symbol, context, this)
      }
      case (Symbols.Builtins.StringToSymbol, List(x)) => x.eval(context, stack) match {
        case AsValueStringCached(x) => Sym(x.x)
        case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_String, context, this)
      }

      case (Symbols.Builtins.Rec, List(WrapperedExp(Id(id)), exp)) => {
        lazy val result: Value = exp.eval_callByName({
          innerContext
        }, stack)
        lazy val innerContext = context.updated(id, result)
        result
      }
      case (Symbols.Builtins.NatToBinary, List(x)) => x.eval(context, stack) match {
        case AsCoreWHNF(ValueNat(x)) => ValueList(NatUtils.nat2booleanList(x).map(ValueBoolean(_)))
        case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Nat, context, this)
      }
      case (Symbols.Builtins.BinaryToNat, List(x)) => x.eval(context, stack) match {
        case AsBooleanListCached(xs) => ValueNat(NatUtils.booleanList2nat(xs))
        case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Binary, context, this)
      }
      case (Symbols.Builtins.IsNull, List(x)) => evalIs(
        _ match {
          case Null => true
          case _ => false
        }, x)
      case (Symbols.Builtins.Eval, List(env, exp)) => env.eval(context, stack) match {
        case AsMappingCached(env) => exp.eval(context, stack).eval(env, stack)
        case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Mapping, context, this)
      }
      case (Symbols.ApplyFunc, List(f, xs)) => xs.eval(context, stack) match {
        case ListUtils.ConsList(xs) => f.eval(context, stack).app(xs, stack)
        case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_List, context, this)
      }
      case (Symbols.Func, List(args, exp)) => args match {
        case ListUtils.ConsList(AsIdList(args)) =>
          InterpretedClosure(args, None, context, exp)
        case ListUtils.ConsListMaybeWithTail(AsIdList(args), WrapperedExp(tail: Id)) =>
          InterpretedClosure(args, Some(tail), context, exp)
        case _ => CoreException(stack, Symbols.CoreExceptions.IllegalExp, context, this)
      }

      case (Symbols.Builtins.ConsList, xs) => ValueList(xs.map(_.eval(context, stack)))
      case (Symbols.Builtins.ElimBoolean, List(v, expTrue, expFalse)) =>
        v.eval(context, stack) match {
          case AsValueBooleanCached(x) => (if (x.toBoolean) {
            expTrue
          } else {
            expFalse
          }).eval(context, stack)
          case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Boolean, context, this)
        }
      case (Symbols.Builtins.AppendMapping, List(xs, ys)) =>
        (xs.eval(context, stack), ys.eval(context, stack)) match {
          case (AsMappingCached(x), AsMappingCached(y)) => x.merged(y)
          case _ => CoreException(stack, Symbols.CoreExceptions.TypeMismatch_Mapping, context, this)
        }

      case _ => CoreException(stack, Symbols.CoreExceptions.IllegalExp, context, this)
    }
  }
}

private object ListHelpers {

  implicit final class ListWithFlatMapOption[A](xs: List[A]) {
    def flatMapOption[B](f: A => Option[B]): Option[List[B]] = xs match {
      case Nil => Some(Nil)
      case x :: xs => for {
        head <- f(x)
        tail <- xs.flatMapOption(f)
      } yield head :: tail
    }
  }

}

private final object AsBooleanListCached {

  import ListHelpers._

  private final object AsValueBooleanCachedForList {
    def unapply(xs: List[Value]): Option[List[ValueBoolean]] = xs.flatMapOption(AsValueBooleanCached.unapply(_))
  }

  def unapply(xs: Value): Option[List[Boolean]] = xs match {
    case ListUtils.ConsList(AsValueBooleanCachedForList(xs)) => Some(xs.map(_.toBoolean))
    case _ => None
  }

}

private final object AsIdList {

  import ListHelpers._

  def unapply(xs: List[Value]): Option[List[Id]] = xs.flatMapOption(
    _ match {
      case WrapperedExp(x: Id) => Some(x)
      case _ => None
    })
}
