def f(a: Int) = {}
{
  def f(a: String) = {}
  println(/* offset: 27, applicable: false */ f(1))
  println(/* offset: 27 */ f("foo"))
}
