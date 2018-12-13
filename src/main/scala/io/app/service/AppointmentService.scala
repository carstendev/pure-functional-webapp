package io.app.service

import cats.effect.{IO, _}
import cats.implicits._
import io.app.model.{Appointment, AppointmentWithId}
import io.app.repository.AppointmentRepository
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object AppointmentService extends Http4sDsl[IO] {

  val Appointments = "appointments"

  def service[F[_]](repository: AppointmentRepository[F])(implicit F: ConcurrentEffect[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / Appointments =>
        repository.getAllAppointments.flatMap { appointments =>
          F.pure(Response(status = Status.Ok).withEntity(appointments.asJson))
        }

      case GET -> Root / Appointments / LongVar(id) =>
        repository.getAppointment(id).flatMap {
          case Some(appointment) =>
            F.pure(Response(status = Status.Ok).withEntity(appointment.asJson))
          case None =>
            F.pure(Response(status = Status.NotFound))
        }

      case req@POST -> Root / Appointments =>
        req.decodeJson[Appointment]
          .flatMap(repository.insertAppointment)
          .flatMap(appointment => F.pure(Response(status = Status.Created).withEntity(appointment.asJson)))

      case req@PUT -> Root / Appointments =>
        req.decodeJson[AppointmentWithId]
          .flatMap(repository.updateAppointment)
          .flatMap(_ => F.pure(Response(status = Status.Ok)))

      case DELETE -> Root / Appointments / LongVar(id) =>
        repository.deleteAppointment(id)
          .flatMap(_ => F.pure(Response(status = Status.NoContent)))
    }
}
