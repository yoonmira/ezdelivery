package ezdelivery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.lang.reflect.*;

import sun.misc.Unsafe;



 @RestController
 public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @RequestMapping(value = "/orders/order.do", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> changeOrder(@RequestBody HashMap<String, String> userMap) {
        
		System.out.println("\n\n##### changeOrder userMap : " + userMap + "\n\n");
		
		String  bizDvn = userMap.get("bizDvn"); 
		String  orderId = userMap.get("orderId");
		String  status = userMap.get("status");

		Map<String, Object> result = new HashMap<>();


		if( "PATCH".equals(bizDvn)) {
			Optional<Order> orders = orderRepository.findById(Long.parseLong(orderId));

			if(!orders.isPresent()) {

				System.out.println("\n\n##### orders table no data found by : " + orderId + "\n\n");
				return null;
	
			}

			Order order = orders.get();

			order.setStatus(status);

			orderRepository.save(order);

		}

		return ResponseEntity.ok().body(result);
    }


 @GetMapping("/callMemleak")
 public void callMemleak() {
  try {
   this.memLeak();
  }catch (Exception e){
   e.printStackTrace();
  }
 }

 public void memLeak() throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
  Class unsafeClass = Class.forName("sun.misc.Unsafe");
  Field f = unsafeClass.getDeclaredField("theUnsafe");
  f.setAccessible(true);
  Unsafe unsafe = (Unsafe) f.get(null);
  System.out.print("4..3..2..1...");
  try
  {
   for(;;)
    unsafe.allocateMemory(1024*1024);
  } catch(Error e) {
   System.out.println("Boom :)");
   e.printStackTrace();
  }
 }

 }







