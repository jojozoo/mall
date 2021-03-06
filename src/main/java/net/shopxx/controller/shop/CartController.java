/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package net.shopxx.controller.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopxx.Results;
import net.shopxx.entity.Cart;
import net.shopxx.entity.CartItem;
import net.shopxx.entity.Product;
import net.shopxx.entity.Sku;
import net.shopxx.security.CurrentCart;
import net.shopxx.service.CartService;
import net.shopxx.service.SkuService;
import net.shopxx.util.SystemUtils;

/**
 * Controller - 购物车
 * 
 * @author SHOP++ Team
 * @version 5.0.3
 */
@Controller("shopCartController")
@RequestMapping("/cart")
public class CartController extends BaseController {

	@Inject
	private SkuService skuService;
	@Inject
	private CartService cartService;

	/**
	 * 信息
	 */
	@GetMapping("/info")
	public ResponseEntity<?> info(@CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		if (currentCart != null) {
			data.put("tag", currentCart.getTag());
			data.put("productQuantity", currentCart.getProductQuantity());
			data.put("effectivePrice", currentCart.getEffectivePrice());
			List<Map<String, Object>> items = new ArrayList<>();
			for (CartItem cartItem : currentCart) {
				Map<String, Object> item = new HashMap<>();
				item.put("skuId", cartItem.getSku().getId());
				item.put("skuName", cartItem.getSku().getName());
				item.put("skuThumbnail", cartItem.getSku().getThumbnail());
				item.put("skuPath", cartItem.getSku().getPath());
				item.put("price", cartItem.getPrice());
				item.put("couponPrice", cartItem.getCouponPrice());//券单价
				item.put("quantity", cartItem.getQuantity());
				item.put("subtotal", cartItem.getSubtotal());//钱总数
				item.put("totalCoupon", cartItem.getTotalCoupon());//券总数
				items.add(item);
			}
			data.put("items", items);
		}
		return ResponseEntity.ok(data);
	}

	/**
	 * 添加
	 */
	@PostMapping("/add")
	public ResponseEntity<?> add(Long skuId, Integer quantity, @CurrentCart Cart currentCart) {
		if (quantity == null || quantity < 1) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		Sku sku = skuService.find(skuId);
		if (sku == null) {
			return Results.NOT_FOUND;
		}
		if (!Product.Type.general.equals(sku.getType())) {
			return Results.unprocessableEntity("shop.cart.skuNotForSale");
		}
		if (!sku.getIsMarketable()) {
			return Results.unprocessableEntity("shop.cart.skuNotMarketable");
		}

		int cartItemSize = 1;
		int skuQuantity = quantity;
		if (currentCart != null) {
			if (currentCart.contains(sku)) {
				CartItem cartItem = currentCart.getCartItem(sku);
				cartItemSize = currentCart.size();
				skuQuantity = cartItem.getQuantity() + quantity;
			} else {
				cartItemSize = currentCart.size() + 1;
				skuQuantity = quantity;
			}
		}
		if (Cart.MAX_CART_ITEM_SIZE != null && cartItemSize > Cart.MAX_CART_ITEM_SIZE) {
			return Results.unprocessableEntity("shop.cart.addCartItemSizeNotAllowed", Cart.MAX_CART_ITEM_SIZE);
		}
		if (CartItem.MAX_QUANTITY != null && skuQuantity > CartItem.MAX_QUANTITY) {
			return Results.unprocessableEntity("shop.cart.addSkuQuantityNotAllowed", CartItem.MAX_QUANTITY);
		}
		if (skuQuantity > sku.getAvailableStock()) {
			return Results.unprocessableEntity("shop.cart.skuLowStock");
		}
		if (currentCart == null) {
			currentCart = cartService.create();
		}
		cartService.add(currentCart, sku, quantity);
		return Results.ok("shop.cart.addSuccess");
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(ModelMap model) {
		return "shop/cart/list";
	}

	/**
	 * 修改
	 */
	@PostMapping("/modify")
	public ResponseEntity<?> modify(Long skuId, Integer quantity, @CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		if (quantity == null || quantity < 1) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		Sku sku = skuService.find(skuId);
		if (sku == null) {
			return Results.NOT_FOUND;
		}
		if (currentCart == null || currentCart.isEmpty()) {
			return Results.unprocessableEntity("shop.cart.notEmpty");
		}
		if (!currentCart.contains(sku)) {
			return Results.unprocessableEntity("shop.cart.skuNotContains");
		}
		if (!sku.getIsMarketable()) {
			return Results.unprocessableEntity("shop.cart.skuNotMarketable");
		}
		if (CartItem.MAX_QUANTITY != null && quantity > CartItem.MAX_QUANTITY) {
			return Results.unprocessableEntity("shop.cart.addSkuQuantityNotAllowed", CartItem.MAX_QUANTITY);
		}
		if (quantity > sku.getAvailableStock()) {
			return Results.unprocessableEntity("shop.cart.skuLowStock");
		}
		cartService.modify(currentCart, sku, quantity);
		CartItem cartItem = currentCart.getCartItem(sku);

		data.put("subtotal", SystemUtils.changeCurrency(cartItem.getSubtotal()));
		data.put("totalCoupon", SystemUtils.changeCurrency(cartItem.getTotalCoupon()));
		data.put("isLowStock", cartItem.getIsLowStock());
		data.put("quantity", currentCart.getProductQuantity());
		data.put("effectiveRewardPoint", currentCart.getEffectiveRewardPoint());
		//System.out.println(currentCart.getEffectiveCoupon());
		data.put("effectiveCoupon", SystemUtils.changeCurrency(currentCart.getEffectiveCoupon()));
		data.put("effectivePrice", SystemUtils.changeCurrency(currentCart.getEffectivePrice()));
		data.put("giftNames", currentCart.getGiftNames());
		data.put("promotionNames", currentCart.getPromotionNames());
		return ResponseEntity.ok(data);
	}

	/**
	 * 移除
	 */
	@PostMapping("/remove")
	public ResponseEntity<?> remove(Long skuId, @CurrentCart Cart currentCart) {
		Map<String, Object> data = new HashMap<>();
		Sku sku = skuService.find(skuId);
		if (sku == null) {
			return Results.NOT_FOUND;
		}
		if (currentCart == null || currentCart.isEmpty()) {
			return Results.unprocessableEntity("shop.cart.notEmpty");
		}
		if (!currentCart.contains(sku)) {
			return Results.unprocessableEntity("shop.cart.skuNotContains");
		}
		cartService.remove(currentCart, sku);

		data.put("isLowStock", currentCart.getIsLowStock());
		data.put("quantity", currentCart.getProductQuantity());
		data.put("effectiveRewardPoint", currentCart.getEffectiveRewardPoint());
		data.put("effectivePrice", currentCart.getEffectivePrice());
		data.put("giftNames", currentCart.getGiftNames());
		data.put("promotionNames", currentCart.getPromotionNames());
		return ResponseEntity.ok(data);
	}

	/**
	 * 清空
	 */
	@PostMapping("/clear")
	public ResponseEntity<?> clear(@CurrentCart Cart currentCart) {
		if (currentCart != null) {
			cartService.clear(currentCart);
		}
		return Results.OK;
	}

}