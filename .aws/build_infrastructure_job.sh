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
    -d|--domain)
    DOMAIN="$2"
    shift # past argument
    ;;
    -pu|--prefix-url)
    PREFIX_URL="$2"
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
    printf "* Error: Invalid argument in build infra job %s=%s.*\n" "$1" "$2"
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

## Build Public Url
PUBLIC_URL=${PREFIX_URL}.${DOMAIN}

export_stack_outputs symeo-backend-sg-${ENV} ${REGION}
export_stack_outputs symeo-backend-iam-${ENV} ${REGION}
export_stack_outputs symeo-backend-monitoring-${ENV} ${REGION}
export_stack_outputs symeo-backend-aurora-${ENV} ${REGION}
export_stack_outputs symeo-backend-s3-${ENV} ${REGION}

## Application Load Balancer
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      CertificateArn=${ACM_ARN_ALB} \
      Env=${ENV} \
      SecurityGroup=${SymeoBackendAlbSg} \
      Subnets=${SUBNETS} \
      VpcId=${VPC_ID} \
  --region ${REGION} \
  --stack-name symeo-backend-alb-job-${ENV} \
  --template-file cloudformation/job/alb.yml

export_stack_outputs symeo-backend-alb-job-${ENV} ${REGION}

## Cloudfront
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      AlbDNS=${ServiceId} \
      CertificateArn=${ACM_ARN} \
      Env=${ENV} \
      PublicAlias=${PUBLIC_URL} \
  --region ${REGION} \
  --stack-name symeo-backend-cloudfront-job-${ENV} \
  --template-file cloudformation/job/cloudfront.yml \

export_stack_outputs symeo-backend-cloudfront-job-${ENV} ${REGION}


## ECS Repository
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --stack-name symeo-backend-ecs-repository-job-${ENV} \
  --region ${REGION} \
  --template-file cloudformation/job/ecs-repository.yml

export_stack_outputs symeo-backend-ecs-repository-job-${ENV} ${REGION}

## Build Docker Image and push it to the ECS Repository
if docker_image_exists_in_ecr $SymeoBackendRepositoryName $MY_TAG $REGION; then
  echo "Docker image with tag ${MY_TAG} already exists, skipping build..."
else
  echo "No image found with tag ${MY_TAG}, building it..."
  ./build_docker.sh -r "$REGION" -e "$ENV" -t "$MY_TAG" -p "$PROFILE" -s "symeo-job" -sp "aws,job"
fi

ENV_FILE_PATH="./.env"

./build_env_file.sh \
  --file ${ENV_FILE_PATH} \
  --region ${REGION} \
  --env ${ENV} \
  --db-password ${DB_PASSWORD} \
  --profile ${PROFILE}

aws s3 cp $ENV_FILE_PATH s3://${EnvFilesS3Bucket}

## ECS Cluster
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --region ${REGION} \
  --stack-name symeo-backend-ecs-cluster-job-${ENV} \
  --template-file cloudformation/job/ecs-cluster.yml \

export_stack_outputs symeo-backend-ecs-cluster-job-${ENV} ${REGION}

## ECS Services
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      AlbName=${AlbName} \
      CloudwatchLogsGroup=${CloudwatchLogsGroup} \
      DockerRepository=${SymeoBackendRepository} \
      ECSAutoScaleRole=${SymeoBackendAutoScaleRole} \
      ECSCluster=${ECSCluster} \
      ECSTaskRole=${SymeoBackendTaskRole} \
      ECSExecutionRole=${SymeoBackendECSExecutionRole} \
      Env=${ENV} \
      EnvFilesS3Bucket=${EnvFilesS3Bucket} \
      Tag=${MY_TAG} \
      TargetGroup=${TargetGroup} \
      TargetGroupName=${TargetGroupName} \
      SecurityGroup=${SymeoBackendSg} \
      Subnets=${SUBNETS} \
  --region ${REGION} \
  --stack-name symeo-backend-ecs-services-job-${ENV} \
  --template-file cloudformation/job/ecs-services.yml \
