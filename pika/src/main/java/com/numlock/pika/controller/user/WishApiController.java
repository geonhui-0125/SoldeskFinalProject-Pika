package com.numlock.pika.controller.user;

import com.numlock.pika.dto.FpApiDto;
import com.numlock.pika.service.user.WishApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class WishApiController {

    private final WishApiService wishApiService;

    @PostMapping("/api/product/{productId}/wish")
    public FpApiDto upWishCount(@PathVariable int productId, Principal principal) {
        return wishApiService.upWishCount(productId, principal);
    }

    @DeleteMapping("/api/product/{productId}/wish")
    public FpApiDto downWishCount(@PathVariable int productId, Principal principal) {
        return  wishApiService.downWishCount(productId, principal);
    }

}
