package io.app.repository

import java.time.ZonedDateTime

import cats.effect.IO
import doobie.h2.H2Transactor
import doobie.util.transactor.Transactor
import io.app.model.{Appointment, AppointmentWithId}
import org.scalatest.{MustMatchers, WordSpec}

class AppointmentRepositorySpec extends WordSpec with MustMatchers {

  "The appointment repo" should {


    H2Transactor.newH2Transactor("", "", "")
    "be empty" in {
      val repo = AppointmentRepositoryF.empty[IO].unsafeRunSync()
      repo.getAllAppointments.unsafeRunSync mustBe Nil
    }

    "correctly handle inserts, updates and deletes" in {
      val repo = AppointmentRepositoryF.empty[IO].unsafeRunSync()

      val appointment = Appointment(ZonedDateTime.now(), ZonedDateTime.now(), "something")

      repo.insertAppointment(appointment).unsafeRunSync() mustBe 1
      repo.insertAppointment(appointment).unsafeRunSync() mustBe 2

      repo.getAppointment(1).unsafeRunSync().get.id mustBe 1
      repo.getAppointment(2).unsafeRunSync().get.id mustBe 2

      repo.getAllAppointments.unsafeRunSync().sortBy(_.id).size mustBe 2

      repo.deleteAppointment(1).unsafeRunSync()
      repo.getAppointment(1).unsafeRunSync() mustBe None

      val appointmentToUpdate = AppointmentWithId(2, ZonedDateTime.now(), ZonedDateTime.now(), "somethingNew")
      repo.updateAppointment(appointmentToUpdate).unsafeRunSync()
      repo.getAppointment(2).unsafeRunSync() mustBe Some(appointmentToUpdate)
    }

  }

}
