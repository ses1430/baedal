package baedal;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long paymentId;
    private String status;

    @PostPersist
    public void onPostPersist(){

        // delivery start <- pay
        DeliveryStarted deliveryStarted = new DeliveryStarted();
        BeanUtils.copyProperties(this, deliveryStarted);
        deliveryStarted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){

        // delivery complete <- actor
        if ("complete".equals(this.getStatus())) {
            DeliveryCompleted deliveryCompleted = new DeliveryCompleted();
            BeanUtils.copyProperties(this, deliveryCompleted);
            deliveryCompleted.publishAfterCommit();
        } 
        // delivery cancel <- pay
        else if ("cancel".equals(this.getStatus())) {
            DeliveryCancelled deliveryCancelled = new DeliveryCancelled();
            BeanUtils.copyProperties(this, deliveryCancelled);
            deliveryCancelled.publishAfterCommit();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
