package cn.smbms.controller;

import cn.smbms.entiy.Role;
import cn.smbms.entiy.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.utils.Constants;
import cn.smbms.utils.PageSupport;
import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    private Logger logger=Logger.getLogger(UserController.class);
//    UserService u=new UserServiceImpl();
    @Resource
    private RoleService roleService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/login.html")
    public String Login(){
        return "/user/login";
    }

    @RequestMapping("/logins")
    public String farme(HttpSession session){
        User user=(User) session.getAttribute(Constants.USER_SESSION);
        if (user==null){
            return "/user/login";
        }
        return "/frame";
    }

//    @PostMapping("/doLogin")
//    public String doLogin(@RequestParam(value ="userCode") String userCode,
//                          @RequestParam(value ="userPassword") String userPassword,
//                          HttpSession session,
//                          HttpServletRequest request) {
//        User user=userService.login(userCode,userPassword);
//        if(null!=user){
//            session.setAttribute(Constants.USER_SESSION,user);
//            return "redirect:/user/logins";
//        }else {
//            request.setAttribute("error","账号或用户名错误，请重新登陆！");
//            return "/user/login";
//        }
//    }

//    /**
//     * 局部异常处理
//     * @param e
//     * @param request
//     * @return
//     */
//    @ExceptionHandler(value = {Exception.class})
//    public String handlerException(Exception e,HttpServletRequest request){
//        request.setAttribute("e",e);
//        return "error";
//    }

    @PostMapping("/exdoLogin")
    public String exdoLogin(@RequestParam(value ="userCode") String userCode,
                          @RequestParam(value ="userPassword") String userPassword,
                            HttpSession session) {
        User user=userService.login(userCode,userPassword);
        if(null==user){
            throw new RuntimeException("用户名或密码不正确");
        }
        session.setAttribute(Constants.USER_SESSION,user);
        return "redirect:/user/logins";
    }

    @RequestMapping("/loginOut")
    public String LoginOut(HttpSession httpSession){
        logger.info("注销");
        httpSession.removeAttribute(Constants.USER_SESSION);
        return "/user/login";
    }

    @RequestMapping(value="/ulist")
    public String userlist(Model model,
                           @RequestParam(value="queryUserName",required=false) String queryUserName,
                           @RequestParam(value="queryUserRole",required=false) String queryUserRole,
                           @RequestParam(value="pageIndex",required=false) String pageIndex){
        logger.info("UserController-->userlist方法");
        // 查询用户列表
        int _queryUserRole = 0;
        List<User> userList = null;
        // 设置页面容量
        int pageSize = Constants.pageSize;
        // 当前页码
        int currentPageNo = 1;
        if (queryUserName == null) {
            queryUserName = "";
        }
        if (queryUserRole != null && !queryUserRole.equals("")) {
            _queryUserRole = Integer.parseInt(queryUserRole);
        }

        if (pageIndex != null) {
            try {
                currentPageNo = Integer.parseInt(pageIndex);
            } catch (NumberFormatException e) {

                //重定向到/user/syserror.html
                return "redirect:/user/syserror.html";
            }
        }
        // 总数量（表）
        int totalCount = userService.getUserCount(queryUserName, _queryUserRole);
        // 总页数
        PageSupport pages = new PageSupport();
        pages.setCurrentPageNo(currentPageNo);
        pages.setPageSize(pageSize);
        pages.setTotalCount(totalCount);

        int totalPageCount = pages.getTotalPageCount();

        // 控制首页和尾页
        if (currentPageNo < 1) {
            currentPageNo = 1;
        } else if (currentPageNo > totalPageCount) {
            currentPageNo = totalPageCount;
        }

        userList = userService.getUserList(queryUserName, _queryUserRole, currentPageNo, pageSize);
        model.addAttribute("userList", userList);
        List<Role> roleList = null;
        roleList = roleService.getRoleList();
        model.addAttribute("roleList", roleList);
        model.addAttribute("queryUserName", queryUserName);
        model.addAttribute("queryUserRole", queryUserRole);
        model.addAttribute("totalPageCount", totalPageCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPageNo", currentPageNo);
        return "user/userlist";
    }

    @RequestMapping("/syserror.html")
    public String syserror(){
        return "syserror";
    }

    @RequestMapping(value="/add.html",method=RequestMethod.GET)
    public String add(@ModelAttribute("user") User user){
        return "user/useradd";
    }

    @RequestMapping(value="/addSave",method=RequestMethod.POST)
    public String add( User u,
                      HttpSession httpSession){
        //设置创建者，创建时间
        User loginUser=(User)httpSession.getAttribute(Constants.USER_SESSION);
        u.setCreatedBy(loginUser.getId());//当前操作用户
        //创建时间
        u.setCreationDate(new Date());
        boolean flag=userService.add(u);
        if(flag){
            //flag为true表示添加成功后，新数据回显到列表页面
            return "redirect:/user/ulist";
        }
        return "user/useradd";//如果添加不成功则返回到添加页面
    }

    @RequestMapping(value="view/{id}",method=RequestMethod.GET)
    public String view(@PathVariable String id,Model model){
        User user=userService.getUserById(id);
        model.addAttribute(user);
        return "user/userview";
    }

    @GetMapping(value="upd/{id}")
    public String upd(@PathVariable String id,Model model){
        User user=userService.getUserById(id);
        model.addAttribute(user);
        return "user/usermodify";
    }

    @PostMapping(value="/usermodifysave")
    public String usermodifysave(User user,HttpSession session){
        user.setModifyDate(new Date());
        //获取修改人的id
        User u=(User)session.getAttribute(Constants.USER_SESSION);
        user.setModifyBy(u.getId());
        boolean flag=userService.modify(user);
        if(flag){
            return "redirect:/user/ulist";
        }
        return "user/usermodify";
    }

    @RequestMapping(value="/addsave.html",method=RequestMethod.POST)
    public String addsave(User u,HttpSession httpSession,
                          HttpServletRequest request,
                          @RequestParam(value="attke",required=false) MultipartFile[] attke){
        //文件上传相关代码
        String id_PicPath=null;
        String work_PicPath=null;
        String path=null;
        String errorInfo = null;
        boolean flag = true;
        String filepath=null;
        //1.判断文件是否为空
        for (int i=0;i< attke.length;i++)
        if(!attke[i].isEmpty()){//不为空
            //2.定义上传的目标文件的路径
            //java.io.File.separator这里表示路径格式是自适应的，可以适应于各种系统，达到低入侵性
            if(i == 0){
                errorInfo = "uploadFileError";
                filepath="uploadfiles";
            }else if(i == 1){
                errorInfo = "uploadFileError2";
                filepath="wkiploadfiles";
            }
             path = request.getSession().getServletContext().getRealPath("statics"+File.separator+filepath);
            //3.获取原文件名,用原文件名可以得到文件的后缀
            String oldFileName=attke[i].getOriginalFilename();
            //4.4.prefix表示得到文件的后缀,得到文件的后缀可以判断上传的文件的格式是否合法
            String prefix= FilenameUtils.getExtension(oldFileName);
            int filesize=5000000;//文件大小定义为500kb
            //5.判断上传文件的大小
            if(attke[i].getSize()>filesize){
                request.setAttribute("uploadFileError", "上传大小不能超过500KB");
                return "useradd";
            }else if(prefix.equalsIgnoreCase("jpg")
                    || prefix.equalsIgnoreCase("jpeg")
                    || prefix.equalsIgnoreCase("png")
                    || prefix.equalsIgnoreCase("pneg")){
                //定义图片名:当前系统时间+随机数+"_Personal.jpg”
                //RandomUtils.nextInt( 10e008日)表示e-108e880之间的随机数--
                String fileName=System.currentTimeMillis()+"_Personal."+prefix;
                //参数1目标路径参数2文件名,这个对象用来接收用户上传的文件
                File targetFile=new File(path,fileName);
                if(!targetFile.exists()){
                    targetFile.mkdirs();//如果这个文件不存在则自己创建一下
                }
                //6.将multipartfile中的文件输出到targetfile中去
                try {
                    attke[i].transferTo(targetFile);
                } catch (Exception e) {
                    //如果上传失败则设置失败信息,并且返回到页面
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    request.setAttribute(errorInfo, "上传失败");
                   flag=false;
                }
                //7.构建存入数据库中的路径
                if(i == 0){
                    id_PicPath = path+File.separator+fileName;
                }else if(i == 1){
                    work_PicPath = path+File.separator+fileName;
                }
            }else{//上传图片格式不正确时
                request.setAttribute(errorInfo, "上传图片格式错误");
                flag=false;
            }
        }
        //如果没有相应的文件上传
        if(flag){
            u.setIdPicPath(id_PicPath);
            u.setWorkPicPath(work_PicPath);
            //设置创建者，创建时间
            User loginUser=(User)httpSession.getAttribute(Constants.USER_SESSION);
            u.setCreatedBy(loginUser.getId());//当前操作用户
            //创建时间
            u.setCreationDate(new Date());
            if(userService.add(u)){
                //flag为true表示添加成功后，新数据回显到列表页面
                return "redirect:/user/ulist";
            }
        }
        return "user/useradd";//如果添加不成功则返回到添加页面
    }

    @ResponseBody
    @GetMapping("/userExit")
    public Object userExit(@RequestParam(value = "userCode") String userCode){
        HashMap<String,Object> map=new HashMap<>();
        if(StringUtils.isNullOrEmpty(userCode)){
            map.put("exist",1);
        }else{
            User user=userService.selectUserCodeExist(userCode);
            if(null!=user){
                map.put("exist",1);
            }else {
                map.put("exist",0);
            }
        }
        return map;
    }

    @ResponseBody
    @GetMapping(value = "/userview")
    public Object userview(@RequestParam(value = "id") String id){
        User userJson=null;
        if(null==id||"".equals(id)){
            return "nodata";
        }else {
            try{
                userJson=userService.getUserById(id);
            }catch (Exception e){
                e.printStackTrace();
                return "failed";
            }
        }
        return userJson;
    }

    @ResponseBody
    @GetMapping(value = "/flagPwd")
    public Object flagPwd(@RequestParam(value = "oldpassword") String oldpassword,
                           HttpSession session){
        HashMap<String,Object> map=new HashMap<>();
        String pwd=((User)session.getAttribute(Constants.USER_SESSION)).getUserPassword();
        if(null==pwd||"".equals(pwd)){
            map.put("result","error");
        }else{
            if(oldpassword.equals(pwd)){
                map.put("result","true");
            }else {
                map.put("result","false");
            }
        }
        return map;
    }

    @RequestMapping("/pwdmodify.html")
    public String pwdmodify(){
        return "user/pwdmodify";
    }


    @PostMapping(value = "Uupdata")
    public String Uupdata(HttpSession session ,@RequestParam(value = "newpassword") String newpassword){
        logger.info("进行修改密码数据保存操作--------------》");
        Integer id =((User)session.getAttribute(Constants.USER_SESSION)).getId();
        if(userService.updatePwd(id,newpassword)){
            return "redirect:/user/ulist";
        }
        return "user/pwdmodify";
    }

    @ResponseBody
    @GetMapping(value = "/selRole")
    public Object selRole(){
        List<Role> roleList = roleService.getRoleList();
        return roleList;
    }
}
