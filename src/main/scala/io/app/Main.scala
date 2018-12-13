package io.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import doobie.h2.H2Transactor
import doobie.util.transactor.Transactor
import io.app.database.Database
import io.app.repository.AppointmentRepositoryF
import io.app.service.{AppointmentService, HealthService}
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.middleware.Metrics

object Main extends IOApp {

  private val tx =
    Transactor
      .fromDriverManager[IO]("org.h2.Driver", "jdbc:h2:mem:appointment_db;DB_CLOSE_DELAY=-1")

  override def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      _ <- Database.createTables(tx)
      repo <- AppointmentRepositoryF.apply[IO](tx)
      prometheusService <- PrometheusExportService.build[IO]
    } yield {

      val appointmentService = AppointmentService.service(repo)
      val healthService = HealthService.service(repo)
      val services = appointmentService <+> healthService
      val meteredRoutes = Metrics[IO](Prometheus(prometheusService.collectorRegistry, "server"))(services)

      val allRoutes = meteredRoutes <+> prometheusService.routes

      val httpApp = Logger(
        logBody = true,
        logHeaders = true
      )(Router("/" -> allRoutes).orNotFound)

      BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
    program.flatten
  }

}
