package com.taotao.order.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.CookieUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.order.service.OrderService;
import com.taotao.order.service.pojo.OrderInfo;
import com.taotao.pojo.TbItem;
import com.taotao.pojo.TbUser;

/**
 * 订单确认页面controller
 * <p>
 * Title: OrderController
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Company: www.itcast.cn
 * </p>
 * 
 * @version 1.0
 */
@Controller
public class OrderController {
	
	@Value("${COOKIE_CART_KEY}")
	private String COOKIE_CART_KEY;
	@Autowired
	private OrderService orderService;

	@RequestMapping("/order/order-cart")
	public String showOrderCart(HttpServletRequest request) {
		// 取购物车商品列表
		List<TbItem> cartList = getCartList(request);
		//取用户id
		TbUser user = (TbUser) request.getAttribute("user");
		System.out.println(user.getUsername());
		//根据用户id查询收货地址列表。静态数据
		//从数据库中查询支付方式列表。
		//传递给页面
		request.setAttribute("cartList", cartList);
		//返回逻辑视图
		return "order-cart";
	}

	// 从cookie中取购物车列表
	private List<TbItem> getCartList(HttpServletRequest request) {
		// 使用CookieUtils取购物车列表
		String json = CookieUtils.getCookieValue(request, COOKIE_CART_KEY, true);
		// 判断cookie中是否有值
		if (StringUtils.isBlank(json)) {
			// 没有值返回一个空List
			return new ArrayList<>();
		}
		// 把json转换成list对象
		List<TbItem> list = JsonUtils.jsonToList(json, TbItem.class);
		return list;

	}
	
	@RequestMapping(value="/order/create", method=RequestMethod.POST)
	public String createOrder(OrderInfo orderInfo, Model model, HttpServletRequest request) {
		//取用户id
		TbUser user = (TbUser) request.getAttribute("user");
		orderInfo.setUserId(user.getId());
		orderInfo.setBuyerNick(user.getUsername());
		//生成订单
		TaotaoResult result = orderService.createOrder(orderInfo);
		//取订单号，并传递给页面
		String orderId = result.getData().toString();
		model.addAttribute("orderId", orderId);
		model.addAttribute("payment", orderInfo.getPayment());
		//预计送达时间三天后。
		DateTime dateTime = new DateTime();
		dateTime = dateTime.plusDays(3);
		model.addAttribute("date", dateTime.toString("yyyy-MM-dd"));
		//返回逻辑视图
		return "success";
	}
}
