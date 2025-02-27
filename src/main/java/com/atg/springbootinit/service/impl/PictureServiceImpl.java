package com.atg.springbootinit.service.impl;

import com.atg.springbootinit.api.aliyuAi.AliYunAiApi;
import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskRequest.Parameters;
import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskRequest.Input;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskRequest;
import com.atg.springbootinit.api.aliyuAi.model.CreateOutPaintingTaskResponse;
import com.atg.springbootinit.common.ErrorCode;
import com.atg.springbootinit.constant.CommonConstant;
import com.atg.springbootinit.exception.BusinessException;
import com.atg.springbootinit.exception.ThrowUtils;
import com.atg.springbootinit.manager.CosManager;
import com.atg.springbootinit.manager.FileManager;
import com.atg.springbootinit.manager.upload.FilePictureUpload;
import com.atg.springbootinit.manager.upload.PictureUploadTemplate;
import com.atg.springbootinit.manager.upload.UrlPictureUpload;
import com.atg.springbootinit.mapper.PictureMapper;
import com.atg.springbootinit.model.dto.file.UploadPictureRequest;
import com.atg.springbootinit.model.dto.picture.*;
import com.atg.springbootinit.model.entity.Picture;
import com.atg.springbootinit.model.entity.Space;
import com.atg.springbootinit.model.entity.User;
import com.atg.springbootinit.model.enums.PictureReviewStatusEnum;
import com.atg.springbootinit.model.vo.PictureVO;
import com.atg.springbootinit.model.vo.UserVO;
import com.atg.springbootinit.service.PictureService;
import com.atg.springbootinit.service.SpaceService;
import com.atg.springbootinit.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 啊汤哥
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-02-05 19:23:45
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    @Resource
    private FileManager fileManager;
    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Autowired
    private CosManager cosManager;
    @Resource
    private AliYunAiApi aliYunAiApi;

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改时id不能为空
        if (ObjUtil.isNull(id) || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id不能为空");
        }
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "introduction过长");
        }

    }

    /**
     * 上传图片
     *
     * @param inputSource
     * @param LoginUser
     * @param pictureUploadRequest
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, User LoginUser, PictureUploadRequest pictureUploadRequest) {
        // 1. 校验用户是否登录
        if (LoginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//            if (!LoginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
//            }
            // 空间额度校验
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间条数已满");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间额度已满");
            }

        }

        // 2. 判断新增还是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }


        // 3. 如果更新的话，检查是否有该图片
        if (pictureId != null && pictureId > 0) {
//            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
            // 仅本人和管理员可以修改
//            if (!userService.isAdmin(LoginUser) && !LoginUser.getId().equals(oldPicture.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
//             上传的校验空间是否一致
           if(spaceId == null){
               spaceId = oldPicture.getSpaceId();
               if (spaceId != null){
                   spaceId = oldPicture.getSpaceId();
               }
           }else{
               if(ObjUtil.notEqual(spaceId,oldPicture.getSpaceId())){
                   throw new BusinessException(ErrorCode.PARAMS_ERROR,"上传的图片空间不一致");
               }
           }
        }
        // 4. 上传图片并获取上传结果
        String uploadPathPrefix = null;
        if (spaceId != null) {
            uploadPathPrefix = String.format("space/%s/%s", spaceId, LoginUser.getId());
        } else {
            uploadPathPrefix = String.format("public/%s", LoginUser.getId());
        }
        // 根据传入的格式进行区分什么什么上传
        PictureUploadTemplate picUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            picUploadTemplate = urlPictureUpload;
        }
        UploadPictureRequest uploadPictureRequest = picUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        // 5. 构造图片实体对象并设置相关属性
        Picture picture = new Picture();
        picture.setSpaceId(spaceId);
        picture.setUrl(uploadPictureRequest.getUrl());
        picture.setThumbnailUrl(uploadPictureRequest.getThumbnailUrl());
        picture.setName(uploadPictureRequest.getPicName());
        picture.setPicSize(uploadPictureRequest.getPicSize());
        picture.setPicWidth(uploadPictureRequest.getPicWidth());
        picture.setPicHeight(uploadPictureRequest.getPicHeight());
        picture.setPicScale(uploadPictureRequest.getPicScale());
        picture.setPicFormat(uploadPictureRequest.getPicFormat());
        picture.setUserId(LoginUser.getId());
        this.fillReviewPicture(picture, LoginUser);
        // 5.1 如果是更新，设置id
        if (pictureId != null && pictureId > 0) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 6. 操作数据库，保存或更新图片信息
        Long finalPictureId = spaceId;
        transactionTemplate.execute(status -> {
            // 更新图片信息
            boolean isSuccess = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "图片上传失败");
            // 更新空间信息
            if (finalPictureId != null) {
                boolean update = spaceService.lambdaUpdate().eq(Space::getId, finalPictureId)
                        .setSql("totalCount = totalCount + 1")
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间信息更新失败");
            }


            return picture;
        });

        // 7. 返回图片信息
        return PictureVO.objToVo(picture);

    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 空间
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        // 添加审核条件
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        // 根据开始时间，结束时间进行收索
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        // 收索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);

        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        // 根据时间查询
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);


        queryWrapper.isNull(nullSpaceId, "spaceId");
        // 数组查询 --tags
        if (CollectionUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 填充用户信息
        Long userId = picture.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                pictureVO.setUser(userVO);
            }
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User LoginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();

        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询这个图片是否存在
        Picture oldPicture = this.getById(id);
        if (oldPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 检查审核---目的避免重复审核
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片已审核");
        }
        // 操作数据库
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(LoginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean b = this.updateById(updatePicture);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片审核失败");
        }

    }

    // 通用审核方法 添加审核参数
    @Override
    public void fillReviewPicture(Picture picture, User LoginUser) {
        // 管理员自动通过审核
        if (userService.isAdmin(LoginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(LoginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动过审");
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadBatchPicture(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 1. 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        if (StringUtils.isBlank(searchText) || count == null || count <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (count > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多抓取30张图片");
        }

        // 2. 抓取图片
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document = null;
        try {
            // 设置User-Agent模拟浏览器请求
            document = Jsoup.connect(fetchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页面获取失败");
        }

        Element element = document.getElementsByClass("dgControl").first();
        if (element == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "解析页面失败");
        }

        Elements imgElementList = element.select("img.mimg");
        int uploadCount = 0;

        for (Element imgElement : imgElementList) {
            // 优先使用data-src属性，如果不存在则使用src属性
            String imgUrl = imgElement.attr("data-src");
            if (StringUtils.isBlank(imgUrl)) {
                imgUrl = imgElement.attr("src");
            }

            if (StringUtils.isBlank(imgUrl)) {
                log.info("当前链接为空，已跳过: {}", imgUrl);
                continue;
            }

            // 处理图片上传地址，防止转义
            int indexOf = imgUrl.indexOf("?");
            if (indexOf != -1) {
                imgUrl = imgUrl.substring(0, indexOf);
            }

            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(imgUrl);

            // 上传图片
            try {
                PictureVO pictureVO = this.uploadPicture(imgUrl, loginUser, pictureUploadRequest);
                log.info("图片上传成功: {}", pictureVO.getUrl());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败，已跳过: {}", imgUrl, e);
            }

            if (uploadCount >= count) {
                break;
            }
        }

        // 上传数据库
        return uploadCount;
    }

    @Override
    public void deletePicture(Long pictureId, User LoginUser) {

        if (pictureId == null || pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (LoginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Picture oldPicture = this.getById(pictureId);
        if (oldPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 删除-// 仅本人或者管理员可删除
//        this.checkPictureAuthority(oldPicture, LoginUser);


        // 6. 操作数据库，保存或更新图片信息
        transactionTemplate.execute(status -> {
            // 更新图片信息
            boolean result = this.removeById(pictureId);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            // 如果有空间id才进行更新空间信息
            if (oldPicture.getSpaceId() != null) {
                // 更新空间信息
                boolean update = spaceService.lambdaUpdate().
                        eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalCount = totalCount - 1")
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间信息更新失败");

            }

            return true;
        });

        this.clearPicture(oldPicture);
    }


    @Async
    @Override
    public void clearPicture(Picture oldPicture) {
        // 判断删除图片是否有多个位置引用
        String oldPictureUrl = oldPicture.getUrl();
        Long count = this.lambdaQuery().eq(Picture::getUrl, oldPictureUrl).count();

        ThrowUtils.throwIf(count > 1, ErrorCode.OPERATION_ERROR, "图片被引用，无法删除");


        // 删除图片
        cosManager.deleteObject(oldPictureUrl);
        // 删除缩列图片
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StringUtils.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        this.validPicture(picture);

        Picture oldPicture = this.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 权限校验
        this.checkPictureAuthority(oldPicture, loginUser);
        // 补充审核信息
        this.fillReviewPicture(picture, loginUser);
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void checkPictureAuthority(Picture picture, User LoginUser) {
        Long spaceId = picture.getSpaceId();
        Long id = LoginUser.getId();
        if (spaceId != null) {
            // 私有空间---仅自己可见
            if (!picture.getUserId().equals(id)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
            }
        } else {
            // 公共空间---管理员可见还有本人
            if (!userService.isAdmin(LoginUser) && !picture.getUserId().equals(id)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
            }
        }
    }

    @Override
    public void batchEditPicture(PictureBatchByEditRequest pictureBatchByEditRequest, User LoginUser) {
        // 1. 获取值
        Long spaceId = pictureBatchByEditRequest.getSpaceId();
        List<Long> pictureIdList = pictureBatchByEditRequest.getPictureIdList();
        String category = pictureBatchByEditRequest.getCategory();
        List<String> tags = pictureBatchByEditRequest.getTags();
        String nameRule = pictureBatchByEditRequest.getNameRule();
        // 2. 数据校验
        ThrowUtils.throwIf(CollectionUtils.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR, "图片ID列表不能为空");
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        ThrowUtils.throwIf(LoginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 3. 查询指定空间的图片ID列表
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        ThrowUtils.throwIf(CollectionUtils.isEmpty(pictureList), ErrorCode.NOT_FOUND_ERROR, "指定空间下没有图片");

        // 更新 分类和标签
        for (Picture picture : pictureList) {
            if (StringUtils.isNotBlank(category)) {

                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {

                picture.setTags(JSONUtil.toJsonStr(tags));
            }


        }

        // 批量重命名
        if (StringUtils.isNotBlank(nameRule)) {
            fillPictureWithNameRule(pictureList, nameRule);
        }
        // 4. 更新图片信息
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片信息更新失败");

    }


    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String name = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(name);

            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "重命名规则错误");
        }
    }


    @Override
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User LoginUser) {
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        // 权限校验
        Picture picture = Optional.ofNullable(this.getById(pictureId)).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
        checkPictureAuthority(picture, LoginUser);
        // 创建扩图任务
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);
    }
}




