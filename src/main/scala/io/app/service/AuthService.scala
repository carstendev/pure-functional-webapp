package io.app.service

import cats.effect.ConcurrentEffect
import org.http4s.{HttpRoutes, Response, Status}
import org.http4s.dsl.Http4sDsl
import cats.implicits._


final class AuthService[F[_]](implicit F: ConcurrentEffect[F]) extends Http4sDsl[F] {
  def service(authProvider: BasicAuthProvider[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req@ GET -> Root / "login" =>
        authProvider.logIn.run(req)

      case req@ GET -> Root / "register" =>
        authProvider.registerUser(req).flatMap {
          case Right(_) =>
            F.pure(Response(status = Status.Ok).withEntity("User is registered"))

          case Left(ex) =>
            F.pure(Response(status = Status.BadRequest).withEntity(ex))
        }
    }
}

