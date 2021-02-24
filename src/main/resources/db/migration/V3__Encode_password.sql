ALTER USER sweater WITH SUPERUSER;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

update usr
set password = crypt(password, gen_salt('bf', 8));