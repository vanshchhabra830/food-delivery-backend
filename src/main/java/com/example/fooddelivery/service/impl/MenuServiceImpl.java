package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.dto.request.MenuRequest;
import com.example.fooddelivery.dto.response.MenuResponse;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.mapper.MenuMapper;
import com.example.fooddelivery.repository.MenuRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuServiceImpl(MenuRepository menuRepository, RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public MenuResponse createMenu(Long restaurantId, MenuRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        Menu menu = MenuMapper.toMenu(request, restaurant);
        Menu savedMenu = menuRepository.save(menu);
        log.info("Menu created successfully: {}", savedMenu.getName());
        return MenuMapper.toMenuResponse(savedMenu);
    }

    @Override
    public MenuResponse getMenuById(Long menuId) {
        Menu menu = findMenuById(menuId);
        return MenuMapper.toMenuResponse(menu);
    }

    @Override
    public Page<MenuResponse> getAllMenus(Pageable pageable) {
        return menuRepository.findAll(pageable)
                .map(MenuMapper::toMenuResponse);
    }

    @Override
    public MenuResponse updateMenu(Long menuId, MenuRequest request) {
        Menu menu = findMenuById(menuId);

        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setPrice(request.getPrice());
        menu.setCategory(request.getCategory());
        menu.setImageUrl(request.getImageUrl());

        Menu updatedMenu = menuRepository.save(menu);
        log.info("Menu updated successfully: {}", updatedMenu.getName());
        return MenuMapper.toMenuResponse(updatedMenu);
    }

    @Override
    public void deleteMenu(Long menuId) {
        Menu menu = findMenuById(menuId);
        menuRepository.delete(menu);
        log.info("Menu deleted successfully: {}", menu.getName());
    }

    private Menu findMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + id));
    }

}
