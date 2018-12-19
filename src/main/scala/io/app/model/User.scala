package io.app.model

case class User(name: String, password: String)
case class UserWithId(id: Long, name: String, password: String)

case class UserLoginRequest(name: String, password: String)
