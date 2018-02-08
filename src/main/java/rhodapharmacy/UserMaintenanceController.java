package rhodapharmacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import rhodapharmacy.domain.User;
import rhodapharmacy.domain.UserSession;
import rhodapharmacy.repo.UserRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
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
        List<User> users = new LinkedList<>();
        userRepository.findAll().forEach(users::add);
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
            User user = userRepository.findOne(userId);
            UserRole.validate(roles);
            user.setRoles(roles);
            userRepository.save(user);
        }
        else if("create".equalsIgnoreCase(operation)) {
            if(email != null && !"".equals(email.trim())) {
                UserRole.validate(roles);
                User user = new User();
                user.setEmail(email);
                user.setRoles(roles);
                userRepository.save(user);
            }
        }
        else if("disable".equalsIgnoreCase(operation)) {
            User user = userRepository.findOne(userId);
            user.setDisabled(new Date());
            userRepository.save(user);
        }
        else if("enable".equalsIgnoreCase(operation)) {
            User user = userRepository.findOne(userId);
            user.setDisabled(null);
            userRepository.save(user);
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
        User user = userRepository.findOne(userId);
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
