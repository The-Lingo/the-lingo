/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo

private final object ListUtils {

  final object ConsList {
    def apply(xs: List[Value], tail: Value): Value = xs match {
      case x :: xs => apply(xs, Pair(x, tail))
      case Nil => tail
    }

    def apply(xs: List[Value]): Value = ValueList(xs)

    def unapply(arg: Value): Option[List[Value]] = AsListValueCached.unapply(arg).map({
      _.xs
    })
  }

  def list(xs: Value*): Value = ConsList(xs.toList)
}