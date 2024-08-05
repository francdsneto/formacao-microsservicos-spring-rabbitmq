#!/bin/sh

set -e

# Senha do usuário root fixo
MYSQL_ROOT_USER="root"
MYSQL_ROOT_PASSWORD="root"

# Espera até o MySQL estar pronto
until mysql -h"$DB_HOST" -u"$MYSQL_ROOT_USER" -p"$MYSQL_ROOT_PASSWORD" -e 'SHOW DATABASES;' 2>/dev/null; do
  >&2 echo "Tentando conectar ao MySQL em $DB_HOST..."
  >&2 echo "Variáveis de ambiente: MYSQL_ROOT_USER=$MYSQL_ROOT_USER, MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD"
  sleep 5
done

>&2 echo "MySQL está pronto - iniciando a aplicação"
exec "$@"
