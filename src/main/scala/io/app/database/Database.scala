package io.app.database

import cats.effect._
import doobie._
import cats.implicits._
import io.app.config.DatabaseConfig
import org.flywaydb.core.Flyway
import doobie.hikari._
import doobie.util.ExecutionContexts
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator
import org.flywaydb.core.api.logging.LogFactory


object Database {

  def transactor[F[_] : Async : ContextShift](config: DatabaseConfig): F[Transactor[F]] = {
    val F = implicitly[Async[F]]
    F.delay {
      Transactor
        .fromDriverManager[F](config.driver, config.url, config.user, config.password)
    }
  }

  /**
    * Provides a transactor using the given config.
    */
  def hikariTransactor[F[_] : Async : ContextShift](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[F] // our transaction EC
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driver,
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ce,
        transactEC = te
      )
    } yield xa


  def migrate[F[_]](config: DatabaseConfig)(implicit F: Sync[F]): F[Unit] = F.delay {

    LogFactory.setLogCreator(new Slf4jLogCreator())
    val cl = Class.forName(config.driver).getClassLoader

    Flyway.configure(cl).dataSource(
      config.url,
      config.user,
      config.password
    ).load().migrate()

    ()
  }
}
