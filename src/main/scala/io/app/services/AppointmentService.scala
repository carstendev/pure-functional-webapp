package io.app.services

import cats.effect.{IO, _}
import cats.implicits._
import io.app.model.{Appointment, AppointmentWithId}
import io.app.repository.AppointmentRepository
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger

object AppointmentService extends Http4sDsl[IO] {

  val Appointments = "appointments"

  def service[F[_]](repository: AppointmentRepository[F])(implicit F: ConcurrentEffect[F]): HttpService[F] =
    Logger(
      logHeaders = true,
      logBody = true
    ) {
      HttpService[F] {

        case GET -> Root / Appointments =>
          repository.getAllAppointments.flatMap { appointments =>
            Response(status = Status.Ok).withBody(appointments.asJson)
          }

        case GET -> Root / Appointments / LongVar(id) =>
          repository.getAppointment(id).flatMap {
            case Some(appointment) =>
              Response(status = Status.Ok).withBody(appointment.asJson)
            case None =>
              F.pure(Response(status = Status.NotFound))
          }

        case req @ POST -> Root / Appointments =>
          req.decodeJson[Appointment]
            .flatMap(repository.insertAppointment)
            .flatMap(appointment => Response(status = Status.Created).withBody(appointment.asJson))

        case req @ PUT -> Root / Appointments =>
          req.decodeJson[AppointmentWithId]
            .flatMap(repository.updateAppointment)
            .flatMap(_ => F.pure(Response(status = Status.Ok)))

        case DELETE -> Root / Appointments / LongVar(id) =>
          repository.deleteAppointment(id)
            .flatMap(_ => F.pure(Response(status = Status.NoContent)))

      }
    }
}
