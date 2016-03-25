# yt-eliza

## Deploying

Run `lein cljs-lambda default-iam-role` if you don't have yet have suitable
execution role to place in your project file.  This command will create an IAM
role under your default (or specified) AWS CLI profile, and modify your project
file to specify it as the execution default.

```
$ lein cljs-lambda default-iam-role
```

Otherwise, add an IAM role ARN under the function's `:role` key in the
`:functions` vector of your profile file, or in `:cljs-lambda` -> `:defaults` ->
`:role`.

```
$ lein cljs-lambda deploy
```

After deploying your Lambda function, running `create-api.sh` will use the AWS
CLI to create an API Gateway endpoint which accepts Slack's slash command
notifications:

```
$ ./create-api.sh [--function-name yt-eliza --name yt-eliza --profile default]
````

The URL output by the shell script can be used as a `POST` target, invoking your
Lambda function with a JSON object constructed from the form parameters
submitted by Slack.

The API will be tied to the $LATEST version of your Lambda function - subsequent
`cljs-lambda deploy` invocations will cause accesses of the exposed API Gateway
endpoint to invoke the most recently deployed code.

## Testing

```sh
lein doo node yt-eliza-test
```

Doo is provided to avoid including code to set the process exit code after a
 test run.
