package baedal;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String status;
    private Long orderId;
    private Long deliveryId;

    @PostPersist
    public void onPostPersist(){
        
        // paid <- order
        if ("paid".equals(this.getStatus())) {
            Paid paid = new Paid();
            BeanUtils.copyProperties(this, paid);
            paid.publishAfterCommit();
        }
    }

    @PreUpdate
    public void onPreUpdate(){

        // cancel pay <- order
        if ("cancel".equals(this.getStatus())) {
            PayCancelled payCancelled = new PayCancelled();
            BeanUtils.copyProperties(this, payCancelled);
            payCancelled.publishAfterCommit();

            //Following code causes dependency to external APIs
            // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

            baedal.external.Delivery delivery = new baedal.external.Delivery();
            delivery.setOrderId(this.getOrderId());
            delivery.setPaymentId(this.getId());
            delivery.setStatus(this.getStatus());
            // mappings goes here
            PaymentApplication.applicationContext.getBean(baedal.external.DeliveryService.class).cancel(this.getDeliveryId(), delivery);
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }




}
