

CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS user_db; /* (Ya la creamos para el siguiente paso) */

GRANT ALL PRIVILEGES ON auth_db.* TO 'barrisense_user'@'%';
GRANT ALL PRIVILEGES ON user_db.* TO 'barrisense_user'@'%';
FLUSH PRIVILEGES;