FROM oraclelinux:7-slim

RUN  yum -y install oracle-release-el7 oracle-nodejs-release-el7 && \
     yum-config-manager --disable ol7_developer_EPEL && \
     yum -y install oracle-instantclient19.3-basiclite nodejs && \
     rm -rf /var/cache/yum && \
     groupadd --gid 1000 fn && \
     adduser --uid 1000 --gid fn fn

WORKDIR /function
ADD . /function/
RUN npm install

CMD exec node func.js