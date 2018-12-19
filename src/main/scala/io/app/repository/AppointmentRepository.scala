package io.app.repository

import io.app.model.{Appointment, AppointmentWithId}

/**
  * Represents the repository for appointments.
  * All functions should suspend their effects, and not actually evaluate.
  */
trait AppointmentRepository[F[_]] {

  def ping: F[Unit]

  def getAllAppointments: F[Seq[AppointmentWithId]]

  def getAppointment(id: Long): F[Option[AppointmentWithId]]

  def insertAppointment(appointment: Appointment): F[Unit]

  def updateAppointment(appointmentWithId: AppointmentWithId): F[Unit]

  def deleteAppointment(id: Long): F[Unit]
}
