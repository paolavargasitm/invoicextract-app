import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AppRouter from './AppRouter'
import { initKeycloak } from './auth/keycloak'

async function bootstrap() {
  await initKeycloak();
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <AppRouter />
    </StrictMode>,
  )
}

bootstrap()
