package com.ritsuai.launcher.avatar3d

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Motor de física para el avatar de Ritsu.
 * Gestiona la física del avatar para movimientos realistas.
 */
class RitsuPhysicsEngine {

    // Velocidad actual
    private var velocityX = 0f
    private var velocityY = 0f
    
    // Aceleración
    private var accelerationX = 0f
    private var accelerationY = 0f
    
    // Masa del avatar
    private val mass = 1f
    
    // Coeficientes
    private val friction = 0.95f
    private val elasticity = 0.8f
    private val gravity = 0.5f
    
    // Estado
    private var isDragging = false
    
    /**
     * Inicia el arrastre del avatar
     */
    fun startDrag() {
        isDragging = true
        
        // Resetear velocidad
        velocityX = 0f
        velocityY = 0f
    }
    
    /**
     * Actualiza el arrastre del avatar
     */
    fun updateDrag(dx: Float, dy: Float) {
        if (!isDragging) return
        
        // Actualizar velocidad
        velocityX = dx * 0.1f
        velocityY = dy * 0.1f
    }
    
    /**
     * Finaliza el arrastre del avatar
     */
    fun endDrag(finalVelocityX: Float, finalVelocityY: Float) {
        isDragging = false
        
        // Establecer velocidad final
        velocityX = finalVelocityX * 0.1f
        velocityY = finalVelocityY * 0.1f
        
        // Limitar velocidad máxima
        velocityX = max(-50f, min(50f, velocityX))
        velocityY = max(-50f, min(50f, velocityY))
    }
    
    /**
     * Actualiza la física del avatar
     */
    fun update(deltaTime: Float, screenWidth: Int, screenHeight: Int, avatarWidth: Int, avatarHeight: Int, x: Int, y: Int): Pair<Int, Int> {
        if (isDragging) {
            return Pair(x, y)
        }
        
        // Aplicar gravedad
        accelerationY = gravity
        
        // Actualizar velocidad
        velocityX += accelerationX * deltaTime
        velocityY += accelerationY * deltaTime
        
        // Aplicar fricción
        velocityX *= friction
        velocityY *= friction
        
        // Detener si la velocidad es muy baja
        if (abs(velocityX) < 0.1f) velocityX = 0f
        if (abs(velocityY) < 0.1f) velocityY = 0f
        
        // Calcular nueva posición
        var newX = x + (velocityX * deltaTime).toInt()
        var newY = y + (velocityY * deltaTime).toInt()
        
        // Comprobar colisiones con los bordes de la pantalla
        if (newX < 0) {
            newX = 0
            velocityX = -velocityX * elasticity
        } else if (newX > screenWidth - avatarWidth) {
            newX = screenWidth - avatarWidth
            velocityX = -velocityX * elasticity
        }
        
        if (newY < 0) {
            newY = 0
            velocityY = -velocityY * elasticity
        } else if (newY > screenHeight - avatarHeight) {
            newY = screenHeight - avatarHeight
            velocityY = -velocityY * elasticity
        }
        
        return Pair(newX, newY)
    }
    
    /**
     * Aplica una fuerza al avatar
     */
    fun applyForce(forceX: Float, forceY: Float) {
        accelerationX += forceX / mass
        accelerationY += forceY / mass
    }
    
    /**
     * Obtiene la velocidad actual
     */
    fun getVelocity(): Pair<Float, Float> {
        return Pair(velocityX, velocityY)
    }
    
    /**
     * Establece la velocidad
     */
    fun setVelocity(vx: Float, vy: Float) {
        velocityX = vx
        velocityY = vy
    }
    
    /**
     * Resetea la física
     */
    fun reset() {
        velocityX = 0f
        velocityY = 0f
        accelerationX = 0f
        accelerationY = 0f
        isDragging = false
    }
}

