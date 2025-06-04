DO $$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_database WHERE datname = 'springbootmicroservicesjwtproduct'
   ) THEN
      CREATE DATABASE springbootmicroservicesjwtproduct;
   END IF;
END
$$;

DO $$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_database WHERE datname = 'springbootmicroservicesjwtuser'
   ) THEN
      CREATE DATABASE springbootmicroservicesjwtuser;
   END IF;
END
$$;
