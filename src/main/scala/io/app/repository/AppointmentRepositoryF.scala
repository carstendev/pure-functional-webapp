package io.app.repository

import java.sql.{SQLException, Timestamp}
import java.time.{ZoneId, ZonedDateTime}

import fs2.Stream
import io.app.repository.AppointmentRepositoryF._
import cats.effect.ConcurrentEffect
import doobie._
import doobie.implicits._
import io.app.model.{Appointment, AppointmentDao, AppointmentWithId}

final class AppointmentRepositoryF[F[_] : ConcurrentEffect](tx: Transactor[F]) extends AppointmentRepository[F] {

  override def ping: F[Either[SQLException, Option[Long]]] =
    sql"select id from appointment limit 1".query[Long].option.attemptSql.transact(tx)

  override def getAllAppointments: Stream[F, AppointmentDao] = {
    sql"select * from appointment".query[AppointmentDao].stream.transact(tx)
  }

  override def getAppointment(id: Long): F[Option[AppointmentDao]] =
    sql"select id, start, end, description, user_id from appointment where id = $id".query[AppointmentDao].option.transact(tx)

  override def insertAppointment(appointment: Appointment, userId: Long): F[Long] = {
    val start = appointment.start
    val end = appointment.end
    val description = appointment.description
    sql"insert into appointment (start, end, description, user_id) values ($start, $end, $description, $userId)"
      .update.withUniqueGeneratedKeys[Long]("id").transact(tx)
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

  def apply[F[_]](tx: Transactor[F])(implicit F: ConcurrentEffect[F]): F[AppointmentRepositoryF[F]] =
    F.pure { new AppointmentRepositoryF(tx) }

  implicit val DateTimeMeta: Meta[ZonedDateTime] = {
    Meta[Timestamp]
      .imap[ZonedDateTime] { ts =>ZonedDateTime.ofInstant(ts.toInstant, ZoneId.of("UTC"))
    }(dt => new Timestamp(dt.toInstant.toEpochMilli))
  }
}
