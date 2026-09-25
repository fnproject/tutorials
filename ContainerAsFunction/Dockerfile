FROM container-registry.oracle.com/oci_functions/node:22-dev as build-stage
WORKDIR /function
ADD package.json /function/
RUN npm install

FROM container-registry.oracle.com/oci_functions/node:22
RUN microdnf install -y oracle-epel-release-el9 && \
  microdnf install -y --enablerepo=ol9_developer_EPEL ImageMagick && \
  microdnf clean all
WORKDIR /function
ADD . /function/
COPY --from=build-stage /function/node_modules/ /function/node_modules/
ENTRYPOINT ["node", "func.js"]