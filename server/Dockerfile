FROM node:22-alpine

ENV NODE_ENV=production
WORKDIR /app

COPY package.json ./
RUN npm install --omit=dev --ignore-scripts && npm cache clean --force
COPY src ./src

USER node
EXPOSE 8787
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD node -e "fetch('http://127.0.0.1:8787/healthz').then(r=>{if(r.status!==200)process.exit(1)}).catch(()=>process.exit(1))"
CMD ["node", "src/server.js"]
