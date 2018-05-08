package uk.neilgall.kanren

sealed class Term {
    object None : Term()
    data class String(val s: kotlin.String) : Term()
    data class Int(val i: kotlin.Int) : Term()
    data class Boolean(val b: kotlin.Boolean) : Term()
    data class Variable(val v: kotlin.Int) : Term()
    data class Pair(val p: Term, val q: Term) : Term()
    data class BinaryExpr(val lhs: Term, val op: BinaryOperation, val rhs: Term) : Term()

    override fun toString(): kotlin.String = when (this) {
        is Term.None -> "nil"
        is Term.String -> "\"$s\""
        is Term.Int -> i.toString()
        is Term.Boolean -> b.toString()
        is Term.Variable -> ".$v"
        is Term.Pair -> "($p, $q)"
        is Term.BinaryExpr -> "($lhs $op $rhs)"
    }
}

enum class BinaryOperation(val str: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MOD("%"),
    AND("&&"),
    OR("||");

    override fun toString(): String = str
}

fun Any.toTerm(): Term = when(this) {
    is Int -> Term.Int(this)
    is String -> Term.String(this)
    is Boolean -> Term.Boolean(this)
    is Pair<*, *> -> Term.Pair(first!!.toTerm(), second!!.toTerm())
    is List<*> -> this.map { it!!.toTerm() }.foldRight(Term.None, Term::Pair)
    else -> throw IllegalArgumentException()
}

// General operations
operator fun Term.plus(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.PLUS, rhs)

operator fun Term.minus(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.MINUS, rhs)
operator fun Term.times(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.MULTIPLY, rhs)
operator fun Term.div(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.DIVIDE, rhs)
operator fun Term.rem(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.MOD, rhs)

// String operations
operator fun Term.plus(rhs: String): Term = this + rhs.toTerm()

operator fun String.plus(rhs: Term): Term = toTerm() + rhs

// Integer arithmetic
operator fun Term.plus(rhs: Int): Term = this + rhs.toTerm()

operator fun Term.minus(rhs: Int): Term = this - rhs.toTerm()
operator fun Term.times(rhs: Int): Term = this * rhs.toTerm()
operator fun Term.div(rhs: Int): Term = this / rhs.toTerm()
operator fun Term.rem(rhs: Int): Term = this % rhs.toTerm()
operator fun Int.plus(rhs: Term): Term = toTerm() + rhs
operator fun Int.minus(rhs: Term): Term = toTerm() - rhs
operator fun Int.times(rhs: Term): Term = toTerm() * rhs
operator fun Int.div(rhs: Term): Term = toTerm() / rhs
operator fun Int.rem(rhs: Term): Term = toTerm() % rhs

// Boolean operations
infix fun Term._and_(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.AND, rhs)

infix fun Term._or_(rhs: Term): Term = Term.BinaryExpr(this, BinaryOperation.OR, rhs)
infix fun Term._and_(rhs: Boolean): Term = this _and_ rhs.toTerm()
infix fun Term._or_(rhs: Boolean): Term = this _or_ rhs.toTerm()
infix fun Boolean._and_(rhs: Term): Term = toTerm() _and_ rhs
infix fun Boolean._or_(rhs: Term): Term = toTerm() _or_ rhs

fun Term.toMatch(): Any? = when(this) {
    is Term.String -> s
    is Term.Int -> i
    is Term.Boolean -> b
    is Term.Pair -> when (q) {
        is Term.None -> listOf(p.toMatch())
        is Term.Pair -> listOf(p.toMatch()) + (q.toMatch() as List<Any?>)
        else -> Pair(p.toMatch(), q.toMatch())
    }
    else -> null
}
