package uk.neilgall.kanren

import io.kotlintest.matchers.contain
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldHave
import io.kotlintest.specs.StringSpec

class ComplexExample : StringSpec({
    "DOG + CAT = BAD" {
        run { digits, d, o, g, c, a, t, b -> listOf(
                    digits _is_ term(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                    membero(d, digits),
                    membero(o, digits), o _is_not_ d,
                    membero(g, digits), g _is_not_ d, g _is_not_ o,
                    membero(c, digits), c _is_not_ d, c _is_not_ o, c _is_not_ g,
                    membero(a, digits), a _is_not_ d, a _is_not_ o, a _is_not_ g, a _is_not_ c,
                    membero(t, digits), t _is_not_ d, t _is_not_ o, t _is_not_ g, t _is_not_ c, t _is_not_ a,
                    membero(b, digits), b _is_not_ d, b _is_not_ o, b _is_not_ g, b _is_not_ c, b _is_not_ a, b _is_not_ t,
                    d + c _is_ b,
                    o + a _is_ a,
                    g + c _is_ d
            )
        }.first() shouldEqual listOf(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 0, 1, 2, 4, 6, 5)
    }


})