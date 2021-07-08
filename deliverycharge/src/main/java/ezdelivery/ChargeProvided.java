package ezdelivery;

public class ChargeProvided extends AbstractEvent {

    private Long id;
    private Long orderId;
    private String status;
    private Long amount;


    public ChargeProvided(){
        super();
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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
    @Override
    public String toString() {
        return "DeliveryStarted [id=" + id + ", orderId=" + orderId + ", status=" + status + "]";
    }
}
