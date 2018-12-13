package io.app.services

import cats.effect.{IO, _}
import cats.implicits._
import io.app.repository.AppointmentRepository
import org.http4s._
import org.http4s.dsl.Http4sDsl

object HealthService extends Http4sDsl[IO] {

  def service[F[_]](repository: AppointmentRepository[F])(implicit F: ConcurrentEffect[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "healthz" =>
        repository.ping.flatMap(_ => F.pure(Response(status = Status.Ok)))
    }

}
