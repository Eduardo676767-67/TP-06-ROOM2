#!/usr/bin/env sh
# Bootstrap simples do Gradle 8.9 para máquinas sem Gradle instalado.
# O pacote oficial é baixado diretamente de services.gradle.org.
set -eu
VERSION="8.9"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/approomproduto-wrapper"
GRADLE_HOME="$CACHE_DIR/gradle-$VERSION"
ZIP_PATH="$CACHE_DIR/gradle-$VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    mkdir -p "$CACHE_DIR"
    if [ ! -f "$ZIP_PATH" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP_PATH"
        elif command -v wget >/dev/null 2>&1; then
            wget -O "$ZIP_PATH" "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
        else
            echo "Erro: instale curl ou wget para baixar o Gradle." >&2
            exit 1
        fi
    fi
    if ! command -v unzip >/dev/null 2>&1; then
        echo "Erro: instale unzip para extrair o Gradle." >&2
        exit 1
    fi
    unzip -q -o "$ZIP_PATH" -d "$CACHE_DIR"
fi
exec "$GRADLE_HOME/bin/gradle" "$@"
