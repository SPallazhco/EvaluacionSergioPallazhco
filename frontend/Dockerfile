FROM node:24-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.30-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY nginx/cabeceras-seguridad.conf /etc/nginx/snippets/cabeceras-seguridad.conf
COPY --from=build /app/dist/transferencias-web/browser /usr/share/nginx/html

EXPOSE 8080
