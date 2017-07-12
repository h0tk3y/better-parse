# better-parse

[ ![Download](https://api.bintray.com/packages/hotkeytlt/maven/better-parse/images/download.svg) ](https://bintray.com/hotkeytlt/maven/better-parse/_latestVersion) [![Build Status](https://travis-ci.org/h0tk3y/better-parse.svg?branch=master)](https://travis-ci.org/h0tk3y/better-parse) 

A nice parser combinator library for Kotlin

```kotlin
    val booleanGrammar = object : Grammar<BooleanExpression>() {
        val id by token("\\w+")
        val not by token("!")
        val and by token("&")
        val or by token("|")
        val ws by token("\\s+", ignore = true)
        val lpar by token("\\(")
        val rpar by token("\\)")
        
        val term = 
            (id use { Variable(text) }) or
            (-not * parser(this::term) map { (Not(it) }) or
            (-lpar * parser(this::rootParser) * -rpar)
            
        val andChain = leftAssociative(term, and) { l, _, r -> And(l, r) }
        val rootParser = leftAssociative(andChain, or) { l, _, r -> Or(l, r) }
    }
    
    val ast = booleanGrammar.parseToEnd("a & !b | b & (!a | c)")
 ```
    
### Using with Gradle

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.github.h0tk3y.betterParse:better-parse:0.1'
}
```

## Lexer & tokens ##
As many other language recognition tools, `better-parse` abstracts away from raw character input by 
pre-processing it with a `Lexer`, that can match `Token`s by their patterns (regular expressions) against an input sequence.

A `Lexer` tokenizes an input sequence such as `InputStream` or a `String` into a `Sequence<TokenMatch>`, providing each with a position in the input.

One way to create a `Lexer` is to first define the `Tokens` to be matched:

```kotlin
val id = Token("identifier", pattern = "\\w+")
val cm = Token("comma", pattern = ",")
val ws = Token("whitespace", pattern = "\\s+", ignore = true)
```

> A `Token` can be ignored by setting its `ignore = true`. An ignored token can still be matched explicitly, but if 
another token is expected, the ignored one is just dropped from the sequence.

```kotlin
val lexer = Lexer(listOf(id, cm, ws))
```
    
> Note: the tokens order matters in some cases, because the lexer tries to match them in exactly this order. For instance, if `Token("singleA", "a")` 
is listed before `Token("doubleA", "aa")`, the latter will never be matched. Be careful with keyword tokens!

```kotlin
val tokenMatches: Sequence<TokenMatch> = lexer.tokenize("hello, world") // Support other types of input as well.
```
    
> A more convenient way of defining tokens and creating a lexer is described in the **Grammar** section.

## Parser ##

A `Parser<T>` is an object that accepts an input sequence (a sequence of tokens, `Sequence<TokenMatch>`) and
tries to convert some (from none to all) of its items into a `T`. In `better-parse`, parsers are also used 
as build blocks to create new parsers by *combining* them.

When a parser tries to process the input, there are two possible outcomes:

* If it succeeds, it returns `Parsed<T>` containing the `T` result and the `remainder: Sequence<TokenMatch>` that it left unprocessed. 
The latter can then be, and often is, passed to another parser.

* If it fails, it reports the failure returning an `ErrorResult`, which provides detailed information about the failure.

A very basic parser to start with is a `Token` itself: when given an input `Sequence<TokenMatch>`, it succeeds if the sequence starts 
with the match of this token itself _(possibly, skipping some **ignored** tokens)_ and returns that `TokenMatch`, also excluding it 
_(and, possibly, some ignored tokens)_ from the remainder.

```kotlin
val a = Token(name = "a", pattern = "a+")
val b = Token(name = "b", pattern = "b+")
val tokenMatches = Lexer(listOf(a, b)).tokenize("aabbaaa")
val result = a.tryParse(tokenMatches) // contains the match for "aa" and the remainder with "bbaaa" in it
```
    
## Combinators ## 

Simpler parsers can be combined to build a more complex parser, from tokens to terms and to the whole language. 
There are several kinds of combinators included in `better-parse`:

* `map`, `use`, `justAs`
 
    The map combinator takes a successful input of another parser and applies a transforming function to it. 
    The error results are returned unchanged.
    
    ```kotlin
    val id = Token("identifier", pattern = "\\w+")
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
     val expr = const or var or fCall
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
        
# Grammar

As a convenient way of defining a grammar of a language, there is an abstract class `Grammar`, that collects the `by token(...)`-delegated 
properties into a `Lexer` automatically, and also behaves as a composition of the `Lexer` and the `rootParser`.

```kotlin
interface Item
class Number(val value: Int) : Item
class Variable(val name: String) : Item

object ItemsParser : Grammar<Item>() {
    val num by token("\\d+")
    val word by token("[A-Za-z]")
    val comma by token(",\\s+")

    val numParser = num use { Number(text.toInt()) }
    val varParser = word use { Variable(text) }

    override val rootParser = separatedTerms(numParser or varParser, comma)
}

val result: List<Item> = ItemsParser.parseToEnd("one, 2, three, 4, five")
```
    
To use a parser that has not been constructed yet, reference it with `parser { someParser }` or `parser(this::someParser)`:

```kotlin
val term = 
    constParser or 
    variableParser or 
    (-lpar and parser(this::term) and -rpar)
```
        
# Examples

* A boolean expressions parser that constructs a simple AST: [`BooleanExpression.kt`](https://github.com/h0tk3y/better-parse/blob/master/demo/src/main/kotlin/com/example/BooleanExpression.kt)
* An integer arithmetic expressions evaluator: [`ArithmeticsEvaluator.kt`](https://github.com/h0tk3y/better-parse/blob/master/demo/src/main/kotlin/com/example/ArithmeticsEvaluator.kt)
      
