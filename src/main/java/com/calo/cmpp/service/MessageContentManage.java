package com.calo.cmpp.service;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageContentManage {

    private final static List<String> CONTENT_LIST = new ArrayList<>();

    private static final Random random = new Random();

    static {
        CONTENT_LIST.add("【你说啥】对方隔了一条江给你打电话说手机快没电了，江不宽，你仔细听我说，但是你还是听不太清。");
        CONTENT_LIST.add("【听不见】我接到你的电话，你说手机快没电了，你要隔江对话，可是我听不清。");
        CONTENT_LIST.add("【太远啦】听不见你在说什么，于是大声对你喊道：“太远啦！我听不见。”");
        CONTENT_LIST.add("【大点声】继续对你说：“大点声儿！”");
        CONTENT_LIST.add("【我说】我听到你说听不见了，双手比作扩音器对着嘴巴，超大声回答你:“我说......”");
        CONTENT_LIST.add("【我们隔了一条江】“我们隔了一条江，你听不到我的声音很正常。”");
        CONTENT_LIST.add("【你当然听不见】我隐约听懂他在说什么，心里想着“这**不是废话吗”┌( ´_ゝ` )┐（无语颜文字）");
        CONTENT_LIST.add("【或许你能看得懂手语】然后对方好像在用手比划些什么，他在问，你懂手语吗？");
        CONTENT_LIST.add("【yes i can】我懂，然后他说，bushi，他比划道“不小心在隔条江是个意外，因为他的家乡没有江。”");
        CONTENT_LIST.add("【祝福】一个节日思念无数，一条短信祝福万千。端午节到了，祝你节日安康，同时祝你健健康康地，快安康乐地，和和美美地，顺顺利利地过好生命中的每一天。");
        CONTENT_LIST.add("【天空】当天空呈现出彩屏，当心跳调节为振动，当笑声设定为和弦，当天线内置为心灵感应，当友情设置为时空连线，当电波转化为快乐信号，请接受我的新年祝福！");
        CONTENT_LIST.add("【12306】春运开始了，我的吉祥号列车已发车，现已沿幸福铁轨，以迅雷不及掩耳的速度，以大站不歇小站不停的气势，向您奔驰而去，将祝福捎给您！祝您春节快乐！新年大吉");
        CONTENT_LIST.add("【幸福】春节来到，欢声不断；电话打搅，多有不便；短信拜年，了我心愿；祝您全家，身体康健；生活幸福，来年多赚；提早拜年，免得占线！新年快乐春节快乐！新年大吉！新春佳节到，祝福没忘掉，平时联系少，友谊非常牢，理由不多说，这次全代表，恭贺全家新年好，福气满身财运绕，人人健康天天笑，幸福生活更美妙！");
        CONTENT_LIST.add("【老师】有了您，我嘀一生才精彩！！谢谢您！我嘀老师！由苍白而绚丽，由肤浅而深邃，生命因您而升华。");
        CONTENT_LIST.add("【告别祝福】：再见，期待下次再见面！保持联系! 告别的你");
        CONTENT_LIST.add("【音乐推荐】：听了这首歌，立刻30也来听听吧! repo");
        CONTENT_LIST.add("【下一步】每条路都是人走出来的，留在地球上最壮观的是路，最难消失的也是路，当我们的目光落在脚下的路时，就应该去思考路上的人生。屈原那越过千山万水“路漫漫其修远兮，吾将上下而求索”的沉沉呼号，难道不是一声回肠荡气的警示吗？拨开各种各样的路障，用我们的智慧和勇气去探求，就会发觉，每一条路，都是天底下最美的景观。");
        CONTENT_LIST.add("【下面的路】斩断自己的退路，才能更好地赢得出路。在很多时候，我们都需要一种斩断自己退路的勇气,因为身后有退路，我们就会心存侥幸和安逸，前行的脚步也会放慢；如果身后无退路，我们就会集中全部精力，勇往直前，为自己赢得出路。");
        CONTENT_LIST.add("【信念】信念是一份纯真的向往是一种美丽的追求也是人们赖以生存的原动力。信念是一种欲望是一份内心的倔强也是你生命葱笼的甘泉。信念是一份曲折是一种跋涉也是一种艰辛与磨难。信念是一种境界是一种崇高。信念是雄鹰展翅高飞的决心是鱼儿遨游大海的豪情是种子破土时生命的洒脱和展现。信念是追求是血泪是成功是永恒");
        CONTENT_LIST.add("【三要诀】人生幸福三诀：不要拿自己的错误来惩罚自己；不要拿自己的错误来惩罚别人；不要拿别人的错误来惩罚自己。有了这三条，人生就不会太累了……");
        CONTENT_LIST.add("【走】第一名和最后一名本质上没有区别，即使第一，你是全校第一吗，你是全国第一吗？你是全世界第一阿！即使你是倒数第一，你是全校倒数第一吗？你是全市倒数第一吗？那你也没法证明你是全球倒数第一啊！");
        CONTENT_LIST.add("【善与恶】 “恶”，恐人知，便是大恶。“善”，欲人知，不是真善。");
    }

    public static String getContentRandom(String content, boolean randomContent) {
        String messageContent = content;
        if (randomContent) {
            messageContent = CONTENT_LIST.get(random.nextInt(CONTENT_LIST.size()));
        }
        return messageContent.replaceAll("\\{P}", String.valueOf(random.nextInt(999999)));
    }

    public static int count(String content) {
        int count;
        if (StringUtils.isBlank(content) || content.length() <= 67) {
            count = 1;
        } else {
            count = (int) Math.ceil((double) content.length() / 70);
        }
        return count;
    }


}
