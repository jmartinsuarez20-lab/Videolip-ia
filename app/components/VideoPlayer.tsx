'use client'

import { useRef, useEffect } from 'react'

interface VideoPlayerProps {
  videoUrl: string
  onAnalyze: () => void
  isAnalyzing: boolean
}

export default function VideoPlayer({ videoUrl, onAnalyze, isAnalyzing }: VideoPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null)

  useEffect(() => {
    if (videoRef.current) {
      videoRef.current.load()
    }
  }, [videoUrl])

  return (
    <div className="video-player-container bg-gray-800/50 rounded-xl p-6 backdrop-blur-sm">
      <h3 className="text-xl font-semibold text-white mb-4">Vista previa del video</h3>
      
      <div className="relative rounded-lg overflow-hidden bg-black">
        <video
          ref={videoRef}
          className="w-full max-h-80 object-contain"
          controls
          preload="metadata"
        >
          <source src={videoUrl} />
          Tu navegador no soporta el elemento de video.
        </video>
      </div>

      <div className="mt-6 flex flex-col sm:flex-row gap-4">
        <button
          onClick={onAnalyze}
          disabled={isAnalyzing}
          className="flex-1 bg-gradient-to-r from-green-600 to-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-green-700 hover:to-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-105 disabled:transform-none"
        >
          {isAnalyzing ? (
            <span className="flex items-center justify-center">
              <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Analizando...
            </span>
          ) : (
            'Iniciar Análisis IA'
          )}
        </button>
        
        <button className="px-6 py-3 border border-gray-600 text-gray-300 rounded-lg hover:bg-gray-700 transition-colors duration-300">
          Configuración
        </button>
      </div>
    </div>
  )
}
