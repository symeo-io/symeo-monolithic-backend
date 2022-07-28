package fr.catlean.monolithic.backend.domain.exception;

public interface CatleanExceptionCode {

    // AWS Exception Codes
    String AWS_API_EXCEPTION = "T.AWS_API_EXCEPTION";
    String AWS_S3_SERIALIZATION_EXCEPTION = "T.AWS_S3_SERIALIZATION_EXCEPTION";
    String AWS_PARTIAL_S3_UPLOAD = "T.AWS_PARTIAL_S3_UPLOAD";
    String AWS_INVALID_BUCKET_NAME = "F.AWS_INVALID_BUCKET_NAME";

    // Postgres Exception Codes
    String ORGANISATION_NOT_FOUND = "F.ORGANISATION_NOT_FOUND";
    String ORGANIZATION_NAME_NOT_FOUND = "F.ORGANIZATION_NAME_NOT_FOUND";
    String POSTGRES_EXCEPTION = "T.POSTGRES_EXCEPTION";


    // Github Exception Codes
    String UNHANDLED_HTTP_STATUS_CODE = "F.UNHANDLED_HTTP_STATUS_CODE";
    String ERROR_WHILE_EXECUTING_HTTP_REQUEST = "T.ERROR_WHILE_EXECUTING_HTTP_REQUEST";
    String GITHUB_ORG_TOKEN_NOT_FOUND = "F.GITHUB_ORG_TOKEN_NOT_FOUND";
    String INVALID_URI_FOR_GITHUB = "T.INVALID_URI_FOR_GITHUB";
    String GITHUB_APP_JWT_GENERATION = "T.GITHUB_APP_JWT_GENERATION";

    // Auth0 Exception Codes
    String MISSING_MAIL_AUTH0 = "T.MISSING_MAIL_AUTH0";

    // Thread Exception Codes
    String INTERRUPTED_THREAD = "T.INTERRUPTED_THREAD";

    // TeamGoal Exception Codes
    String INVALID_TEAM_STANDARD_CODE = "F.INVALID_TEAM_STANDARD_NAME";

}
