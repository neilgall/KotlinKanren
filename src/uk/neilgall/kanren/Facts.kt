package uk.neilgall.kanren

typealias Fact = List<Term>

private fun unify(fact: Fact, terms: Array<Term>): Goal? =
    if (fact.size != terms.size) null
    else conj_(fact.zip(terms).map { it.first _is_ it.second })

fun relation(vararg facts: Array<Any>): (Array<Term>) -> Goal {
    val factTerms = facts.map { it.map(::term) }
    return { terms -> disj_(factTerms.map({ unify(it, terms) }).filterNotNull()) }
}
