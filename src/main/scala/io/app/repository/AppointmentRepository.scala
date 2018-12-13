package io.app.repository

import io.app.model.{Appointment, AppointmentWithId}

trait AppointmentRepository[F[_]] {

  def ping: F[Unit]

  def getAllAppointments: F[Seq[AppointmentWithId]]

  def getAppointment(id: Long): F[Option[AppointmentWithId]]

  def insertAppointment(appointment: Appointment): F[Long]

  def updateAppointment(appointmentWithId: AppointmentWithId): F[Unit]

  def deleteAppointment(id: Long): F[Unit]
}
