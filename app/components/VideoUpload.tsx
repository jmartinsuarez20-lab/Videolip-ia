'use client'

import { useCallback, useState } from 'react'

interface VideoUploadProps {
  onVideoUpload: (videoUrl: string) => void
}

export default function VideoUpload({ onVideoUpload }: VideoUploadProps) {
  const [isDragOver, setIsDragOver] = useState(false)

  const handleFileSelect = useCallback((file: File) => {
    if (file && file.type.startsWith('video/')) {
      const url = URL.createObjectURL(file)
      onVideoUpload(url)
    }
  }, [onVideoUpload])

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    
    const files = Array.from(e.dataTransfer.files)
    const videoFile = files.find(file => file.type.startsWith('video/'))
    
    if (videoFile) {
      handleFileSelect(videoFile)
    }
  }, [handleFileSelect])

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(true)
  }, [])

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
  }, [])

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      handleFileSelect(file)
    }
  }, [handleFileSelect])

  return (
    <div className="video-upload-container">
      <div
        className={`border-2 border-dashed rounded-xl p-8 text-center transition-all duration-300 ${
          isDragOver 
            ? 'border-purple-400 bg-purple-500/10 scale-105' 
            : 'border-gray-600 bg-gray-800/50 hover:border-purple-500'
        }`}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
      >
        <div className="space-y-4">
          <div className="mx-auto w-16 h-16 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
          </div>
          
          <div>
            <h3 className="text-xl font-semibold text-white mb-2">
              Sube tu video para análisis
            </h3>
            <p className="text-gray-400 mb-4">
              Arrastra y suelta tu archivo de video o haz clic para seleccionar
            </p>
            <p className="text-sm text-gray-500">
              Formatos soportados: MP4, MOV, AVI, WebM
            </p>
          </div>

          <label className="inline-block">
            <span className="bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-3 rounded-lg font-semibold cursor-pointer hover:from-purple-700 hover:to-pink-700 transition-all duration-300 transform hover:scale-105">
              Seleccionar Archivo
            </span>
            <input
              type="file"
              accept="video/*"
              onChange={handleInputChange}
              className="hidden"
            />
          </label>
        </div>
      </div>
    </div>
  )
}
