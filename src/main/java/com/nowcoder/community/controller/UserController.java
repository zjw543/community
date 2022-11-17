package com.nowcoder.community.controller;


import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.Hostholder;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.events.Event;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domin;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String upLoadPath;
    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private Hostholder hostholder;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path ="/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    //上传头像
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    private String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择照片！");
        }
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf('.'));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确！");
        }

        //生成随机文件名
        filename = CommunityUtil.generateUUID()+suffix;
        //确定文件存放路径
        File dest = new File(upLoadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败！"+e.getMessage());
            throw new RuntimeException("上传文件失败！,服务器异常"+e.getMessage());
        }
        //更新用户头像路径（web路径）
        String headerUrl = domin + contextPath + "/user/header/"+filename;
        User user = hostholder.getUser();
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";

    }

    //显示头像
    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename")String filename, HttpServletResponse response) throws IOException {
        filename = upLoadPath + "/" + filename;
        //获得后缀
        String suffix = filename.substring(filename.lastIndexOf('.'));
        //响应图片
        response.setContentType("image/" + suffix);
        //输出到浏览器
        FileInputStream fis = new FileInputStream(filename);
        OutputStream os = response.getOutputStream();//springMVC 会自动关闭
        try {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取文件错误"+e.getMessage());
        } finally {
            fis.close();
        }
    }

    //修改密码
    @LoginRequired
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword,String newPassword){
        User user = hostholder.getUser();
        Map map = userService.updatePassword(user.getId(),oldPassword,newPassword);
        if(map==null){
            model.addAttribute("passwordMsg","密码修改成功");
        }else {
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
        }
        return "/site/setting";
    }

    //访问用户首页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("user is not exist");
        }

        //用户点赞总数
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);

        //粉丝数量
        long followerCount = followService.fingFollowerCount(ENTITY_TYPE_USER,userId);

        //是否关注当前用户
        boolean isFollowed = false;
        if(hostholder!=null){
            isFollowed = followService.hasFollowed(hostholder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }

        model.addAttribute("followeeCount",followeeCount);
        model.addAttribute("followerCount",followerCount);
        model.addAttribute("hasFollowed",isFollowed);

        return "/site/profile";
    }
}
