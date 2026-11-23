# syntax=docker/dockerfile:1
# Frontend TechGear: build Vite rồi phục vụ bằng nginx. Đặt ngoài source/frontend (thư mục do thành viên khác phụ trách);
# ngữ cảnh build là source/frontend (xem docker-compose.yml). Địa chỉ backend dùng mặc định trong code: http://localhost:8080.
FROM node:22-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
# BrowserRouter: đường dẫn không phải file tĩnh thì trả index.html
COPY <<'EOF' /etc/nginx/conf.d/default.conf
server {
    listen 80;
    root /usr/share/nginx/html;
    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
COPY --from=build /src/dist /usr/share/nginx/html
EXPOSE 80
