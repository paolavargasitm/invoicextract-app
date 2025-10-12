# Build stage
FROM node:18.16.1 AS build
WORKDIR /app

# Copia los manifests primero (soporta lockfile si existe)
COPY package*.json ./

# Instala dependencias (usa npm ci si hay lockfile, de lo contrario npm install)
RUN if [ -f package-lock.json ]; then npm ci --no-audit --no-fund; else npm install --no-audit --no-fund; fi

# Copia el resto del código
COPY . .

# Variables de build para Vite (se inyectan como ENV VITE_*)
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM
ARG VITE_KEYCLOAK_CLIENT_ID
ARG VITE_BACKEND_BASE_URL
ARG VITE_MAPPINGS_BASE_URL
ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}
ENV VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}
ENV VITE_BACKEND_BASE_URL=${VITE_BACKEND_BASE_URL}
ENV VITE_MAPPINGS_BASE_URL=${VITE_MAPPINGS_BASE_URL}

# Compila la app
RUN npm run build

# Stage final con Nginx
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
