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
    -p|--profile)
    PROFILE="$2"
    shift # past argument
    ;;
    *)
    printf "***************************\n"
    printf "* Error: Invalid argument in deploy api %s=%s.*\n" "$1" "$2"
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

export_stack_outputs symeo-backend-monitoring-${ENV} ${REGION}
export_stack_outputs symeo-backend-iam-${ENV} ${REGION}
export_stack_outputs symeo-backend-ecs-repository-job-${ENV} ${REGION}
export_stack_outputs symeo-backend-ecs-cluster-job-${ENV} ${REGION}
export_stack_outputs symeo-backend-ecs-services-job-${ENV} ${REGION}

AccountId=$(get_aws_account_id)

aws ecs register-task-definition \
  --task-role-arn arn:aws:iam::${AccountId}:role/${SymeoBackendTaskRole} \
  --execution-role-arn arn:aws:iam::${AccountId}:role/${SymeoBackendECSExecutionRole} \
  --family ${FamilyName} \
  --region ${REGION} \
  --requires-compatibilities FARGATE \
  --cpu 2048 \
  --memory 4096 \
  --network-mode awsvpc \
  --container-definitions "
[
  {
    \"name\":\"SymeoBackendContainerJob-${ENV}\",
    \"image\":\"${SymeoBackendRepository}:${TAG}\",
    \"portMappings\":[{\"containerPort\":9999}],
    \"cpu\":1948,
    \"memory\":3840,
    \"dockerLabels\": {
      \"com.datadoghq.ad.instances\": \"[{\\\"host\\\": \\\"%%host%%\\\", \\\"port\\\": 9999}]\",
      \"com.datadoghq.ad.check_names\": \"[\\\"symeo-api-${ENV}\\\"]\",
      \"com.datadoghq.ad.init_configs\": \"[{}]\"
    },
    \"environmentFiles\": [{
      \"type\":\"s3\",
      \"value\":\"arn:aws:s3:::${EnvFilesS3Bucket}/.env\"
    }],
    \"logConfiguration\":{
          \"logDriver\":\"awslogs\",
          \"options\":{
            \"awslogs-group\":\"${CloudwatchLogsGroup}\",
            \"awslogs-region\":\"${REGION}\",
            \"awslogs-stream-prefix\":\"symeo-backend\"
          }
        }
  }
]"

aws ecs update-service --cluster ${ECSCluster} --service ${ServiceName} --task-definition ${FamilyName} --region ${REGION}

aws ecs wait services-stable --cluster ${ECSCluster} --services ${ServiceName} --region ${REGION}

echo "DONE"
