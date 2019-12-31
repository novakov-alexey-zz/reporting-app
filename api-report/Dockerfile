FROM surnet/alpine-wkhtmltopdf:3.7-0.12.4-small

COPY Rocket.toml /
COPY target/x86_64-unknown-linux-musl/release/api-report /
COPY templates/* /templates/
COPY demokeys /demokeys/

# replace original entrypoint which is pointed to wkhtmltopdf binary
ENTRYPOINT []

CMD ["/api-report"]