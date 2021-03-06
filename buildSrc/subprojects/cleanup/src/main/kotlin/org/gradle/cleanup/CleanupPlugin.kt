/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.cleanup

import BuildHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.process.KillLeakingJavaProcesses

class CleanupPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        tasks.create<CleanUpCaches>("cleanUpCaches") {
            dependsOn(":createBuildReceipt")
        }
        tasks.create<CleanUpDaemons>("cleanUpDaemons")

        val killExistingProcessesStartedByGradle by tasks.creating(KillLeakingJavaProcesses::class)

//        if (BuildHost.isCiServer) {
        if (!BuildHost.isCiServer) {
            tasks {
                getByName("clean") {
                    dependsOn(killExistingProcessesStartedByGradle)
                }
                subprojects {
                    this.tasks.all {
                        mustRunAfter(killExistingProcessesStartedByGradle)
                    }
                }
            }
        }
    }
}
