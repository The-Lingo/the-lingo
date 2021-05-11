/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo

import the.lingo.Showable.Implicits._

sealed trait MonadIO extends WHNF {
  def run(stack: DebugStack = DebugStack.Empty): Value
}

final case class MonadIOReturn(x: Value) extends MonadIO {
  override def impl_toCore() = TODO()

  override def impl_show(implicit showContext: ShowContext): String = s"MonadIOReturn"

  override def run(stack: DebugStack = DebugStack.Empty) = x
}

final case class MonadIOBind(x: MonadIO, f: Value, stack: DebugStack = DebugStack.Empty) extends MonadIO {
  override def impl_toCore() = TODO()

  override def impl_show(implicit showContext: ShowContext): String = s"MonadIOBind(${x.show},${f.show})"

  override def run(outerStack: DebugStack = DebugStack.Empty) = {
    val innerStack = outerStack.concat(stack)
    AsMonadIOCached.unapply(f.app(List(x.run()), innerStack)) match {
      case Some(result) => result.run(innerStack)
      case _ => TODO()
    }
  }
}

final case class MonadIOOp(f: Sym, args: List[Value]) extends MonadIO {
  override def impl_toCore() = TODO()

  override def impl_show(implicit showContext: ShowContext): String = s"MonadIOOp(${f.show},${args.show})"

  override def run(stack: DebugStack = DebugStack.Empty) = TODO()

}

private final object AsMonadIOCached {
  private val unapply_v = Value.cached_option_as((arg: WHNF) => arg match {
    case x: MonadIO => Some(x)
    case _ => arg.toCore() match {
      case _ => TODO()
    }
  })

  def unapply(x: Value): Option[MonadIO] = unapply_v.apply(x)
}

final class IOVar(private var value: Value) extends OpaqueWHNF {
  override def impl_toCore() = TODO()

  override def impl_show(implicit showContext: ShowContext): String = s"IOVar(${get.show})"

  def get: Value = this.synchronized {
    value
  }

  def put(x: Value) = this.synchronized {
    value = x
  }

  def transform(f: Value => Value): Unit = this.synchronized {
    value = f(value)
  }

  def swap(current: Value, toset: Value): Boolean = this.synchronized {
    if (current eq value) {
      value = toset
      true
    } else {
      false
    }
  }

  def transformSTM(f: Value => Value): Unit = {
    val current = get
    val result = f(current)
    if (!swap(current, result)) {
      transformSTM(f)
    }
  }
}

final object IOSymbols {
  val Return: Sym = Sym("返回")
  val Bind: Sym = Sym(">>=")
  val Op: Sym = Sym("算子")
}