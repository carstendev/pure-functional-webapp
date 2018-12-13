package io.app.repository

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}
import io.app.repository.AppointmentRepositoryF._

import cats.effect.Effect
import doobie._
import doobie.implicits._
import io.app.model.{Appointment, AppointmentWithId}

final class AppointmentRepositoryF[F[_] : Effect](tx: Transactor[F]) extends AppointmentRepository[F] {

  override def ping: F[Unit] =
    sql"select id from appointment".query[Long].option.map(_ => ()).transact(tx)

  override def getAllAppointments: F[Seq[AppointmentWithId]] = {
    sql"select id, start, end, description from appointment".query[AppointmentWithId].to[Seq].transact(tx)
  }

  override def getAppointment(id: Long): F[Option[AppointmentWithId]] =
    sql"select id, start, end, description from appointment where id = $id".query[AppointmentWithId].option.transact(tx)

  override def insertAppointment(appointment: Appointment): F[Unit] = {
    val start = appointment.start
    val end = appointment.end
    val description = appointment.description
    sql"insert into appointment (start, end, description) values ($start, $end, $description)"
      .update.run.map(_ => ()).transact(tx)
  }

  override def updateAppointment(appointmentWithId: AppointmentWithId): F[Unit] = {
    val id = appointmentWithId.id
    val start = appointmentWithId.start
    val end = appointmentWithId.end
    val description = appointmentWithId.description
    sql"update appointment set start = $start, end = $end, description = $description where id = $id"
      .update.run.map(_ => ()).transact(tx)
  }

  override def deleteAppointment(id: Long): F[Unit] =
    sql"delete from appointment where id = $id".update.run.map(_ => ()).transact(tx)
}

object AppointmentRepositoryF {

  def apply[F[_]](tx: Transactor[F])(implicit F: Effect[F]): F[AppointmentRepositoryF[F]] =
    F.pure {
      new AppointmentRepositoryF(tx)
    }

  implicit val DateTimeMeta: Meta[ZonedDateTime] = {
    Meta[Timestamp]
      .imap[ZonedDateTime] {
      ts => ZonedDateTime.ofInstant(ts.toInstant, ZoneId.of("UTC"))
    }(dt => new Timestamp(dt.toInstant.toEpochMilli))
  }
}
