package example

object Main {
  def main(args: Array[String]): Unit = {
    val lib = new MyLibrary
    println(lib.sq(2))

    println(s"Using Scala Native version ${System.getProperty("java.vm.version")}")
  }
}
