package uk.neilgall.kanren

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.forAll

class TermSpec: StringSpec({

    "Integers can be terms" {
        forAll<Int> { n -> term(n) == Term.Int(n) }
    }

    "Strings can be terms" {
        forAll<String> { s -> term(s) == Term.String(s) }
    }

    "Booleans can be terms" {
        forAll<Boolean> { b -> term(b) == Term.Boolean(b) }
    }

    "Pairs can be terms" {
        forAll<Int, String> { a, b -> term(Pair(a, b)) == Term.Pair(term(a), term(b)) }
    }

    "Lists can be terms" {
        forAll<List<Int>> { xs -> term(xs) == xs.map { term(it) }.foldRight(Term.None, Term::Pair) }
    }
})