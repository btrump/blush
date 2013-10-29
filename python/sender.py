#!/usr/bin/env python
import pika
import sys

message = ' '.join(sys.argv[1:])
host_ip = '192.168.1.147'
host_port = 5672
queue_name = 'hello'

connection = pika.BlockingConnection(pika.ConnectionParameters(host=host_ip, port=host_port))
channel = connection.channel()
channel.queue_declare(queue=queue_name)
channel.basic_publish(exchange='',
										routing_key=queue_name,
										body=message)
print " [x] Sent %r" % message
connection.close()
