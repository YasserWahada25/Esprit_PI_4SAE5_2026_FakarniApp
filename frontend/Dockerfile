# Build : depuis le dossier frontend/  →  docker build -t fakarni-frontend:local .
# L’appli Angular (projet careTemplate) est servie en SSR (Node) sur le port 4000 par défaut.
#
# syntax=docker/dockerfile:1.4
FROM node:20-bookworm-slim AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi
COPY . .
ENV NODE_OPTIONS=--max-old-space-size=8192
RUN npm run build -- --configuration=production

FROM node:20-bookworm-slim
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=4000
COPY --from=build /app/dist/careTemplate ./
COPY --from=build /app/package.json ./
COPY --from=build /app/package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci --omit=dev; else npm install --omit=dev; fi \
  && chown -R node:node /app
USER node
EXPOSE 4000
CMD ["node", "server/server.mjs"]
