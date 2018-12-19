package io.app.repository

import io.app.model.{User, UserWithId}

trait UserRepository[F[_]] {
  def ping: F[Unit]

  def getUser(id: Long): F[Option[UserWithId]]

  def getUser(name: String): F[Option[UserWithId]]

  def insertUser(user: User): F[Unit]

  def updateUser(userWithId: UserWithId): F[Unit]

  def deleteUser(id: Long): F[Unit]
}
