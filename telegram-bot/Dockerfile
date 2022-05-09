from alpine:latest
RUN apk add --no-cache py3-pip \
    && pip3 install --upgrade pip

COPY . /app

ENV VIRTUAL_ENV=/app/venv
RUN python3 -m venv $VIRTUAL_ENV
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

RUN chgrp -R 0 /app && \
    chmod -R g=u /app

WORKDIR /app


RUN pip3 --no-cache-dir install -r requirements.txt

EXPOSE 80
EXPOSE 88
EXPOSE 443
EXPOSE 8443
EXPOSE 995
ENTRYPOINT ["python3"]
CMD ["main.py"]