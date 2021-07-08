package ezdelivery;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name="Cook_table")
public class Cook {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long storeId;
    private Long orderId;
    private String menuName;
    private Long orderNumber;
    private String status;

    @PostPersist
    public void onPostPersist(){
        FoodCooked foodCooked = new FoodCooked();
        BeanUtils.copyProperties(this, foodCooked);

        foodCooked.setOrderId(orderId);
        foodCooked.setStatus("요리완료");
        foodCooked.setStoreId(storeId);
        foodCooked.setMenuName(menuName);
        foodCooked.setOrderNumber(orderNumber);
    
        
        
    
        
        foodCooked.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
