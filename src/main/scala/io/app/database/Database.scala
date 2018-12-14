package io.app.database

import cats.effect.{Async, ContextShift, Effect}
import doobie._
import doobie.implicits._
import io.app.config.DatabaseConfig

object Database {

  def transactor[F[_] : Async : ContextShift](config: DatabaseConfig): F[Transactor[F]] = {
    val F = implicitly[Async[F]]
    F.delay { Transactor
      .fromDriverManager[F](config.driver, config.url, config.user, config.password)
    }
  }

  def createTables[F[_] : Effect](tx: Transactor[F]): F[Unit] = {
    sql"""CREATE TABLE IF NOT EXISTS "appointment"(
         "id" int AUTO_INCREMENT,
         "start" timestamp without time zone NOT NULL,
         "end" timestamp without time zone NOT NULL,
         "description" text,
         CONSTRAINT "appointment_pkey" PRIMARY KEY ("id")
         );""".update.run.map(_ => ()).transact(tx)
  }
}
