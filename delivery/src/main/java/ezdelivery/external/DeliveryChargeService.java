
package ezdelivery.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="DeliveryCharge", url="http://localhost:8088")
public interface DeliveryChargeService {

    @RequestMapping(method= RequestMethod.GET, path="/deliveryCharges")
    public void provideCharge(@RequestBody DeliveryCharge deliveryCharge);

}