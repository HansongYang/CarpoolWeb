package web;

import dao.MessageDAO;
import dao.UserManagementDAO;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import pojo.Message;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenjunhao on 2017/11/15.
 */
public class MessageServlet extends BaseServlet {
    public String list(HttpServletRequest req, HttpServletResponse res){
        //get user id
        String uid = req.getSession().getAttribute("uid").toString();
        //get message instance
        MessageDAO messageDAO = MessageDAO.getInstance();
        List<Message> msg_list = messageDAO.getReceiverMessage(uid);
        req.setAttribute("messages",msg_list);
        return "/user/message.jsp";
    }
    public String read(HttpServletRequest req,HttpServletResponse res){
        String mid = req.getParameter("mid");
        MessageDAO messageDAO = MessageDAO.getInstance();
        Message m = messageDAO.getMessageByID(mid);
        req.getSession().setAttribute("message",m);
        messageDAO.readMessage(mid);
        return "/user/message_reply.jsp";
    }
    public String send(HttpServletRequest req, HttpServletResponse res){
        Message prev_m = (Message)req.getSession().getAttribute("message");
        String rec_uid = prev_m.getSender_uid();
        String sd_uid = prev_m.getReceiver_uid();
        String message_send = req.getParameter("message_send");
        String nickname = UserManagementDAO.getInstance().getUserNicknameByUID(sd_uid);
        String ref = prev_m.getRef();
        Message newMsg = new Message(sd_uid,rec_uid,message_send,ref,nickname);
        if(MessageDAO.getInstance().addMessage(newMsg)){
            req.setAttribute("msg","Send message successfully");
        }else {
            req.setAttribute("msg","Send message failed");
        }
        req.getSession().removeAttribute("messgae");

        return "@user_message_list";
    }
    public String remove(HttpServletRequest req, HttpServletResponse res){
        String mid= req.getParameter("mid");
        if(MessageDAO.getInstance().delete(mid)){
            req.setAttribute("msg","Delete Message success");
        }else{
            req.setAttribute("msg","Delete Message failed");
        }
        return "@user_message_list";
    }
}
