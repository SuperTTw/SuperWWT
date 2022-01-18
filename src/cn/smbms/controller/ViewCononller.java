package cn.smbms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.annotation.WebServlet;

@Controller
public class ViewCononller {
    @RequestMapping("/view")
    public String index(){
        return "ViewIndex";
    }

    @RequestMapping(value = "/view2",produces = "text/html;charset=UTF-8")
    public String index2(@RequestParam(value = "name") String name, Model model){
        model.addAttribute("name",name);
        return "success";
    }
}
