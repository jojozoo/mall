/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package net.shopxx.controller.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import net.shopxx.Message;
import net.shopxx.entity.Country;
import net.shopxx.entity.Product;
import net.shopxx.entity.ProductCategory;
import net.shopxx.service.BrandService;
import net.shopxx.service.CountryService;
import net.shopxx.service.ProductCategoryService;
import net.shopxx.service.PromotionService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONArray;

/**
 * Controller - 商品分类
 * 
 * @author SHOP++ Team
 * @version 5.0.3
 */
@Controller("adminProductCategoryController")
@RequestMapping("/admin/product_category")
public class ProductCategoryController extends BaseController {

	@Inject
	private ProductCategoryService productCategoryService;
	@Inject
	private BrandService brandService;
	@Inject
	private PromotionService promotionService;
	
	@Inject
    private CountryService countryService;

	/**
	 * 添加
	 */
	@GetMapping("/add")
	public String add(ModelMap model) {
	    List<Country> countries = countryService.findRoots();
	    if (countries != null && !countries.isEmpty()) {
	        return listByCountry(countries.get(0).getId(), model);
	    }
		/*model.addAttribute("productCategoryTree", productCategoryService.findTree());
		model.addAttribute("countries", countryService.findRoots());
		model.addAttribute("brands", brandService.findAll());
		model.addAttribute("promotions", promotionService.findAll());*/
		return "admin/product_category/add";
	}
	
	/**
     * 添加
     */
    @GetMapping("/listByCountry")
    public String listByCountry(Long countryId,ModelMap model) {
        Country country = countryService.find(countryId);
        model.addAttribute("countryId", countryId);
        model.addAttribute("productCategoryTree", productCategoryService.findTree(country));
        model.addAttribute("countries", countryService.findRoots());
        model.addAttribute("brands", country.getBrands());
        model.addAttribute("promotions", promotionService.findAll());
        return "admin/product_category/add";
    }

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public String save(ProductCategory productCategory, Long parentId, Long[] brandIds, Long[] promotionIds, RedirectAttributes redirectAttributes) {
		productCategory.setParent(productCategoryService.find(parentId));
		productCategory.setBrands(new HashSet<>(brandService.findList(brandIds)));
		productCategory.setPromotions(new HashSet<>(promotionService.findList(promotionIds)));
		if (!isValid(productCategory)) {
			return ERROR_VIEW;
		}
		productCategory.setTreePath(null);
		productCategory.setGrade(null);
		productCategory.setChildren(null);
		productCategory.setProducts(null);
		productCategory.setParameters(null);
		productCategory.setAttributes(null);
		productCategory.setSpecifications(null);
		productCategory.setCountry(countryService.find(productCategory.getCountry().getId()));
		productCategoryService.save(productCategory);
		addFlashMessage(redirectAttributes, Message.success(SUCCESS_MESSAGE));
		return "redirect:list";
	}

	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(Long id, ModelMap model) {
		ProductCategory productCategory = productCategoryService.find(id);
		model.addAttribute("productCategoryTree", productCategoryService.findTree(productCategory.getCountry()));
		model.addAttribute("brands", productCategory.getCountry().getBrands());
		model.addAttribute("promotions", promotionService.findAll());
		model.addAttribute("countries", countryService.findRoots());
		model.addAttribute("productCategory", productCategory);
		model.addAttribute("children", productCategoryService.findChildren(productCategory, true, null));
		return "admin/product_category/edit";
	}

	/**
	 * 更新
	 */
	@PostMapping("/update")
	public String update(ProductCategory productCategory, Long parentId, Long[] brandIds, Long[] promotionIds, RedirectAttributes redirectAttributes) {
		productCategory.setParent(productCategoryService.find(parentId));
		productCategory.setBrands(new HashSet<>(brandService.findList(brandIds)));
		productCategory.setPromotions(new HashSet<>(promotionService.findList(promotionIds)));
		if (!isValid(productCategory)) {
			return ERROR_VIEW;
		}
		if (productCategory.getParent() != null) {
			ProductCategory parent = productCategory.getParent();
			if (parent.equals(productCategory)) {
				return ERROR_VIEW;
			}
			List<ProductCategory> children = productCategoryService.findChildren(parent, true, null);
			if (children != null && children.contains(parent)) {
				return ERROR_VIEW;
			}
		}
		
		productCategory.setCountry(countryService.find(productCategoryService.find(productCategory.getId()).getCountry().getId()));
		productCategoryService.update(productCategory, "treePath", "grade", "children", "products", "parameters", "attributes", "specifications");
		addFlashMessage(redirectAttributes, Message.success(SUCCESS_MESSAGE));
		return "redirect:list";
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(ModelMap model,Long countryId) {
	    model.addAttribute("countries", countryService.findRoots());
	    if (countryId != null) {
	        model.addAttribute("productCategoryTree", productCategoryService.findTree(countryService.find(countryId)));
	    } else {
	        model.addAttribute("productCategoryTree", productCategoryService.findTree());
	    }
	   
		return "admin/product_category/list";
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public @ResponseBody Message delete(Long id) {
		ProductCategory productCategory = productCategoryService.find(id);
		if (productCategory == null) {
			return Message.error(ERROR_MESSAGE);
		}
		Set<ProductCategory> children = productCategory.getChildren();
		if (children != null && !children.isEmpty()) {
			return Message.error("admin.productCategory.deleteExistChildrenNotAllowed");
		}
		Set<Product> products = productCategory.getProducts();
		if (products != null && !products.isEmpty()) {
			return Message.error("admin.productCategory.deleteExistProductNotAllowed");
		}
		productCategoryService.delete(id);
		return Message.success(SUCCESS_MESSAGE);
	}
	
	/**
	 * 根据国家选择分类
	 */
	@GetMapping("/findTree")
	public @ResponseBody JSONArray findTree(@RequestParam("country") String country) {
		JSONArray data = new JSONArray();
		// 获取国家
		Country countryBean = countryService.findByName(country);
		List<ProductCategory> list = productCategoryService.findTree(countryBean);
		if (null != list) {
			for (ProductCategory category : list) {
				// 会员编号、姓名、余额、收货地址、手机号、会员角色
				Map<String, Object> item = new HashMap<>();
				item.put("id", category.getId());
				item.put("name", category.getName());
				item.put("grade", category.getGrade());
				data.add(item);
			}
		}
		return data;
	}

}