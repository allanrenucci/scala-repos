case class CC

type A = CC
type A = ��

println(/* resolved: false */ A.getClass)
println(classOf[/* resolved: false */ A])

