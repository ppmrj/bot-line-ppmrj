package com.dicoding.menirukanmu;

import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.postback.PostbackContent;

public class Events {
    public String type;
    public String replyToken;
    public Source source;
    public Long timestamp;
    public Message message;
    public PostbackContent postbackContent;
}
