import './globals.css'

export const metadata = {
  title: 'Videolip-IA | Análisis de Video con Inteligencia Artificial',
  description: 'Plataforma avanzada de análisis de video con IA. Analiza emociones, calidad de audio, sincronización labial y más.',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="es">
      <body className="bg-gray-900 text-white">{children}</body>
    </html>
  )
}
