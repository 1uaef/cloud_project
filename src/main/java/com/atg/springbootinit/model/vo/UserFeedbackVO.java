package com.atg.springbootinit.model.vo;

import com.atg.springbootinit.model.entity.Feedback;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户反馈视图
 *

 */
@Data
public class UserFeedbackVO implements Serializable {

    /**
     * 建议 ID
     */

    private Long id;

    /**
     * 建议详细描述
     */
    private String description;

    /**
     * 建议状态(0-正在审核、1-审核通过、2-审核不通过)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 封装类转对象
     *
     * @param UserFeedbackVO
     * @return
     */
    public static Feedback voToObj(UserFeedbackVO UserFeedbackVO) {
        if (UserFeedbackVO == null) {
            return null;
        }
        Feedback UserFeedback = new Feedback();
        BeanUtils.copyProperties(UserFeedbackVO, UserFeedback);
        return UserFeedback;
    }

    /**
     * 对象转封装类
     *
     * @param UserFeedback
     * @return
     */
    public static UserFeedbackVO objToVo(Feedback UserFeedback) {
        if (UserFeedback == null) {
            return null;
        }
        UserFeedbackVO UserFeedbackVO = new UserFeedbackVO();
        BeanUtils.copyProperties(UserFeedback, UserFeedbackVO);
        return UserFeedbackVO;
    }
}
