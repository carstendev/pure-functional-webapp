package io.app.database

import cats.effect.Effect
import doobie.implicits._
import doobie._

object Database {

  def createTables[F[_] : Effect](tx: Transactor[F]) = {
    sql"""CREATE TABLE IF NOT EXISTS "APPOINTMENT"(
         "ID" int AUTO_INCREMENT,
         "START" timestamp without time zone NOT NULL,
         "END" timestamp without time zone NOT NULL,
         "DESCRIPTION" varchar(255),
         CONSTRAINT "appointment_pkey" PRIMARY KEY ("ID")
         );""".update.run.transact(tx)
  }
}
