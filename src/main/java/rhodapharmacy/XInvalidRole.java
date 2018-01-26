package rhodapharmacy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class XInvalidRole extends Exception {

    public XInvalidRole(String role) {
        super("Invalid role: " + role);
    }

}
