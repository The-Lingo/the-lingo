/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo

import the.lingo.Value.Implicits._

private final object ListUtils {

  final object ConsList {
    def apply(xs: List[Value]): Value = ValueList(xs)

    def unapply(arg: Value): Option[List[Value]] = AsListValueCached.unapply(arg).map(_.xs)
  }

  final object ConsListMaybeWithTail {
    def apply(xs: List[Value], tail: Value): Value = xs match {
      case x :: xs => apply(xs, Pair(x, tail))
      case Nil => tail
    }

    def unapply(xs: Value): Option[(List[Value], Value)] = xs.reduce_rec() match {
      case ValueList(xs) => Some(xs, Null)
      case xs => xs.toCore() match {
        case Pair(head, tail) => unapply(tail).map({
          _ match {
            case (result, resultTail) => (head :: result, resultTail)
          }
        })
        case _ => Some(Nil, xs)
      }
    }
  }

  final object List {
    def apply(xs: Value*): Value = ConsList(xs.toList)

    def unapplySeq(x: Value): Option[Seq[Value]] = x match {
      case ConsList(scala.List(xs@_*)) => Some(xs)
      case _ => None
    }
  }

}