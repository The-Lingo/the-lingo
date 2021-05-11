/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
package the.lingo

final object Symbols {
  val Core: Sym = Sym("核")
  val Id: Sym = Sym("識別子")
  val Exp: Sym = Sym("式")
  val ApplyFunc: Sym = Sym("用-函式")
  val Quote: Sym = Sym("引用")
  val ApplyMacro: Sym = Sym("用-構式子")
  val Comment: Sym = Sym("注釋")
  val Exception: Sym = Sym("異常")
  val Builtin: Sym = Sym("內建")
  val Nat: Sym = Sym("自然數")
  val Positioned: Sym = Sym("具座標")
  val Func: Sym = Sym("函式")

  final object Builtins {
    //val NatZero: Sym = Sym("自然數/零")
    //val NatSucc: Sym = Sym("自然數/增一")
    val IsPair: Sym = Sym("序列/連結？")
    val ConsList: Sym = Sym("構造-序列*")
    val ConsPair: Sym = Sym("構造-序列/連結")
    val ElimPair: Sym = Sym("解構-序列/連結")
    val IsTagged: Sym = Sym("具標籤？")
    val ConsTagged: Sym = Sym("構造-具標籤")
    val ElimTagged: Sym = Sym("解構-具標籤")
    val IsException: Sym = Sym("異常？")
    val ConsException: Sym = Sym("構造-異常")
    val ElimException: Sym = Sym("解構-異常")
    val IsNull: Sym = Sym("序列/空？")
    val IsSymbol: Sym = Sym("符號？")
    val SymbolToString: Sym = Sym("符號→字串")
    val StringToSymbol: Sym = Sym("字串→符號")
    val Rec: Sym = Sym("遞歸")
    val NatToBinary: Sym = Sym("自然數→二進位")
    val BinaryToNat: Sym = Sym("二進位→自然數")
    val Eval: Sym = Sym("解算")
    val ElimBoolean: Sym = Sym("解構-陰陽")
    val AppendMapping: Sym = Sym("連-映射")
  }

  final object Tags {
    val UNIXFilePosition: Sym = Sym("座標/UNIX文件")
    val NamedPosition: Sym = Sym("座標/識別子")
    val False: Sym = Sym("陰")
    val True: Sym = Sym("陽")
    val Char: Sym = Sym("字符")
    val String: Sym = Sym("字串")
    val Mapping: Sym = Sym("映射")
    val Macro: Sym = Sym("構式子")
  }

  final object CoreExceptions {
    val IllegalExp: Sym = Sym("非法式")
    val NoDefinition: Sym = Sym("無定義")
    val ArgsMismatch: Sym = Sym("參數不匹配")
    val TypeMismatch_Func: Sym = Sym("標籤不匹配/函式")
    val TypeMismatch_Exp: Sym = Sym("標籤不匹配/式")
    val TypeMismatch_Pair: Sym = Sym("標籤不匹配/序對")
    val TypeMismatch_Tagged: Sym = Sym("標籤不匹配/具標籤")
    val TypeMismatch_Exception: Sym = Sym("標籤不匹配/異常")
    val TypeMismatch_Nat: Sym = Sym("標籤不匹配/自然數")
    val TypeMismatch_Binary: Sym = Sym("標籤不匹配/二進位")
    val TypeMismatch_Symbol: Sym = Sym("標籤不匹配/符號")
    val TypeMismatch_String: Sym = Sym("標籤不匹配/字串")
    val TypeMismatch_Mapping: Sym = Sym("標籤不匹配/映射")
    val TypeMismatch_List: Sym = Sym("標籤不匹配/序列")
    val TypeMismatch_Macro: Sym = Sym("標籤不匹配/構式子")
    val TypeMismatch_Boolean: Sym = Sym("標籤不匹配/陰陽")
  }

}