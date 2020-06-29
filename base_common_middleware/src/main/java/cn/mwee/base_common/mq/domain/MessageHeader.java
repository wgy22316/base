package cn.mwee.base_common.mq.domain;

import cn.mwee.base_common.utils.date.MwJdk8DateUtil;
import cn.mwee.base_common.utils.misc.MwIdGeneratorUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by liaomengge on 2019/11/18.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageHeader {
    private String mqTraceId = MwIdGeneratorUtil.uuid();
    private long sendTime = MwJdk8DateUtil.getMilliSecondsTime();
}
