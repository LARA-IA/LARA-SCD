import pika
import json
import os
import sys
import service.predict as predict_service

def main():
    connection = None
    try:
        host = os.getenv('RABBITMQ_HOST', 'localhost')
        connection = pika.BlockingConnection(pika.ConnectionParameters(host=host))
        channel = connection.channel()

        channel.queue_declare(queue='predict_queue', durable=True)

        def callback(ch, method, properties, body):
            print(f" [x] Recebido mensagem")
            try:
                data = json.loads(body)
                predict_service.process_message(data)
            except Exception as e:
                print(f"     Erro ao processar consumer: {e}")

        print(' [*] Aguardando mensagens. Para sair pressione CTRL+C')
        channel.basic_consume(queue='predict_queue', on_message_callback=callback, auto_ack=True)

        channel.start_consuming()
    except Exception as e:
        print(f"Erro: {e}")
        if connection:
            connection.close()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print('Interrompido')
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
