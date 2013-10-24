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
                                   queue=self.callback_queue)

    def on_response(self, ch, method, props, body):
        if self.corr_id == props.correlation_id:
            self.response = body

    def call(self, message):
        print " [x] Sending '%s'" % (message)
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(exchange='',
                                   routing_key='lobby',
                                   properties=pika.BasicProperties(
                                         reply_to = self.callback_queue,
                                         correlation_id = self.corr_id,
                                         ),
                                   body=str(message))
        while self.response is None:
            self.connection.process_data_events()
        return self.response
    
    def get_user_input(self):
        prompt = '> '
        message = raw_input(prompt)
        return message
    
    def handle_message(self, message):
        print " [.] Received %r" % message
        
    def start(self):
        while(True):
            self.handle_message(self.call(self.get_user_input()))

client = Client()
client.start()
