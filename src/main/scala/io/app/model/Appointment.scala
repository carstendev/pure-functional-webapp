package io.app.model

import java.time.ZonedDateTime

final case class Appointment(start: ZonedDateTime, end: ZonedDateTime, description: String)
final case class AppointmentWithId(id: Long, start: ZonedDateTime, end: ZonedDateTime, description: String)
