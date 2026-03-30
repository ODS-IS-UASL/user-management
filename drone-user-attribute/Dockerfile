# docker image
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21

# Update installed packages and install Apache
RUN yum update --security -y \
   && yum install shadow-utils glibc glibc-langpack-ja groff less procps findutils -y \
   && yum clean all

# Set env
ENV LANG=ja_JP.utf8 \
      TZ=Asia/Tokyo

# Add user and group
RUN groupadd -g 1100 drngroup \ 
    && adduser -u 1100 drnuser -g drngroup

# Create tmp dir
RUN mkdir -p /tmp && chown drnuser:drngroup /tmp

# Set user
USER drnuser

# Set VOLUME
VOLUME /tmp

# Copy application
WORKDIR /drn/bin/app
COPY --chown=drnuser:drngroup --chmod=755 ./bin ./

ENTRYPOINT ["./docker-entrypoint.sh"]
