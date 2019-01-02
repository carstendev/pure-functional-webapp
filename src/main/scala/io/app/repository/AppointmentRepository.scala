package io.app.repository

import java.sql.SQLException

import fs2.Stream
import io.app.model.{Appointment, AppointmentDao, AppointmentWithId}

/**
  * Represents the repository for appointments.
  * All functions should suspend their effects, and not actually evaluate.
  */
trait AppointmentRepository[F[_]] {

  def ping: F[Either[SQLException, Option[Long]]]

  def getAllAppointments: Stream[F, AppointmentDao]

  def getAppointment(id: Long): F[Option[AppointmentDao]]

  def insertAppointment(appointment: Appointment, userId: Long): F[Long]

  def updateAppointment(appointmentWithId: AppointmentWithId): F[Unit]

  def deleteAppointment(id: Long): F[Unit]
}
