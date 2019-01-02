package io.app.repository

import java.time.{ZoneId, ZonedDateTime}

import cats.effect.IO
import doobie.util.transactor.Transactor
import io.app.database.Database
import io.app.model.{Appointment, AppointmentDao}
import org.scalatest.{MustMatchers, WordSpec}

class AppointmentRepositorySpec extends WordSpec with MustMatchers {

  implicit val cs: cats.effect.ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.global)

  private val tx =
    Transactor
      .fromDriverManager[IO]("org.h2.Driver", "jdbc:h2:mem:appointment_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false")

  "The appointment repo" should {

    Database.createTables(tx).unsafeRunSync()

    "be empty" in {
      val repo = AppointmentRepositoryF.apply[IO](tx).unsafeRunSync()
      repo.getAllAppointments.unsafeRunSync mustBe Nil
    }

    "correctly handle inserts, updates and deletes" in {
      val repo = AppointmentRepositoryF.apply[IO](tx).unsafeRunSync()

      val appointment = Appointment(ZonedDateTime.now(), ZonedDateTime.now(), "something")

      repo.insertAppointment(appointment).unsafeRunSync()
      repo.insertAppointment(appointment).unsafeRunSync()

      repo.getAppointment(1).unsafeRunSync().get.id mustBe 1
      repo.getAppointment(2).unsafeRunSync().get.id mustBe 2

      repo.getAllAppointments.unsafeRunSync().sortBy(_.id).size mustBe 2

      repo.deleteAppointment(1).unsafeRunSync()
      repo.getAppointment(1).unsafeRunSync() mustBe None

      val now = ZonedDateTime.now(ZoneId.of("UTC"))
      val appointmentToUpdate = AppointmentWithId(2, now, now.plusHours(1), "somethingNew")
      repo.updateAppointment(appointmentToUpdate).unsafeRunSync()
      repo.getAppointment(2).unsafeRunSync() mustBe Some(appointmentToUpdate)
    }

  }

}
