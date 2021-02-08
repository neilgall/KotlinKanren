# Kotlin Kanren

Kotlin implementation of µKanren. I have also done a Swift port [here](https://github.com/neilgall/SwiftyKanren).

See the original paper at http://webyrd.net/scheme-2013/papers/HemannMuKanren2013.pdf

## What's going on?
[miniKanren](http://www.minikanren.org)'s website describes it as a Domain Specific Language for logic programming. Logic programming is somewhat different from most other kinds of programming. Rather than tell the computer exactly what to do, we describe the problem to be solved. Unknowns are left as placeholders and the logic system finds solutions to them.

For (a classic) example, in traditional programming you might append two lists: `append(a,b)` gives you `a+b`. You have to know `a` and `b` first, so the computer's not being very smart. In logic programming `append` takes three arguments `a`, `b` and `c` and finds a solution where `c == a+b`.

    append(a, [4,5], [1,2,3,4,5])
    
Gives an answer for `a` which is `[1,2,3]`, since that's what you need to append `[4,5]` to to get `[1,2,3,4,5]`.

    append([1,2], b, [1,2,3,4,5])
    
Gives an answer for `b` which is `[3,4,5]`, since that's what you need to append to `[1,2]` to get `[1,2,3,4,5]`. And

    append([1,2], [3,4,5], c)
    
Gives an answer for `c` which is `[1,2,3,4,5]`, much like traditional programming.

But what if you do this?

    append(a, b, [1,2,3,4,5])

You get a list of answers:

    [], [1,2,3,4,5]
    [1], [2,3,4,5]
    [1,2], [3,4,5]
    [1,2,3], [4,5]
    [1,2,3,4], [5]
    [1,2,3,4,5], []

All of those are acceptable solutions, and logic programming finds them all. Finally, you might try:

    append([8,9], [3,4,5], [1,2,3,4,5])

A logic programming system will give you no answers, as the _relation_ defined by `append` does not hold for these inputs.

## Why Kotlin

The original miniKanren is embedded in Scheme and there are many other implementations, but none I could find in Swift or Kotlin. Standalone systems are all well and good, but embedding a DSL in your main programming language makes it easy to use within the scope of a larger application. Kotlin's language features (first class function types, tail lambda syntax) make it a decent choice for DSLs.

## The DSL

The language embedded in Kotlin aims to be an implementation of miniKanren, as far as Kotlin syntax and semantics allow. There are two main concepts: _Terms_ and _Goals_. A `Term` is a value, which can be a string, integer, boolean, nil, or a pair of any of these. If you've read SICP you know that pairs allow you to implement lists. `term()` constructs terms from `String`, `Int`, `Boolean` or `Pair` or `List` of any of those types.

    term(listOf(1,2,,3))

and the compiler turns it into the `Term`

    Term.Pair(Term.Int(1), Term.Pair(Term.Int(2), Term.Pair(Term.Int(3), Term.None)))

A `Term` can also be a variable. Logic variables are introduced with the primitive `fresh`, of which there are currently variations for introducing one to five variables at a time. You pass `fresh` a block which receives these one to five new `Term`s and from the block you return a `Goal`.

Under the hood a `Goal` is a function of type `State -> Sequence<State>` but in general you can ignore that. The DSL provides a few primitives and combinators which all yield `Goal`s, so you can keep working (and thinking) at the logic DSL level.

The fundamental primitive for building `Goal`s is the infix function `_is_`, which attempts to unify the terms on each side. If one term is a constant and the other a variable, the variable takes on that constant value for subsequent computation down the same path. More on that in a bit. First an example:

    fresh { a -> a _is_ 2 }

This yields a `Goal` which unifies the variable `a` with the constant 2. Execute a `Goal` with `run`:

    run { a -> a _is_ 2 }

The return value of `run` is a `KanrenResult` which is just a typealias for `List<Match>`. `Match` is like a list of `Term`s but cannot hold variables. Each `Match` in a result therefore consists of a value for every variable introduced by `fresh` or `run`. In the above case there will be one `Match` with a single integer value 2. But that's not very interesting. We can assign 2 to `a` in any language.

    run { a,b,c -> (a _is_ b) _and_ (b _is_ c) _and_ (c _is_ 5) }

This yields a result containing a single `Match`, containing three integers all equal to 5. `c` is 5 like the previous example, but `b` is also 5 because it is unified with `c`, and `a` is 5 similarly.

    run { a,b,c -> ((a _is_ b) _or_ (a _is_ c)) _and_ (b _is_ 8) _and_ (c _is_ 3) }

This yields two `Match`es with values `listOf(8,8,3)` and `listOf(3,8,3)`. `b` and `c` are fixed but there are two unifications for `a` which work.

## Lists

Two list operations are currently implemented: `appendo` and `membero`. It is a miniKanren tradition to attach an o to relational versions of functions.

`appendo` works like the example above:

    run { a,b -> appendo(a, b, term(1,2,3,4,5)) }

This yields all the combinations of two lists which append to give 1..5.

`membero` unifies a `Term` with any member of a list:

    run { a -> membero(a, listOf(1,2,3)) }

The above finds three results containing 1, 2 and 3 respectively.

More list operations to come.

## Facts

Logic programming is often used to find the solutions to one or more unknowns within the context of a set of fixed relations. The classic example is geneaology. KotlinKanren lets you introduce fixed relations with `relation2`, `relation3`, etc.:

    let parent = relation2(
        "Homer", "Bart",
        "Homer", "Lisa",
        "Homer", "Maggie",
        "Marge", "Bart",
        "Marge", "Lisa",
        "Marge", "Maggie",
        "Abe", "Homer
    )

Each `Fact` is just a list of non-variable `Term`s. For `relation2` each pair of `Term`s forms a `Fact`; for `relation3` each triple, etc. The return value of `relation` is a function which, like `_is_`, `appendo`, etc. can be used to generate `Goal`s.

    run { a -> parent(a, "Bart") }

You get two `Match`es each with one value: "Marge" and "Homer". Swapping the variable to the other position performs the fact lookup in the other direction:

    run { a -> parent("Homer", a) }

Here there are three results containing "Bart", "Lisa" and "Maggie".

## Goal generators

You can write your own `Goal` generation functions by combining existing ones. All you have to remember is that these functions must accept one or more `Term`s and always return a `Goal`. You can introduce new variables if that helps the definition of your goal:

    fun grandparent(a: Term, b: Term): Goal = 
    	fresh { c -> parent(a, c) _and_ parent(c, b) }

The `grandparent` relation holds if for a given `a` and `b`, there is a `c` which is a child of `a` and the parent of `b`.

That's it for now. Please download it and tinker with it. I'd love to hear what you think!

## Complex example

Let's solve a classic symbolic problem: Find the digits to substitute for letters such that DOG + CAT = BAD.

Starting with all the digits from 0..9, remove one and assign to each letter. Then add the per digit constraints: `D + C` should equal `B`, etc.

    run { d, o, g, c, a, t, b ->
        fresh { d1, d2, d3, d4, d5, d6, d7, d8 ->
            conj(
                d1 _is_ term(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                removeo(d, d2, d1),
                removeo(o, d3, d2),
                removeo(g, d4, d3),
                removeo(c, d5, d4),
                removeo(a, d6, d5),
                removeo(t, d7, d6),
                removeo(b, d8, d7),
                d + c _is_ b,
                o + a _is_ a,
                g + t _is_ d
            )
        }
    }

## What's Missing

The original SwiftyKanren supported infinite generators by allowing a goal generator function to refer to itself. This is possible for simple cases in Kotlin too:

    fun fives(t: Term): Goal = (t _is_ 5) _or_ fresh(::fives)

    runGoal(10, fresh(::fives))

This yields an infinite list of the value 5 and takes the first 10 results from it. However:

    fun sixes(t: Term): Goal = (t _is_ 6) _or_ fresh(::fives)

    runGoal(10, fresh(::fives) _or_ fresh(::sixes))

In the original SwiftyKanren this yields an infinite list of alternating 5 and 6 values, but the lazy interleaving of sequences has not been implemented in Kotlin and this just yields fives.
