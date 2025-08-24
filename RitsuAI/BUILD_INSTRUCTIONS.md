# 🔨 Instrucciones de Compilación - Ritsu AI

Esta guía te ayudará a compilar Ritsu AI desde el código fuente.

## 📋 Prerrequisitos

### Software Requerido
- **Android Studio** 2022.3.1 (Giraffe) o superior
- **JDK 17** o superior
- **Android SDK** con API Level 34
- **Git** para clonar el repositorio

### Configuración del Entorno

1. **Instalar Android Studio:**
   - Descarga desde [developer.android.com](https://developer.android.com/studio)
   - Instala con la configuración por defecto
   - Acepta las licencias del SDK

2. **Configurar SDK:**
   - Abre Android Studio
   - Ve a `File` > `Settings` > `Appearance & Behavior` > `System Settings` > `Android SDK`
   - Instala Android API 34 (Android 14)
   - Instala Android SDK Build-Tools 34.0.0

## 🚀 Compilación

### Método 1: Android Studio (Recomendado)

1. **Clonar el Repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/ritsu-ai.git
   cd ritsu-ai
   ```

2. **Abrir en Android Studio:**
   - Abre Android Studio
   - Selecciona `Open an Existing Project`
   - Navega a la carpeta `RitsuAI` y ábrela

3. **Sincronizar Proyecto:**
   - Android Studio sincronizará automáticamente
   - Si hay errores, ve a `File` > `Sync Project with Gradle Files`

4. **Compilar:**
   - Para debug: `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`
   - Para release: `Build` > `Generate Signed Bundle / APK`

### Método 2: Línea de Comandos

1. **Clonar y Navegar:**
   ```bash
   git clone https://github.com/tu-usuario/ritsu-ai.git
   cd ritsu-ai/RitsuAI
   ```

2. **Compilar Debug:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Compilar Release:**
   ```bash
   ./gradlew assembleRelease
   ```

4. **Ubicación de APKs:**
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

## 🔐 Firma de APK (Para Release)

### Crear Keystore

```bash
keytool -genkey -v -keystore ritsu-ai-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ritsu-ai-key
```

### Configurar Firma en build.gradle

```gradle
android {
    signingConfigs {
        release {
            storeFile file('path/to/ritsu-ai-keystore.jks')
            storePassword 'your-store-password'
            keyAlias 'ritsu-ai-key'
            keyPassword 'your-key-password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 🧪 Testing

### Ejecutar Tests Unitarios
```bash
./gradlew test
```

### Ejecutar Tests de Instrumentación
```bash
./gradlew connectedAndroidTest
```

### Ejecutar Lint
```bash
./gradlew lint
```

## 📦 Distribución

### Crear Release en GitHub

1. **Tag la Versión:**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. **Crear Release:**
   - Ve a GitHub > Releases > New Release
   - Selecciona el tag creado
   - Sube el APK firmado
   - Incluye notas de la versión

### Generar Checksums

```bash
# SHA256
sha256sum app-release.apk > app-release.apk.sha256

# MD5
md5sum app-release.apk > app-release.apk.md5
```

## 🔧 Configuración de Desarrollo

### Variables de Entorno

Crea un archivo `local.properties` en la raíz del proyecto:

```properties
sdk.dir=/path/to/Android/Sdk
ndk.dir=/path/to/Android/Sdk/ndk/version

# API Keys (si las necesitas)
RITSU_API_KEY=your-api-key-here
GITHUB_TOKEN=your-github-token
```

### Configuración de Debug

Para habilitar logs de debug, agrega en `build.gradle`:

```gradle
android {
    buildTypes {
        debug {
            buildConfigField "boolean", "DEBUG_MODE", "true"
            buildConfigField "String", "LOG_LEVEL", "\"VERBOSE\""
        }
        release {
            buildConfigField "boolean", "DEBUG_MODE", "false"
            buildConfigField "String", "LOG_LEVEL", "\"ERROR\""
        }
    }
}
```

## 🚨 Solución de Problemas

### Error: "SDK location not found"
```bash
# Crear local.properties con la ruta correcta del SDK
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
```

### Error: "Gradle sync failed"
```bash
# Limpiar y reconstruir
./gradlew clean
./gradlew build
```

### Error: "Out of memory"
```bash
# Aumentar memoria de Gradle en gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### Error de Permisos en Linux/Mac
```bash
chmod +x gradlew
```

## 📊 Métricas de Build

### Tamaño del APK
- Debug: ~15-20 MB
- Release (con ProGuard): ~8-12 MB
- Release (con R8): ~6-10 MB

### Tiempo de Compilación
- Primera compilación: 3-5 minutos
- Compilaciones incrementales: 30-60 segundos
- Clean build: 2-3 minutos

## 🔄 CI/CD (GitHub Actions)

Ejemplo de workflow para compilación automática:

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      working-directory: ./RitsuAI
      
    - name: Build with Gradle
      run: ./gradlew assembleRelease
      working-directory: ./RitsuAI
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: ritsu-ai-apk
        path: RitsuAI/app/build/outputs/apk/release/app-release.apk
```

## 📝 Notas de Desarrollo

### Estructura de Branches
- `main` - Código estable para releases
- `develop` - Desarrollo activo
- `feature/*` - Nuevas características
- `hotfix/*` - Correcciones urgentes

### Convenciones de Código
- Usar Kotlin para todo el código nuevo
- Seguir las guías de estilo de Android
- Documentar funciones públicas
- Escribir tests para lógica crítica

### Versionado
- Seguir Semantic Versioning (MAJOR.MINOR.PATCH)
- Incrementar versionCode en cada build
- Actualizar versionName en releases

---

**¡Happy Coding! 🚀**

*"La programación es como el arte: requiere paciencia, creatividad y mucha dedicación."* - Ritsu AI

