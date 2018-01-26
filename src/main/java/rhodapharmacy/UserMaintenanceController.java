package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserMaintenanceController {

    private static Logger log = LoggerFactory.getLogger(UserMaintenanceController.class);
    private UserRepository userRepository;

    public UserMaintenanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ModelAndView home(@RequestAttribute UserSession userSession)
    throws SQLException {
        log.debug("{} session {}, user {} ", userSession, userSession.getSessionKey(), !userSession.isAnonymous() ? "authorised" : "NOT authorised");
        if(!userSession.isAuthorised() || !userSession.hasRole(UserRole.USER_ADMIN)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform user administration");
        }
        List<User> users = userRepository.listUsers();
        return new ModelAndView("user", Util.mapOf("users", users));
    }

    @PostMapping
    public String updateUser(
            @RequestAttribute UserSession userSession,
            String operation,
            Long userId,
            String email,
            String roles)
    throws SQLException, XInvalidRole {
        if(!userSession.isAuthorised()
                || !userSession.hasRole(UserRole.USER_ADMIN)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform user administration");
        }
        if("update".equalsIgnoreCase(operation)) {
            userRepository.updateUser(userId, roles);
        }
        else if("create".equalsIgnoreCase(operation)) {
            if(email != null && !"".equals(email.trim())) {
                userRepository.addUser(email, roles);
            }
        }
        else if("disable".equalsIgnoreCase(operation)) {
            userRepository.disableUser(userId);
        }
        else if("enable".equalsIgnoreCase(operation)) {
            userRepository.enableUser(userId);
        }
        else {
            throw new XUnsupportedOperation();
        }
        return "redirect:/user";
    }

    @GetMapping(path = "/{userId}")
    public ModelAndView showUser(
            @RequestAttribute UserSession userSession,
            @PathVariable Long userId)
    throws SQLException {
        if(!userSession.isAuthorised()
                || !userSession.hasRole(UserRole.USER_ADMIN)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform user administration");
        }
        User user = userRepository.retrieveUser(userId);
        return new ModelAndView("show_user", Util.mapOf("allRoles", UserRole.values(), "user", user));
    }

    @GetMapping(path = "/add")
    public ModelAndView newUser(
            @RequestAttribute UserSession userSession) {
        if(!userSession.isAuthorised()
                || !userSession.hasRole(UserRole.USER_ADMIN)) {
            throw new XPermissionDenied("\"" + userSession.getUserEmail() + "\" may not perform user administration");
        }
        return new ModelAndView("add_user", Collections.emptyMap());
    }
}
