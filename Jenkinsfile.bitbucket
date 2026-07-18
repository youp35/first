pipeline {
  agent any

  triggers {
    bitbucketPush()
  }

  options {
    timestamps()
  }

  environment {
    AWS_DEFAULT_REGION = 'ap-southeast-1'
    PIPELINE_BUCKET    = 'jenkins-public-site-src-74e7ee8d366adc66c6eae0411c'
    CODEBUILD_PROJECT  = 'public-site-deploy'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Package Source') {
      steps {
        sh '''
          rm -f /tmp/source.zip
          jar --create --file /tmp/source.zip -C "$WORKSPACE" .
        '''
      }
    }

    stage('Upload Source Bundle') {
      steps {
        sh '''
          aws s3 cp /tmp/source.zip "s3://${PIPELINE_BUCKET}/bundles/${JOB_NAME}/${BUILD_NUMBER}/source.zip"
        '''
      }
    }

    stage('Build And Deploy') {
      steps {
        script {
          def imageTag = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
          def sourceLocation = "${env.PIPELINE_BUCKET}/bundles/${env.JOB_NAME}/${env.BUILD_NUMBER}/source.zip"
          def buildId = sh(
            returnStdout: true,
            script: '''
              aws codebuild start-build \
                --project-name "$CODEBUILD_PROJECT" \
                --source-type-override S3 \
                --source-location-override "$SOURCE_LOCATION" \
                --environment-variables-override name=IMAGE_TAG,value="$IMAGE_TAG",type=PLAINTEXT \
                --query 'build.id' \
                --output text
            ''',
            environment: [
              "IMAGE_TAG=${imageTag}",
              "SOURCE_LOCATION=${sourceLocation}"
            ]
          ).trim()

          timeout(time: 30, unit: 'MINUTES') {
            waitUntil {
              def status = sh(
                returnStdout: true,
                script: "aws codebuild batch-get-builds --ids '${buildId}' --query 'builds[0].buildStatus' --output text"
              ).trim()

              if (status == 'SUCCEEDED') {
                return true
              }

              if (status in ['FAILED', 'FAULT', 'STOPPED', 'TIMED_OUT']) {
                error("CodeBuild failed with status ${status}")
              }

              sleep time: 15, unit: 'SECONDS'
              return false
            }
          }
        }
      }
    }
  }
}
