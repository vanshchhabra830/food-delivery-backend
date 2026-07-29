package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.MenuRequest;
import com.example.fooddelivery.dto.response.MenuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuService {

    MenuResponse createMenu(Long restaurantId, MenuRequest request);

    MenuResponse getMenuById(Long menuId);

    Page<MenuResponse> getAllMenus(Pageable pageable);

    MenuResponse updateMenu(Long menuId, MenuRequest request);

    void deleteMenu(Long menuId);

}
