#!/usr/bin/env sh
set -e

python - <<'PY'
import os
import time
import psycopg2

host = os.getenv('DB_HOST', 'postgres')
port = int(os.getenv('DB_PORT', '5432'))
user = os.getenv('DB_USER', 'postgres')
password = os.getenv('DB_PASSWORD', 'postgres')
db = os.getenv('DB_NAME', 'parksmart')

for i in range(60):
    try:
        psycopg2.connect(host=host, port=port, user=user, password=password, dbname=db).close()
        print('Database ready')
        break
    except Exception:
        print(f'Database not ready, retry {i+1}/60')
        time.sleep(1)
else:
    raise SystemExit('Database unreachable')
PY

python -m src.storage.db_models.create_tables
python -m src.storage.db_models.seed_admin
exec uvicorn src.api.main:app --host 0.0.0.0 --port 8000
