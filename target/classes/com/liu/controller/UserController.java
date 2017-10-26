package com.liu.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.liu.pojo.User;
import com.liu.pojo.UserLog;
import com.liu.service.impl.UserLogServiceImpl;
import com.liu.service.impl.UserServiceImpl;
import com.liu.utils.DateUtil;

@Controller
public class UserController {
	@Resource
	private UserServiceImpl userServiceImpl;
	@Resource
	private UserLogServiceImpl userLogServiceImpl;
	DateUtil dateUtil=new DateUtil();
//	չʾ�û���Ϣ
	@RequestMapping("information/{userid}")
	public ModelAndView information(@PathVariable("userid")String userid)
	{
		ModelAndView modelAndView=new ModelAndView();
		User user=userServiceImpl.selectUserByUserId(userid);
		modelAndView.addObject("user", user);
		modelAndView.setViewName("information");
		return modelAndView;
	}
//	��Ϣ����
	@RequestMapping("infosetting/{userid}")
	public ModelAndView infosetting(@PathVariable("userid")String userid,HttpServletRequest request){
		ModelAndView modelAndView=new ModelAndView();
		User user=userServiceImpl.selectUserByUserId(userid);
		modelAndView.addObject("user", user);
		modelAndView.setViewName("info-setting");
		return modelAndView;
	}
//	�޸��û���Ϣ
	@RequestMapping("/updateUser/{userid}")
	public String updateUser(User user,@PathVariable("userid")String userid,RedirectAttributes redirectAttributes,HttpServletRequest request){
		user.setUserid(userid);
		boolean flag=userServiceImpl.updateUser(user);
		if(flag)
		{
			UserLog userLog=new UserLog();
			String ip=request.getRemoteAddr();
			userLog.setUserid(user.getUserid());
			userLog.setTime(dateUtil.getDateformat());
			userLog.setType("�޸�");
			userLog.setDetail("�޸�����");
			userLog.setIp(ip);
			userLogServiceImpl.insertLog(userLog);
			redirectAttributes.addFlashAttribute("message", "["+userid+"]�����޸ĳɹ�!");
			return "redirect:/information/"+userid;
		}
		else
		{
			redirectAttributes.addFlashAttribute("error", "["+userid+"]�����޸�ʧ��!");
			return "redirect:/information/"+userid;
		}
		
	}
//	�ϴ�ͷ��
	@RequestMapping("/upload/{userid}")
	public String upload(@PathVariable("userid")String userid,MultipartFile file,HttpServletRequest request,RedirectAttributes redirectAttributes) throws IllegalStateException, IOException{
		String real=request.getServletContext().getRealPath("/");
		String imagename=file.getOriginalFilename();
		System.out.println("imagename:"+imagename);
		String imageurlnotag="/information/upload/"+userid+"/"+imagename;
		File file1=new File(real+"\\information\\upload"+"\\"+userid);
		if(!file1.exists())
		{
			file1.mkdirs();
		}
		File imageurl=new File(file1+"\\"+imagename);
		file.transferTo(imageurl);//��ԴͼƬ���Ƶ��ƶ�λ��
		User user=new User();
		user.setUserid(userid);
		user.setProfilehead(imageurlnotag);
		boolean flag=userServiceImpl.updateUser(user);
		if(flag)
		{
			UserLog userLog=new UserLog();
			String ip=request.getRemoteAddr();
			userLog.setUserid(user.getUserid());
			userLog.setTime(dateUtil.getDateformat());
			userLog.setType("�޸�");
			userLog.setDetail("�޸�ͷ��");
			userLog.setIp(ip);
			userLogServiceImpl.insertLog(userLog);
			redirectAttributes.addFlashAttribute("message", "["+userid+"]ͷ���ϴ��ɹ�!");
			return "redirect:/information/"+userid;
		}
		else
		{
			redirectAttributes.addFlashAttribute("error", "["+userid+"]ͷ���ϴ�ʧ��!");
			return "redirect:/information/"+userid;
		}
	}
//	�޸�����
	@RequestMapping("/modifypassword/{userid}")
	public String modifyPassowrd(@PathVariable("userid")String userid,String oldpass,String newpass,RedirectAttributes redirectAttributes,HttpServletRequest request){
		User user=userServiceImpl.selectUserByUserId(userid);
		String password=user.getPassword();
		if(password.equals(oldpass))
		{
			User user1=new User();
			user1.setPassword(newpass);
			user1.setUserid(userid);
			boolean flag=userServiceImpl.updateUser(user1);
			if(flag)
			{
				UserLog userLog=new UserLog();
				String ip=request.getRemoteAddr();
				userLog.setUserid(user.getUserid());
				userLog.setTime(dateUtil.getDateformat());
				userLog.setType("�޸�");
				userLog.setDetail("�޸�����");
				userLog.setIp(ip);
				userLogServiceImpl.insertLog(userLog);
			redirectAttributes.addFlashAttribute("message", "["+userid+"]�����޸ĳɹ�!");
			return "redirect:/information/"+userid;
			}
			else {
				redirectAttributes.addFlashAttribute("error", "["+userid+"]�����޸�ʧ��!");
				return "redirect:/infosetting/"+userid;
			}
		}
		else{
			System.out.println("�޸�ʧ��!");
			redirectAttributes.addFlashAttribute("error", "["+userid+"]�����޸�ʧ��!");
		}
		return "redirect:/infosetting/"+userid;
	}
//	�鿴��־
	@RequestMapping("/log/{userid}")
	public String log(@PathVariable("userid")String userid,@RequestParam("page")int page,HttpServletRequest request)
	{
		if( request.getSession().getAttribute("pageSize") == null)
		{
			request.getSession().setAttribute("pageSize", 5);
		}
		int pageSize=(Integer) request.getSession().getAttribute("pageSize");
		int count;
		List<UserLog> loglist=new ArrayList<UserLog>();
		loglist=userLogServiceImpl.selectLogByUserid(userid, page, pageSize);
		count=userLogServiceImpl.selectLogCountByUserid(userid, pageSize);
		request.getSession().setAttribute("loglist", loglist);
		request.getSession().setAttribute("count", count);
		return "log";
	}
//	���ڻ�ȡ������Ϣʱ��ͷ��ʹ����
	@RequestMapping("/head/{userid}")
	public void gethead(@PathVariable("userid")String userid,HttpServletRequest request,HttpServletResponse response) throws IOException
	{
		String realurl=request.getServletContext().getRealPath("/");
		String imageurl=userServiceImpl.selectUserByUserId(userid).getProfilehead();
		String url=realurl+imageurl;
		InputStream inputStream=new FileInputStream(url);
		ServletOutputStream outputStream=response.getOutputStream();
		response.setContentType("image/jpeg; charset=UTF-8");
		byte[] buffer=new byte[1024];
		int i=-1;
		while((i=inputStream.read(buffer))!=-1)
		{
			outputStream.write(buffer, 0, i);
		}
		outputStream.flush();
		outputStream.close();
		inputStream.close();
	}
//	����
	@RequestMapping("/help")
	public String help()
	{
		return "help";
		
	}
//	����
	@RequestMapping("/about")
	public String about()
	{
		return "about";
	}
//	ϵͳ����
	@RequestMapping("system-setting/{userid}")
	public String systemsetting(@PathVariable("userid")String userid,HttpServletRequest request)
	{
		User user=userServiceImpl.selectUserByUserId(userid);
		request.setAttribute("user", user);
		return "system-setting";
	}
//	�޸�ϵͳ����
	@RequestMapping("changesystem/{userid}")
	public String changesystem(@PathVariable("userid")String userid,int options,int secrecy,HttpServletRequest request,RedirectAttributes redirectAttributes)
	{
		request.getSession().setAttribute("pageSize", options);
		User user=new User();
		user.setUserid(userid);
		user.setStatus(secrecy);
		boolean flag=userServiceImpl.updateUser(user);
		if(flag)
		{
			redirectAttributes.addFlashAttribute("message", "ϵͳ�����޸ĳɹ�!");
		}
		else
		{
			redirectAttributes.addFlashAttribute("error", "ϵͳ�޸�ʧ��!");
		}
		return "redirect:/system-set";
	}
//	��������Ϣ
	@RequestMapping("otherinfo/{userid}")
	public String otherinformation(@PathVariable("userid")String userid,HttpServletRequest request,RedirectAttributes redirectAttributes)
	{
		int status=userServiceImpl.selectUserByUserId(userid).getStatus();
		if(status==-1)
		{
			redirectAttributes.addFlashAttribute("error", userid+"����Ϣδ����!");
			redirectAttributes.addFlashAttribute("userid", userid);
			return "redirect:/errorinfo";
		}
		else
		{
			return "redirect:/information/"+userid;
		}
	}
	@RequestMapping("errorinfo")
	public String error()
	{
		return "errorinfo";
	}
	@RequestMapping("/system-set")
	public String systemsett()
	{
		return "system-setting";
	}
}