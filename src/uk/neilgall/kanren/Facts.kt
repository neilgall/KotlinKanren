package uk.neilgall.kanren

typealias Fact = List<Term>

private fun unify(fact: Fact, terms: List<Term>): Goal? =
    if (fact.size != terms.size) null
    else conj_(fact.zip(terms).map { it.first _is_ it.second })

fun relation(facts: List<Fact>): (List<Term>) -> Goal =
        { terms -> disj_(facts.map({ unify(it, terms) }).filterNotNull()) }
