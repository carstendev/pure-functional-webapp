CREATE TABLE IF NOT EXISTS user(
  id int IDENTITY,
  name varchar(99) NOT NULL UNIQUE,
  CONSTRAINT user_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS appointment(
  id int IDENTITY,
  start timestamp without time zone NOT NULL,
  end timestamp without time zone NOT NULL,
  description varchar(99),
  CONSTRAINT appointment_pkey PRIMARY KEY (id)
);
