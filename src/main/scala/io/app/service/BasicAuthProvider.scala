package io.app.service

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.implicits._
import io.app.config.AuthConfig
import io.app.model.{User, UserWithId}
import io.app.repository.UserRepository
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, Request, Response, ResponseCookie, Status, _}
import org.reactormonk.{CryptoBits, PrivateKey}

import scala.io.Codec
import scala.util.{Success, Try}


case class BasicAuthProvider[F[_]](
  authConfig: AuthConfig,
  userRepository: UserRepository[F]
)(implicit F: Effect[F]) {

  private val key = PrivateKey(Codec.toUTF8(authConfig.privateKey))
  private val crypto = CryptoBits(key)
  private val clock = java.time.Clock.systemUTC

  def registerUser(request: Request[F]): F[Either[String, Unit]] = {
    F.delay {

      Try(request.headers.get(Authorization)
        .map(_.value)
        .map(_.substring("Basic".length).trim)
        .map(BasicCredentials.apply).get)
        .map { credentials =>
          User(
            name = credentials.username,
            password = credentials.password
          )
        }

    }.flatMap {
      case Success(userToRegister) =>

        userRepository.getUser(userToRegister.name).flatMap {
          case Some(user) =>
            F.pure(Left(s"User with name: ${user.name} already exists"))

          case None =>
            userRepository.insertUser(userToRegister).map(Right(_))
        }

      case _ =>
        F.pure(Left("User could not be registered"))
    }
  }

  def retrieveUser: Kleisli[F, Long, Either[String, UserWithId]] =
    Kleisli { id =>
      userRepository.getUser(id).map(_.toRight("No user found!"))
    }


  def verifyLogin(request: Request[F]): F[Either[String, UserWithId]] = {
    F.delay {

      Try(request.headers.get(Authorization)
        .map(_.value)
        .map(_.substring("Basic".length).trim)
        .map(BasicCredentials.apply).get)

    }.flatMap { credentialsTry =>

      credentialsTry.map { credentials =>
        userRepository.getUser(credentials.username).flatMap[Either[String, UserWithId]] {
          case Some(user) if user.password == credentials.password => // TODO: we are saving the password as plain text
            F.pure(Right(user))
          case _ =>
            F.pure(Left("User or password incorrect!"))
        }
      }.getOrElse(F.pure[Either[String, UserWithId]](Left[String, UserWithId]("Invalid auth header")))
    }
  }

  def logIn: Kleisli[F, Request[F], Response[F]] = Kleisli { request =>
    verifyLogin(request: Request[F]).flatMap {
      case Left(error) =>
        F.pure(Response[F](Status.Forbidden).withEntity(error))
      case Right(user) =>
        F.delay {
          val message = crypto.signToken(user.id.toString, clock.millis.toString)
          val expires = clock.instant().plusSeconds(60 * 60) // TODO: this does not prevent forgery!

          val httpDate = HttpDate.unsafeFromInstant(expires)

          Response[F](Status.Ok)
            .withEntity("Logged in")
            .addCookie(ResponseCookie("authcookie", message, Some(httpDate)))
        }
    }
  }

  def authMiddleware: AuthMiddleware[F, UserWithId] = {

    val onFailure: AuthedService[String, F] = Kleisli(
      _ => OptionT.pure(Response[F](Status.Unauthorized)))

    val authUser: Kleisli[F, Request[F], Either[String, UserWithId]] = Kleisli({ request =>
      val message = for {
        header <- headers.Cookie.from(request.headers).toRight("Cookie parsing error")
        cookie <- header.values.toList.find(_.name == "authcookie").toRight("Couldn't find the authcookie")
        token <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
        message <- Either.catchOnly[NumberFormatException](token.toLong).leftMap(_.toString)
      } yield message

      message match {
        case Right(id) =>
          retrieveUser.run(id)

        case Left(error) =>
          F.pure[Either[String, UserWithId]](Left(error))
      }

    })

    AuthMiddleware(authUser, onFailure)
  }
}
