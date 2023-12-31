package com.fruit.mall_admin.review;

import com.fruit.mall_admin.review.dto.DetailReviewDto;
import com.fruit.mall_admin.review.dto.ReviewCountDto;
import com.fruit.mall_admin.review.dto.ReviewResDto;
import com.fruit.mall_admin.review.dto.ReviewSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepository implements ReviewMapper {
    private final ReviewMapper reviewMapper;

    @Override
    public List<ReviewResDto> selectAllReview() {
        return reviewMapper.selectAllReview();
    }

    @Override
    public List<ReviewResDto> selectAllBySearchCond(ReviewSearchCond cond) {
        return reviewMapper.selectAllBySearchCond(cond);
    }

    @Override
    public List<DetailReviewDto> selectAllByProductId(Long productId) {
        return reviewMapper.selectAllByProductId(productId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        reviewMapper.deleteByReviewId(reviewId);
    }

    @Override
    public ReviewCountDto countReviews() {
        return reviewMapper.countReviews();
    }

    @Override
    public Long selectUserIdByReviewId(Long userId) {
        return reviewMapper.selectUserIdByReviewId(userId);
    }
}
