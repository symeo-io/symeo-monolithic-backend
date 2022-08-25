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
    -r|--region)
    REGION="$2"
    shift # past argument
    ;;
    -t|--tag)
    TAG="$2"
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
    -ddk|--datadog-api-key)
    DATADOG_API_KEY="$2"
    shift # past argument
    ;;
    -p|--profile)
    PROFILE="$2"
    shift # past argument
    ;;
    *)
    printf "***************************\n"
    printf "* Error: Invalid argument in deploy %s=%s.*\n" "$1" "$2"
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

export_stack_outputs symeo-backend-s3-${ENV} ${REGION}

ENV_FILE_PATH="./.env"

./build_env_file.sh \
  --file ${ENV_FILE_PATH} \
  --region ${REGION} \
  --env ${ENV} \
  --db-password ${DB_PASSWORD} \
  --profile ${PROFILE}

aws s3 cp $ENV_FILE_PATH s3://${EnvFilesS3Bucket}

./deploy_api \
  --region ${REGION} \
  --tag ${TAG} \
  --env ${ENV} \
  --db-password ${DB_PASSWORD} \
  --datadog-api-key ${DATADOG_API_KEY} \
  --profile ${PROFILE}

./deploy_job \
  --region ${REGION} \
  --tag ${TAG} \
  --env ${ENV} \
  --db-password ${DB_PASSWORD} \
  --datadog-api-key ${DATADOG_API_KEY} \
  --profile ${PROFILE}

echo "DONE"
