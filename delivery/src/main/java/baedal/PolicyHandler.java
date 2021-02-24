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
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_requestDelivery(@Payload Paid paid){

        if(paid.isMe()){
            System.out.println("##### listener  : " + paid.toJson());

            Delivery delivery = new Delivery();
            delivery.setOrderId(paid.getOrderId());
            delivery.setPaymentId(paid.getId());
            delivery.setStatus("started");
            deliveryRepository.save(delivery);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_cancelDelivery(@Payload PayCancelled payCancelled){

        if(payCancelled.isMe()){
            System.out.println("##### listener  : " + payCancelled.toJson());

            Delivery delivery = deliveryRepository.findById(payCancelled.getOrderId()).get();
            delivery.setStatus("cancel");
            deliveryRepository.save(delivery);
        }
    }
}
