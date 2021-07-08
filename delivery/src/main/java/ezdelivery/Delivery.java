package ezdelivery;

import javax.management.RuntimeErrorException;
import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String status;

    @PostPersist
    public void onPostPersist(){

        ezdelivery.external.DeliveryCharge deliveryCharge = new ezdelivery.external.DeliveryCharge();
        BeanUtils.copyProperties(this, deliveryCharge);
        deliveryCharge.setOrderId(orderId);
        deliveryCharge.setStauts("배달료지불");

        try {
            DeliveryApplication.applicationContext.getBean(ezdelivery.external.DeliveryChargeService.class).provideCharge(deliveryCharge);
        }catch(Exception e){
            throw new RuntimeErrorException(null, "배달료지불 실패 입니다"+e.getLocalizedMessage());
        }

        //-----------------------
        //라이더에게 배달료 지불되면 최종적으로 배달 시작 이벤트발생
        //-------------------------

        DeliveryStarted deliveryStarted = new DeliveryStarted();
        BeanUtils.copyProperties(this, deliveryStarted);    
        deliveryStarted.setStatus("배달중");
        
        deliveryStarted.publishAfterCommit();


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
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
