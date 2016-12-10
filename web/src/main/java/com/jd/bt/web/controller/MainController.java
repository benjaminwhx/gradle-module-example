package com.jd.bt.web.controller;

import com.jd.bt.common.CookieManager;
import com.jd.bt.domain.Collections;
import com.jd.bt.domain.Product;
import com.jd.bt.domain.User;
import com.jd.bt.domain.bo.CheckResult;
import com.jd.bt.service.UserService;
import com.jd.bt.utils.BCrypt;
import com.jd.bt.utils.IPUtil;
import com.jd.bt.utils.MD5Util;
import com.jd.bt.utils.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benjamin on 16/6/14.
 */
@Controller
public class MainController extends BaseController {
    private static final String NO_LOGIN_ERROR_MSG = "您还没有登录，请登录后再进行操作!";
    private static final String MSG_KEY = "msg";
    public static final String REMEMBER_LOGIN_STATUS_TOKEN_KEY = "YS_RM_TOKEN";
    public static final String USERNAME_COOKIE_KEY= "YS_EM";
    Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/index.html", "/"})
    public String goMainPage() {
        return "main";
    }

    /** 访问登录页 **/
    @RequestMapping(value = "/login.html")
    public String goLoginPage() {
        return "login";
    }

    /** 访问注册页 **/
    @RequestMapping(value = "/register.html")
    public String goRegisterPage() {
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(User user, RedirectAttributes redirectAttributes) {
        CheckResult checkResult = userService.checkUserNameAndEmail(user.getUserName(), user.getEmail());
        if (checkResult.isPassCheck()) {
            String ip = IPUtil.getIp(request);
            user.setIpAddress(ip);
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userService.save(user);
            redirectAttributes.addFlashAttribute(MSG_KEY, "恭喜 " + user.getUserName() + " 注册成功!");
            return "redirect:/login.html";
        } else {
            redirectAttributes.addFlashAttribute(MSG_KEY, checkResult.getErrorResult());
            return "redirect:/register.html";
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(User user) {
        ModelAndView modelAndView = new ModelAndView();
        if (session.getAttribute("userName") != null) {
            logger.error("login to the error process");
            modelAndView.setViewName("redirect:/index.html");
        } else {
            String userName = userService.login(user.getEmail(), user.getPassword());
            if (userName != null) {
                // 登录成功把用户名放入session
                session.setAttribute("userName", userName);
                String remember = request.getParameter("remember");
                if (remember != null && "on".equals(remember)) {
                    String randomAlphabet = RandomUtil.getRandomAlphabet(10);
                    // token值为 用户名+随机生成的数 加密md5得到
                    String token = MD5Util.MD5(userName + randomAlphabet);
                    logger.info("user " + userName + " open remember me, default two weeks. token value is: " + token);
                    // 更新库里面的token值
                    if (userService.updateToken(userName, token)) {
                        CookieManager.addCookie(response, REMEMBER_LOGIN_STATUS_TOKEN_KEY, token, 14* 24* 60* 60);
                        CookieManager.addCookie(response, USERNAME_COOKIE_KEY, userName, 14* 24* 60* 60);
                    } else {
                        logger.error("update token error.");
                    }
                }
                modelAndView.setViewName("redirect:/index.html");
            } else {
                modelAndView.setViewName("login");
                modelAndView.addObject(MSG_KEY, "邮箱或密码错误!");
            }
        }
        return modelAndView;
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public @ResponseBody
    String checkUserNameOrEmail(String userNameOrEmail) {
        CheckResult checkResult;
        if (userNameOrEmail.contains("@")) {
            checkResult = userService.checkUserNameAndEmail(null, userNameOrEmail);
        } else {
            checkResult = userService.checkUserNameAndEmail(userNameOrEmail, null);
        }
        if (checkResult.isPassCheck()) {
            return "true";
        } else {
            return "false";
        }
    }

    /** 进入个人收藏页 **/
    @RequestMapping(value = "/collections.html")
    public ModelAndView showCollectionsPage(RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView();
        String userName = (String) session.getAttribute("userName");
        if (userName == null) {
            modelAndView.setViewName("redirect:/login.html");
            redirectAttributes.addFlashAttribute(MSG_KEY, NO_LOGIN_ERROR_MSG);
            return modelAndView;
        }
        User currentUser = userService.getUser("userName", userName);
        List<Product> products = new ArrayList<>();
        List<Collections> collectionsList = currentUser.getCollectionsList();
        for (Collections c : collectionsList) {
            products.add(c.getProduct());
        }
        modelAndView.addObject(products);
        modelAndView.setViewName("collections");
        return modelAndView;
    }
}
