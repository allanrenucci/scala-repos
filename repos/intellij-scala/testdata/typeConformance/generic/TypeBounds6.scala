trait XX
class T {
  type A = XX
  type B <: A
}
object T extends T
import T._
val b: B = error("")
val a: A = b
//True