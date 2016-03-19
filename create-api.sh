#!/usr/bin/env bash

set -e -o pipefail

API_NAME="yt-eliza"
FUNCTION_NAME="yt-eliza-gateway"
PROFILE=

while [[ 1 < $# ]]; do
  key="$1"
  case $key in
    --name) API_NAME="$2"; shift;;
    --function-name) FUNCTION_NAME="$2"; shift;;
    --profile) PROFILE="--profile $2"; shift;;
  esac; shift; done

if [ -z $API_NAME ] || [ -z $FUNCTION_NAME ]; then
  >&2 echo "Requires --name, --function-name"
  exit 1
fi

REGION=$(aws configure get region $PROFILE)
ACCT_ID=$(aws iam get-user $PROFILE --query User.UserId --output text)

FUNCTION_ARN=$(
  aws lambda get-function \
      --function-name $FUNCTION_NAME \
      --query Configuration.FunctionArn \
      --output text \
      $PROFILE)

API_ID=$(
  aws apigateway create-rest-api \
      --name scratch-$API_NAME --query id --output text $PROFILE)

echo "api-id" $API_ID

aws lambda add-permission \
    --function-name $FUNCTION_NAME \
    --statement-id "cljs-slack-slash-command-$API_ID" \
    --action lambda:InvokeFunction \
    --principal apigateway.amazonaws.com \
    --source-arn "arn:aws:execute-api:$REGION:$ACCT_ID:$API_ID/*/POST/" \
    $PROFILE >/dev/null

RES_ID=$(
  aws apigateway get-resources --rest-api-id $API_ID \
      --query "items[0].id" \
      --output text \
      $PROFILE)

ARGS="--rest-api-id $API_ID --resource-id $RES_ID --http-method POST"

aws apigateway put-method $ARGS --authorization-type NONE  $PROFILE >/dev/null

TEMPLATE=$(awk '{gsub ("\"", "\\\"" ); print $0}' ORS='\\n' assets/slack-post.ftl)
TEMPLATES='{"application/x-www-form-urlencoded":"'"$TEMPLATE"'"}'

aws apigateway put-integration $ARGS \
    --integration-http-method POST \
    --type AWS \
    --uri "arn:aws:apigateway:$REGION:lambda:path/2015-03-31/functions/$FUNCTION_ARN/invocations" \
    --request-templates "$TEMPLATES" \
    $PROFILE >/dev/null

aws apigateway put-method-response $ARGS \
    --status-code 200 \
    --response-models '{"application/json": "Empty"}' \
    $PROFILE >/dev/null

aws apigateway put-integration-response $ARGS \
    --status-code 200 \
    --selection-pattern '' \
    $PROFILE >/dev/null

DEPLOYMENT_ID=$(
  aws apigateway create-deployment \
      --rest-api-id $API_ID \
      --stage-name test \
      --query id \
      --output text \
      $PROFILE)
echo "deployment-id" $DEPLOYMENT_ID

aws apigateway create-stage \
    --rest-api-id $API_ID \
    --stage-name auto \
    --deployment-id $DEPLOYMENT_ID \
    --description initial \
    $PROFILE >/dev/null

echo "url https://$API_ID.execute-api.$REGION.amazonaws.com/auto"
