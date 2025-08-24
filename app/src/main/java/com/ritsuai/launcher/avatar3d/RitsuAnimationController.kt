package com.ritsuai.launcher.avatar3d

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.ritsuai.launcher.R

/**
 * Controlador de animaciones para el avatar de Ritsu.
 * Gestiona las diferentes animaciones y expresiones del avatar.
 */
class RitsuAnimationController(private val avatarImageView: ImageView) {

    // Animación actual
    private var currentAnimator: AnimatorSet? = null
    
    // Estado actual
    private var currentState = "idle"
    
    /**
     * Reproduce la animación de idle (reposo)
     */
    fun playIdleAnimation() {
        if (currentState == "idle") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de idle
        avatarImageView.setImageResource(R.drawable.avatar_idle)
        
        // Crear animación de flotación suave
        val floatUp = ObjectAnimator.ofFloat(avatarImageView, "translationY", 0f, -10f)
        floatUp.duration = 1500
        floatUp.interpolator = AccelerateDecelerateInterpolator()
        
        val floatDown = ObjectAnimator.ofFloat(avatarImageView, "translationY", -10f, 0f)
        floatDown.duration = 1500
        floatDown.interpolator = AccelerateDecelerateInterpolator()
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(floatUp, floatDown)
        animatorSet.repeatCount = ValueAnimator.INFINITE
        
        // Iniciar animación
        animatorSet.start()
        
        // Actualizar estado
        currentAnimator = animatorSet
        currentState = "idle"
    }
    
    /**
     * Reproduce la animación de hablar
     */
    fun playSpeakingAnimation() {
        if (currentState == "speaking") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de hablar
        avatarImageView.setImageResource(R.drawable.avatar_speaking)
        
        // Crear animación de parpadeo
        val scaleX = ObjectAnimator.ofFloat(avatarImageView, "scaleX", 1f, 1.05f)
        scaleX.duration = 300
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleX.interpolator = LinearInterpolator()
        
        val scaleY = ObjectAnimator.ofFloat(avatarImageView, "scaleY", 1f, 1.05f)
        scaleY.duration = 300
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatMode = ValueAnimator.REVERSE
        scaleY.interpolator = LinearInterpolator()
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        
        // Iniciar animación
        animatorSet.start()
        
        // Actualizar estado
        currentAnimator = animatorSet
        currentState = "speaking"
    }
    
    /**
     * Reproduce la animación de felicidad
     */
    fun playHappyAnimation() {
        if (currentState == "happy") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de felicidad
        avatarImageView.setImageResource(R.drawable.avatar_happy)
        
        // Crear animación de rebote
        val bounceUp = ObjectAnimator.ofFloat(avatarImageView, "translationY", 0f, -20f)
        bounceUp.duration = 300
        bounceUp.interpolator = AccelerateDecelerateInterpolator()
        
        val bounceDown = ObjectAnimator.ofFloat(avatarImageView, "translationY", -20f, 0f)
        bounceDown.duration = 300
        bounceDown.interpolator = AccelerateDecelerateInterpolator()
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(bounceUp, bounceDown)
        animatorSet.repeatCount = 3
        
        // Iniciar animación
        animatorSet.start()
        
        // Actualizar estado
        currentAnimator = animatorSet
        currentState = "happy"
    }
    
    /**
     * Reproduce la animación de tristeza
     */
    fun playSadAnimation() {
        if (currentState == "sad") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de tristeza
        avatarImageView.setImageResource(R.drawable.avatar_sad)
        
        // Crear animación de caída
        val dropDown = ObjectAnimator.ofFloat(avatarImageView, "translationY", 0f, 10f)
        dropDown.duration = 1000
        dropDown.interpolator = AccelerateDecelerateInterpolator()
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.play(dropDown)
        
        // Iniciar animación
        animatorSet.start()
        
        // Actualizar estado
        currentAnimator = animatorSet
        currentState = "sad"
    }
    
    /**
     * Reproduce la animación de enojo
     */
    fun playAngryAnimation() {
        if (currentState == "angry") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de enojo
        avatarImageView.setImageResource(R.drawable.avatar_angry)
        
        // Crear animación de vibración
        val shakeLeft = ObjectAnimator.ofFloat(avatarImageView, "translationX", 0f, -5f)
        shakeLeft.duration = 100
        
        val shakeRight = ObjectAnimator.ofFloat(avatarImageView, "translationX", -5f, 5f)
        shakeRight.duration = 100
        
        val shakeCenter = ObjectAnimator.ofFloat(avatarImageView, "translationX", 5f, 0f)
        shakeCenter.duration = 100
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(shakeLeft, shakeRight, shakeCenter)
        animatorSet.repeatCount = 3
        
        // Iniciar animación
        animatorSet.start()
        
        // Actualizar estado
        currentAnimator = animatorSet
        currentState = "angry"
    }
    
    /**
     * Reproduce la animación de relajación
     */
    fun playRelaxedAnimation() {
        if (currentState == "relaxed") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de relajación
        avatarImageView.setImageResource(R.drawable.avatar_relaxed)
        
        // Crear animación de respiración
        val breatheIn = ObjectAnimator.ofFloat(avatarImageView, "scaleX", 1f, 1.05f)
        breatheIn.duration = 2000
        breatheIn.interpolator = AccelerateDecelerateInterpolator()
        
        val breatheOut = ObjectAnimator.ofFloat(avatarImageView, "scaleX", 1.05f, 1f)
        breatheOut.duration = 2000
        breatheOut.interpolator = AccelerateDecelerateInterpolator()
        
        val breatheInY = ObjectAnimator.ofFloat(avatarImageView, "scaleY", 1f, 1.05f)
        breatheInY.duration = 2000
        breatheInY.interpolator = AccelerateDecelerateInterpolator()
        
        val breatheOutY = ObjectAnimator.ofFloat(avatarImageView, "scaleY", 1.05f, 1f)
        breatheOutY.duration = 2000
        breatheOutY.interpolator = AccelerateDecelerateInterpolator()
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(avatarImageView, "alpha", 1f, 0.9f, 1f).apply {
                duration = 4000
                repeatCount = ValueAnimator.INFINITE
            }
        )
        
        val breatheSet = AnimatorSet()
        breatheSet.playTogether(
            ObjectAnimator.ofFloat(avatarImageView, "scaleX", 1f, 1.05f, 1f).apply {
                duration = 4000
                repeatCount = ValueAnimator.INFINITE
            },
            ObjectAnimator.ofFloat(avatarImageView, "scaleY", 1f, 1.05f, 1f).apply {
                duration = 4000
                repeatCount = ValueAnimator.INFINITE
            }
        )
        
        val finalSet = AnimatorSet()
        finalSet.playTogether(animatorSet, breatheSet)
        
        // Iniciar animación
        finalSet.start()
        
        // Actualizar estado
        currentAnimator = finalSet
        currentState = "relaxed"
    }
    
    /**
     * Reproduce la animación de somnolencia
     */
    fun playSleepyAnimation() {
        if (currentState == "sleepy") return
        
        // Detener animación actual
        stopCurrentAnimation()
        
        // Establecer imagen de somnolencia
        avatarImageView.setImageResource(R.drawable.avatar_sleepy)
        
        // Crear animación de cabeceo
        val nodDown = ObjectAnimator.ofFloat(avatarImageView, "rotation", 0f, 5f)
        nodDown.duration = 1000
        nodDown.interpolator = AccelerateDecelerateInterpolator()
        
        val nodUp = ObjectAnimator.ofFloat(avatarImageView, "rotation", 5f, 0f)
        nodUp.duration = 500
        nodUp.interpolator = AccelerateDecelerateInterpolator()
        
        // Crear conjunto de animación
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(nodDown, nodUp)
        animatorSet.startDelay = 2000
        animatorSet.repeatCount = ValueAnimator.INFINITE
        
        // Iniciar animación
        animatorSet.start()
        
        // Actualizar estado
        currentAnimator = animatorSet
        currentState = "sleepy"
    }
    
    /**
     * Detiene la animación actual
     */
    private fun stopCurrentAnimation() {
        currentAnimator?.cancel()
        currentAnimator = null
        
        // Resetear propiedades
        avatarImageView.translationY = 0f
        avatarImageView.translationX = 0f
        avatarImageView.scaleX = 1f
        avatarImageView.scaleY = 1f
        avatarImageView.rotation = 0f
        avatarImageView.alpha = 1f
    }
}

