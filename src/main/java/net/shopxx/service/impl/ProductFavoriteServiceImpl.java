/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package net.shopxx.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.shopxx.Filter;
import net.shopxx.Order;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.dao.ProductFavoriteDao;
import net.shopxx.entity.Member;
import net.shopxx.entity.Product;
import net.shopxx.entity.ProductFavorite;
import net.shopxx.service.ProductFavoriteService;

/**
 * Service - 商品收藏
 * 
 * @author SHOP++ Team
 * @version 5.0.3
 */
@Service
public class ProductFavoriteServiceImpl extends BaseServiceImpl<ProductFavorite, Long> implements ProductFavoriteService {

	@Inject
	private ProductFavoriteDao productFavoriteDao;

	@Transactional(readOnly = true)
	public boolean exists(Member member, Product product) {
		return productFavoriteDao.exists(member, product);
	}

	@Transactional(readOnly = true)
	public List<ProductFavorite> findList(Member member, Integer count, List<Filter> filters, List<Order> orders) {
		return productFavoriteDao.findList(member, count, filters, orders);
	}

	@Transactional(readOnly = true)
	public Page<ProductFavorite> findPage(Member member, Pageable pageable) {
		return productFavoriteDao.findPage(member, pageable);
	}

	@Transactional(readOnly = true)
	public Long count(Member member) {
		return productFavoriteDao.count(member);
	}

}