package com.fruit.mall_admin.user;

import com.fruit.mall_admin.user.dto.UserDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserManagementApiController {
    private final UserService userService;

    @ResponseBody
    @GetMapping("/deliveries/{userIdNo}")
    public ResponseEntity<?> getDeliveries(@PathVariable Long userIdNo) {
        List<UserDelivery> userDeliveries = userService.selectDeliveriesByUserId(userIdNo);

        if (userDeliveries.size() == 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.selectDeliveriesByUserId(userIdNo));
    }

    @ResponseBody
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Long> data) {
        userService.updateUserStatusByUserIdNo(data.get("userIdNo"));
        return ResponseEntity.ok().build();
    }
}