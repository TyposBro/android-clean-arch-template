#!/bin/bash
#
# Template Setup Script
# Renames the package, app name, and directory structure for your project.
#
# Usage: ./setup.sh com.mycompany.myapp "My App Name"
#

set -e

if [ $# -lt 2 ]; then
    echo "Usage: ./setup.sh <package.name> <App Name>"
    echo "Example: ./setup.sh com.mycompany.coolapp \"Cool App\""
    exit 1
fi

NEW_PACKAGE="$1"
NEW_APP_NAME="$2"
OLD_PACKAGE="com.example.app"
OLD_APP_NAME="MyApp"

if [ "$NEW_PACKAGE" = "$OLD_PACKAGE" ]; then
    echo "Error: New package is the same as the template package."
    exit 1
fi

# Convert package to path (com.example.app -> com/example/app)
OLD_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')
NEW_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

echo "=== Android Clean Architecture Template Setup ==="
echo ""
echo "  Old package: $OLD_PACKAGE"
echo "  New package: $NEW_PACKAGE"
echo "  App name:    $NEW_APP_NAME"
echo ""

# 1. Rename directory structure FIRST (before sed, so paths exist)
echo "[1/6] Restructuring source directories..."
for SRC_SET in main test androidTest; do
    SRC_DIR="app/src/$SRC_SET/java"
    if [ -d "$SRC_DIR/$OLD_PATH" ]; then
        mkdir -p "$SRC_DIR/$NEW_PATH"
        # Move contents to new path
        cp -R "$SRC_DIR/$OLD_PATH/"* "$SRC_DIR/$NEW_PATH/" 2>/dev/null || true
        # Remove only the old leaf directory, then prune empty parents
        rm -rf "$SRC_DIR/$OLD_PATH"
        # Clean up empty parent dirs (e.g., com/example/ if now empty)
        find "$SRC_DIR" -type d -empty -delete 2>/dev/null || true
    fi
done

# 2. Rename package in all Kotlin files
echo "[2/6] Renaming package in Kotlin files..."
find app/src -name "*.kt" -exec sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} +

# 3. Rename package in Gradle files
echo "[3/6] Updating Gradle configuration..."
sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" app/build.gradle.kts
sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" app/proguard-rules.pro

# 4. Rename package in XML files
echo "[4/6] Updating Android resources..."
find app/src -name "*.xml" -exec sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} +

# 5. Update app name
echo "[5/6] Updating app name..."
sed -i '' "s|$OLD_APP_NAME|$NEW_APP_NAME|g" app/src/main/res/values/strings.xml
sed -i '' "s|$OLD_APP_NAME|$NEW_APP_NAME|g" settings.gradle.kts

# 6. Update theme name in XML
echo "[6/6] Updating theme references..."
NEW_THEME_NAME=$(echo "$NEW_APP_NAME" | tr -d ' ')
sed -i '' "s/Theme.MyApp/Theme.$NEW_THEME_NAME/g" app/src/main/AndroidManifest.xml
sed -i '' "s/Theme.MyApp/Theme.$NEW_THEME_NAME/g" app/src/main/res/values/themes.xml
sed -i '' "s/Theme.MyApp/Theme.$NEW_THEME_NAME/g" app/src/main/res/values-night/themes.xml

echo ""
echo "=== Setup complete! ==="
echo ""
echo "Next steps:"
echo "  1. Open the project in Android Studio"
echo "  2. Update BASE_URL in app/build.gradle.kts"
echo "  3. Customize colors in core/ui/theme/Color.kt"
echo "  4. Run: ./gradlew assembleDebug"
echo ""
