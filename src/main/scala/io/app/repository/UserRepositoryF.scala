package io.app.repository

import cats.effect.ConcurrentEffect
import doobie._
import doobie.implicits._
import io.app.model.{User, UserWithId}

final class UserRepositoryF[F[_] : ConcurrentEffect](tx: Transactor[F]) extends UserRepository[F] {

  override def ping: F[Unit] =
    sql"select id from user limit 1".query[Long].analysis.map(_ => ()).transact(tx)

  override def getUser(id: Long): F[Option[UserWithId]] =
    sql"select id, name, password from user where id = $id".query[UserWithId].option.transact(tx)

  override def getUser(name: String): F[Option[UserWithId]] =
    sql"select id, name, password from user where name = $name".query[UserWithId].option.transact(tx)

  override def insertUser(user: User): F[Unit] = {
    sql"insert into user (name, password) values (${user.name}, ${user.password})"
      .update.run.map(_ => ()).transact(tx)
  }

  override def updateUser(userWithId: UserWithId): F[Unit] = {
    sql"update user set name = ${userWithId.name} where id = ${userWithId.id}"
      .update.run.map(_ => ()).transact(tx)
  }

  override def deleteUser(id: Long): F[Unit] =
    sql"delete from user where id = $id".update.run.map(_ => ()).transact(tx)
}

object UserRepositoryF {

  def apply[F[_]](tx: Transactor[F])(implicit F: ConcurrentEffect[F]): F[UserRepositoryF[F]] =
    F.pure {
      new UserRepositoryF(tx)
    }
}


