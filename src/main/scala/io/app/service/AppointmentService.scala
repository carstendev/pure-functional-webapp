package io.app.service

import cats.effect.{IO, _}
import cats.implicits._
import io.app.model.{Appointment, AppointmentWithId, UserWithId}
import io.app.repository.AppointmentRepository
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object AppointmentService extends Http4sDsl[IO] {

  private val logger = org.log4s.getLogger

  val Appointments = "appointments"

  def service[F[_]](repository: AppointmentRepository[F])(implicit F: ConcurrentEffect[F]): AuthedService[UserWithId, F] =
    AuthedService[UserWithId, F] {
      case GET -> Root / Appointments as user =>
        logger.info(user.toString)
        repository.getAllAppointments.flatMap { appointments =>
          F.pure(Response(status = Status.Ok).withEntity(appointments.asJson))
        }

      case GET -> Root / Appointments / LongVar(id) as _ =>
        repository.getAppointment(id).flatMap {
          case Some(appointment) =>
            F.pure(Response(status = Status.Ok).withEntity(appointment.asJson))
          case None =>
            F.pure(Response(status = Status.NotFound))
        }

      case authedReq@POST -> Root / Appointments as _ =>
        authedReq.req.decodeJson[Appointment]
          .flatMap(repository.insertAppointment)
          .flatMap(appointment => F.pure(Response(status = Status.Created).withEntity(appointment.asJson)))

      case authedReq@PUT -> Root / Appointments as _ =>
        authedReq.req.decodeJson[AppointmentWithId]
          .flatMap(repository.updateAppointment)
          .flatMap(_ => F.pure(Response(status = Status.Ok)))

      case DELETE -> Root / Appointments / LongVar(id) as _ =>
        repository.deleteAppointment(id)
          .flatMap(_ => F.pure(Response(status = Status.NoContent)))
    }
}
