# Cloud API de SenseWork ZenRoom

Requiere **JDK 21** (tambien en la configuracion de Gradle de IntelliJ) y PostgreSQL.
Desde la raiz del repositorio: `docker compose up -d db`, y despues ejecutar
`CloudApiApplication` en IntelliJ o `./gradlew bootRun`.
El servicio usa el puerto 8080; Swagger esta en `/swagger-ui.html`.

## Primer administrador y conexion con el Edge

1. Registrar una cuenta con `POST /api/v1/users` en Swagger. El registro publico
   siempre crea un miembro; no acepta que el visitante se asigne ADMIN.
2. Habilitar expresamente esa cuenta desde una terminal con acceso a la base.
   En PowerShell, usando el contenedor local del proyecto:

   ```powershell
   Get-Content tools/bootstrap-admin.sql | docker compose exec -T db psql -U comfort -d comfort -v admin_email=tu-correo@ejemplo.com
   ```

   El script exige una cuenta activa existente, conserva sus otros roles y puede
   repetirse. No crea usuarios ni contrasenas por defecto.
3. Iniciar sesion en `POST /api/v1/auth/login` y autorizar Swagger con `access_token`.
   Si la sesion era anterior al cambio de rol, iniciar sesion de nuevo.
4. Crear el local con `POST /api/v1/sites`:
   `{"code":"site-1","name":"ZenRoom laboratorio","address":"Prueba local","timezone":"America/Lima"}`.
5. Emitir la credencial con `POST /api/v1/credentials`:
   `{"code":"edge-1","scopes":["readings:write","thresholds:read","edges:heartbeat"]}`.
   Guardar `api_key` como `CLOUD_API_KEY` en el proceso del Edge; se muestra una vez.
6. Usar `SITE_CODE=site-1` en el Edge. El ESP32 envia sus lecturas al puerto 5000
   de la laptop; la credencial del Cloud permanece en el Edge.

## Contrato de ingesta

`POST /api/v1/readings` recibe `{"site_code":"site-1","readings":[...]}`.
Las salas se resuelven por `(local, codigo de sala)`. Un local desconocido se
rechaza. Para clientes anteriores, omitir `site_code` solo funciona si existe
exactamente un local; con varios devuelve `MONITORING_SITE_CODE_REQUIRED`.

`GET /api/v1/rooms` incluye las salas de todos los locales activos, con su
`site_id`, para que las aplicaciones puedan mostrar el local correspondiente.

Un reenvio del mismo `(room_id, ts)` actualiza las metricas conservando el UUID de
la lectura. Asi llegan al Cloud las correcciones de minutos con lotes tardios.
La transaccion bloquea el local durante la ingesta para serializar reintentos
concurrentes y el alta automatica de sus salas.

## Verificacion

Los agregados pueden incluir `sensors` con `sampled_at`, `movement`,
`distance_cm` (nulo si no hubo eco), `potentiometer` (ADC 0–4095) y los
booleanos `button1`, `button2`, `button3`. La migracion V4 anade columnas
opcionales y conserva las lecturas existentes. Los datos aparecen en
`GET /api/v1/rooms` bajo `latest.sensors` y en el historial de la sala.
La temperatura y humedad siguen en `climate`; no se generan valores de ruido
a partir del potenciometro. La guia de prueba USB esta en `edge-api/docs/usb-local.md`.

Con PostgreSQL disponible: `./gradlew test`. La prueba de carga del contexto usa
la base configurada en `DB_URL`, `DB_USER` y `DB_PASSWORD`; usar una base de pruebas.
