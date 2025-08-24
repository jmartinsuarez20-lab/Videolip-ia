# 📱 Guía Completa de Instalación - Ritsu AI

Esta guía te llevará paso a paso para instalar y configurar correctamente Ritsu AI en tu dispositivo Android.

## 🔍 Verificación de Compatibilidad

Antes de instalar, verifica que tu dispositivo cumple con los requisitos:

### Requisitos Mínimos
- ✅ Android 8.0 (API 26) o superior
- ✅ 2GB de RAM
- ✅ 500MB de espacio libre
- ✅ Conexión a internet estable

### Requisitos Recomendados
- 🌟 Android 10.0 o superior
- 🌟 4GB de RAM o más
- 🌟 1GB de espacio libre
- 🌟 Procesador de 8 núcleos

## 📥 Descarga e Instalación

### Paso 1: Descargar el APK

**Opción A: Desde GitHub Releases (Recomendado)**
1. Ve a [GitHub Releases](https://github.com/tu-usuario/ritsu-ai/releases)
2. Descarga la última versión: `ritsu-ai-v1.0.0.apk`
3. Verifica el checksum SHA256 para seguridad

**Opción B: Compilar desde Código Fuente**
```bash
git clone https://github.com/tu-usuario/ritsu-ai.git
cd ritsu-ai
./gradlew assembleRelease
```

### Paso 2: Habilitar Instalación de APKs

**Android 8.0 - 9.0:**
1. Ve a `Configuración` > `Seguridad`
2. Activa `Fuentes desconocidas`

**Android 10.0+:**
1. Ve a `Configuración` > `Apps y notificaciones` > `Acceso especial a apps`
2. Selecciona `Instalar apps desconocidas`
3. Elige tu navegador/gestor de archivos
4. Activa `Permitir desde esta fuente`

### Paso 3: Instalar el APK

1. Localiza el archivo `ritsu-ai-v1.0.0.apk` descargado
2. Toca el archivo para iniciar la instalación
3. Si aparece una advertencia de seguridad, selecciona `Instalar de todas formas`
4. Espera a que termine la instalación
5. Toca `Abrir` o busca "Ritsu AI" en tu lista de apps

## ⚙️ Configuración de Permisos

Esta es la parte **MÁS IMPORTANTE** de la instalación. Ritsu necesita permisos especiales para funcionar correctamente.

### Paso 1: Permisos Básicos

Al abrir Ritsu por primera vez, se solicitarán estos permisos:

1. **📞 Teléfono** - Para hacer llamadas por ti
   - Toca `Permitir` cuando se solicite

2. **💬 SMS** - Para enviar mensajes
   - Toca `Permitir` cuando se solicite

3. **👥 Contactos** - Para acceder a tu lista de contactos
   - Toca `Permitir` cuando se solicite

4. **🎤 Micrófono** - Para reconocimiento de voz
   - Toca `Permitir` cuando se solicite

5. **📁 Archivos** - Para gestionar archivos
   - Toca `Permitir` cuando se solicite

### Paso 2: Permiso de Overlay (CRÍTICO)

Este permiso permite que Ritsu aparezca sobre otras aplicaciones:

1. Ritsu te redirigirá automáticamente a la configuración
2. Si no, ve manualmente a:
   - `Configuración` > `Apps` > `Permisos especiales` > `Mostrar sobre otras apps`
3. Busca `Ritsu AI` en la lista
4. Toca el interruptor para **ACTIVARLO**
5. Confirma con `Permitir`

**⚠️ Sin este permiso, Ritsu NO podrá aparecer como avatar flotante**

### Paso 3: Servicio de Accesibilidad (ESENCIAL)

Este es el permiso más importante - permite que Ritsu controle tu teléfono:

1. Ve a `Configuración` > `Accesibilidad`
2. Busca `Ritsu AI` en la lista de servicios
3. Toca `Ritsu AI`
4. Activa el interruptor en la parte superior
5. Lee la advertencia y toca `Aceptar`

**Descripción del servicio:**
"Permite a Ritsu AI controlar aplicaciones y realizar tareas automáticas en tu teléfono. Ritsu puede abrir apps, enviar mensajes, hacer llamadas y mucho más para ayudarte en tu día a día."

**🚨 IMPORTANTE: Sin este permiso, Ritsu NO podrá controlar aplicaciones**

### Paso 4: Permisos Adicionales (Opcionales pero Recomendados)

**Notificaciones:**
- Ve a `Configuración` > `Apps` > `Ritsu AI` > `Notificaciones`
- Activa todas las categorías de notificaciones

**Batería (Importante):**
- Ve a `Configuración` > `Batería` > `Optimización de batería`
- Busca `Ritsu AI` y selecciona `No optimizar`
- Esto evita que Android cierre Ritsu en segundo plano

**Inicio Automático:**
- Ve a `Configuración` > `Apps` > `Ritsu AI` > `Permisos`
- Activa `Inicio automático` si está disponible

## 🎉 Primera Configuración

### Paso 1: Abrir Ritsu AI

1. Abre la aplicación `Ritsu AI`
2. Verás la pantalla principal con el avatar de Ritsu
3. El estado debería mostrar: "✅ Ritsu está completamente configurada"

### Paso 2: Primer Contacto

1. En el campo de texto, escribe: `Hola Ritsu`
2. Toca `Enviar`
3. Ritsu debería responder con su presentación
4. Si tienes audio activado, escucharás su voz kawaii

### Paso 3: Activar Avatar Flotante

1. Ritsu debería aparecer automáticamente como avatar flotante
2. Si no aparece, verifica el permiso de overlay
3. Puedes mover el avatar arrastrándolo por la pantalla
4. Doble toque para minimizar/maximizar

### Paso 4: Probar Funcionalidades

**Prueba de Control de Teléfono:**
```
"Abre WhatsApp"
"Busca gatos en Google"
"Abre la configuración"
```

**Prueba de Cambio de Ropa:**
```
"Ponte un vestido elegante"
"Cambia a ropa casual"
"Quiero verte con uniforme escolar"
```

**Prueba de Comunicación:**
```
"¿Cómo estás?"
"Cuéntame sobre ti"
"¿Qué puedes hacer?"
```

## 🔧 Solución de Problemas

### Problema: Ritsu no aparece como avatar flotante

**Solución:**
1. Verifica el permiso de overlay en Configuración
2. Reinicia la aplicación
3. Ve a Configuración > Apps > Ritsu AI > Forzar detención
4. Abre Ritsu AI nuevamente

### Problema: Ritsu no puede controlar aplicaciones

**Solución:**
1. Verifica el servicio de accesibilidad
2. Ve a Configuración > Accesibilidad > Ritsu AI
3. Desactiva y vuelve a activar el servicio
4. Reinicia el teléfono si es necesario

### Problema: Ritsu no habla

**Solución:**
1. Verifica que el volumen multimedia esté activado
2. Ve a Configuración > Idioma y entrada > Síntesis de voz
3. Asegúrate de que hay un motor TTS instalado
4. Prueba con "Hola Ritsu, habla conmigo"

### Problema: La aplicación se cierra sola

**Solución:**
1. Desactiva la optimización de batería para Ritsu AI
2. Ve a Configuración > Batería > Optimización de batería
3. Busca Ritsu AI y selecciona "No optimizar"
4. Activa el inicio automático si está disponible

### Problema: Ritsu no responde a comandos

**Solución:**
1. Verifica la conexión a internet
2. Reinicia la aplicación
3. Borra caché: Configuración > Apps > Ritsu AI > Almacenamiento > Borrar caché
4. Si persiste, reinstala la aplicación

## 🔐 Modo Especial

Para desbloquear las funcionalidades especiales de Ritsu:

1. En el chat, escribe exactamente: `262456`
2. Ritsu confirmará que el modo especial está desbloqueado
3. Ahora puedes usar comandos especiales de cambio de ropa
4. **Nota:** Este modo es solo para entretenimiento

## 📊 Verificación de Estado

Para verificar que todo funciona correctamente:

1. **Estado de la App:** Debe mostrar "✅ Ritsu está completamente configurada"
2. **Avatar Flotante:** Debe ser visible sobre otras apps
3. **Respuesta a Comandos:** Debe responder a "Hola Ritsu"
4. **Control de Apps:** Debe poder abrir aplicaciones
5. **Síntesis de Voz:** Debe hablar cuando se lo solicites

## 🆘 Soporte

Si sigues teniendo problemas:

1. **Revisa los logs:** Ve a Configuración > Apps > Ritsu AI > Almacenamiento > Ver logs
2. **Reporta el problema:** [GitHub Issues](https://github.com/tu-usuario/ritsu-ai/issues)
3. **Únete al Discord:** [Servidor de la Comunidad](https://discord.gg/ritsu-ai)
4. **Email de soporte:** support@ritsu-ai.com

## ✅ Lista de Verificación Final

Antes de considerar la instalación completa, verifica:

- [ ] APK instalado correctamente
- [ ] Todos los permisos básicos concedidos
- [ ] Permiso de overlay activado
- [ ] Servicio de accesibilidad activado
- [ ] Optimización de batería desactivada
- [ ] Avatar flotante visible
- [ ] Ritsu responde a comandos
- [ ] Síntesis de voz funcionando
- [ ] Control de aplicaciones operativo

**¡Felicidades! Ritsu AI está lista para ser tu compañera virtual perfecta! 🎌✨**

---

*"Configuración completada exitosamente. Ahora puedo ayudarte con todo lo que necesites en tu teléfono."* - Ritsu AI

