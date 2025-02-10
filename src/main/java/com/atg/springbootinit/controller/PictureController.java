package com.atg.springbootinit.controller;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.atg.springbootinit.annotation.AuthCheck;
import com.atg.springbootinit.common.BaseResponse;
import com.atg.springbootinit.common.DeleteRequest;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.common.ResultUtils;
import com.atg.springbootinit.config.RedisConfig;
import com.atg.springbootinit.constant.UserConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.model.dto.picture.*;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.PictureReviewStatusEnum;
import com.atg.springbootinit.model.vo.PictureVO;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
author: atg
time: 2025/2/6 17:37
*/
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;



    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(3L, TimeUnit.MINUTES)
                    .build();

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, loginUser, pictureUploadRequest);
        return ResultUtils.success(pictureVO);
    }
    /**
     * 根据URL上传图片
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest.getFileUrl(), loginUser, pictureUploadRequest);
        return ResultUtils.success(pictureVO);
    }
    /**
     * 批量上传图片
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadBatchPicture(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer integer = pictureService.uploadBatchPicture(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(integer);
    }


    /**
     * 删除图片
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long deleteRequestId = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(deleteRequestId);
        if (oldPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 删除-// 仅本人或者管理员可删除
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(oldPicture.getUserId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        pictureService.deletePicture(oldPicture);
        boolean result = pictureService.removeById(deleteRequestId);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        return ResultUtils.success(true);
    }

    /**
     * 更新图片--管理员可修改
     * <p>
     * todo 看更新的数据是否存在--id
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 实体类转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意事项--tags
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        pictureService.validPicture(picture);
        //
        Picture oldPicture = pictureService.getById(pictureUpdateRequest.getId());
        if (oldPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 补充审核信息
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewPicture(picture, loginUser);

        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 获取图片--管理员可获取
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(id);
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(id);
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 获取图片列表（管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        if (pictureQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 查看审核的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));

    }

    // 使用缓存查询图片列表
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageAndCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        if (pictureQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 查看审核的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存key
        // 构建缓存条件 ---
        String questionsCondition =  JSONUtil.toJsonStr(pictureQueryRequest);
        String haxValue = DigestUtils.md5DigestAsHex(questionsCondition.getBytes());
        String cacheKey = "atgPicture:listPictureVOByPageAndCache:" + haxValue;

        // 从本地缓存中查找
        String Local_Cache = LOCAL_CACHE.getIfPresent(cacheKey);
        if (StringUtils.isNotBlank(Local_Cache)){
            // 命中本地缓存
            Page<PictureVO> pictureVO = JSONUtil.toBean(Local_Cache, Page.class);
            return ResultUtils.success(pictureVO);
        }
//        2. 本地缓存未命中，查询 Redis 分布式缓存
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String cacheValue = valueOperations.get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)){
            // 命中缓存
            Page<PictureVO> pictureVO = JSONUtil.toBean(cacheValue, Page.class);
            return ResultUtils.success(pictureVO);
        }

        // 没有的话查询数据库
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 对象转换为 JSON 字符串
        String cache_Value = JSONUtil.toJsonStr(pictureVOPage);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 +  RandomUtil.randomInt(0, 300);
        valueOperations.set(cacheKey,cache_Value, cacheExpireTime, TimeUnit.MINUTES);
        // 添加到本地缓存
        LOCAL_CACHE.put(cacheKey, cache_Value);
        return ResultUtils.success(pictureVOPage);

    }

    /**
     * 编辑图片--用户
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);

        User loginUser = userService.getLoginUser(request);
        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 补充审核信息
        pictureService.fillReviewPicture(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> getPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();

        List<String> tagList = Arrays.asList("图标", "人物", "热门", "搞笑", "生活", "高清",
                "艺术", "校园", "背景", "简历", "创意",
                "旅游", "美食", "夜景", "卡通", "自热风光", "街头艺术");
        List<String> categoryList = Arrays.asList("网页元素", "启动页", "模板", "艺术画作", "电商", "表情包", "摄影作品", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);

    }

    // 图片审核
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {

        if (pictureReviewRequest == null || pictureReviewRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

}
