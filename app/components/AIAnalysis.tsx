'use client'

interface AnalysisResults {
  duration: string
  emotions: string[]
  speechAnalysis: {
    wordsPerMinute: number
    clarity: number
    confidence: number
  }
  lipSyncAccuracy: number
  videoQuality: {
    resolution: string
    fps: number
    quality: string
  }
  keyTopics: string[]
  transcription: string
}

interface AIAnalysisProps {
  results: AnalysisResults | null
  isAnalyzing: boolean
}

export default function AIAnalysis({ results, isAnalyzing }: AIAnalysisProps) {
  if (isAnalyzing) {
    return (
      <div className="analysis-container bg-gray-800/50 rounded-xl p-6 backdrop-blur-sm">
        <h3 className="text-xl font-semibold text-white mb-6">Análisis IA en progreso...</h3>
        <div className="space-y-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="animate-pulse">
              <div className="h-4 bg-gray-600 rounded mb-2"></div>
              <div className="h-8 bg-gray-700 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  if (!results) {
    return (
      <div className="analysis-container bg-gray-800/50 rounded-xl p-6 backdrop-blur-sm">
        <h3 className="text-xl font-semibold text-white mb-4">Resultados del Análisis IA</h3>
        <div className="text-center py-12">
          <div className="mx-auto w-16 h-16 bg-gray-700 rounded-full flex items-center justify-center mb-4">
            <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
          </div>
          <p className="text-gray-400">Sube un video y ejecuta el análisis para ver los resultados</p>
        </div>
      </div>
    )
  }

  return (
    <div className="analysis-container bg-gray-800/50 rounded-xl p-6 backdrop-blur-sm">
      <h3 className="text-xl font-semibold text-white mb-6">Resultados del Análisis IA</h3>
      
      <div className="space-y-6">
        {/* Video Info */}
        <div className="analysis-section">
          <h4 className="font-semibold text-purple-400 mb-3">Información del Video</h4>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-400">Duración:</span>
              <span className="text-white ml-2">{results.duration}</span>
            </div>
            <div>
              <span className="text-gray-400">Resolución:</span>
              <span className="text-white ml-2">{results.videoQuality.resolution}</span>
            </div>
            <div>
              <span className="text-gray-400">FPS:</span>
              <span className="text-white ml-2">{results.videoQuality.fps}</span>
            </div>
            <div>
              <span className="text-gray-400">Calidad:</span>
              <span className="text-white ml-2">{results.videoQuality.quality}</span>
            </div>
          </div>
        </div>

        {/* Emotions */}
        <div className="analysis-section">
          <h4 className="font-semibold text-purple-400 mb-3">Emociones Detectadas</h4>
          <div className="flex flex-wrap gap-2">
            {results.emotions.map((emotion, index) => (
              <span 
                key={index}
                className="px-3 py-1 bg-purple-600/30 text-purple-300 rounded-full text-sm"
              >
                {emotion}
              </span>
            ))}
          </div>
        </div>

        {/* Speech Analysis */}
        <div className="analysis-section">
          <h4 className="font-semibold text-purple-400 mb-3">Análisis de Voz</h4>
          <div className="space-y-3">
            <div className="progress-item">
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-400">Palabras por minuto</span>
                <span className="text-white">{results.speechAnalysis.wordsPerMinute}</span>
              </div>
            </div>
            <div className="progress-item">
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-400">Claridad</span>
                <span className="text-white">{results.speechAnalysis.clarity}%</span>
              </div>
              <div className="w-full bg-gray-700 rounded-full h-2">
                <div 
                  className="bg-gradient-to-r from-green-500 to-blue-500 h-2 rounded-full"
                  style={{ width: `${results.speechAnalysis.clarity}%` }}
                ></div>
              </div>
            </div>
            <div className="progress-item">
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-400">Confianza</span>
                <span className="text-white">{results.speechAnalysis.confidence}%</span>
              </div>
              <div className="w-full bg-gray-700 rounded-full h-2">
                <div 
                  className="bg-gradient-to-r from-yellow-500 to-orange-500 h-2 rounded-full"
                  style={{ width: `${results.speechAnalysis.confidence}%` }}
                ></div>
              </div>
            </div>
          </div>
        </div>

        {/* Lip Sync */}
        <div className="analysis-section">
          <h4 className="font-semibold text-purple-400 mb-3">Sincronización Labial</h4>
          <div className="flex justify-between text-sm mb-1">
            <span className="text-gray-400">Precisión</span>
            <span className="text-white">{results.lipSyncAccuracy}%</span>
          </div>
          <div className="w-full bg-gray-700 rounded-full h-3">
            <div 
              className="bg-gradient-to-r from-pink-500 to-purple-500 h-3 rounded-full"
              style={{ width: `${results.lipSyncAccuracy}%` }}
            ></div>
          </div>
        </div>

        {/* Key Topics */}
        <div className="analysis-section">
          <h4 className="font-semibold text-purple-400 mb-3">Temas Principales</h4>
          <div className="flex flex-wrap gap-2">
            {results.keyTopics.map((topic, index) => (
              <span 
                key={index}
                className="px-3 py-1 bg-blue-600/30 text-blue-300 rounded-full text-sm"
              >
                {topic}
              </span>
            ))}
          </div>
        </div>

        {/* Transcription */}
        <div className="analysis-section">
          <h4 className="font-semibold text-purple-400 mb-3">Transcripción</h4>
          <div className="bg-gray-900/50 rounded-lg p-4">
            <p className="text-gray-300 text-sm leading-relaxed">
              {results.transcription}
            </p>
          </div>
        </div>

        {/* Export Button */}
        <button className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-purple-700 transition-all duration-300 transform hover:scale-105">
          Exportar Resultados
        </button>
      </div>
    </div>
  )
}
