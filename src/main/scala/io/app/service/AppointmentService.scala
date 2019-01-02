package io.app.service

import cats.effect._
import cats.implicits._
import io.app.model.{Appointment, AppointmentWithId, UserWithId}
import io.app.repository.AppointmentRepository
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

final class AppointmentService[F[_]] extends Http4sDsl[F] {

  val Appointments = "appointments"

  def service(repository: AppointmentRepository[F])(implicit F: ConcurrentEffect[F]): AuthedService[UserWithId, F] =
    AuthedService[UserWithId, F] {
      case GET -> Root / Appointments as user => // Users can only see their own appointments
        repository.getAllAppointments.filter(_.userId == user.id).compile.toList.flatMap { appointments =>
          F.pure(Response(status = Status.Ok).withEntity(appointments.asJson))
        }

      case GET -> Root / Appointments / LongVar(id) as user =>
        repository.getAppointment(id).flatMap {
          case Some(appointment) if appointment.userId == user.id =>
            F.pure(Response(status = Status.Ok).withEntity(appointment.asJson))

          case _ =>
            F.pure(Response(status = Status.NotFound))
        }

      case authedReq@POST -> Root / Appointments as user =>
        authedReq.req.decodeJson[Appointment]
          .flatMap(repository.insertAppointment(_, user.id))
          .flatMap(id => F.pure(Response(status = Status.Created).withEntity(id.asJson)))

      case authedReq@PUT -> Root / Appointments as _ =>
        authedReq.req.decodeJson[AppointmentWithId]
          .flatMap(repository.updateAppointment)
          .flatMap(_ => F.pure(Response(status = Status.Ok)))

      case DELETE -> Root / Appointments / LongVar(id) as user =>
        repository.getAppointment(id).flatMap {
          case Some(appointment) if appointment.userId == user.id =>
            repository.deleteAppointment(id)
              .flatMap(_ => F.pure(Response(status = Status.NoContent)))

          case _ =>
            F.pure(Response(status = Status.NoContent))
        }
    }
}
