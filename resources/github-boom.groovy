// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

@Library('apm@current') _

// Global variables can be only set usinig the @Field pattern
import groovy.transform.Field
@Field def variable

pipeline {
  agent none
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    PIPELINE_LOG_LEVEL='INFO'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    rateLimitBuilds(throttle: [count: 60, durationName: 'hour', userBoost: true])
    quietPeriod(10)
    skipDefaultCheckout()
  }
  parameters {
    string(name: 'step', defaultValue: '10', description: 'number the request to increment on each iteration')
    string(name: 'times', defaultValue: "10", description: 'Times to iterate the incremental the test')
  }
  stages {
    stage('Test') {
      agent { label 'linux && immutable' }
      options { skipDefaultCheckout() }
      steps {
        parallelSteps("test-1", 1)
        parallelSteps("test-10", 10)
        parallelSteps("test-20", 20)
        parallelSteps("test-30", 30)
        parallelSteps("test-40", 40)
        parallelSteps("test-50", 50)
      }
    }
  }
}


def parallelSteps(prefix, num){
  def parallelSteps = [:]
  for (i = 0; i < num; i++) {
      parallelSteps["${prefix}-step-${i}"] = {
        node('linux && immutable'){ checkout scm }
      }
  }
  parallel parallelSteps
}
