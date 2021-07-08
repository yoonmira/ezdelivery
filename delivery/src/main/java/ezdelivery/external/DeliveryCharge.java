package ezdelivery.external;

public class DeliveryCharge {

    private Long id;
    private Long orderId;
    private String stauts;
    private Long amount;

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
