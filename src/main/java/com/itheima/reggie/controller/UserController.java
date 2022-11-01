package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送邮箱验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取邮箱账号
        String phone = user.getPhone();

        String subject = "瑞吉餐购登录验证码";

        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            String context = "欢迎使用瑞吉餐购，登录验证码为: " + code + ",五分钟内有效，请妥善保管!";
            log.info("code={}", code);

            // 真正地发送邮箱验证码
            userService.sendMsg(phone, subject, context);

            //  将随机生成的验证码保存到session中
//            session.setAttribute(phone, code);

            // 验证码由保存到session 优化为 缓存到Redis中，并且设置验证码的有效时间为 5分钟
             redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("验证码发送成功，请及时查看!");
        }
        return R.error("验证码发送失败，请重新输入!");
    }

    /**
     * 用户信息
     *
     * @param
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
//        log.info(map.toString());
        //获得邮箱
        String mailbox = map.get("phone").toString();
        //获得验证码
        String code = map.get("code").toString();
        //从session中获取保存的验证码
//        Object codeInSession = session.getAttribute(mailbox);
        //从redis中获取保存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(mailbox);
        //进行验证码的比对
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果比对成功，说明登录成功
            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,mailbox);

            User user = userService.getOne(queryWrapper);
            if (user == null){
                user=new User();
                user.setPhone(mailbox);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            redisTemplate.delete(mailbox);
            return R.success(user);
        }

        return R.error("登录失败 ");
    }
}
