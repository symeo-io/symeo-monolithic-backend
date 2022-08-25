#!/bin/bash
source ./utils.sh

set -e

########################
### NAMING ARGUMENTS ###
########################

while [[ $# -gt 1 ]]
do
key="$1"

case $key in
    -f|--file)
    FILE="$2"
    shift # past argument
    ;;
    -r|--region)
    REGION="$2"
    shift # past argument
    ;;
    -e|--env)
    ENV="$2"
    shift # past argument
    ;;
    -dbp|--db-password)
    DB_PASSWORD="$2"
    shift # past argument
    ;;
    -p|--profile)
    PROFILE="$2"
    shift # past argument
    ;;
    *)
    printf "***************************\n"
    printf "* Error: Invalid argument in build env file %s=%s.*\n" "$1" "$2"
    printf "***************************\n"
    exit 1
esac
shift # past argument or value
done

# We check if AWS Cli profile is in parameters to set env var
if [ -z "${PROFILE}" ]
then
echo "Profile parameter is empty, the default profile will be used!"
else
    export AWS_PROFILE=${PROFILE}
fi

if [ -z "${FILE}" ]
then
    FILE_PATH=./.env
else
    FILE_PATH=${FILE}
fi

export_stack_outputs symeo-backend-aurora-${ENV} ${REGION}
export_stack_outputs symeo-backend-s3-${ENV} ${REGION}

rm -f $FILE_PATH

echo "AWS_REGION=${REGION}
S3_DATALAKE_BUCKET_NAME=${DatalakeS3Bucket}
DATABASE_USERNAME=${DBUsername}
DATABASE_PASSWORD=${DB_PASSWORD}
DATABASE_URL=jdbc:postgresql://${ClusterEndpoint}:${DBPort}/${DBName}?rewriteBatchedStatements=true
AUTH0_AUDIENCE=${AUTH0_AUDIENCE}
AUTH0_ISSUER=${AUTH0_ISSUER}
GITHUB_APP_ID=${GITHUB_APP_ID}
GITHUB_WEBHOOK_SECRET=${GITHUB_WEBHOOK_SECRET}
FRONTEND_CORS_HOST=${FRONTEND_CORS_HOST}
SYMEO_EMAIL=${SYMEO_EMAIL}
SENDGRID_TEMPLATE_ID=${SENDGRID_TEMPLATE_ID}
SENDGRID_API_KEY=${SENDGRID_API_KEY}" >> $FILE_PATH
