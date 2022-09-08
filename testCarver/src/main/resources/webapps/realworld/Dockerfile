FROM node:14.17.0
WORKDIR /
RUN mkdir app
ADD build app
RUN cd /app
RUN npm install
WORKDIR /app
CMD ["npm", "run", "dev"]

