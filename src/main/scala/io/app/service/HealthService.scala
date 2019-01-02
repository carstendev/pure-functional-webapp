package io.app.service

import cats.effect._
import cats.implicits._
import io.app.repository.{AppointmentRepository, UserRepository}
import org.http4s._
import org.http4s.dsl.Http4sDsl

final class HealthService[F[_]] extends Http4sDsl[F] {


  def service(
    appRepo: AppointmentRepository[F],
    userRepo: UserRepository[F]
  )(implicit F: ConcurrentEffect[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "healthz" =>
        val dbPingCheck = for {
          appRepoPing <- appRepo.ping
          userRepoPing <- userRepo.ping
        } yield
          for {
            _ <- appRepoPing
            _ <- userRepoPing
          } yield ()

        dbPingCheck.flatMap {
          case Right(_) =>
            F.pure(Response(status = Status.Ok))

          case Left(ex) =>
            F.pure(Response(status = Status.ServiceUnavailable).withEntity(ex.getMessage))
        }
    }
}
