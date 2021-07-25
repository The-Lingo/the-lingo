/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo.utils

final class MutableBox[T](private var x: T) {
  def update(x1: T): Unit = {
    x = x1
  }

  def get: T = x
}
