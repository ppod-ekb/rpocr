FROM spark-base:2.4.4

ENV SPARK_NO_DAEMONIZE=true
ENV SPARK_MASTER_WEBUI_PORT=8080
ENV SPARK_MASTER_LOG=/spark/logs
ENV SPARK_MASTER_HOST=172.17.0.5
ENV SPARK_MASTER_PORT=7077
ENV SPARK_HOME=/opt/spark-2.4.4-bin-hadoop2.7
ENV SPARK_SCALA_VERSION=2.12

RUN mkdir -p /mnt/spark/volume

EXPOSE 8080 7077 6066

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/opt/spark-2.4.4-bin-hadoop2.7/sbin/start-master.sh"]
