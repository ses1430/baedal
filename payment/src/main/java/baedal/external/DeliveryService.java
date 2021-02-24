
package baedal.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="delivery", url="http://delivery:8080")
public interface DeliveryService {

    /*
    @RequestMapping(method= RequestMethod.PATCH, path="/deliveries")
    public void cancel(@RequestBody Delivery delivery);
    */

    @RequestMapping(method= RequestMethod.PUT, path="/deliveries/{id}")
    public void cancel(@PathVariable(value="id") long id, @RequestBody Delivery delivery);
}