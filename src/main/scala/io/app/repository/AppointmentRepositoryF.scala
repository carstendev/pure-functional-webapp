package io.app.repository

import cats.effect.Effect
import io.app.model.{Appointment, AppointmentWithId}

import scala.collection.mutable

final case class AppointmentRepositoryF[F[_]: Effect](
  private val appointments: mutable.Map[Long, AppointmentWithId]
) extends AppointmentRepository[F] {

  val F = implicitly[Effect[F]]

  override def ping: F[Unit] =
    F.delay { () }

  override def getAllAppointments: F[Seq[AppointmentWithId]] =
    F.delay { appointments.values.toSeq }

  override def getAppointment(id: Long): F[Option[AppointmentWithId]] =
    F.delay { appointments.get(id) }

  override def insertAppointment(appointment: Appointment): F[Long] =
    F.delay {
      val maxId = if (appointments.isEmpty) 0 else appointments.keys.max
      val newId = maxId + 1
      appointments.put(newId, AppointmentWithId(newId, appointment.startDate, appointment.endTime, appointment.description))
      newId
    }

  override def updateAppointment(appointmentWithId: AppointmentWithId): F[Unit] = {
    F.delay { appointments.update(appointmentWithId.id, appointmentWithId) }
  }

  override def deleteAppointment(id: Long): F[Unit] =
    F.delay { appointments.remove(id) }
}

object AppointmentRepositoryF {
  def empty[F[_]](implicit F: Effect[F]): F[AppointmentRepositoryF[F]] =
    F.pure { new AppointmentRepositoryF[F](mutable.Map()) }
}
