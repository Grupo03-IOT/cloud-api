package com.pe.cloudapi.iam.domain.model.errors;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

public enum IamError implements ErrorCatalog {

    EMAIL_ALREADY_USED(ErrorKind.CONFLICT,
            "An account with email '%s' already exists"),

    USER_NOT_FOUND(ErrorKind.NOT_FOUND,
            "User %s does not exist"),

    /**
     * A propósito no dice si falló el correo o la contraseña: distinguirlo
     * permitiría averiguar qué cuentas existen probando correos.
     */
    INVALID_CREDENTIALS(ErrorKind.VALIDATION,
            "Email or password is not correct"),

    ACCOUNT_DISABLED(ErrorKind.CONFLICT,
            "This account is disabled"),

    CREDENTIAL_CODE_ALREADY_USED(ErrorKind.CONFLICT,
            "A credential with code '%s' already exists"),

    CREDENTIAL_REVOKED(ErrorKind.CONFLICT,
            "The credential '%s' is revoked"),

    UNKNOWN_SCOPE(ErrorKind.VALIDATION,
            "'%s' is not a scope this API grants");

    private static final String PREFIX = "IAM";

    private final ErrorKind kind;
    private final String messageTemplate;

    IamError(ErrorKind kind, String messageTemplate) {
        this.kind = kind;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String code() {
        return PREFIX + "_" + name();
    }

    @Override
    public ErrorKind kind() {
        return kind;
    }

    @Override
    public String messageTemplate() {
        return messageTemplate;
    }
}
