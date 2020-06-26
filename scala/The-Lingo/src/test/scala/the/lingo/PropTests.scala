/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo

import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import Value.Implicits._

final object PropTests extends Properties("the lingo") {
  private val spaces = Gen.nonEmptyListOf(Gen.oneOf(" ", "\t")).map(_.mkString(""))
  property("parse 2 elements list") = forAll(spaces, spaces, spaces, Gen.identifier, Gen.identifier) { (s1, s2, s3, idx, idy) =>
    LangParser("file").parseValue(s"$s1($idx$s2$idy)$s3")
      .equal_reduce_rec(ValueList(List(Sym(idx), Sym(idy))))
  }
  property("parse identifier list") = forAll(spaces, spaces, Gen.listOf(Gen.identifier)) { (s, s1, xs) =>
    LangParser("file").parseValue(s"($s1${xs.mkString(s)})")
      .equal_reduce_rec(ValueList(xs.map(Sym(_))))
  }
}