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
    -e|--env)
    ENV="$2"
    shift # past argument
    ;;
    -p|--profile)
    PROFILE="$2"
    shift # past argument
    ;;
    -dbp|--db-password)
    DB_PASSWORD="$2"
    shift # past argument
    ;;
    -ddik|--datadog-api-key)
    DATADOG_API_KEY="$2"
    shift # past argument
    ;;
    -ddpk|--datadog-app-key)
    DATADOG_APP_KEY="$2"
    shift # past argument
    ;;
    -d|--domain)
    DOMAIN="$2"
    shift # past argument
    ;;
    -pu|--prefix-url)
    PREFIX_URL="$2"
    shift # past argument
    ;;
    -jpu|--job-prefix-url)
    JOB_PREFIX_URL="$2"
    shift # past argument
    ;;
    -acmc|--acm-arn)
    ACM_ARN="$2"
    shift # past argument
    ;;
    -acma|--acm-arn-alb)
    ACM_ARN_ALB="$2"
    shift # past argument
    ;;
    -t|--tag)
    TAG="$2"
    shift # past argument
    ;;
    -v|--vpc-id)
    VPC_ID="$2"
    shift # past argument
    ;;
    -s|--subnets)
    SUBNETS="$2"
    shift # past argument
    ;;
    *)
    printf "***************************\n"
    printf "* Error: Invalid argument in build infra %s=%s.*\n" "$1" "$2"
    printf "***************************\n"
    exit 1
esac
shift # past argument or value
done

# We check if AWS Cli profile is in parameters to set env var
if [ -z "$PROFILE" ]
then
    echo "Profile parameter is empty, the default profile will be used !"
else
    export AWS_PROFILE=${PROFILE}
fi

if [ -z "$TAG" ]
then
    MY_TAG="latest"
else
    MY_TAG=$TAG
fi

## Security Groups
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
      VpcId=${VPC_ID} \
  --region ${REGION} \
  --stack-name symeo-backend-sg-${ENV} \
  --template-file cloudformation/security-groups.yml

export_stack_outputs symeo-backend-sg-${ENV} ${REGION}

## IAM Roles
aws cloudformation deploy \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
      SecretName=${SECRET_ID} \
  --region ${REGION} \
  --stack-name symeo-backend-iam-${ENV} \
  --template-file cloudformation/iam.yml

export_stack_outputs symeo-backend-iam-${ENV} ${REGION}

## Monitoring (Log Group, Alarms, ...)
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --region ${REGION} \
  --stack-name symeo-backend-monitoring-${ENV} \
  --template-file cloudformation/monitoring.yml

export_stack_outputs symeo-backend-monitoring-${ENV} ${REGION}

## TODO: make a backup/snapshot of the db before running potential update (which could empty the db)

## Database
# Only create db stack if it does not already exist
# TODO: add a way to force the update with script parameter
if ! stack_exists "symeo-backend-aurora-${ENV}" $REGION
then
  aws cloudformation deploy \
    --no-fail-on-empty-changeset \
    --parameter-overrides \
        DBPassword=${DB_PASSWORD} \
        Env=${ENV} \
        SymeoBackendDatabaseSg=${SymeoBackendDatabaseSg} \
    --region ${REGION} \
    --stack-name symeo-backend-aurora-${ENV} \
    --template-file cloudformation/aurora.yml
fi

export_stack_outputs symeo-backend-aurora-${ENV} ${REGION}

## S3
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --region ${REGION} \
  --stack-name symeo-backend-s3-${ENV} \
  --template-file cloudformation/s3.yml

export_stack_outputs symeo-backend-s3-${ENV} ${REGION}

./build_infrastructure_api.sh \
  --region "$REGION" \
  --env "$ENV" \
  --profile "$PROFILE" \
  --db-password "$DB_PASSWORD" \
  --datadog-api-key "$DATADOG_API_KEY" \
  --domain "$DOMAIN" \
  --prefix-url "$PREFIX_URL" \
  --acm-arn "$ACM_ARN" \
  --acm-arn-alb "$ACM_ARN_ALB" \
  --tag "$MY_TAG" \
  --vpc-id "$VPC_ID" \
  --subnets "$SUBNETS"

./build_infrastructure_job.sh \
  --region "$REGION" \
  --env "$ENV" \
  --profile "$PROFILE" \
  --db-password "$DB_PASSWORD" \
  --datadog-api-key "$DATADOG_API_KEY" \
  --domain "$DOMAIN" \
  --prefix-url "$JOB_PREFIX_URL" \
  --acm-arn "$ACM_ARN" \
  --acm-arn-alb "$ACM_ARN_ALB" \
  --tag "$MY_TAG" \
  --vpc-id "$VPC_ID" \
  --subnets "$SUBNETS"

## Datadog integration
aws cloudformation deploy \
--no-fail-on-empty-changeset \
  --parameter-overrides \
       APIKey=${DATADOG_API_KEY} \
       APPKey=${DATADOG_APP_KEY} \
  --region ${REGION} \
  --stack-name symeo-datadog-integration \
  --capabilities CAPABILITY_IAM \
  --capabilities CAPABILITY_NAMED_IAM \
  --template-file cloudformation/datadog-aws-integration.yml \

set_datadog_forwarder_arn_to_env symeo-datadog-integration ${REGION}

## Datadog log forwarders
aws cloudformation deploy \
--no-fail-on-empty-changeset \
  --parameter-overrides \
       DatadogForwarderArn=${DatadogForwarderArn} \
       CloudwatchLogsGroup=${CloudwatchLogsGroup} \
  --region ${REGION} \
  --stack-name symeo-datadog-log-forwarder-${ENV} \
  --template-file cloudformation/datadog-log-forwarder.yml

echo "DONE"
