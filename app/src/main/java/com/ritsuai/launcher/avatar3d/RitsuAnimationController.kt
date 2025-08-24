package com.ritsuai.launcher.avatar3d

import io.github.sceneview.node.ModelNode

/**
 * Controlador de animaciones para el avatar 3D de Ritsu.
 * Gestiona la reproducción de animaciones y transiciones entre ellas.
 */
class RitsuAnimationController {

    // Animación actual
    private var currentAnimation: String? = null
    
    // Mapa de animaciones disponibles
    private val animations = mapOf(
        "idle" to AnimationInfo("idle", true, 1.0f),
        "talk" to AnimationInfo("talk", true, 1.2f),
        "walk" to AnimationInfo("walk", true, 1.0f),
        "run" to AnimationInfo("run", true, 1.5f),
        "jump" to AnimationInfo("jump", false, 1.0f),
        "wave" to AnimationInfo("wave", false, 1.0f),
        "happy" to AnimationInfo("happy", false, 1.2f),
        "sad" to AnimationInfo("sad", false, 0.8f),
        "angry" to AnimationInfo("angry", false, 1.3f),
        "surprised" to AnimationInfo("surprised", false, 1.5f),
        "thinking" to AnimationInfo("thinking", true, 0.7f),
        "dance" to AnimationInfo("dance", true, 1.0f)
    )
    
    /**
     * Reproduce una animación en el modelo
     *
     * @param modelNode Nodo del modelo 3D
     * @param animationName Nombre de la animación a reproducir
     * @param transitionDuration Duración de la transición en segundos
     * @return true si la animación se reprodujo correctamente, false en caso contrario
     */
    fun playAnimation(modelNode: ModelNode?, animationName: String, transitionDuration: Float = 0.5f): Boolean {
        if (modelNode == null) return false
        
        // Obtener información de la animación
        val animInfo = animations[animationName] ?: return false
        
        try {
            // Reproducir la animación
            modelNode.animator?.let { animator ->
                // Verificar si la animación existe
                if (animator.getAnimationNames().contains(animInfo.name)) {
                    // Reproducir la animación
                    animator.playAnimation(
                        animationName = animInfo.name,
                        loop = animInfo.loop,
                        transitionDuration = transitionDuration,
                        speed = animInfo.speed
                    )
                    
                    // Actualizar animación actual
                    currentAnimation = animationName
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return false
    }
    
    /**
     * Detiene la animación actual
     *
     * @param modelNode Nodo del modelo 3D
     * @param transitionDuration Duración de la transición en segundos
     */
    fun stopAnimation(modelNode: ModelNode?, transitionDuration: Float = 0.5f) {
        if (modelNode == null) return
        
        try {
            modelNode.animator?.stopAllAnimations(transitionDuration)
            currentAnimation = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Obtiene el nombre de la animación actual
     *
     * @return Nombre de la animación actual o null si no hay ninguna
     */
    fun getCurrentAnimation(): String? {
        return currentAnimation
    }
    
    /**
     * Verifica si una animación está disponible
     *
     * @param animationName Nombre de la animación
     * @return true si la animación está disponible, false en caso contrario
     */
    fun isAnimationAvailable(animationName: String): Boolean {
        return animations.containsKey(animationName)
    }
    
    /**
     * Clase interna para almacenar información de animaciones
     */
    data class AnimationInfo(
        val name: String,
        val loop: Boolean,
        val speed: Float
    )
}

