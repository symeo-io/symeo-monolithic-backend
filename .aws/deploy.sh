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
    -p|--profile)
    PROFILE="$2"
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
if [ -z "${PROFILE}" ]
then
echo "Profile parameter is empty, the default profile will be used!"
else
    export AWS_PROFILE=${PROFILE}
fi

export_stack_outputs catlean-backend-monitoring-${ENV} ${REGION}
export_stack_outputs catlean-backend-iam-${ENV} ${REGION}
export_stack_outputs catlean-backend-ecs-repository-${ENV} ${REGION}
export_stack_outputs catlean-backend-ecs-cluster-${ENV} ${REGION}
export_stack_outputs catlean-backend-ecs-services-${ENV} ${REGION}
export_stack_outputs catlean-backend-aurora-${ENV} ${REGION}
export_stack_outputs catlean-backend-s3-${ENV} ${REGION}

AccountId=$(get_aws_account_id)

aws ecs register-task-definition \
  --task-role-arn arn:aws:iam::${AccountId}:role/${CatleanBackendTaskRole} \
  --execution-role-arn arn:aws:iam::${AccountId}:role/${CatleanBackendECSExecutionRole} \
  --family ${FamilyName} \
  --region ${REGION} \
  --requires-compatibilities FARGATE \
  --cpu 1024 \
  --memory 2GB \
  --network-mode awsvpc \
  --container-definitions "
[
  {
    \"name\":\"CatleanBackendContainer-${ENV}\",
    \"image\":\"${CatleanBackendRepository}:${TAG}\",
    \"portMappings\":[{\"containerPort\":9999}],
    \"environment\":[
      {\"name\":\"AWS_REGION\",\"value\":\"${REGION}\"},
      {\"name\":\"S3_DATALAKE_BUCKET_NAME\",\"value\":\"${DatalakeS3Bucket}\"},
      {\"name\":\"DATABASE_USERNAME\",\"value\":\"${DBUsername}\"},
      {\"name\":\"DATABASE_PASSWORD\",\"value\":\"${DB_PASSWORD}\"},
      {\"name\":\"AUTH0_AUDIENCE\",\"value\":\"${AUTH0_AUDIENCE}\"},
      {\"name\":\"AUTH0_ISSUER\",\"value\":\"${AUTH0_ISSUER}\"},
      {\"name\":\"GITHUB_APP_ID\",\"value\":\"${GITHUB_APP_ID}\"},
      {\"name\":\"GITHUB_WEBHOOK_SECRET\",\"value\":\"${GITHUB_WEBHOOK_SECRET}\"},
      {\"name\":\"FRONTEND_CORS_HOST\",\"value\":\"${FRONTEND_CORS_HOST}\"},
      {\"name\":\"DATABASE_URL\",\"value\":\"jdbc:postgresql://${ClusterEndpoint}:${DBPort}/${DBName}?rewriteBatchedStatements=true\"}
    ],
    \"logConfiguration\":{
      \"logDriver\":\"awslogs\",
      \"options\":{
        \"awslogs-group\":\"${CloudwatchLogsGroup}\",
        \"awslogs-region\":\"${REGION}\",
        \"awslogs-stream-prefix\":\"catlean-backend\"
      }
    }
  }
]"

aws ecs update-service --cluster ${ECSCluster} --service ${ServiceName} --task-definition ${FamilyName} --region ${REGION}

aws ecs wait services-stable --cluster ${ECSCluster} --services ${ServiceName} --region ${REGION}

echo "DONE"
