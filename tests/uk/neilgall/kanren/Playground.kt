package uk.neilgall.kanren

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec

class PlaygroundTests : StringSpec({

    "can debug" {
        run { a -> listOf(trace("a is 3")(a _is_ 3)) }.first() shouldEqual listOf(3)
    }

    "can evaluate simple arithmetic" {
        run { a -> listOf(a + 5 _is_ 9) }.first() shouldEqual listOf(4)
    }

    "can do indeterminate boolean logic" {
        run { a -> listOf(a _and_ false _is_ false) } shouldEqual listOf(listOf(false), listOf(true))
    }

    "can generate infinite streams" {
        fun fives(t: Term): Goal = fresh { a -> a _is_ 5 } _or_ fresh(::fives)

        run(10, listOf(fresh(::fives))) shouldEqual listOf(listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5))
    }

    "can unify lists" {
        run { a -> listOf(a _is_ listOf(1, 2, 3)) }.first() shouldEqual listOf(listOf(1, 2, 3))
    }

    "can perform list appends" {
        run { a -> listOf(appendo(a, term(4, 5), term(1, 2, 3, 4, 5))) }.first() shouldEqual listOf(listOf(1, 2, 3))
    }

    "can perform indeterminate list appends" {
        run { a, b -> listOf(appendo(a, b, term(1, 2, 3, 4, 5))) } shouldEqual listOf(
                listOf(null, listOf(1, 2, 3, 4, 5)),
                listOf(listOf(1), listOf(2, 3, 4, 5)),
                listOf(listOf(1, 2), listOf(3, 4, 5)),
                listOf(listOf(1, 2, 3), listOf(4, 5)),
                listOf(listOf(1, 2, 3, 4), listOf(5)),
                listOf(listOf(1, 2, 3, 4, 5), null)
        )
    }

    "can find list members" {
        run { a -> listOf(membero(a, term(1, 2, 3 ,4 ,5))) } shouldEqual listOf(listOf(1), listOf(2), listOf(3), listOf(4), listOf(5))
    }

    "can remove list members" {
        run { a, b -> listOf(removeo(a, b, term(1, 2, 3, 4, 5))) } shouldEqual listOf(
                listOf(1, listOf(2, 3, 4, 5)),
                listOf(2, listOf(1, 3, 4, 5)),
                listOf(3, listOf(1, 2, 4, 5)),
                listOf(4, listOf(1, 2, 3, 5)),
                listOf(5, listOf(1, 2, 3, 4))
        )
    }

    "can deal with relations" {
        val parent = relation(
                arrayOf("Homer", "Bart"),
                arrayOf("Homer", "Lisa"),
                arrayOf("Homer", "Maggie"),
                arrayOf("Marge", "Bart"),
                arrayOf("Marge", "Lisa"),
                arrayOf("Marge", "Maggie"),
                arrayOf("Abe", "Homer")
        )

        fun grandparent(a: Term, b: Term): Goal = fresh { c -> parent(arrayOf(a, c)) _and_ parent(arrayOf(c, b)) }

        run { a -> listOf(parent(arrayOf(a, term("Bart")))) } shouldEqual listOf(listOf("Homer"), listOf("Marge"))
        run { a -> listOf(parent(arrayOf(term("Homer"), a))) } shouldEqual listOf(listOf("Bart"), listOf("Lisa"), listOf("Maggie"))
        run { a -> listOf(grandparent(a, term("Bart"))) } shouldEqual listOf(listOf("Abe"))
    }
})