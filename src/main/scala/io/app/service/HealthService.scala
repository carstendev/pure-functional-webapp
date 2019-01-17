package io.app.service

import cats.effect._
import cats.implicits._
import io.app.repository.AppointmentRepository
import org.http4s._
import org.http4s.dsl.Http4sDsl

final class HealthService[F[_]] extends Http4sDsl[F] {

  def service(
    appRepo: AppointmentRepository[F]
  )(implicit F: ConcurrentEffect[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "healthz" =>
        appRepo.ping.flatMap {
          case Right(_) =>
            F.pure(Response(status = Status.Ok))

          case Left(ex) =>
            F.pure(Response(status = Status.ServiceUnavailable).withEntity(ex.getMessage))
        }
    }
}
