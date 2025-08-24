package com.ritsuai.launcher.avatar3d

import android.graphics.PointF
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import kotlin.math.cos
import kotlin.math.sin

/**
 * Motor de física para el avatar 3D de Ritsu.
 * Gestiona el movimiento y las colisiones del avatar en la pantalla.
 */
class RitsuPhysicsEngine {

    // Mundo de física
    private val world = World(Vec2(0f, 9.8f))
    
    // Cuerpo del avatar
    private var avatarBody: Body? = null
    
    // Límites de la pantalla
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f
    
    // Tiempo de la última actualización
    private var lastUpdateTime: Long = System.currentTimeMillis()
    
    // Constantes de física
    private val PIXELS_PER_METER = 100f
    private val TIME_STEP = 1.0f / 60.0f
    private val VELOCITY_ITERATIONS = 6
    private val POSITION_ITERATIONS = 2
    
    /**
     * Inicializa el motor de física
     *
     * @param screenWidth Ancho de la pantalla en píxeles
     * @param screenHeight Alto de la pantalla en píxeles
     */
    fun initialize(screenWidth: Float, screenHeight: Float) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        
        // Crear cuerpo del avatar
        createAvatarBody()
        
        // Crear límites de la pantalla
        createScreenBoundaries()
    }
    
    /**
     * Crea el cuerpo físico del avatar
     */
    private fun createAvatarBody() {
        // Definición del cuerpo
        val bodyDef = BodyDef().apply {
            type = BodyType.DYNAMIC
            position.set(screenWidth / (2f * PIXELS_PER_METER), screenHeight / (2f * PIXELS_PER_METER))
        }
        
        // Crear cuerpo
        avatarBody = world.createBody(bodyDef)
        
        // Definición de la forma (círculo)
        val shape = CircleShape().apply {
            radius = 50f / PIXELS_PER_METER // Radio en metros
        }
        
        // Definición de la fixture
        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 1.0f
            friction = 0.3f
            restitution = 0.5f // Rebote
        }
        
        // Añadir fixture al cuerpo
        avatarBody?.createFixture(fixtureDef)
    }
    
    /**
     * Crea los límites de la pantalla
     */
    private fun createScreenBoundaries() {
        // Convertir dimensiones a metros
        val width = screenWidth / PIXELS_PER_METER
        val height = screenHeight / PIXELS_PER_METER
        
        // Crear límites (suelo, techo, paredes)
        createBoundary(Vec2(width / 2f, height), Vec2(width, 0.1f)) // Suelo
        createBoundary(Vec2(width / 2f, 0f), Vec2(width, 0.1f)) // Techo
        createBoundary(Vec2(0f, height / 2f), Vec2(0.1f, height)) // Pared izquierda
        createBoundary(Vec2(width, height / 2f), Vec2(0.1f, height)) // Pared derecha
    }
    
    /**
     * Crea un límite estático
     *
     * @param position Posición del límite
     * @param size Tamaño del límite
     */
    private fun createBoundary(position: Vec2, size: Vec2) {
        // Definición del cuerpo
        val bodyDef = BodyDef().apply {
            type = BodyType.STATIC
            this.position.set(position)
        }
        
        // Crear cuerpo
        val body = world.createBody(bodyDef)
        
        // Definición de la forma (caja)
        val shape = org.jbox2d.collision.shapes.PolygonShape()
        shape.setAsBox(size.x / 2f, size.y / 2f)
        
        // Definición de la fixture
        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 0f
            friction = 0.3f
            restitution = 0.5f // Rebote
        }
        
        // Añadir fixture al cuerpo
        body.createFixture(fixtureDef)
    }
    
    /**
     * Actualiza la simulación física
     *
     * @return Posición actual del avatar en píxeles
     */
    fun update(): PointF {
        // Calcular delta de tiempo
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastUpdateTime) / 1000f
        lastUpdateTime = currentTime
        
        // Actualizar mundo físico
        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
        
        // Obtener posición del avatar
        val position = avatarBody?.position ?: Vec2(0f, 0f)
        
        // Convertir a píxeles
        return PointF(
            position.x * PIXELS_PER_METER,
            position.y * PIXELS_PER_METER
        )
    }
    
    /**
     * Aplica una fuerza al avatar en una dirección específica
     *
     * @param angle Ángulo en radianes
     * @param force Magnitud de la fuerza
     */
    fun applyForce(angle: Float, force: Float) {
        avatarBody?.apply {
            val forceVec = Vec2(
                cos(angle) * force,
                sin(angle) * force
            )
            applyForceToCenter(forceVec)
        }
    }
    
    /**
     * Aplica un impulso al avatar en una dirección específica
     *
     * @param angle Ángulo en radianes
     * @param impulse Magnitud del impulso
     */
    fun applyImpulse(angle: Float, impulse: Float) {
        avatarBody?.apply {
            val impulseVec = Vec2(
                cos(angle) * impulse,
                sin(angle) * impulse
            )
            applyLinearImpulse(impulseVec, position)
        }
    }
    
    /**
     * Establece la posición del avatar
     *
     * @param x Coordenada X en píxeles
     * @param y Coordenada Y en píxeles
     */
    fun setPosition(x: Float, y: Float) {
        avatarBody?.setTransform(
            Vec2(x / PIXELS_PER_METER, y / PIXELS_PER_METER),
            avatarBody?.angle ?: 0f
        )
    }
    
    /**
     * Obtiene la posición actual del avatar
     *
     * @return Posición en píxeles
     */
    fun getPosition(): PointF {
        val position = avatarBody?.position ?: Vec2(0f, 0f)
        return PointF(
            position.x * PIXELS_PER_METER,
            position.y * PIXELS_PER_METER
        )
    }
    
    /**
     * Obtiene la velocidad actual del avatar
     *
     * @return Velocidad en píxeles por segundo
     */
    fun getVelocity(): PointF {
        val velocity = avatarBody?.linearVelocity ?: Vec2(0f, 0f)
        return PointF(
            velocity.x * PIXELS_PER_METER,
            velocity.y * PIXELS_PER_METER
        )
    }
    
    /**
     * Establece la velocidad del avatar
     *
     * @param x Componente X de la velocidad en píxeles por segundo
     * @param y Componente Y de la velocidad en píxeles por segundo
     */
    fun setVelocity(x: Float, y: Float) {
        avatarBody?.linearVelocity = Vec2(
            x / PIXELS_PER_METER,
            y / PIXELS_PER_METER
        )
    }
    
    /**
     * Detiene el movimiento del avatar
     */
    fun stop() {
        avatarBody?.linearVelocity = Vec2(0f, 0f)
        avatarBody?.angularVelocity = 0f
    }
}

