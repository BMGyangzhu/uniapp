package org.example.im.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import org.example.im.entity.Message;
import org.example.im.entity.MessageList;
import org.example.im.entity.WsResult;
import org.example.im.service.MessageListService;
import org.example.im.service.MessageService;
import org.example.im.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final ConcurrentHashMap<Long, ChannelHandlerContext> userChannels = new ConcurrentHashMap<>();
    private static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    private final MessageService messageService;
    private final MessageListService messageListService;

    @Autowired
    public WebSocketHandler(MessageService messageService, MessageListService messageListService) {
        this.messageService = messageService;
        this.messageListService = messageListService;
   }

    // ========================== 握手阶段：JWT + 登录互斥 ==========================

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {

            WebSocketServerProtocolHandler.HandshakeComplete handshake =
                    (WebSocketServerProtocolHandler.HandshakeComplete) evt;

            System.out.println(" HandshakeComplete - URI: " + handshake.requestUri());

            String uri = handshake.requestUri();
            QueryStringDecoder decoder = new QueryStringDecoder(uri);

            //  1. 从 URL 获取 token（不再使用 userId）
            List<String> tokens = decoder.parameters().get("token");

            if (tokens == null || tokens.isEmpty()) {
                System.out.println(" WebSocket 握手失败：缺少 token");
                ctx.close();
                return;
            }

            String token = tokens.get(0);

            //  2. 校验 token
            if (!JwtUtils.checkToken(token)) {
                System.out.println(" WebSocket 握手失败：token 无效");
                ctx.close();
                return;
            }

            //  3. 解析 userId
            Long userId = JwtUtils.getUserIdByToken(token);
            if (userId == null) {
                System.out.println(" WebSocket 握手失败：无法解析用户");
                ctx.close();
                return;
            }

            //  4. 登录互斥：踢掉旧连接
            ChannelHandlerContext oldCtx = userChannels.get(userId);
            if (oldCtx != null && oldCtx != ctx) {
                System.out.println("用户 " + userId + " 已在其他地方登录，强制下线旧连接");

                WsResult<String> kickMsg = new WsResult<>("KICK_OUT", "你的账号在其他地方登录");
                oldCtx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(kickMsg)));

                oldCtx.close();
            }

            //  5. 绑定当前连接
            ctx.channel().attr(USER_ID_KEY).set(userId);
            userChannels.put(userId, ctx);

            System.out.println("User " + userId + " 握手成功，总在线: " + userChannels.size());
        }

        super.userEventTriggered(ctx, evt);
    }

    // ==========================  消息处理 ==========================

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msgFrame) {

        if (msgFrame instanceof TextWebSocketFrame) {

            String text = ((TextWebSocketFrame) msgFrame).text();
            System.out.println("消息内容: " + text);

            try {
                Message message = JSON.parseObject(text, Message.class);

                messageService.saveMessage(
                        message.getFromUserId(),
                        message.getToUserId(),
                        message.getContent()
                );

                Message savedMessage = messageService.getLastMessage(
                        message.getFromUserId(),
                        message.getToUserId()
                );

                messageListService.updateLastMessageTime(
                        savedMessage.getFromUserId(),
                        savedMessage.getToUserId(),
                        savedMessage.getGmtCreated()
                );

                messageListService.updateLastMessageTime(
                        savedMessage.getToUserId(),
                        savedMessage.getFromUserId(),
                        savedMessage.getGmtCreated()
                );

                //  推送给发送方
                WsResult<Message> chatResult = new WsResult<>("CHAT_MESSAGE", savedMessage);
                ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(chatResult)));

                //  推送给接收方
                ChannelHandlerContext toCtx = userChannels.get(message.getToUserId());
                if (toCtx != null && toCtx != ctx) {
                    toCtx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(chatResult)));
                }

                //  联系人更新时间推送
                MessageList fromSide = messageListService.getMessageList(
                        new MessageList(message.getFromUserId(), message.getToUserId())
                );

                MessageList toSide = messageListService.getMessageList(
                        new MessageList(message.getToUserId(), message.getFromUserId())
                );

//                ctx.writeAndFlush(new TextWebSocketFrame(
//                        JSON.toJSONString(new WsResult<>("CONTACT_UPDATE", fromSide))
//                ));
//
//                if (toCtx != null) {
//                    toCtx.writeAndFlush(new TextWebSocketFrame(
//                            JSON.toJSONString(new WsResult<>("CONTACT_UPDATE", toSide))
//                    ));
//                }

            } catch (Exception e) {
                System.err.println("消息解析错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ==========================  断开连接时清理 ==========================

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        userChannels.entrySet().removeIf(entry -> {
            if (entry.getValue() == ctx) {
                System.out.println(" 用户下线: " + entry.getKey());
                return true;
            }
            return false;
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(" 异常捕获: " + cause.getMessage());
        ctx.close();
    }
}
