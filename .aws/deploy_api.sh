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
export_stack_outputs symeo-backend-ecs-repository-${ENV} ${REGION}
export_stack_outputs symeo-backend-ecs-cluster-${ENV} ${REGION}
export_stack_outputs symeo-backend-ecs-services-${ENV} ${REGION}
export_stack_outputs symeo-backend-aurora-${ENV} ${REGION}

AccountId=$(get_aws_account_id)

api_container="
{
  \"name\":\"SymeoBackendContainer-${ENV}\",
  \"image\":\"${SymeoBackendRepository}:${TAG}\",
  \"portMappings\":[{\"containerPort\":9999}],
  \"cpu\":1948,
  \"memory\":3840,
  \"dockerLabels\": {
    \"com.datadoghq.ad.instances\": \"[{\\\"host\\\": \\\"%%host%%\\\", \\\"port\\\": 9999}]\",
    \"com.datadoghq.ad.check_names\": \"[\\\"symeo-front-api-${ENV}\\\"]\",
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
}"

datadog_container="
{
  \"name\":\"DataDogAgent-${ENV}\",
  \"image\":\"public.ecr.aws/datadog/agent:latest\",
  \"cpu\":100,
  \"memory\":256,
  \"portMappings\":[{
    \"hostPort\":8126,
    \"protocol\":\"tcp\",
    \"containerPort\":8126
  }],
  \"dockerLabels\": {
    \"com.datadoghq.ad.instances\": \"[{\\\"dbm\\\":true,\\\"host\\\":\\\"${ClusterEndpoint}\\\",\\\"username\\\":\\\"datadog\\\"\\\"password\\\": \\\"${DB_PASSWORD}\\\"\\\"port\\\": 9999}]\",
    \"com.datadoghq.ad.check_names\": \"[\\\"postgres\\\"]\",
    \"com.datadoghq.ad.init_configs\": \"[{}]\"
  },
  \"environment\":[
    {\"name\":\"DD_API_KEY\",\"value\":\"${DATADOG_API_KEY}\"},
    {\"name\":\"DD_SITE\",\"value\":\"datadoghq.eu\"},
    {\"name\":\"DD_APM_ENABLED\",\"value\":\"true\"},
    {\"name\":\"DD_APM_NON_LOCAL_TRAFFIC\",\"value\":\"true\"},
    {\"name\":\"ECS_FARGATE\",\"value\":\"true\"},
    {\"name\":\"DD_APM_IGNORE_RESOURCES\",\"value\":\"GET /actuator/health\"}
  ]
}
"

if [ "$ENV" = "prod" ]
then
  container_definition="[${api_container},${datadog_container}]"
else
  container_definition="[${api_container}]"
fi


aws ecs register-task-definition \
  --task-role-arn arn:aws:iam::${AccountId}:role/${SymeoBackendTaskRole} \
  --execution-role-arn arn:aws:iam::${AccountId}:role/${SymeoBackendECSExecutionRole} \
  --family ${FamilyName} \
  --region ${REGION} \
  --requires-compatibilities FARGATE \
  --cpu 2048 \
  --memory 4096 \
  --network-mode awsvpc \
  --container-definitions "$container_definition"

aws ecs update-service --cluster ${ECSCluster} --service ${ServiceName} --task-definition ${FamilyName} --region ${REGION}

aws ecs wait services-stable --cluster ${ECSCluster} --services ${ServiceName} --region ${REGION}

echo "DONE"
