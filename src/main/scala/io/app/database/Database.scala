package io.app.database

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import cats.implicits._
import io.app.config.DatabaseConfig
import org.flywaydb.core.Flyway
import doobie.hikari._
import doobie.util.ExecutionContexts
import io.app.model.User

object Database {

  def transactor[F[_] : Async : ContextShift](config: DatabaseConfig): F[Transactor[F]] = {
    val F = implicitly[Async[F]]
    F.delay {
      Transactor
        .fromDriverManager[F](config.driver, config.url, config.user, config.password)
    }
  }

  def hikariTransactor[F[_] : Async : ContextShift](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[F] // our transaction EC
      _  <- Resource.liftF(Async[F].delay(Class.forName("org.h2.Driver")))
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driver,
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ce,
        transactEC = te
      )
    } yield xa


  // TODO: somehow the flyway migrations are not working...
  // org.flywaydb.core.api.FlywayException: Unable to instantiate JDBC driver: org.h2.Driver
  // => Check whether the jar file is present
  def migrate[F[_]](config: DatabaseConfig)(implicit F: Sync[F]): F[Unit] = {
    F.delay {

      val flyway = new Flyway()

      flyway.setDataSource(
        config.url,
        config.user,
        config.password
      )

      flyway.migrate()

      ()
    }
  }

  def createTables[F[_] : Effect](tx: Transactor[F]): F[Unit] = {
    val appointment =
      sql"""CREATE TABLE IF NOT EXISTS "appointment"(
         "id" int AUTO_INCREMENT,
         "start" timestamp without time zone NOT NULL,
         "end" timestamp without time zone NOT NULL,
         "description" text,
         CONSTRAINT "appointment_pkey" PRIMARY KEY ("id")
         );""".update.run.map(_ => ()).transact(tx)

    val user =
      sql"""CREATE TABLE IF NOT EXISTS "user"(
         "id" int AUTO_INCREMENT,
         "name" text,
         "password" text,
         CONSTRAINT "user_pkey" PRIMARY KEY ("id")
         );""".update.run.map(_ => ()).transact(tx)

    val defaultUser = User("carsten", "123")
    val userInsert =
      sql"insert into user (name, password) values (${defaultUser.name}, ${defaultUser.password})"
        .update.run.map(_ => ()).transact(tx)

    appointment.flatMap(_ => user).flatMap(_ => userInsert)
  }
}
