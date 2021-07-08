
package ezdelivery.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//api.url.payment ==> http://localhost:8083
@FeignClient(name="payment", url="http://localhost:8083")
//@FeignClient(name="payment", url="${api.url.payment}")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.GET, path="/payments")
    public void makePay(@RequestBody Payment payment);

}