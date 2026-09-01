CREATE SCHEMA IF NOT EXISTS iam;

CREATE TABLE iam.user_account (
    id            UUID         PRIMARY KEY,
    email         VARCHAR(160) NOT NULL,
    password_hash VARCHAR(72)  NOT NULL,
    display_name  VARCHAR(128) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    deleted_at    TIMESTAMPTZ,
    created_by    UUID,
    updated_by    UUID
);

CREATE UNIQUE INDEX ux_user_account_email
    ON iam.user_account (LOWER(email)) WHERE deleted_at IS NULL;

CREATE TABLE iam.user_role (
    user_id UUID        NOT NULL REFERENCES iam.user_account (id) ON DELETE CASCADE,
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE iam.api_credential (
    id         UUID         PRIMARY KEY,
    code       VARCHAR(64)  NOT NULL,
    token_hash CHAR(64)     NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    deleted_at TIMESTAMPTZ,
    created_by UUID,
    updated_by UUID
);

CREATE UNIQUE INDEX ux_api_credential_token ON iam.api_credential (token_hash);
CREATE UNIQUE INDEX ux_api_credential_code
    ON iam.api_credential (LOWER(code)) WHERE deleted_at IS NULL;

CREATE TABLE iam.api_credential_scope (
    credential_id UUID        NOT NULL REFERENCES iam.api_credential (id) ON DELETE CASCADE,
    scope         VARCHAR(48) NOT NULL,
    PRIMARY KEY (credential_id, scope)
);
