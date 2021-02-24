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
    OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_updateStatus(@Payload Paid paid){

        if(paid.isMe()){
            System.out.println("##### listener  : " + paid.toJson());

            Order order = orderRepository.findById(paid.getOrderId()).get();
            order.setPaymentId(paid.getId());
            order.setPaymentStatus(paid.getStatus());
            orderRepository.save(order);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCancelled_updateStatus(@Payload PayCancelled payCancelled){

        if(payCancelled.isMe()){
            System.out.println("##### listener  : " + payCancelled.toJson());

            Order order = orderRepository.findById(payCancelled.getOrderId()).get();
            order.setPaymentStatus(payCancelled.getStatus());
            orderRepository.save(order);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCompleted_updateStatus(@Payload DeliveryCompleted deliveryCompleted){

        if(deliveryCompleted.isMe()){
            System.out.println("##### listener  : " + deliveryCompleted.toJson());

            Order order = orderRepository.findById(deliveryCompleted.getOrderId()).get();
            order.setStatus(deliveryCompleted.getStatus());
            orderRepository.save(order);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_updateStatus(@Payload DeliveryStarted deliveryStarted){

        if(deliveryStarted.isMe()){
            System.out.println("##### listener  : " + deliveryStarted.toJson());

            Order order = orderRepository.findById(deliveryStarted.getOrderId()).get();
            order.setDeliveryId(deliveryStarted.getId());
            order.setStatus(deliveryStarted.getStatus());
            orderRepository.save(order);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCancelled_updateStatus(@Payload DeliveryCancelled deliveryCancelled){

        if(deliveryCancelled.isMe()){
            System.out.println("##### listener  : " + deliveryCancelled.toJson());

            Order order = orderRepository.findById(deliveryCancelled.getOrderId()).get();
            order.setStatus(deliveryCancelled.getStatus());
            orderRepository.save(order);
        }
    }
}
