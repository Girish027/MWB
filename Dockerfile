FROM nexus.cicd.sv2.247-inc.net:5000/alpine as common_settings
FROM nexus.cicd.sv2.247-inc.net:5000/el7:gcc5-jdk8-nodes_8_10_sc

ARG GROUP_LABEL="nltools.nlworkbench"
ARG GIT_BRANCH

LABEL group_label=$GROUP_LABEL

# vvvvvvvvvvvvvvvvvvvv
# TODO: move to standard docker repo
ENV PROXY_HOST=proxy-grp1.lb-priv.sv2.247-inc.net
ENV PROXY_PORT=3128
ENV HTTP_PROXY_URL=http://$PROXY_HOST:$PROXY_PORT
ENV HTTPS_PROXY_URL=https://$PROXY_HOST:$PROXY_PORT
ENV NO_PROXY="*.tellme.com,127.0.0.1,*.247-inc.net"
ENV GITHUB_HOST=github.home.247-inc.net
ENV NEXUS_HOST=nexus.cicd.sv2.247-inc.net
ENV BRANCH_NAME=$GIT_BRANCH
# ^^^^^^^^^^^^^^^^^^^^

WORKDIR /mydir
ENTRYPOINT ["/bin/bash","--"]

RUN mkdir -p /root/.ssh/
COPY --from=common_settings /root/id_rsa* /root/.ssh/
RUN chmod 600 /root/.ssh/id_rsa
RUN echo "StrictHostKeyChecking no" >> /root/.ssh/config

COPY --from=common_settings /root/settings.xml /root/settings.xml

COPY . .
