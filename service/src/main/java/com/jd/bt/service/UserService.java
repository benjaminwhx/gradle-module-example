package com.jd.bt.service;

import com.jd.bt.dao.UserDao;
import com.jd.bt.domain.User;
import com.jd.bt.domain.bo.CheckResult;
import com.jd.bt.utils.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by piqiu on 2/23/16.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class UserService {

    @Autowired
    private UserDao userDao;

    public void save(User user) {
        userDao.saveOrUpdate(user);
    }
    /**
     * check userName and email is not exists
     * @param userName
     * @param email
     * @return
     */
    public CheckResult checkUserNameAndEmail(String userName, String email) {
        CheckResult checkResult = new CheckResult();
        List list = userDao.find("select 1 from User where email = ? or userName = ?", email, userName);
        if (list!= null && list.size() > 0) {
            checkResult.setPassCheck(false);
            checkResult.setErrorResult("账号已被注册，请重新注册");
        } else {
            checkResult.setPassCheck(true);
        }
        return checkResult;
    }

    /**
     * login success return userName
     * login failed return null
     * @param email login needed email
     * @param password  login needed password
     * @return
     */
    public String login(String email, String password) {
        User u = userDao.findUniqueBy("email", email);
        if (u != null) {
            if (BCrypt.checkpw(password, u.getPassword())) {
                return u.getUserName();
            }
        }
        return null;
    }

    public boolean updateToken(String userName, String token) {
        int result = userDao.batchExecute("update User set token = ? where userName = ?", token, userName);
        return result > 0;
    }

    public User getUser(String property, Object value) {
        return userDao.findUniqueBy(property, value);
    }

    public String getTokenByUserName(String userName) {
        return userDao.findUnique("select u.token from User u where u.userName = ?", userName);
    }
}
