package cn.smbms.controller;

import cn.smbms.entiy.Bill;
import cn.smbms.entiy.Provider;
import cn.smbms.entiy.User;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.utils.Constants;
import com.mysql.jdbc.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/bill")
public class BillController {
    private Logger logger=Logger.getLogger(BillController.class);
    @Resource
    private BillService billService;
    @Resource
    private ProviderService providerService;


    @RequestMapping(value = "/billList")
    public String billList(Model model,
                           @RequestParam(value ="queryProductName",required = false) String queryProductName,
                           @RequestParam(value ="queryProviderId",required = false) String queryProviderId,
                           @RequestParam(value ="queryIsPayment",required = false) String queryIsPayment){
//        Bill bill=new Bill();
//        bill.setProductName(queryProductName);
//        bill.setProviderId(Integer.parseInt(queryProviderId));
//        bill.setIsPayment(Integer.parseInt(queryIsPayment));
//        List<Bill> billList=billService.getBillList(bill);
//        model.addAttribute("billList",billList);
        List<Bill> billList = null;
        try {
            //供应商名字为空时
            if(StringUtils.isNullOrEmpty(queryProductName)){
                queryProductName = "";
            }
            //订单实体类
            Bill bill = new Bill();
            //是否付款
            if(StringUtils.isNullOrEmpty(queryIsPayment)){
                bill.setIsPayment(0);
            }else{
                bill.setIsPayment(Integer.parseInt(queryIsPayment));
            }
            //供应商id为空时
            if(StringUtils.isNullOrEmpty(queryProviderId)){
                bill.setProviderId(0);
            }else{
                bill.setProviderId(Integer.parseInt(queryProviderId));
            }
            bill.setProductName(queryProductName);
            billList = billService.getBillList(bill);
            //供应商List
            List<Provider> providerList = new ArrayList<Provider>();
            providerList = providerService.getProviderList("","");

            model.addAttribute("providerList", providerList);
            model.addAttribute("billList", billList);
            model.addAttribute("queryProductName", queryProductName);
            model.addAttribute("queryProviderId", queryProviderId);
            model.addAttribute("queryIsPayment", queryIsPayment);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("订单列表接口访问失败");
            return "redirect:/toError";
        }

        return "bill/billlist";
    }

    @RequestMapping(value="/add.html",method= RequestMethod.GET)
    public String add(@ModelAttribute("bill") Bill bill){
        return "bill/billadd";
    }

    @RequestMapping(value="/addSave",method=RequestMethod.POST)
    public String add( Bill b,
                       HttpSession httpSession){
        //设置创建者，创建时间
        b.setCreatedBy(((User)httpSession.getAttribute(Constants.USER_SESSION)).getId());
        b.setCreationDate(new Date());
        boolean flag=billService.add(b);
        if(flag){
            //flag为true表示添加成功后，新数据回显到列表页面
            return "redirect:/bill/billList";
        }
        return "bill/billadd";//如果添加不成功则返回到添加页面
    }

    @RequestMapping(value="view/{id}",method=RequestMethod.GET)
    public String view(@PathVariable String id, Model model){
        Provider provider=providerService.getProviderById(id);
        model.addAttribute(provider);
        return "provider/providerview";
    }

    @GetMapping(value="upd/{id}")
    public String upd(@PathVariable String id,Model model){
        Bill bill=billService.getBillById(id);
        model.addAttribute(bill);
        return "bill/billmodify";
    }

    @PostMapping(value="/billmodifysave")
    public String usermodifysave(Bill bill,HttpSession session){
        bill.setModifyDate(new Date());
        //获取修改人的id
        User u=(User)session.getAttribute(Constants.USER_SESSION);
        bill.setModifyBy(u.getId());
        boolean flag=billService.modify(bill);
        if(flag){
            return "redirect:/bill/billList";
        }
        return "bill/billmodify";
    }
}
