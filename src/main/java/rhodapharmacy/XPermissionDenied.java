package rhodapharmacy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class XPermissionDenied extends Error {
    public XPermissionDenied(String message) {
        super(message);
    }
}
