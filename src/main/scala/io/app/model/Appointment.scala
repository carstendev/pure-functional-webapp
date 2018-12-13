package io.app.model

import java.time.ZonedDateTime

final case class Appointment(startDate: ZonedDateTime, endTime: ZonedDateTime, description: String)
final case class AppointmentWithId(id: Long, startDate: ZonedDateTime, endTime: ZonedDateTime, description: String)
