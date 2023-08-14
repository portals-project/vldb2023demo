FROM portals:latest

COPY . /vldb2023demo

WORKDIR /vldb2023demo

RUN sbt compile

CMD sbt "runMain portals.vldb2023demo.ServerMain 0.0.0.0 8080"