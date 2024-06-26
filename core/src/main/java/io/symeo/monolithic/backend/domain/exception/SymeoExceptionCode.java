package io.symeo.monolithic.backend.domain.exception;

public interface SymeoExceptionCode {

    // AWS Exception Codes
    String AWS_API_EXCEPTION = "T.AWS_API_EXCEPTION";
    String AWS_S3_SERIALIZATION_EXCEPTION = "T.AWS_S3_SERIALIZATION_EXCEPTION";
    String AWS_PARTIAL_S3_UPLOAD = "T.AWS_PARTIAL_S3_UPLOAD";
    String AWS_INVALID_BUCKET_NAME = "F.AWS_INVALID_BUCKET_NAME";

    // Postgres Exception Codes
    String ORGANISATION_NOT_FOUND = "F.ORGANISATION_NOT_FOUND";
    String ORGANIZATION_NAME_NOT_FOUND = "F.ORGANIZATION_NAME_NOT_FOUND";
    String POSTGRES_EXCEPTION = "T.POSTGRES_EXCEPTION";
    String TEAM_STANDARD_NOT_FOUND = "F.TEAM_STANDARD_NOT_FOUND";
    String TEAM_NOT_FOUND = "F.TEAM_NOT_FOUND";
    String ORGANIZATION_SETTINGS_NOT_FOUND = "F.ORGANIZATION_SETTINGS_NOT_FOUND";
    String VCS_ORGANIZATION_NOT_FOUND = "F.VCS_ORGANIZATION_NOT_FOUND";
    String REPOSITORIES_NOT_FOUND = "F.REPOSITORIES_NOT_FOUND";
    String POSTGRES_JSON_MAPPING_ERROR = "T.POSTGRES_JSON_MAPPING_ERROR";


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

    // Sendgrid Exception Codes
    String SENDGRID_EXCEPTION = "T.SEND_GRID_EMAIL_EXCEPTION";

    // Date Exception Codes
    String FAILED_TO_PARSE_DATE = "F.FAILED_TO_PARSE_DATE";

    // Pagination Exception Codes
    String PAGINATION_MAXIMUM_SIZE_EXCEEDED = "F.PAGINATION_MAXIMUM_SIZE_EXCEEDED";
    String INVALID_SORTING_PARAMETER = "F.INVALID_SORTING_PARAMETER";
    String INVALID_SORTING_DIRECTION = "F.INVALID_SORTING_DIRECTION";
    String GITHUB_JSON_MAPPING_ERROR = "T.GITHUB_JSON_MAPPING_ERROR";

    // Symeo http client Exception Codes
    String SYMEO_HTTP_CLIENT_ERROR = "T.EXCEPTION_WHILE_SENDING_HTTP_REQUEST_TO_SYMEO_API";

    // Testing Exception Codes
    String FAILED_TO_PARSE_COVERAGE_REPORT = "F.FAILED_TO_PARSE_COVERAGE_REPORT";

    // Api key Exception Codes
    String UNKNOWN_API_KEY = "F.UNKNOWN_API_KEY";

    // Job Exception Codes
    String INVALID_JOB_CODE = "F.INVALID_JOB_CODE";
    String INVALID_DEPLOYEMENT_DETECTION_TYPE = "F.INVALID_DEPLOY_DETECTION_TYPE";
    String JOB_FAILED_FOR_UNKNOWN_REASON = "T.JOB_FAILED_FOR_UNKNOWN_REASON";
}
