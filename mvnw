#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper script
# ----------------------------------------------------------------------------
# This project uses Maven Wrapper to provide a consistent Maven version without
# requiring Maven to be installed on the machine/CI environment.
#
# If .mvn/wrapper/maven-wrapper.jar is missing, this script will download it
# from Maven Central (wrapperUrl from .mvn/wrapper/maven-wrapper.properties).
# ----------------------------------------------------------------------------

set -e

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"

WRAPPER_DIR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

# Resolve Java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="$(command -v java || true)"
fi

if [ -z "$JAVACMD" ]; then
  echo "ERROR: Java not found. Please install Java 21+ or set JAVA_HOME." >&2
  exit 1
fi

# Read wrapperUrl from properties (fallback to a sane default)
WRAPPER_URL_DEFAULT="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
WRAPPER_URL="$WRAPPER_URL_DEFAULT"

if [ -f "$WRAPPER_PROPERTIES" ]; then
  # shellcheck disable=SC2002
  PROP_URL=$(grep -E '^wrapperUrl=' "$WRAPPER_PROPERTIES" 2>/dev/null | sed 's/^wrapperUrl=//')
  if [ -n "$PROP_URL" ]; then
    WRAPPER_URL="$PROP_URL"
  fi
fi

download() {
  URL="$1"
  OUT="$2"

  mkdir -p "$(dirname "$OUT")"

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$OUT"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$OUT"
  else
    echo "ERROR: Neither curl nor wget is available to download Maven Wrapper jar." >&2
    exit 1
  fi
}

# Download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper jar..."
  download "$WRAPPER_URL" "$WRAPPER_JAR"
fi

exec "$JAVACMD"   -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR"   ${MAVEN_OPTS:-}   -classpath "$WRAPPER_JAR"   org.apache.maven.wrapper.MavenWrapperMain "$@"
