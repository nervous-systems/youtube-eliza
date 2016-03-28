# yt-eliza

Assuming the project file contains an IAM role capable of executing further Lambda functions:

```
$ lein cljs-lambda deploy
```

Running `create-api.sh` will use the AWS CLI to create an API Gateway endpoint
which accepts Slack's slash command notifications:

```
$ ./create-api.sh [--function-name yt-eliza-gateway --name yt-eliza --profile default]
````

The URL output by the shell script can be used as a `POST` target, invoking your
Lambda function with a JSON object constructed from the form parameters
submitted by Slack.

The API will be tied to the $LATEST version of your Lambda function - subsequent
`cljs-lambda deploy` invocations will cause accesses of the exposed API Gateway
endpoint to invoke the most recently deployed code.
