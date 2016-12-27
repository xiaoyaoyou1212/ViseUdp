package com.vise.udpapp;

import android.content.Context;
import android.widget.TextView;

import com.vise.common_base.adapter.helper.HelperAdapter;
import com.vise.common_base.adapter.helper.HelperViewHolder;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-27 13:30
 */
public class ChatAdapter extends HelperAdapter<ChatInfo> {
    public ChatAdapter(Context context) {
        super(context, R.layout.item_chat_info_left, R.layout.item_chat_info_right);
    }

    @Override
    public void HelpConvert(HelperViewHolder viewHolder, int position, ChatInfo chatInfo) {
        if (chatInfo == null) {
            return;
        }
        TextView timeTv;
        TextView msgTv;
        TextView nameTv;
        if(chatInfo.isSend()){
            timeTv = viewHolder.getView(R.id.item_chat_right_time);
            msgTv = viewHolder.getView(R.id.item_chat_right_msg);
            nameTv = viewHolder.getView(R.id.item_chat_right_name);
            timeTv.setText(chatInfo.getSendTime());
            msgTv.setText(chatInfo.getSendMsg());
        } else{
            timeTv = viewHolder.getView(R.id.item_chat_left_time);
            msgTv = viewHolder.getView(R.id.item_chat_left_msg);
            nameTv = viewHolder.getView(R.id.item_chat_left_name);
            timeTv.setText(chatInfo.getReceiveTime());
            msgTv.setText(chatInfo.getReceiveMsg());
        }
        nameTv.setText(chatInfo.getNickName());
    }

    @Override
    public int checkLayout(int position, ChatInfo item) {
        if(item != null && item.isSend()){
            return 1;
        }
        return 0;
    }
}
