class T {
  def fadfa = 1
}

implicit def str2T(x: String): T = new T

""./*caret*/fadfa
/*
class T {
  def NameAfterRename = 1
}

implicit def str2T(x: String): T = new T

""./*caret*/NameAfterRename
*/