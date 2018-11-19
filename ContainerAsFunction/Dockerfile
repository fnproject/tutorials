FROM fnproject/node:dev as build-stage
WORKDIR /function
ADD package.json /function/
RUN npm install

FROM fnproject/node
RUN apk add --no-cache imagemagick
WORKDIR /function
ADD . /function/
COPY --from=build-stage /function/node_modules/ /function/node_modules/
ENTRYPOINT ["node", "func.js"]