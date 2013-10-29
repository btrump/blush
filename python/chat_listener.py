#!/usr/bin/env python
import pika
import uuid

class Client(object):
    def __init__(self):
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(
                host='localhost'))

        self.channel = self.connection.channel()

        result = self.channel.queue_declare(exclusive=True)
        self.callback_queue = result.method.queue

        self.channel.basic_consume(self.on_response, no_ack=True,
                                   queue="rpc_queue_chat")

    def on_response(self, ch, method, props, body):
        if self.corr_id == props.correlation_id:
            self.response = body

    def listen(self):
        self.response = None
        while self.response is None:
            self.connection.process_data_events()
        return self.response

client = Client()

while (True):
    response = client.listen()
    print " [.] Got %r" % (response,)
