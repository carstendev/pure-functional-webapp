package io.app

import cats.effect.{Clock, ExitCode, IO, IOApp}
import cats.implicits._
import io.app.repository.AppointmentRepositoryF
import io.app.services.{AppointmentService, HealthService}
import io.prometheus.client.CollectorRegistry
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.middleware.Metrics

object Main extends IOApp {

  implicit val clock = Clock.create[IO]
  val registry = new CollectorRegistry()

  override def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      repo <- AppointmentRepositoryF.empty[IO]
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
