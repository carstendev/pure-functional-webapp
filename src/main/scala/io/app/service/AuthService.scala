package io.app.service

import cats.effect.{ConcurrentEffect, IO}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object AuthService extends Http4sDsl[IO] {
  def service[F[_]](authProvider: BasicAuthProvider[F])(implicit F: ConcurrentEffect[F]): HttpRoutes[F] =
    HttpRoutes.of[F] { //TODO: add register route
      case req@ GET -> Root / "login" =>
        authProvider.logIn.run(req)
    }
}

