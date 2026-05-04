# Stage 1: Build
FROM node:20-bookworm-slim AS build
WORKDIR /app

# Copy package files
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm ci || npm install

# Copy source code
COPY . .

# Build Angular app
ENV NODE_OPTIONS=--max-old-space-size=8192
RUN npm run build -- --configuration=production

# Stage 2: Runtime with Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built application (browser files only)
COPY --from=build /app/dist/careTemplate/browser ./

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
