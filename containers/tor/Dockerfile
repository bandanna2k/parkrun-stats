# docker build . -t tor_agent
FROM alpine:3.14

RUN apk add --no-cache bash
ENTRYPOINT ["/bin/sh"]

RUN apk add --no-cache curl

RUN apk add --no-cache tor
COPY torrc /etc/tor/torrc

ENTRYPOINT ["tor"]
CMD ["-f", "/etc/tor/torrc"]
