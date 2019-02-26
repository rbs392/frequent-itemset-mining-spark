import org.scalatest.FunSuite
import sequential.Util.printItemsets
import sequential.{Apriori, NaiveFIM, Util}

class AprioriTest extends FunSuite {

  private val fimInstances = Set(new Apriori(), new NaiveFIM())

  fimInstances.foreach(fim => {
    val className = fim.getClass.getSimpleName

    test(s"$className - Should return proper frequent itemsets") {
      val itemsets =
        """|a,b,c
           |a,b,c
           |a,b,c
        """.stripMargin
      val frequentSets = fim.findFrequentItemsets(itemsets, 3)

      val expectedItemsets =
        """|a,b,c
           |a,b
           |b,c
           |a,c
           |a
           |b
           |c
        """.stripMargin
      val expectedSets = Util.parseTransactionsByText(expectedItemsets)
      assert(frequentSets.size === expectedSets.size)
      assert(expectedSets.intersect(frequentSets).size === expectedSets.size)
    }

    test(s"$className - Many k-itemsets") {
      val itemsets =
        """|1,3
           |1,2,3
           |1
           |1,3,4
           |3
           |1,2
           |1,2,3,4
        """.stripMargin
      val frequentSets = fim.findFrequentItemsets(itemsets, 3) // sup = (itemsets.size() * 0.4 + 0.5).toInt
      val expectedSets = List(
        List("1"), List("2"), List("3"),
        List("1", "3"),
        List("1", "2")
      )

      assert(frequentSets.size === expectedSets.size)
      assert(expectedSets.intersect(frequentSets).size === expectedSets.size)
    }

  })

  test("itemsets comparison") {
    val items1 = List(
      List("1", "3"),
      List("1", "2")
    )
    val items2 = List(
      List(new String("1"), "2"),
      List("1", "3")
    )
    assert(items1.size === items2.size)
    assert(items1.intersect(items2).size === items2.size)
  }

}
