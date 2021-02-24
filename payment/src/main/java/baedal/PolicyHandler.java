package baedal;

import baedal.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @Autowired
    PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_requestPay(@Payload Ordered ordered){

        if(ordered.isMe()){
            System.out.println("##### listener  : " + ordered.toJson());

            Payment payment = new Payment();
            payment.setOrderId(ordered.getId());
            payment.setStatus("paid");
            paymentRepository.save(payment);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_updateDeliveryId(@Payload DeliveryStarted deliveryStarted){

        if(deliveryStarted.isMe()){
            System.out.println("##### listener  : " + deliveryStarted.toJson());

            Payment payment = paymentRepository.findById(deliveryStarted.getPaymentId()).get();
            payment.setDeliveryId(deliveryStarted.getId());
            paymentRepository.save(payment);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_cancelPay(@Payload OrderCancelled orderCancelled){

        if(orderCancelled.isMe()){
            System.out.println("##### listener  : " + orderCancelled.toJson());

            Payment payment = paymentRepository.findById(orderCancelled.getPaymentId()).get();
            payment.setStatus(orderCancelled.getStatus());
            paymentRepository.save(payment);
        }
    }
}
