package ezdelivery;

public class FoodCooked extends AbstractEvent {

    private Long id;

    public FoodCooked(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
