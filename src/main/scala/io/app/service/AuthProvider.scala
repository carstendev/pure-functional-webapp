package io.app.service

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.implicits._
import io.app.config.AuthConfig
import io.app.model.User
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, Request, Response, Status, _}
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver

import scala.util.Try

case class AuthProvider[F[_]](
  authConfig: AuthConfig
)(implicit F: Effect[F]) {

  def authMiddleware: AuthMiddleware[F, User] = {
    val httpsJwks = new HttpsJwks(authConfig.jwksLocation)
    val httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJwks)

    val jwtConsumer = new JwtConsumerBuilder()
      .setRequireExpirationTime()
      .setAllowedClockSkewInSeconds(30)
      .setRequireSubject()
      .setJweAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE)
      .setExpectedIssuer(authConfig.issuer)
      .setVerificationKeyResolver(httpsJwksKeyResolver)
      .setExpectedAudience(authConfig.audience)
      .build()

    def verifyToken(token: String): F[Either[String, User]] = F.delay {
      Try(jwtConsumer.process(token))
        .map(ctx => {
          val name = ctx.getJwtClaims.getStringClaimValue("nickname")
          User(1, name)
        })
        .toEither
        .leftMap(_ => "Invalid authentication")
    }

    val onFailure: AuthedService[String, F] = Kleisli { _ =>
      OptionT.pure(Response[F](Status.Unauthorized))
    }

    val authUser: Kleisli[F, Request[F], Either[String, User]] = Kleisli { request =>
      val token = request.headers
        .get(Authorization)
        .map(h => h.value.drop(AuthScheme.Bearer.length + 1))

      val user = token.map { t =>
        verifyToken(t)
      }
      user.getOrElse(F.pure("Resource requires authentication".asLeft[User]))
    }

    AuthMiddleware(authUser, onFailure)
  }

}
