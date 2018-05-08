package uk.neilgall.kanren

import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec

class TermSpec: StringSpec({

    "Integers can be terms" {
        forAll { n: Int -> n.toTerm() == Term.Int(n) }
    }

    "Strings can be terms" {
        forAll { s: String -> s.toTerm() == Term.String(s) }
    }

    "Booleans can be terms" {
        forAll { b: Boolean -> b.toTerm() == Term.Boolean(b) }
    }

    "Pairs can be terms" {
        forAll { a: Int, b: String -> Pair(a, b).toTerm() == Term.Pair(a.toTerm(), b.toTerm()) }
    }

    "Lists can be terms" {
        forAll { xs: List<Int> -> xs.toTerm() == xs.map { it.toTerm() }.foldRight(Term.None, Term::Pair) }
    }
})