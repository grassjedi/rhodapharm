package rhodapharmacy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class XUnsupportedOperation extends Error {
    public XUnsupportedOperation() {
        super("can't do that");
    }
}
