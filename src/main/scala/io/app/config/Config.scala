package io.app.config

import cats.effect.Sync
import cats.implicits._
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException
import pureconfig._
import pureconfig.generic.auto._

case class ServerConfig(host: String, port: Int)
case class DatabaseConfig(driver: String, url: String, user: String, password: String)
case class AuthConfig(privateKey: String)

// representation of the complete config
case class Config(server: ServerConfig, database: DatabaseConfig, auth: AuthConfig)

object Config {

  /**
    * Loads the config from the resource folder.
    */
  def load[F[_]](configFile: String = "application.conf")(implicit F: Sync[F]): F[Config] =
    F.pure(ConfigFactory.load(configFile)) // must use pure, not delay, otherwise loading the cfg fails
      .map(loadConfig[Config](_))
      .flatMap {
        case Left(e) => F.raiseError[Config](new ConfigReaderException[Config](e))
        case Right(config) => F.pure(config)
      }
}
