# Ritsu AI

Asistente personal con capacidad de evolución inspirado en Ritsu de Assassination Classroom.

## Características

- **Avatar Anime Kawaii**: Avatar de cuerpo completo que flota en la pantalla
- **Control Total del Dispositivo**: Capacidad para controlar aplicaciones y funciones del teléfono
- **Auto-aprendizaje**: Sistema de evolución que mejora con el uso
- **Personalización**: Cambio de apariencia y comportamiento
- **Actualizaciones Automáticas**: Sistema de actualización desde GitHub

## Requisitos

- Android 7.0 (API 24) o superior
- Permisos de accesibilidad
- Permisos de superposición de pantalla
- Permisos de almacenamiento
- Permisos de instalación de paquetes

## Compilación

### Opción 1: Android Studio

1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Sincronizar Gradle
4. Compilar el proyecto (Build > Build Bundle(s) / APK(s) > Build APK(s))

### Opción 2: Línea de comandos

```bash
# Clonar el repositorio
git clone https://github.com/jmartinsuarez20-lab/Videolip-ia.git

# Navegar al directorio del proyecto
cd Videolip-ia/RitsuAI

# Dar permisos de ejecución a Gradle
chmod +x gradlew

# Compilar
./gradlew assembleDebug
```

El APK resultante se encontrará en `app/build/outputs/apk/debug/app-debug.apk`

### Opción 3: Compilación en la nube

El proyecto incluye configuraciones para servicios de compilación en la nube:

- **AppCenter**: Usar el archivo `cloud-build.yml`
- **Bitrise**: Usar el archivo `cloud-build.yml`

## Instalación

1. Descargar el APK
2. Habilitar "Fuentes desconocidas" en la configuración de seguridad
3. Instalar el APK
4. Seguir las instrucciones en pantalla para conceder los permisos necesarios

## Uso

1. Iniciar Ritsu desde la aplicación principal
2. El avatar aparecerá flotando en la pantalla
3. Tocar el avatar para interactuar
4. Usar comandos de voz o texto para controlar Ritsu

## Modo Especial

Para activar el modo especial, introducir el código: `262456`

## Licencia

Este proyecto es de código abierto bajo la licencia MIT.

