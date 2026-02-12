FROM oraclelinux:9-slim

RUN microdnf update && \
    microdnf install -y oracle-instantclient-release-26ai-el9 && \
    microdnf install -y oracle-instantclient-basiclite nodejs && \
    microdnf clean all

WORKDIR /function
ADD . /function/
RUN npm install

CMD exec node func.js
