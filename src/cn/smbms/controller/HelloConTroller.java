package cn.smbms.controller;

import cn.smbms.entiy.User;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/user")
public class HelloConTroller{
   Logger logger=Logger.getLogger(HelloConTroller.class);

    /**
     * 第一个Spring注解方式方法
     *
     * @return index
     */
   @RequestMapping("/index")
   public String index(){
       logger.debug("我的第一个注解方式SpringMVC运行成功！");
       return "index";
   }

    @RequestMapping("/index2")
    public ModelAndView index2(@RequestParam(required = false) String name){
        ModelAndView model=new ModelAndView();

        User user=new User();
        user.setUserName(name);
//        logger.debug("我的第一个注解方式SpringMVC运行成功！name="+name);
        model.addObject("uName",name);
        model.addObject("user",user);
        model.addObject(name);
        model.setViewName("/user/userIndex");
        return model;
    }
}
