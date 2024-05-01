FROM alpine:3.14

RUN apk add --no-cache tor

RUN apk add --no-cache bash
ENTRYPOINT ["/bin/sh"]

# Set `tor` as the default user during the container runtime
USER tor

# Set `tor` as the entrypoint for the image
ENTRYPOINT ["tor"]

# Set the default container command
# This can be overridden later when running a container
CMD ["-f", "/etc/tor/torrc"]