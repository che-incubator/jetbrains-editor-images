#!/usr/bin/env bash
#
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

CONFIG_OPTIONS_DIR="$PROJECTOR_CONFIG_DIR/config/options"

COLORS_SCHEME_XML_PATH="$CONFIG_OPTIONS_DIR/colors.scheme.xml"
read -r -d '' COLORS_SCHEME_XML <<-EOM
<application>
  <component name="EditorColorsManagerImpl">
    <global_color_scheme name="Darcula"/>
  </component>
</application>
EOM

IDE_GENERAL_XML_PATH="$CONFIG_OPTIONS_DIR/ide.general.xml"
read -r -d '' IDE_GENERAL_XML <<-EOM
<application>
  <component name="GeneralSettings">
    <option name="defaultProjectDirectory" value="$PROJECTS_ROOT" />
    <option name="autoSaveIfInactive" value="true" />
    <option name="inactiveTimeout" value="5" />
    <option name="showTipsOnStartup" value="false" />
  </component>
</application>
EOM

LAF_XML_PATH="$CONFIG_OPTIONS_DIR/laf.xml"
read -r -d '' LAF_XML <<-EOM
<application>
  <component name="LafManager">
    <laf class-name="com.intellij.ide.ui.laf.darcula.DarculaLaf"/>
  </component>
</application>
EOM

OTHER_XML_PATH="$CONFIG_OPTIONS_DIR/other.xml"
read -r -d '' OTHER_XML <<-EOM
<application>
  <component name="PropertiesComponent">
    <property name="last_opened_file_path" value="$PROJECTS_ROOT" />
    <property name="terminalCustomCommandExecutionTurnOff" value="false" />
    <property name="ide.memory.adjusted" value="true"/>
  </component>
</application>
EOM

PATH_MACROS_XML_PATH="$CONFIG_OPTIONS_DIR/path.macros.xml"
read -r -d '' PATH_MACROS_XML <<-EOM
<application>
  <component name="PathMacrosImpl">
    <macro name="KOTLIN_BUNDLED" value="$PROJECTOR_ASSEMBLY_DIR/ide/plugins/Kotlin/kotlinc"/>
    <macro name="MAVEN_REPOSITORY" value="$PROJECTOR_CONFIG_DIR/.m2/repository"/>
  </component>
</application>
EOM

TRUSTED_PATHS_XML_PATH="$CONFIG_OPTIONS_DIR/trusted-paths.xml"
read -r -d '' TRUSTED_PATHS_XML <<-EOM
<application>
    <component name="Trusted.Paths.Settings">
        <option name="TRUSTED_PATHS">
            <list>
                <option value="$PROJECTS_ROOT" />
            </list>
        </option>
    </component>
</application>
EOM

UI_INF_XML_PATH="$CONFIG_OPTIONS_DIR/ui.lnf.xml"
read -r -d '' UI_INF_XML <<-EOM
<application>
  <component name="UISettings">
    <option name="smoothScrolling" value="false"/>
  </component>
</application>
EOM

UPDATES_XML_PATH="$CONFIG_OPTIONS_DIR/updates.xml"
read -r -d '' UPDATES_XML <<-EOM
<application>
  <component name="UpdatesConfigurable">
    <option name="CHECK_NEEDED" value="false" />
  </component>
</application>
EOM

WEB_BROWSERS_XML_PATH="$CONFIG_OPTIONS_DIR/web-browsers.xml"
read -r -d '' WEB_BROWSERS_XML <<-EOM
<application>
  <component name="WebBrowsersConfiguration" showHover="false" />
</application>
EOM

createConfigFiles() {
  mkdir -p "$CONFIG_OPTIONS_DIR"

  echo "Creating '$COLORS_SCHEME_XML_PATH'"
  echo "$COLORS_SCHEME_XML" > "$COLORS_SCHEME_XML_PATH"

  echo "Creating '$IDE_GENERAL_XML_PATH'"
  echo "$IDE_GENERAL_XML" > "$IDE_GENERAL_XML_PATH"

  echo "Creating '$LAF_XML_PATH'"
  echo "$LAF_XML" > "$LAF_XML_PATH"

  echo "Creating '$OTHER_XML_PATH'"
  echo "$OTHER_XML" > "$OTHER_XML_PATH"

  echo "Creating '$PATH_MACROS_XML_PATH'"
  echo "$PATH_MACROS_XML" > "$PATH_MACROS_XML_PATH"

  echo "Creating '$TRUSTED_PATHS_XML_PATH'"
  echo "$TRUSTED_PATHS_XML" > "$TRUSTED_PATHS_XML_PATH"

  echo "Creating '$UI_INF_XML_PATH'"
  echo "$UI_INF_XML" > "$UI_INF_XML_PATH"

  echo "Creating '$UPDATES_XML_PATH'"
  echo "$UPDATES_XML" > "$UPDATES_XML_PATH"

  echo "Creating '$WEB_BROWSERS_XML_PATH'"
  echo "$WEB_BROWSERS_XML" > "$WEB_BROWSERS_XML_PATH"
}

# copy default configuration if it doesn't exist
if [ ! -d "$PROJECTOR_CONFIG_DIR" ]; then
  mkdir -p "$PROJECTOR_CONFIG_DIR"
  createConfigFiles
elif [ -z "$(ls -A -- "$PROJECTOR_CONFIG_DIR")" ]; then
  echo "Configuration directory '$PROJECTOR_CONFIG_DIR' is empty."
  createConfigFiles
fi

# overwrite default configuration paths for IDE
cat <<EOT >> "$PROJECTOR_ASSEMBLY_DIR"/ide/bin/idea.properties
idea.config.path=$PROJECTOR_CONFIG_DIR/config
idea.system.path=$PROJECTOR_CONFIG_DIR/caches
idea.plugins.path=$PROJECTOR_CONFIG_DIR/plugins
idea.log.path=$PROJECTOR_CONFIG_DIR/logs
EOT
