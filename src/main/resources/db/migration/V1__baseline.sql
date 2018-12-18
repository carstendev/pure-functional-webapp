CREATE TABLE IF NOT EXISTS "appointment"(
  "id" int AUTO_INCREMENT,
  "start" timestamp without time zone NOT NULL,
  "end" timestamp without time zone NOT NULL,
  "description" text,
  CONSTRAINT "appointment_pkey" PRIMARY KEY ("id")
);
