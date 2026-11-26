package com.foodie.admin.seller.controller;

import com.foodie.admin.service.UploadService;
import com.foodie.admin.seller.dto.SellerMenuDtos.ImageUploadResponse;
import com.foodie.admin.seller.dto.SellerMenuDtos.MenuItemRequest;
import com.foodie.admin.seller.dto.SellerMenuDtos.MenuItemView;
import com.foodie.admin.seller.security.SellerAuthInterceptor;
import com.foodie.admin.seller.service.SellerMenuService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/menu")
public class SellerMenuController {

    private final SellerMenuService menu;
    private final UploadService uploads;

    public SellerMenuController(
            SellerMenuService menu,
            UploadService uploads) {
        this.menu = menu;
        this.uploads = uploads;
    }

    @GetMapping
    public List<MenuItemView> list(HttpServletRequest request) {
        return menu.list(sellerId(request));
    }

    @PostMapping
    public MenuItemView add(
            @RequestBody MenuItemRequest body,
            HttpServletRequest request) {
        return menu.add(sellerId(request), body);
    }

    @PutMapping("/{itemId}")
    public MenuItemView update(
            @PathVariable Long itemId,
            @RequestBody MenuItemRequest body,
            HttpServletRequest request) {
        return menu.update(sellerId(request), itemId, body);
    }

    @PatchMapping("/{itemId}/availability")
    public MenuItemView updateAvailability(
            @PathVariable Long itemId,
            @RequestBody Map<String, Boolean> body,
            HttpServletRequest request) {

        Boolean inStock = body.get("inStock");

        if (inStock == null) {
            throw new IllegalArgumentException(
                    "inStock field is required");
        }

        return menu.updateAvailability(
                sellerId(request), itemId, inStock);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @PathVariable Long itemId,
            HttpServletRequest request) {
        menu.delete(sellerId(request), itemId);
    }

    @PostMapping(
            value = "/upload-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        // The SellerAuthInterceptor authenticates the seller
        // before this method is reached.
        sellerId(request);

        return new ImageUploadResponse(
                uploads.saveMenuImage(file));
    }

    private String sellerId(HttpServletRequest request) {
        return (String) request.getAttribute(
                SellerAuthInterceptor.SELLER_ID_ATTR);
    }
}