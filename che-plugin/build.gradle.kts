/*
 * Copyright (c) 2022 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "che.incubator.intellij.remote.devfile"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.eclipse.jetty.websocket:websocket-jetty-client:11.0.9")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("io.github.che-incubator:che-api:1.0")
}


intellij {
    version.set("2021.2")
    plugins.add("terminal")
}

tasks {
    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
    }
}

tasks.buildSearchableOptions {
    enabled = false
}
