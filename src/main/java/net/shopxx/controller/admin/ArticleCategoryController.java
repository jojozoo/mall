/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package net.shopxx.controller.admin;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import net.shopxx.Message;
import net.shopxx.entity.Article;
import net.shopxx.entity.ArticleCategory;
import net.shopxx.entity.Country;
import net.shopxx.service.ArticleCategoryService;
import net.shopxx.service.CountryService;
import net.shopxx.util.StringUtil;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Controller - 文章分类
 * 
 * @author SHOP++ Team
 * @version 5.0.3
 */
@Controller("adminArticleCategoryController")
@RequestMapping("/admin/article_category")
public class ArticleCategoryController extends BaseController {

	@Inject
	private ArticleCategoryService articleCategoryService;
	@Inject
	private CountryService countryService;
	
	/**
	 * 添加
	 */
	@GetMapping("/add")
	public String add(ModelMap model) {
		model.addAttribute("articleCategoryTree", articleCategoryService.findTree());
		return "admin/article_category/add";
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public String save(ArticleCategory articleCategory, Long parentId, String countryName, RedirectAttributes redirectAttributes) {
		articleCategory.setParent(articleCategoryService.find(parentId));
		if (!isValid(articleCategory)) {
			return ERROR_VIEW;
		}
		articleCategory.setTreePath(null);
		articleCategory.setGrade(null);
		articleCategory.setChildren(null);
		articleCategory.setArticles(null);
		articleCategory.setCountry(countryService.findByName(countryName));
		articleCategoryService.save(articleCategory);
		addFlashMessage(redirectAttributes, Message.success(SUCCESS_MESSAGE));
		return "redirect:list";
	}

	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(Long id, ModelMap model) {
		ArticleCategory articleCategory = articleCategoryService.find(id);
		model.addAttribute("articleCategoryTree", articleCategoryService.findTree(articleCategory.getCountry()));
		model.addAttribute("articleCategory", articleCategory);
		model.addAttribute("children", articleCategoryService.findChildren(articleCategory, true, null));
		return "admin/article_category/edit";
	}

	/**
	 * 更新
	 */
	@PostMapping("/update")
	public String update(ArticleCategory articleCategory, Long parentId, String countryName, RedirectAttributes redirectAttributes) {
		articleCategory.setParent(articleCategoryService.find(parentId));
		if (!isValid(articleCategory)) {
			return ERROR_VIEW;
		}
		if (articleCategory.getParent() != null) {
			ArticleCategory parent = articleCategory.getParent();
			if (parent.equals(articleCategory)) {
				return ERROR_VIEW;
			}
			List<ArticleCategory> children = articleCategoryService.findChildren(parent, true, null);
			if (children != null && children.contains(parent)) {
				return ERROR_VIEW;
			}
		}
		articleCategory.setCountry(countryService.findByName(countryName));
		articleCategoryService.update(articleCategory, "treePath", "grade", "children", "articles");
		addFlashMessage(redirectAttributes, Message.success(SUCCESS_MESSAGE));
		return "redirect:list";
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(String countryName, ModelMap model) {
		Country country = null;
		if (StringUtil.isNotEmpty(countryName)) {
			country = countryService.findByName(countryName);
		}
		model.addAttribute("countryName", countryName);
		model.addAttribute("articleCategoryTree", articleCategoryService.findTree(country));
		return "admin/article_category/list";
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public @ResponseBody Message delete(Long id) {
		ArticleCategory articleCategory = articleCategoryService.find(id);
		if (articleCategory == null) {
			return Message.error(ERROR_MESSAGE);
		}
		Set<ArticleCategory> children = articleCategory.getChildren();
		if (children != null && !children.isEmpty()) {
			return Message.error("admin.articleCategory.deleteExistChildrenNotAllowed");
		}
		Set<Article> articles = articleCategory.getArticles();
		if (articles != null && !articles.isEmpty()) {
			return Message.error("admin.articleCategory.deleteExistArticleNotAllowed");
		}
		articleCategoryService.delete(id);
		return Message.success(SUCCESS_MESSAGE);
	}
	
	/**
	 * 根据国家选择上级文章分类
	 */
	@GetMapping("/listByCountry")
	public @ResponseBody JSONArray listByCountry(String countryName) {
		Country country = null;
		if (StringUtil.isNotEmpty(countryName)) {
			country = countryService.findByName(countryName);
		}
		List<ArticleCategory> list = articleCategoryService.findTree(country);
		JSONArray jsonArray = new JSONArray();
		if (null != list) {
			for (ArticleCategory bean : list) {
				JSONObject object = new JSONObject();
				object.put("id", bean.getId());
				object.put("name", bean.getName());
				object.put("grade", bean.getGrade());
				jsonArray.add(object);
			}
		}
		return jsonArray;
	}

}