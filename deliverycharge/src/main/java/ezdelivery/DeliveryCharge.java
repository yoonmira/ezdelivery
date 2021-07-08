package ezdelivery;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="DeliveryCharge_table")
public class DeliveryCharge {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String stauts;
    private Long amount;

    @PostPersist
    public void onPostPersist(){
        
        
        try{
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        ChargeProvided chargeProvided = new ChargeProvided();
        BeanUtils.copyProperties(this, chargeProvided);
        chargeProvided.setStatus ("배달료지급");
        chargeProvided.publishAfterCommit();


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
    public String getStauts() {
        return stauts;
    }

    public void setStauts(String stauts) {
        this.stauts = stauts;
    }
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }




}
