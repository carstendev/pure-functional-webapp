package io.app.service

import cats.effect.ConcurrentEffect
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final class AuthService[F[_]](implicit F: ConcurrentEffect[F]) extends Http4sDsl[F] {
  def service(authProvider: BasicAuthProvider[F]): HttpRoutes[F] =
    HttpRoutes.of[F] { // TODO: add register route
      case req@ GET -> Root / "login" =>
        authProvider.logIn.run(req)
    }
}

