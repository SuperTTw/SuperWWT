package cn.smbms.controller;

import cn.smbms.entiy.Provider;
import cn.smbms.entiy.User;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.utils.Constants;
import cn.smbms.utils.PageSupport;
import com.mysql.jdbc.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/provider")
public class ProviderController {
    private static final Logger logger= Logger.getLogger(UserController.class);
    @Resource
    private ProviderService providerService;

    @RequestMapping(value="/providerlist")
    public String providerlist(Model model,
                               @RequestParam(value="queryProName",required=false) String queryProName,
                               @RequestParam(value="queryProCode",required=false) String queryProCode
                               ){
        logger.info("ProviderController-->providerlist方法");
        // 查询用户列表
        List<Provider> providerList = null;

        System.out.println("queryProName servlet--------" + queryProName);
        System.out.println("queryProrCode servlet--------" + queryProCode);

        if (queryProName == null) {
            queryProName = "";
        }
        if (queryProCode == null) {
            queryProCode = "";
        }


        providerList = providerService.getProviderList(queryProName, queryProCode);
        model.addAttribute("providerList", providerList);
        model.addAttribute("queryProName", queryProName);
        model.addAttribute("queryProCode", queryProCode);

        return "/provider/providerlist";
    }

    @RequestMapping(value="/add.html",method= RequestMethod.GET)
    public String add(@ModelAttribute("provider") Provider provider){
        return "provider/provideradd";
    }

    @RequestMapping(value="/addSave",method=RequestMethod.POST)
    public String add( Provider p,
                       HttpSession httpSession){
        //设置创建者，创建时间
        p.setCreatedBy(((User)httpSession.getAttribute(Constants.USER_SESSION)).getId());
        p.setCreationDate(new Date());
        boolean flag=providerService.add(p);
        if(flag){
            //flag为true表示添加成功后，新数据回显到列表页面
            return "redirect:/provider/providerlist.html";
        }
        return "provider/provideradd";//如果添加不成功则返回到添加页面
    }

    @RequestMapping(value="view/{id}",method=RequestMethod.GET)
    public String view(@PathVariable String id, Model model){
        Provider provider=providerService.getProviderById(id);
        model.addAttribute(provider);
        return "provider/providerview";
    }

    @GetMapping(value="upd/{id}")
    public String upd(@PathVariable String id,Model model){
        Provider provider=providerService.getProviderById(id);
        model.addAttribute(provider);
        return "provider/providermodify";
    }

    @PostMapping(value="/providermodifysave")
    public String usermodifysave(Provider provider,HttpSession session){
        provider.setModifyDate(new Date());
        //获取修改人的id
        User u=(User)session.getAttribute(Constants.USER_SESSION);
        provider.setModifyBy(u.getId());
        boolean flag=providerService.modify(provider);
        if(flag){
            return "redirect:/provider/providerlist";
        }
        return "provider/providermodify";
    }

    @ResponseBody
    @GetMapping(value = "/providerview")
    public Object providerview(@RequestParam(value = "id") String id){
        Provider providerJson=null;
        if(null==id||"".equals(id)){
            return "nodata";
        }else {
            try{
                providerJson=providerService.getProviderById(id);
            }catch (Exception e){
                e.printStackTrace();
                return "failed";
            }
        }
        return providerJson;
    }

    @ResponseBody
    @GetMapping("/providerExit")
    public Object providerExit(@RequestParam(value = "proCode") String proCode){
        HashMap<String,Object> map=new HashMap<>();
        if(StringUtils.isNullOrEmpty(proCode)){
            map.put("exist",1);
        }else{
            Provider provider=providerService.getProviderById(proCode);
            if(null!=provider){
                map.put("exist",1);
            }else {
                map.put("exist",0);
            }
        }
        return map;
    }
}
