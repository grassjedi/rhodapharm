package rhodapharmacy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/")
public class HomeController {

    @GetMapping
    public ModelAndView home(UserSession userSession) {
        Map<String, Object> model = new HashMap<>();
        model.put("email", userSession.getUser().getEmail());
        return new ModelAndView("index", model);
    }



    @ModelAttribute("userSession")
    public UserSession getAuthToken(HttpServletRequest request) {
        return (UserSession) request.getAttribute("userSession");
    }
}
