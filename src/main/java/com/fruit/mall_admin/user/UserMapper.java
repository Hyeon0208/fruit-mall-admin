package com.fruit.mall_admin.user;

import com.fruit.mall_admin.user.dto.UserCountDto;
import com.fruit.mall_admin.user.dto.UserDelivery;
import com.fruit.mall_admin.user.dto.UserInfoDto;
import com.fruit.mall_admin.user.dto.UserSearchCond;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserInfoDto> selectUserInfo();

    List<UserInfoDto> selectUserInfoBySearchCond(UserSearchCond cond);

    List<UserDelivery> selectDeliveriesByUserId(@Param("userIdNo") Long userIdNo);

    UserCountDto countUsersByStatus();

    void updateUserStatusByUserIdNo(@Param("userIdNo") Long userIdNo);
}
