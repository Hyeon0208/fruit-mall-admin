package com.fruit.mall_admin.category;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper {
    Long selectIdByCategoryName(@Param("categoryName") String categoryName);
}
