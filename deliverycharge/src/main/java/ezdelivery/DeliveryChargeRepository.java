package ezdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="deliveryCharges", path="deliveryCharges")
public interface DeliveryChargeRepository extends PagingAndSortingRepository<DeliveryCharge, Long>{


}
