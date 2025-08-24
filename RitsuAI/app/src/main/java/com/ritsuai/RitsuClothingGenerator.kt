package com.ritsuai

import android.content.Context
import android.graphics.Color
import kotlin.random.Random

/**
 * Generador inteligente de ropa para Ritsu
 * Crea outfits basados en descripciones de texto
 */
class RitsuClothingGenerator(private val context: Context) {
    
    private val clothingDatabase = ClothingDatabase()
    private val colorDatabase = ColorDatabase()
    
    /**
     * Analiza una solicitud de ropa y genera un outfit
     */
    fun parseClothingRequest(input: String): ClothingRequest {
        val lowerInput = input.lowercase()
        
        val style = when {
            lowerInput.contains("escolar") || lowerInput.contains("uniforme") -> ClothingStyle.SCHOOL
            lowerInput.contains("casual") || lowerInput.contains("cómodo") -> ClothingStyle.CASUAL
            lowerInput.contains("elegante") || lowerInput.contains("formal") -> ClothingStyle.ELEGANT
            lowerInput.contains("deportivo") || lowerInput.contains("ejercicio") -> ClothingStyle.SPORTY
            lowerInput.contains("verano") || lowerInput.contains("playa") -> ClothingStyle.SUMMER
            lowerInput.contains("kawaii") || lowerInput.contains("lindo") -> ClothingStyle.KAWAII
            lowerInput.contains("sexy") || lowerInput.contains("atractivo") -> ClothingStyle.SPECIAL
            else -> ClothingStyle.CASUAL
        }
        
        val colors = extractColors(lowerInput)
        val specificItems = extractSpecificItems(lowerInput)
        
        return ClothingRequest(
            style = style,
            preferredColors = colors,
            specificItems = specificItems,
            mood = extractMood(lowerInput)
        )
    }
    
    private fun extractColors(input: String): List<Int> {
        val colors = mutableListOf<Int>()
        
        colorDatabase.colorMap.forEach { (name, color) ->
            if (input.contains(name)) {
                colors.add(color)
            }
        }
        
        return colors.ifEmpty { listOf(getRandomColor()) }
    }
    
    private fun extractSpecificItems(input: String): List<ClothingType> {
        val items = mutableListOf<ClothingType>()
        
        when {
            input.contains("vestido") -> items.add(ClothingType.DRESS)
            input.contains("falda") -> items.add(ClothingType.SCHOOL_SKIRT)
            input.contains("pantalón") -> items.add(ClothingType.CASUAL_PANTS)
            input.contains("camisa") -> items.add(ClothingType.CASUAL_SHIRT)
            input.contains("bikini") -> {
                items.add(ClothingType.BIKINI)
                items.add(ClothingType.BIKINI_BOTTOM)
            }
        }
        
        return items
    }
    
    private fun extractMood(input: String): ClothingMood {
        return when {
            input.contains("feliz") || input.contains("alegre") -> ClothingMood.HAPPY
            input.contains("tímida") || input.contains("vergonzosa") -> ClothingMood.SHY
            input.contains("elegante") || input.contains("sofisticada") -> ClothingMood.ELEGANT
            input.contains("juguetona") || input.contains("divertida") -> ClothingMood.PLAYFUL
            input.contains("seductora") || input.contains("sensual") -> ClothingMood.SEDUCTIVE
            else -> ClothingMood.NORMAL
        }
    }
    
    /**
     * Genera un outfit completo basado en la solicitud
     */
    fun generateOutfit(request: ClothingRequest): ClothingOutfit {
        return when (request.style) {
            ClothingStyle.SCHOOL -> generateSchoolOutfit(request)
            ClothingStyle.CASUAL -> generateCasualOutfit(request)
            ClothingStyle.ELEGANT -> generateElegantOutfit(request)
            ClothingStyle.SPORTY -> generateSportyOutfit(request)
            ClothingStyle.SUMMER -> generateSummerOutfit(request)
            ClothingStyle.KAWAII -> generateKawaiiOutfit(request)
            ClothingStyle.SPECIAL -> generateSpecialOutfit(request)
        }
    }
    
    private fun generateSchoolOutfit(request: ClothingRequest): ClothingOutfit {
        val primaryColor = request.preferredColors.firstOrNull() ?: Color.BLUE
        val secondaryColor = Color.WHITE
        
        return ClothingOutfit(
            topType = ClothingType.SCHOOL_UNIFORM,
            topColor = primaryColor,
            bottomType = ClothingType.SCHOOL_SKIRT,
            bottomColor = Color.parseColor("#000080"), // Azul marino
            accessories = listOf(
                Accessory(AccessoryType.HAIR_BOW, Color.RED),
                Accessory(AccessoryType.NECKLACE, Color.GOLD)
            ),
            description = "Uniforme escolar clásico con falda plisada y corbata roja"
        )
    }
    
    private fun generateCasualOutfit(request: ClothingRequest): ClothingOutfit {
        val topColor = request.preferredColors.firstOrNull() ?: getRandomColor()
        val bottomColor = getComplementaryColor(topColor)
        
        return ClothingOutfit(
            topType = ClothingType.CASUAL_SHIRT,
            topColor = topColor,
            bottomType = if (Random.nextBoolean()) ClothingType.CASUAL_PANTS else ClothingType.SHORT_SKIRT,
            bottomColor = bottomColor,
            accessories = generateRandomAccessories(),
            description = "Outfit casual cómodo y moderno"
        )
    }
    
    private fun generateElegantOutfit(request: ClothingRequest): ClothingOutfit {
        val elegantColors = listOf(Color.BLACK, Color.parseColor("#800080"), Color.parseColor("#000080"))
        val color = request.preferredColors.firstOrNull() ?: elegantColors.random()
        
        return ClothingOutfit(
            topType = ClothingType.DRESS,
            topColor = color,
            bottomType = ClothingType.NONE, // El vestido cubre todo
            bottomColor = color,
            accessories = listOf(
                Accessory(AccessoryType.EARRINGS, Color.parseColor("#FFD700")),
                Accessory(AccessoryType.NECKLACE, Color.parseColor("#C0C0C0"))
            ),
            description = "Vestido elegante perfecto para ocasiones especiales"
        )
    }
    
    private fun generateSportyOutfit(request: ClothingRequest): ClothingOutfit {
        val sportyColors = listOf(Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"), Color.parseColor("#45B7D1"))
        val color = request.preferredColors.firstOrNull() ?: sportyColors.random()
        
        return ClothingOutfit(
            topType = ClothingType.CASUAL_SHIRT,
            topColor = color,
            bottomType = ClothingType.CASUAL_PANTS,
            bottomColor = Color.parseColor("#2C3E50"),
            accessories = emptyList(),
            description = "Outfit deportivo cómodo para actividades físicas"
        )
    }
    
    private fun generateSummerOutfit(request: ClothingRequest): ClothingOutfit {
        val summerColors = listOf(Color.parseColor("#FFE4B5"), Color.parseColor("#87CEEB"), Color.parseColor("#FFB6C1"))
        val color = request.preferredColors.firstOrNull() ?: summerColors.random()
        
        return ClothingOutfit(
            topType = ClothingType.CASUAL_SHIRT,
            topColor = color,
            bottomType = ClothingType.SHORT_SKIRT,
            bottomColor = Color.WHITE,
            accessories = listOf(
                Accessory(AccessoryType.HAIR_BOW, Color.parseColor("#FF69B4"))
            ),
            description = "Outfit fresco y colorido perfecto para el verano"
        )
    }
    
    private fun generateKawaiiOutfit(request: ClothingRequest): ClothingOutfit {
        val kawaiiColors = listOf(Color.parseColor("#FFB6C1"), Color.parseColor("#DDA0DD"), Color.parseColor("#98FB98"))
        val color = request.preferredColors.firstOrNull() ?: kawaiiColors.random()
        
        return ClothingOutfit(
            topType = ClothingType.DRESS,
            topColor = color,
            bottomType = ClothingType.NONE,
            bottomColor = color,
            accessories = listOf(
                Accessory(AccessoryType.HAIR_BOW, Color.parseColor("#FF1493")),
                Accessory(AccessoryType.EARRINGS, Color.parseColor("#FFB6C1"))
            ),
            description = "Outfit súper kawaii con colores pastel y accesorios adorables"
        )
    }
    
    private fun generateSpecialOutfit(request: ClothingRequest): ClothingOutfit {
        // Solo disponible en modo especial
        val color = request.preferredColors.firstOrNull() ?: Color.parseColor("#FF69B4")
        
        return ClothingOutfit(
            topType = ClothingType.BIKINI,
            topColor = color,
            bottomType = ClothingType.BIKINI_BOTTOM,
            bottomColor = color,
            accessories = emptyList(),
            description = "Outfit especial... *sonrojo*"
        )
    }
    
    private fun generateRandomAccessories(): List<Accessory> {
        val accessories = mutableListOf<Accessory>()
        val availableAccessories = AccessoryType.values()
        
        repeat(Random.nextInt(0, 3)) {
            val type = availableAccessories.random()
            val color = getRandomColor()
            accessories.add(Accessory(type, color))
        }
        
        return accessories
    }
    
    private fun getRandomColor(): Int {
        val colors = listOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.parseColor("#FF69B4"), Color.parseColor("#9370DB"),
            Color.parseColor("#20B2AA"), Color.parseColor("#FF6347")
        )
        return colors.random()
    }
    
    private fun getComplementaryColor(color: Int): Int {
        // Lógica simple para obtener colores complementarios
        return when (color) {
            Color.RED -> Color.GREEN
            Color.BLUE -> Color.YELLOW
            Color.GREEN -> Color.RED
            Color.YELLOW -> Color.BLUE
            else -> Color.BLACK
        }
    }
    
    /**
     * Guarda un outfit personalizado
     */
    fun saveCustomOutfit(outfit: ClothingOutfit, name: String) {
        clothingDatabase.saveCustomOutfit(name, outfit)
    }
    
    /**
     * Carga un outfit guardado
     */
    fun loadCustomOutfit(name: String): ClothingOutfit? {
        return clothingDatabase.loadCustomOutfit(name)
    }
    
    /**
     * Obtiene sugerencias de ropa basadas en el clima o la hora
     */
    fun getSuggestions(context: String): List<ClothingOutfit> {
        return when (context.lowercase()) {
            "mañana" -> listOf(generateSchoolOutfit(ClothingRequest(ClothingStyle.SCHOOL)))
            "tarde" -> listOf(generateCasualOutfit(ClothingRequest(ClothingStyle.CASUAL)))
            "noche" -> listOf(generateElegantOutfit(ClothingRequest(ClothingStyle.ELEGANT)))
            "calor" -> listOf(generateSummerOutfit(ClothingRequest(ClothingStyle.SUMMER)))
            else -> listOf(generateCasualOutfit(ClothingRequest(ClothingStyle.CASUAL)))
        }
    }
}

// Clases de datos para el sistema de ropa
data class ClothingRequest(
    val style: ClothingStyle,
    val preferredColors: List<Int> = emptyList(),
    val specificItems: List<ClothingType> = emptyList(),
    val mood: ClothingMood = ClothingMood.NORMAL
)

data class ClothingOutfit(
    val topType: ClothingType,
    val topColor: Int,
    val bottomType: ClothingType,
    val bottomColor: Int,
    val accessories: List<Accessory> = emptyList(),
    val description: String = ""
) {
    companion object {
        fun getDefaultOutfit(): ClothingOutfit {
            return ClothingOutfit(
                topType = ClothingType.SCHOOL_UNIFORM,
                topColor = Color.BLUE,
                bottomType = ClothingType.SCHOOL_SKIRT,
                bottomColor = Color.parseColor("#000080"),
                accessories = listOf(
                    Accessory(AccessoryType.HAIR_BOW, Color.RED)
                ),
                description = "Uniforme escolar por defecto"
            )
        }
    }
}

data class Accessory(
    val type: AccessoryType,
    val color: Int
)

enum class ClothingStyle {
    SCHOOL, CASUAL, ELEGANT, SPORTY, SUMMER, KAWAII, SPECIAL
}

enum class ClothingType {
    SCHOOL_UNIFORM, CASUAL_SHIRT, DRESS, BIKINI,
    SCHOOL_SKIRT, CASUAL_PANTS, SHORT_SKIRT, BIKINI_BOTTOM,
    NONE
}

enum class AccessoryType {
    HAIR_BOW, GLASSES, NECKLACE, EARRINGS
}

enum class ClothingMood {
    NORMAL, HAPPY, SHY, ELEGANT, PLAYFUL, SEDUCTIVE
}

// Base de datos de colores
class ColorDatabase {
    val colorMap = mapOf(
        "rojo" to Color.RED,
        "azul" to Color.BLUE,
        "verde" to Color.GREEN,
        "amarillo" to Color.YELLOW,
        "rosa" to Color.parseColor("#FFB6C1"),
        "morado" to Color.parseColor("#9370DB"),
        "naranja" to Color.parseColor("#FFA500"),
        "negro" to Color.BLACK,
        "blanco" to Color.WHITE,
        "gris" to Color.GRAY,
        "dorado" to Color.parseColor("#FFD700"),
        "plateado" to Color.parseColor("#C0C0C0"),
        "turquesa" to Color.parseColor("#40E0D0"),
        "coral" to Color.parseColor("#FF7F50"),
        "lavanda" to Color.parseColor("#E6E6FA")
    )
}

// Base de datos de ropa (simulada)
class ClothingDatabase {
    private val customOutfits = mutableMapOf<String, ClothingOutfit>()
    
    fun saveCustomOutfit(name: String, outfit: ClothingOutfit) {
        customOutfits[name] = outfit
    }
    
    fun loadCustomOutfit(name: String): ClothingOutfit? {
        return customOutfits[name]
    }
    
    fun getAllCustomOutfits(): Map<String, ClothingOutfit> {
        return customOutfits.toMap()
    }
}

