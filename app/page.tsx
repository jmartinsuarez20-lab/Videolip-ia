'use client'

import { useState, useRef } from 'react'
import VideoUpload from './components/VideoUpload'
import VideoPlayer from './components/VideoPlayer'
import AIAnalysis from './components/AIAnalysis'

export default function Home() {
  const [uploadedVideo, setUploadedVideo] = useState<string | null>(null)
  const [analysisResults, setAnalysisResults] = useState<any>(null)
  const [isAnalyzing, setIsAnalyzing] = useState(false)

  const handleVideoUpload = (videoUrl: string) => {
    setUploadedVideo(videoUrl)
    setAnalysisResults(null)
  }

  const handleAnalyze = async () => {
    if (!uploadedVideo) return
    
    setIsAnalyzing(true)
    
    // Simulate AI analysis
    setTimeout(() => {
      const results = {
        duration: '2:34',
        emotions: ['happy', 'neutral', 'excited'],
        speechAnalysis: {
          wordsPerMinute: 145,
          clarity: 92,
          confidence: 88
        },
        lipSyncAccuracy: 94,
        videoQuality: {
          resolution: '1080p',
          fps: 30,
          quality: 'High'
        },
        keyTopics: ['technology', 'innovation', 'future'],
        transcription: "This is a sample transcription of the video content..."
      }
      setAnalysisResults(results)
      setIsAnalyzing(false)
    }, 3000)
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900">
      <div className="container mx-auto px-4 py-8">
        <div className="text-center mb-12">
          <h1 className="text-6xl font-bold text-white mb-4 bg-gradient-to-r from-pink-400 to-purple-600 bg-clip-text text-transparent">
            Videolip-IA
          </h1>
          <p className="text-xl text-gray-300 max-w-2xl mx-auto">
            Plataforma avanzada de análisis de video con Inteligencia Artificial. 
            Analiza emociones, calidad de audio, sincronización labial y más.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 max-w-7xl mx-auto">
          <div className="space-y-6">
            <VideoUpload onVideoUpload={handleVideoUpload} />
            {uploadedVideo && (
              <VideoPlayer 
                videoUrl={uploadedVideo} 
                onAnalyze={handleAnalyze}
                isAnalyzing={isAnalyzing}
              />
            )}
          </div>
          
          <div>
            <AIAnalysis 
              results={analysisResults} 
              isAnalyzing={isAnalyzing}
            />
          </div>
        </div>
      </div>
    </main>
  )
}
