CREATE TABLE IF NOT EXISTS user(
  id int IDENTITY,
  name varchar(99) NOT NULL UNIQUE,
  password varchar(99) NOT NULL,
  CONSTRAINT user_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS appointment(
  id int IDENTITY,
  start timestamp without time zone NOT NULL,
  end timestamp without time zone NOT NULL,
  description varchar(99),
  user_id int NOT NULL,
  CONSTRAINT appointment_user_fkey FOREIGN KEY (user_id) REFERENCES USER (id),
  CONSTRAINT appointment_pkey PRIMARY KEY (id)
);
