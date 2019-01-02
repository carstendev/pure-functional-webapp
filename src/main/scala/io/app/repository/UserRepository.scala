package io.app.repository

import java.sql.SQLException

import io.app.model.{User, UserWithId}

/**
  * Represents the repository for users.
  * All functions should suspend their effects, and not actually evaluate.
  */
trait UserRepository[F[_]] {
  def ping: F[Either[SQLException, Option[Long]]]

  def getUser(id: Long): F[Option[UserWithId]]

  def getUser(name: String): F[Option[UserWithId]]

  def insertUser(user: User): F[Unit]

  def updateUser(userWithId: UserWithId): F[Unit]

  def deleteUser(id: Long): F[Unit]
}
