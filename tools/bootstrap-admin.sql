-- Ejecutar con psql -v admin_email=correo@ejemplo.com -f tools/bootstrap-admin.sql
-- La cuenta debe registrarse previamente por POST /api/v1/users.
\set ON_ERROR_STOP on
BEGIN;
SELECT set_config('zenroom.bootstrap_email', :'admin_email', true);
DO $$
DECLARE
    target_id UUID;
BEGIN
    SELECT id INTO target_id FROM iam.user_account
    WHERE LOWER(email) = LOWER(current_setting('zenroom.bootstrap_email'))
      AND deleted_at IS NULL AND active = TRUE;
    IF target_id IS NULL THEN
        RAISE EXCEPTION 'Register an active account with that email first';
    END IF;
    INSERT INTO iam.user_role (user_id, role) VALUES (target_id, 'ADMIN')
    ON CONFLICT DO NOTHING;
END $$;
COMMIT;
