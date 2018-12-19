# pure-functional-webapp
An example of a web app, written in Scala, using pure functional programming concepts.

## Goal
The goal for this project is to demonstrate how to build an application using FP techniques in Scala.
Coming from an OO background to Scala can be very hard and this project can hopefully ease the burden for some people.

## Tech-Stack

- [Http4s](http://http4s.org/) the web server
- [Circe](https://circe.github.io/circe/) for json serialization
- [Doobie](https://github.com/tpolecat/doobie) for database access
- [Cats](https://typelevel.org/cats/) for pure FP
- [ScalaTest](https://www.scalatest.org/) for testing
- [PureConfig](https://pureconfig.github.io/docs/) for app config
- Tagless Final for the core domain.

### What the heck is F[_]?
The core domain of this project uses `F[_]` in a lot of placed. `F[_]` is a _higher kinded type_ and represents a type that abstracts over a type that holds another type, i.e. `List` and `Option`.
When using `F[_]` we want to express that evaluation is suspended into "some effect type". This helps us and provides `referential transparency` even for otherwise side-effectful code.
We can leave the effect type abstract, and bind it only at the edge of our application, when we bootstrap the `Server`.
This enables us to swap out the used effect system with relative ease, if we so desire.
