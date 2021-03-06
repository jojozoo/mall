/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package net.shopxx.service;

import java.util.List;

import net.shopxx.Filter;
import net.shopxx.Order;
import net.shopxx.entity.Country;
import net.shopxx.entity.Navigation;
import net.shopxx.entity.Navigation.Position;

/**
 * Service - 导航
 * 
 * @author SHOP++ Team
 * @version 5.0.3
 */
public interface NavigationService extends BaseService<Navigation, Long> {

	/**
	 * 查找导航
	 * 
	 * @param position
	 *            位置
	 * @return 导航
	 */
	List<Navigation> findList(Navigation.Position position);
	
	/**
	 * 查找导航
	 * 
	 * @param position
	 *            位置
	 * @param country
	 *            国家
	 * @return 导航
	 */
	List<Navigation> findList(Navigation.Position position, Country country);

	/**
	 * 查找导航
	 * 
	 * @param count
	 *            数量
	 * @param filters
	 *            筛选
	 * @param orders
	 *            排序
	 * @param useCache
	 *            是否使用缓存
	 * @return 导航
	 */
	List<Navigation> findList(Integer count, List<Filter> filters, List<Order> orders, boolean useCache);
	
	/**
	 * 查找导航
	 * 
	 * @param count
	 *            数量
	 * @param filters
	 *            筛选
	 * @param orders
	 *            排序
	 * @param useCache
	 *            是否使用缓存
	 * @param country
	 *            国家
	 * @return 导航
	 */
	List<Navigation> findList(Integer count, List<Filter> filters, List<Order> orders, boolean useCache, Country country);

	List<Navigation> findList(Position position, boolean useCache,Country country);

}