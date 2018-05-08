package uk.neilgall.kanren

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec

class PlaygroundTests : StringSpec({

    "can debug" {
        run { a -> listOf(trace("a is 3")(a _is_ 3))}.first() shouldEqual listOf(3)
    }

    "can evaluate simple arithmetic" {
        run { a -> listOf(a + 5 _is_ 9) }.first() shouldEqual listOf(4)
    }

    "can do indeterminate boolean logic" {
        run { a -> listOf(a _and_ false _is_ false) } shouldEqual listOf(listOf(false), listOf(true))
    }

    "can generate infinite streams" {
        fun fives(t: Term): Goal = fresh { a -> a _is_ 5 } _or_ fresh(::fives)

        run(10, listOf(fresh(::fives))) shouldEqual listOf(listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5),listOf(5), listOf(5),listOf(5), listOf(5))
    }

    "can unify lists" {
        run { a -> listOf(a _is_ listOf(1,2,3)) }.first() shouldEqual listOf(listOf(1, 2, 3))
    }

    "can perform list appends" {
        run { a -> listOf(appendo(a, listOf(4, 5).toTerm(), listOf(1, 2, 3, 4, 5).toTerm())) }.first() shouldEqual listOf(1, 2, 3)
    }
})