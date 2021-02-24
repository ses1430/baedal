
package baedal.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;

// docker : url="http://payment:8080")
@FeignClient(name="payment", url="http://localhost:8083")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.PUT, path="/payments/{id}")
    public void cancel(@PathVariable(value="id") long id, @RequestBody Payment payment);
}