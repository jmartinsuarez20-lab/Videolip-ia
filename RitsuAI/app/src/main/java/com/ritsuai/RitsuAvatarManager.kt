package com.ritsuai

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Gestor del avatar kawaii de Ritsu con cuerpo completo
 * Maneja animaciones, expresiones y cambios de ropa
 */
class RitsuAvatarManager(private val context: Context) {
    
    private var currentOutfit = ClothingOutfit.getDefaultOutfit()
    private var currentExpression = RitsuExpression.NORMAL
    private var isSpecialModeUnlocked = false
    private var currentPose = RitsuPose.STANDING
    
    // Configuración del avatar
    private val avatarConfig = AvatarConfig(
        height = 400f,
        width = 200f,
        skinTone = Color.parseColor("#FFDBAC"),
        hairColor = Color.parseColor("#8B4513"),
        eyeColor = Color.parseColor("#4169E1")
    )
    
    /**
     * Genera el bitmap del avatar completo con la ropa actual
     */
    fun generateAvatarBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            avatarConfig.width.toInt(),
            avatarConfig.height.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        
        // Dibujar en orden: cuerpo, ropa, cara, expresión
        drawBody(canvas)
        drawClothing(canvas, currentOutfit)
        drawFace(canvas, currentExpression)
        drawHair(canvas)
        drawAccessories(canvas, currentOutfit)
        
        return bitmap
    }
    
    private fun drawBody(canvas: Canvas) {
        val paint = Paint().apply {
            color = avatarConfig.skinTone
            isAntiAlias = true
        }
        
        // Cabeza (círculo)
        canvas.drawCircle(
            avatarConfig.width / 2,
            60f,
            40f,
            paint
        )
        
        // Cuello
        canvas.drawRect(
            avatarConfig.width / 2 - 10,
            100f,
            avatarConfig.width / 2 + 10,
            120f,
            paint
        )
        
        // Torso
        val torsoPath = Path().apply {
            moveTo(avatarConfig.width / 2 - 40, 120f)
            lineTo(avatarConfig.width / 2 + 40, 120f)
            lineTo(avatarConfig.width / 2 + 35, 250f)
            lineTo(avatarConfig.width / 2 - 35, 250f)
            close()
        }
        canvas.drawPath(torsoPath, paint)
        
        // Brazos
        drawArm(canvas, true) // Brazo izquierdo
        drawArm(canvas, false) // Brazo derecho
        
        // Piernas
        drawLeg(canvas, true) // Pierna izquierda
        drawLeg(canvas, false) // Pierna derecha
    }
    
    private fun drawArm(canvas: Canvas, isLeft: Boolean) {
        val paint = Paint().apply {
            color = avatarConfig.skinTone
            isAntiAlias = true
        }
        
        val xOffset = if (isLeft) -45f else 45f
        val centerX = avatarConfig.width / 2 + xOffset
        
        // Brazo superior
        canvas.drawRect(
            centerX - 8,
            130f,
            centerX + 8,
            180f,
            paint
        )
        
        // Brazo inferior
        canvas.drawRect(
            centerX - 6,
            180f,
            centerX + 6,
            220f,
            paint
        )
        
        // Mano
        canvas.drawCircle(centerX, 225f, 8f, paint)
    }
    
    private fun drawLeg(canvas: Canvas, isLeft: Boolean) {
        val paint = Paint().apply {
            color = avatarConfig.skinTone
            isAntiAlias = true
        }
        
        val xOffset = if (isLeft) -15f else 15f
        val centerX = avatarConfig.width / 2 + xOffset
        
        // Muslo
        canvas.drawRect(
            centerX - 10,
            250f,
            centerX + 10,
            320f,
            paint
        )
        
        // Pantorrilla
        canvas.drawRect(
            centerX - 8,
            320f,
            centerX + 8,
            380f,
            paint
        )
        
        // Pie
        canvas.drawOval(
            centerX - 12,
            380f,
            centerX + 12,
            395f,
            paint
        )
    }
    
    private fun drawClothing(canvas: Canvas, outfit: ClothingOutfit) {
        // Dibujar ropa según el outfit actual
        when (outfit.topType) {
            ClothingType.SCHOOL_UNIFORM -> drawSchoolUniform(canvas, outfit.topColor)
            ClothingType.CASUAL_SHIRT -> drawCasualShirt(canvas, outfit.topColor)
            ClothingType.DRESS -> drawDress(canvas, outfit.topColor)
            ClothingType.BIKINI -> if (isSpecialModeUnlocked) drawBikini(canvas, outfit.topColor)
            ClothingType.NONE -> if (isSpecialModeUnlocked) { /* Sin ropa superior */ }
            else -> drawSchoolUniform(canvas, outfit.topColor)
        }
        
        when (outfit.bottomType) {
            ClothingType.SCHOOL_SKIRT -> drawSchoolSkirt(canvas, outfit.bottomColor)
            ClothingType.CASUAL_PANTS -> drawCasualPants(canvas, outfit.bottomColor)
            ClothingType.SHORT_SKIRT -> drawShortSkirt(canvas, outfit.bottomColor)
            ClothingType.BIKINI_BOTTOM -> if (isSpecialModeUnlocked) drawBikiniBottom(canvas, outfit.bottomColor)
            ClothingType.NONE -> if (isSpecialModeUnlocked) { /* Sin ropa inferior */ }
            else -> drawSchoolSkirt(canvas, outfit.bottomColor)
        }
    }
    
    private fun drawSchoolUniform(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        // Camisa
        canvas.drawRect(
            avatarConfig.width / 2 - 35,
            120f,
            avatarConfig.width / 2 + 35,
            250f,
            paint
        )
        
        // Cuello de la camisa
        paint.color = Color.WHITE
        canvas.drawRect(
            avatarConfig.width / 2 - 30,
            120f,
            avatarConfig.width / 2 + 30,
            140f,
            paint
        )
        
        // Corbata
        paint.color = Color.RED
        canvas.drawRect(
            avatarConfig.width / 2 - 5,
            140f,
            avatarConfig.width / 2 + 5,
            200f,
            paint
        )
    }
    
    private fun drawSchoolSkirt(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        // Falda
        canvas.drawRect(
            avatarConfig.width / 2 - 40,
            240f,
            avatarConfig.width / 2 + 40,
            300f,
            paint
        )
        
        // Pliegues de la falda
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        
        for (i in 0..3) {
            val x = avatarConfig.width / 2 - 30 + (i * 20)
            canvas.drawLine(x, 240f, x, 300f, paint)
        }
    }
    
    private fun drawCasualShirt(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        canvas.drawRect(
            avatarConfig.width / 2 - 35,
            120f,
            avatarConfig.width / 2 + 35,
            250f,
            paint
        )
    }
    
    private fun drawDress(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        // Vestido completo
        val dressPath = Path().apply {
            moveTo(avatarConfig.width / 2 - 35, 120f)
            lineTo(avatarConfig.width / 2 + 35, 120f)
            lineTo(avatarConfig.width / 2 + 50, 320f)
            lineTo(avatarConfig.width / 2 - 50, 320f)
            close()
        }
        canvas.drawPath(dressPath, paint)
    }
    
    private fun drawBikini(canvas: Canvas, color: Int) {
        if (!isSpecialModeUnlocked) return
        
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        // Top del bikini
        canvas.drawCircle(avatarConfig.width / 2 - 15, 160f, 12f, paint)
        canvas.drawCircle(avatarConfig.width / 2 + 15, 160f, 12f, paint)
        
        // Tiras
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(avatarConfig.width / 2 - 15, 148f, avatarConfig.width / 2, 130f, paint)
        canvas.drawLine(avatarConfig.width / 2 + 15, 148f, avatarConfig.width / 2, 130f, paint)
    }
    
    private fun drawBikiniBottom(canvas: Canvas, color: Int) {
        if (!isSpecialModeUnlocked) return
        
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        canvas.drawRect(
            avatarConfig.width / 2 - 25,
            240f,
            avatarConfig.width / 2 + 25,
            270f,
            paint
        )
    }
    
    private fun drawCasualPants(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        // Pantalón completo
        canvas.drawRect(
            avatarConfig.width / 2 - 25,
            240f,
            avatarConfig.width / 2 + 25,
            380f,
            paint
        )
    }
    
    private fun drawShortSkirt(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        canvas.drawRect(
            avatarConfig.width / 2 - 35,
            240f,
            avatarConfig.width / 2 + 35,
            280f,
            paint
        )
    }
    
    private fun drawFace(canvas: Canvas, expression: RitsuExpression) {
        val centerX = avatarConfig.width / 2
        val centerY = 60f
        
        // Ojos
        drawEyes(canvas, centerX, centerY, expression)
        
        // Boca
        drawMouth(canvas, centerX, centerY, expression)
        
        // Mejillas (para sonrojo)
        if (expression == RitsuExpression.BLUSHING || expression == RitsuExpression.SHY) {
            drawBlush(canvas, centerX, centerY)
        }
    }
    
    private fun drawEyes(canvas: Canvas, centerX: Float, centerY: Float, expression: RitsuExpression) {
        val paint = Paint().apply {
            color = avatarConfig.eyeColor
            isAntiAlias = true
        }
        
        when (expression) {
            RitsuExpression.HAPPY -> {
                // Ojos cerrados sonriendo
                paint.color = Color.BLACK
                paint.strokeWidth = 3f
                paint.style = Paint.Style.STROKE
                canvas.drawArc(centerX - 25, centerY - 15, centerX - 5, centerY - 5, 0f, 180f, false, paint)
                canvas.drawArc(centerX + 5, centerY - 15, centerX + 25, centerY - 5, 0f, 180f, false, paint)
            }
            RitsuExpression.SHY, RitsuExpression.BLUSHING -> {
                // Ojos mirando hacia abajo
                canvas.drawCircle(centerX - 15, centerY - 5, 8f, Color.WHITE)
                canvas.drawCircle(centerX + 15, centerY - 5, 8f, Color.WHITE)
                canvas.drawCircle(centerX - 15, centerY - 2, 5f, paint.color)
                canvas.drawCircle(centerX + 15, centerY - 2, 5f, paint.color)
            }
            RitsuExpression.SURPRISED -> {
                // Ojos muy abiertos
                canvas.drawCircle(centerX - 15, centerY - 5, 10f, Color.WHITE)
                canvas.drawCircle(centerX + 15, centerY - 5, 10f, Color.WHITE)
                canvas.drawCircle(centerX - 15, centerY - 5, 6f, paint.color)
                canvas.drawCircle(centerX + 15, centerY - 5, 6f, paint.color)
            }
            else -> {
                // Ojos normales
                canvas.drawCircle(centerX - 15, centerY - 5, 8f, Color.WHITE)
                canvas.drawCircle(centerX + 15, centerY - 5, 8f, Color.WHITE)
                canvas.drawCircle(centerX - 15, centerY - 5, 5f, paint.color)
                canvas.drawCircle(centerX + 15, centerY - 5, 5f, paint.color)
            }
        }
    }
    
    private fun drawMouth(canvas: Canvas, centerX: Float, centerY: Float, expression: RitsuExpression) {
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        
        when (expression) {
            RitsuExpression.HAPPY -> {
                // Sonrisa
                canvas.drawArc(centerX - 10, centerY + 5, centerX + 10, centerY + 20, 0f, 180f, false, paint)
            }
            RitsuExpression.SHY, RitsuExpression.BLUSHING -> {
                // Boca pequeña
                canvas.drawCircle(centerX, centerY + 10, 3f, paint)
            }
            RitsuExpression.SURPRISED -> {
                // Boca abierta
                canvas.drawOval(centerX - 5, centerY + 8, centerX + 5, centerY + 15, paint)
            }
            else -> {
                // Boca neutral
                canvas.drawLine(centerX - 8, centerY + 10, centerX + 8, centerY + 10, paint)
            }
        }
    }
    
    private fun drawBlush(canvas: Canvas, centerX: Float, centerY: Float) {
        val paint = Paint().apply {
            color = Color.parseColor("#FFB6C1")
            alpha = 150
            isAntiAlias = true
        }
        
        canvas.drawCircle(centerX - 25, centerY + 5, 8f, paint)
        canvas.drawCircle(centerX + 25, centerY + 5, 8f, paint)
    }
    
    private fun drawHair(canvas: Canvas) {
        val paint = Paint().apply {
            color = avatarConfig.hairColor
            isAntiAlias = true
        }
        
        // Cabello estilo Ritsu
        val hairPath = Path().apply {
            moveTo(avatarConfig.width / 2 - 45, 30f)
            quadTo(avatarConfig.width / 2, 10f, avatarConfig.width / 2 + 45, 30f)
            lineTo(avatarConfig.width / 2 + 40, 80f)
            quadTo(avatarConfig.width / 2, 85f, avatarConfig.width / 2 - 40, 80f)
            close()
        }
        canvas.drawPath(hairPath, paint)
        
        // Flequillo
        canvas.drawRect(
            avatarConfig.width / 2 - 35,
            25f,
            avatarConfig.width / 2 + 35,
            45f,
            paint
        )
    }
    
    private fun drawAccessories(canvas: Canvas, outfit: ClothingOutfit) {
        outfit.accessories.forEach { accessory ->
            when (accessory.type) {
                AccessoryType.HAIR_BOW -> drawHairBow(canvas, accessory.color)
                AccessoryType.GLASSES -> drawGlasses(canvas)
                AccessoryType.NECKLACE -> drawNecklace(canvas, accessory.color)
                AccessoryType.EARRINGS -> drawEarrings(canvas, accessory.color)
            }
        }
    }
    
    private fun drawHairBow(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        canvas.drawCircle(avatarConfig.width / 2 + 30, 35f, 8f, paint)
        canvas.drawCircle(avatarConfig.width / 2 + 40, 35f, 8f, paint)
        canvas.drawCircle(avatarConfig.width / 2 + 35, 35f, 4f, Color.WHITE)
    }
    
    private fun drawGlasses(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        
        canvas.drawCircle(avatarConfig.width / 2 - 15, 55f, 12f, paint)
        canvas.drawCircle(avatarConfig.width / 2 + 15, 55f, 12f, paint)
        canvas.drawLine(avatarConfig.width / 2 - 3, 55f, avatarConfig.width / 2 + 3, 55f, paint)
    }
    
    private fun drawNecklace(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        
        canvas.drawArc(
            avatarConfig.width / 2 - 25,
            110f,
            avatarConfig.width / 2 + 25,
            130f,
            0f, 180f, false, paint
        )
    }
    
    private fun drawEarrings(canvas: Canvas, color: Int) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        
        canvas.drawCircle(avatarConfig.width / 2 - 35, 65f, 4f, paint)
        canvas.drawCircle(avatarConfig.width / 2 + 35, 65f, 4f, paint)
    }
    
    /**
     * Cambia la expresión del avatar
     */
    fun changeExpression(emotion: RitsuEmotion) {
        currentExpression = when (emotion) {
            RitsuEmotion.HAPPY -> RitsuExpression.HAPPY
            RitsuEmotion.SHY -> RitsuExpression.SHY
            RitsuEmotion.EXCITED -> RitsuExpression.HAPPY
            RitsuEmotion.THOUGHTFUL -> RitsuExpression.NORMAL
            RitsuEmotion.DETERMINED -> RitsuExpression.NORMAL
            RitsuEmotion.FOCUSED -> RitsuExpression.NORMAL
            else -> RitsuExpression.NORMAL
        }
    }
    
    /**
     * Cambia la ropa del avatar
     */
    fun changeClothing(outfit: ClothingOutfit) {
        currentOutfit = outfit
    }
    
    /**
     * Desbloquea el modo especial
     */
    fun unlockSpecialMode() {
        isSpecialModeUnlocked = true
    }
    
    /**
     * Procesa interacciones táctiles con el avatar
     */
    fun processInteraction(input: String, specialModeUnlocked: Boolean): AvatarInteraction {
        val lowerInput = input.lowercase()
        
        return when {
            lowerInput.contains("tocar") && specialModeUnlocked -> {
                AvatarInteraction(
                    response = "¡Kyaa! *sonrojo* ¿Q-qué estás haciendo?",
                    emotion = RitsuEmotion.SHY,
                    action = RitsuAction.BLUSH
                )
            }
            
            lowerInput.contains("abrazar") -> {
                AvatarInteraction(
                    response = "*abrazo cálido* Me alegra que quieras abrazarme...",
                    emotion = RitsuEmotion.HAPPY,
                    action = RitsuAction.POSE
                )
            }
            
            lowerInput.contains("sonreír") -> {
                changeExpression(RitsuEmotion.HAPPY)
                AvatarInteraction(
                    response = "*sonrisa brillante* ¡Así está mejor!",
                    emotion = RitsuEmotion.HAPPY,
                    action = RitsuAction.POSE
                )
            }
            
            else -> {
                AvatarInteraction(
                    response = "¿Qué quieres que haga?",
                    emotion = RitsuEmotion.NORMAL,
                    action = RitsuAction.IDLE
                )
            }
        }
    }
    
    fun getCurrentOutfit(): ClothingOutfit = currentOutfit
    fun getCurrentExpression(): RitsuExpression = currentExpression
}

// Enums y clases de datos para el avatar
enum class RitsuExpression {
    NORMAL, HAPPY, SHY, BLUSHING, SURPRISED, FOCUSED
}

enum class RitsuPose {
    STANDING, SITTING, WALKING, POSING
}

data class AvatarConfig(
    val height: Float,
    val width: Float,
    val skinTone: Int,
    val hairColor: Int,
    val eyeColor: Int
)

data class AvatarInteraction(
    val response: String,
    val emotion: RitsuEmotion,
    val action: RitsuAction
)

