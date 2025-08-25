import './globals.css'

export const metadata = {
  title: 'Videolip-ia',
  description: 'Videolip-ia application',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
