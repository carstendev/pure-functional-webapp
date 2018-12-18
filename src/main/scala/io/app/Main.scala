package io.app

import cats.effect._
import cats.implicits._
import io.app.config.Config
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

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, ExitCode] =
    for {
      config <- Resource.liftF(Config.load[F]())
      tx <- Database.hikariTransactor[F](config.database)
      repo <- Resource.liftF(AppointmentRepositoryF.apply[F](tx))
      prometheusService <- Resource.liftF(PrometheusExportService.build[F])


      appointmentService = AppointmentService.service(repo)
      healthService = HealthService.service(repo)
      services = appointmentService <+> healthService
      meteredRoutes = Metrics[F](Prometheus(prometheusService.collectorRegistry, "server"))(services)
      allRoutes = meteredRoutes <+> prometheusService.routes
      httpApp = Logger(logBody = true, logHeaders = true)(Router("/" -> allRoutes).orNotFound)

      _ <- Resource.liftF(Database.createTables(tx))
      //_ <- Resource.liftF(Database.migrate(config.database)) //not working atm

      exitCode <- Resource.liftF(
        BlazeServerBuilder[F]
          .bindHttp(config.server.port, config.server.host)
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
      )

    } yield exitCode


  override def run(args: List[String]): IO[ExitCode] =
    createServer.use(IO.pure)

}
