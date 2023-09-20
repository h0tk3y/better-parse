# better-parse

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h0tk3y.betterParse/better-parse/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h0tk3y.betterParse/better-parse)
[![Gradle build](https://github.com/h0tk3y/better-parse/workflows/Gradle%20build/badge.svg) ](https://github.com/h0tk3y/better-parse/actions?query=workflow%3A%22Gradle+build%22)

A nice parser combinator library for Kotlin JVM, JS, and Multiplatform projects

```kotlin
val booleanGrammar = object : Grammar<BooleanExpression>() {
    val id by regexToken("\\w+")
    val not by literalToken("!")
    val and by literalToken("&")
    val or by literalToken("|")
    val ws by regexToken("\\s+", ignore = true)
    val lpar by literalToken("(")
    val rpar by literalToken(")")

    val term by 
        (id use { Variable(text) }) or
        (-not * parser(this::term) map { Not(it) }) or
        (-lpar * parser(this::rootParser) * -rpar)

    val andChain by leftAssociative(term, and) { l, _, r -> And(l, r) }
    override val rootParser by leftAssociative(andChain, or) { l, _, r -> Or(l, r) }
}

val ast = booleanGrammar.parseToEnd("a & !b | b & (!a | c)")
 ```
    
### Using with Gradle

```groovy
dependencies {
   implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
}
```

With multiplatform projects, it's OK to add the dependency just to the `commonMain` source set, or some other source set if you want it for specific parts of the code.

## Tokens ##
As many other language recognition tools, `better-parse` abstracts away from raw character input by 
pre-processing it with a `Tokenizer`, that can match `Token`s (with regular expressions, literal values or arbitrary 
against an input character sequence.

There are several kinds of supported `Token`s:

* a `regexToken("(?:my)?(?:regex))` is matched as a regular expression;
* a `literalToken("foo")` is matched literally, character to character;
* a `token { (charSequence, from) -> ... }` is matched using the passed function.

A `Tokenizer` tokenizes an input sequence such as `InputStream` or a `String` into a `Sequence<TokenMatch>`, providing 
each with a position in the input.

One way to create a `Tokenizer` is to first define the `Tokens` to be matched:

```kotlin
val id = regexToken("\\w+")
val cm = literalToken(",")
val ws = regexToken("\\s+", ignore = true)
```

> A `Token` can be ignored by setting its `ignore = true`. An ignored token can still be matched explicitly, but if 
another token is expected, the ignored one is just dropped from the sequence.

```kotlin
val tokenizer = DefaultTokenizer(listOf(id, cm, ws))
```
    
> Note: the tokens order matters in some cases, because the tokenizer tries to match them in exactly this order. 
> For instance, if `literalToken("a")` 
> is listed before `literalToken("aa")`, the latter will never be matched. Be careful with keyword tokens! 
> If you match them with regexes, a word boundary `\b` in the end may help against ambiguity.

```kotlin
val tokenMatches: Sequence<TokenMatch> = tokenizer.tokenize("hello, world")
```
    
> A more convenient way of defining tokens is described in the [**Grammar**](#grammar) section.

It is possible to provide a custom implementation of a `Tokenizer`.

## Parser ##

A `Parser<T>` is an object that accepts an input sequence (`TokenMatchesSequence`) and
tries to convert some (from none to all) of its items into a `T`. In `better-parse`, parsers are also 
the building blocks used to create new parsers by *combining* them.

When a parser tries to process the input, there are two possible outcomes:

* If it succeeds, it returns `Parsed<T>` containing the `T` result and the `nextPosition: Int` that points to what 
it left unprocessed. The latter can then be, and often is, passed to another parser.

* If it fails, it reports the failure returning an `ErrorResult`, which provides detailed information about the failure.

A very basic parser to start with is a `Token` itself: given an input `TokenMatchesSequence` and a position in it, 
it succeeds if the sequence starts with the match of this token itself 
_(possibly, skipping some **ignored** tokens)_ and returns that `TokenMatch`, pointing at the next token 
with the `nextPosition`.

```kotlin
val a = regexToken("a+")
val b = regexToken("b+")
val tokenMatches = DefaultTokenizer(listOf(a, b)).tokenize("aabbaaa")
val result = a.tryParse(tokenMatches, 0) // contains the match for "aa" and the next index is 1 for the match of b
```
    
## Combinators ## 

Simpler parsers can be combined to build a more complex parser, from tokens to terms and to the whole language. 
There are several kinds of combinators included in `better-parse`:

* `map`, `use`, `asJust`
 
    The map combinator takes a successful input of another parser and applies a transforming function to it. 
    The error results are returned unchanged.
    
    ```kotlin
    val id = regexToken("\\w+")
    val aText = a map { it.text } // Parser<String>, returns the matched text from the input sequence
    ```
      
    A parser for objects of a custom type can be created with `map`:
    
    ```kotlin
    val variable = a map { JavaVariable(name = it.text) } // Parser<JavaVariable>.
    ```
      
    * `someParser use { ... }` is a `map` equivalent that takes a function with receiver instead. Example: `id use { text }`.
    
    * `foo asJust bar` can be used to map a parser to some constant value.
    
* `optional(...)`
 
     Given a `Parser<T>`, tries to parse the sequence with it, but returns a `null` result if the parser failed, and thus never fails itself:
     
     ```kotlin
     val p: Parser<T> = ...
     val o = optional(p) // Parser<T?>    
     ```

* `and`, `and skip(...)`

    The tuple combinator arranges the parsers in a sequence, so that the remainder of the first one goes to the second one and so on. 
    If all the parsers succeed, their results are merged into a `Tuple`. If either parser failes, its `ErrorResult` is returned by the combinator.
    
    ```kotlin
    val a: Parser<A> = ...
    val b: Parser<B> = ...
    val aAndB = a and b                 // This is a `Parser<Tuple2<A, B>>`
    val bAndBAndA = b and b and a       // This is a `Parser<Tuple3<B, B, A>>`
    ```
      
     You can `skip(...)` components in a tuple combinator: the parsers will be called just as well, but their results won't be included in the
     resulting tuple:
     
     ```kotlin
     val bbWithoutA = skip(a) and b and skip(a) and b and skip(a)  // Parser<Tuple2<B, B>>
     ```
      
     > If all the components in an `and` chain are skipped except for one `Parser<T>`, the resulting parser
      is `Parser<T>`, not `Parser<Tuple1<T>>`. 
      
     To process the resulting `Tuple`, use the aforementioned `map` and `use`. These parsers are equivalent:
     
     * ```val fCall = id and skip(lpar) and id and skip(rpar) map { (fName, arg) -> FunctionCall(fName, arg) }```
      
     * ```val fCall = id and lpar and id and rpar map { (fName, _, arg, _) -> FunctionCall(fName, arg) }```
      
     * ```val fCall = id and lpar and id and rpar use { FunctionCall(t1, t3) }```
     
     * ```val fCall = id * -lpar * id * -rpar use { FunctionCall(t1, t2) }``` (see operators below)
     
     > There are `Tuple` classes up to `Tuple16` and the corresponding `and` overloads.
     
     ##### Operators
     
     There are operator overloads for more compact `and` chains definition:
     
     * `a * b` is equivalent to `a and b`.
     
     * `-a` is equivalent to `skip(a)`.
     
     With these operators, the parser `a and skip(b) and skip(c) and d` can also be defined as 
     `a * -b * -c * d`.
     
 * `or`
 
     The alternative combinator tries to parse the sequence with the parsers it combines one by one until one succeeds. If all the parsers fail,
     the returned `ErrorResult` is an `AlternativesFailure` instance that contains all the failures from the parsers.
     
     The result type for the combined parsers is the least common supertype (which is possibly `Any`).
     
     ```kotlin
     val expr = const or variable or fCall
     ```
     
  * `zeroOrMore(...)`, `oneOrMore(...)`, `N times`, `N timesOrMore`, `N..M times`
  
      These combinators transform a `Parser<T>` into a `Parser<List<T>>`, invokng the parser several times and failing if there was not
      enough matches.
      
      ```kotlin
      val modifiers = zeroOrMore(functionModifier)
      val rectangleParser = 4 times number map { (a, b, c, d) -> Rect(a, b, c, d) }
      ```
      
  * `separated(term, separator)`, `separatedTerms(term, separator)`, `leftAssociative(...)`, `rightAssociative(...)`
  
      Combines the two parsers, invoking them in turn and thus parsing a sequence of `term` matches separated by `separator` matches.
      
      The result is a `Separated<T, S>` which provides the matches of both parsers (note that terms are one more than separators) and 
      can also be reduced in either direction.
      
      ```kotlin
      val number: Parser<Int> = ...
      val sumParser = separated(number, plus) use { reduce { a, _, b -> a + b } }
      ```
  
      The `leftAssociative` and `rightAssociative` combinators do exactly this, but they take the reducing operation as they are built:
      
      ```kotlin
      val term: Parser<Term>
      val andChain = leftAssociative(term, andOperator) { l, _, r -> And(l, r) }
      ```
        
## Grammar

As a convenient way of defining a grammar of a language, there is an abstract class `Grammar`, that collects the `by`-delegated 
properties into a `Tokenizer` automatically, and also behaves as a composition of the `Tokenizer` and the `rootParser`.

*Note:* a `Grammar` also collects `by`-delegated `Parser<T>` properties so that they can be accessed as 
`declaredParsers` along with the tokens. As a good style, declare the parsers inside a `Grammar` by delegation as well.

```kotlin
interface Item
class Number(val value: Int) : Item
class Variable(val name: String) : Item

class ItemsParser : Grammar<List<Item>>() {
    val num by regexToken("\\d+")
    val word by regexToken("[A-Za-z]+")
    val comma by regexToken(",\\s+")

    val numParser by num use { Number(text.toInt()) }
    val varParser by word use { Variable(text) }

    override val rootParser by separatedTerms(numParser or varParser, comma)
}

val result: List<Item> = ItemsParser().parseToEnd("one, 2, three, 4, five")
```
    
To use a parser that has not been constructed yet, reference it with `parser { someParser }` or `parser(this::someParser)`:

```kotlin
val term by
    constParser or 
    variableParser or 
    (-lpar and parser(this::term) and -rpar)
```

A `Grammar` implementation can override the `tokenizer` property to provide a custom implementation of `Tokenizer`.

## Syntax trees

A `Parser<T>` can be converted to another `Parser<SyntaxTree<T>>`, where a `SyntaxTree<T>`, along with the parsed `T` 
contains the children syntax trees, the reference to the parser and the positions in the input sequence. 
This can be done with `parser.liftToSyntaxTreeParser()`.

This can be used for syntax highlighting and inspecting the resulting tree in case the parsed result
does not contain the full syntactic structure.

For convenience, a `Grammar` can also be lifted to that parsing a `SyntaxTree` with 
`grammar.liftToSyntaxTreeGrammar()`. 

```kotlin
val treeGrammar = booleanGrammar.liftToSyntaxTreeGrammar()
val tree = treeGrammar.parseToEnd("a & !b | c -> d")
assertTrue(tree.parser == booleanGrammar.implChain)
val firstChild = tree.children.first()
assertTrue(firstChild.parser == booleanGrammar.orChain)
assertTrue(firstChild.range == 0..9)
```

There are optional arguments for customizing the transformation:

* `LiftToSyntaxTreeOptions`
  * `retainSkipped` — whether the resulting syntax tree should include skipped `and` components;
  * `retainSeparators` — whether the `Separated` combinator parsed separators should be included;
* `structureParsers` — defines the parsers that are retained in the syntax tree; the nodes with parsers that are
  not in this set are flattened so that their children are attached to their parents in their place. 
  
  For `Parser<T>`, the default is `null`, which means no nodes are flattened.
  
  In case of `Grammar<T>`, `structureParsers` defaults to the grammar's `declaredParsers`.
   
* `transformer` — a strategy to transform non-built-in parsers. If you define your own combinators and want them
  to be lifted to syntax tree parsers, pass a `LiftToSyntaxTreeTransformer` that will be called on the parsers. When
  a custom combinator nests another parser, a transformer implementation should call `default.transform(...)` on that parser.

See [`SyntaxTreeDemo.kt`](https://github.com/h0tk3y/better-parse/blob/master/demo/demo-jvm/src/main/kotlin/com/example/SyntaxTreeDemo.kt) for an example of working with syntax trees.   

## Examples

* A boolean expressions parser that constructs a simple AST: [`BooleanExpression.kt`](https://github.com/h0tk3y/better-parse/blob/master/demo/demo-jvm/src/main/kotlin/com/example/BooleanExpression.kt)
* An integer arithmetic expressions evaluator: [`ArithmeticsEvaluator.kt`](https://github.com/h0tk3y/better-parse/blob/master/demo/demo-jvm/src/main/kotlin/com/example/ArithmeticsEvaluator.kt)
* A toy programming language parser: [(link)](https://github.com/h0tk3y/compilers-course/blob/master/src/main/kotlin/com/github/h0tk3y/compilersCourse/parsing/Parser.kt)
* Another toy language parser: [(link)](https://github.com/lucasnlm/rinha-compiler-kotlin/blob/main/src/nativeMain/kotlin/parser/RinhaGrammar.kt)
* A sample JSON parser by [silmeth](https://github.com/silmeth): [(link)](https://github.com/silmeth/jsonParser)

## Benchmarks

See the benchmarks repository [`h0tk3y/better-parse-benchmark`](https://github.com/h0tk3y/better-parse-benchmark) and feel free to contribute.
