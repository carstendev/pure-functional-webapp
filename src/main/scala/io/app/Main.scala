package io.app

import cats.effect.IO
import fs2.StreamApp
import fs2.Stream
import io.app.repository.AppointmentRepositoryF
import io.app.services.AppointmentService
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends StreamApp[IO]{

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    Stream.eval(AppointmentRepositoryF.empty[IO]).flatMap { appointmentRepo =>
      BlazeBuilder[IO]
        .bindHttp(8080, "127.0.0.1")
        .mountService(AppointmentService.service(appointmentRepo), "/")
        .serve
    }

}
