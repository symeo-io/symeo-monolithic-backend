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
    -k|--key-name)
    PEM_KEY="$2"
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
    printf "* Error: Invalid argument.*\n"
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

AmiImageId=$(get_amazon_linux_2_ami_id $REGION)

## Security Groups
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
      VpcId=${VPC_ID} \
  --region ${REGION} \
  --stack-name catlean-backend-sg-${ENV} \
  --template-file cloudformation/security-groups.yml

export_stack_outputs catlean-backend-sg-${ENV} ${REGION}

## IAM Roles
aws cloudformation deploy \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
      SecretName=${SECRET_ID} \
  --region ${REGION} \
  --stack-name catlean-backend-iam-${ENV} \
  --template-file cloudformation/iam.yml

export_stack_outputs catlean-backend-iam-${ENV} ${REGION}

## Monitoring (Log Group, Alarms, ...)
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --region ${REGION} \
  --stack-name catlean-backend-monitoring-${ENV} \
  --template-file cloudformation/monitoring.yml

export_stack_outputs catlean-backend-monitoring-${ENV} ${REGION}

## TODO: make a backup/snapshot of the db before running potential update (which could empty the db)

## Database
# Only create db stack if it does not already exist
# TODO: add a way to force the update with script parameter
if ! stack_exists "catlean-backend-aurora-${ENV}" $REGION
then
  aws cloudformation deploy \
    --no-fail-on-empty-changeset \
    --parameter-overrides \
        DBPassword=${DB_PASSWORD} \
        Env=${ENV} \
        CatleanBackendDatabaseSg=${CatleanBackendDatabaseSg} \
    --region ${REGION} \
    --stack-name catlean-backend-aurora-${ENV} \
    --template-file cloudformation/aurora.yml
fi

export_stack_outputs catlean-backend-aurora-${ENV} ${REGION}

## S3
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --region ${REGION} \
  --stack-name catlean-backend-s3-${ENV} \
  --template-file cloudformation/s3.yml

export_stack_outputs catlean-backend-s3-${ENV} ${REGION}

## Application Load Balancer
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      CertificateArn=${ACM_ARN_ALB} \
      Env=${ENV} \
      SecurityGroup=${CatleanBackendAlbSg} \
      Subnets=${SUBNETS} \
      VpcId=${VPC_ID} \
  --region ${REGION} \
  --stack-name catlean-backend-alb-${ENV} \
  --template-file cloudformation/alb.yml

export_stack_outputs catlean-backend-alb-${ENV} ${REGION}

## Cloudfront
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      AlbDNS=${ServiceId} \
      CertificateArn=${ACM_ARN} \
      Env=${ENV} \
      PublicAlias=${PUBLIC_URL} \
  --region ${REGION} \
  --stack-name catlean-backend-cloudfront-${ENV} \
  --template-file cloudformation/cloudfront.yml \

export_stack_outputs catlean-backend-cloudfront-${ENV} ${REGION}

## ECS Repository
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --stack-name catlean-backend-ecs-repository-${ENV} \
  --region ${REGION} \
  --template-file cloudformation/ecs-repository.yml

export_stack_outputs catlean-backend-ecs-repository-${ENV} ${REGION}

## Build Docker Image and push it to the ECS Repository
if docker_image_exists_in_ecr $CatleanBackendRepositoryName $MY_TAG $REGION; then
  echo "Docker image with tag ${MY_TAG} already exists, skipping build..."
else
  echo "No image found with tag ${MY_TAG}, building it..."
  ./build_docker.sh -r ${REGION} -e ${ENV} -t ${MY_TAG} -p ${PROFILE}
fi


## ECS Cluster
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      Env=${ENV} \
  --region ${REGION} \
  --stack-name catlean-backend-ecs-cluster-${ENV} \
  --template-file cloudformation/ecs-cluster.yml \

export_stack_outputs catlean-backend-ecs-cluster-${ENV} ${REGION}

## ECS AutoScaling group
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      AmiImageId=${AmiImageId} \
      AssociatePublicIp=true \
      ECSCluster=${ECSCluster} \
      Env=${ENV} \
      InstanceProfile=${CatleanBackendEC2InstanceProfile} \
      KeyName=${PEM_KEY} \
      SecurityGroup=${CatleanBackendSg} \
      Subnets=${SUBNETS} \
  --stack-name catlean-backend-ecs-autoscaling-${ENV} \
  --region ${REGION} \
  --template-file cloudformation/autoscaling.yml

export_stack_outputs catlean-backend-ecs-autoscaling-${ENV} ${REGION}

## ECS Services
aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
      AlbName=${AlbName} \
      CloudwatchLogsGroup=${CloudwatchLogsGroup} \
      DockerRepository=${CatleanBackendRepository} \
      ECSAutoScaleRole=${CatleanBackendAutoScaleRole} \
      ECSCluster=${ECSCluster} \
      ECSServiceRole=${CatleanBackendServiceRole} \
      ECSTaskRole=${CatleanBackendTaskRole} \
      Env=${ENV} \
      Tag=${MY_TAG} \
      TargetGroup=${TargetGroup} \
      TargetGroupName=${TargetGroupName} \
  --region ${REGION} \
  --stack-name catlean-backend-ecs-services-${ENV} \
  --template-file cloudformation/ecs-services.yml

echo "DONE"
