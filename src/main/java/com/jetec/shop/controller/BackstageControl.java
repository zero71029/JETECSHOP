package com.jetec.shop.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jetec.shop.Tools.ZeroTools;
import com.jetec.shop.model.AdminBean;
import com.jetec.shop.model.MessageBean;
import com.jetec.shop.model.OrderBean;
import com.jetec.shop.model.OrderDetail;
import com.jetec.shop.model.PdfUrlBean;
import com.jetec.shop.model.ProductBean;
import com.jetec.shop.model.ProductOptionBean;
import com.jetec.shop.model.ProductTypeBean;
import com.jetec.shop.model.UserBean;
import com.jetec.shop.repository.AdminRepository;
import com.jetec.shop.repository.MessageRepository;
import com.jetec.shop.repository.OrderDetailRepository;
import com.jetec.shop.repository.OrderRepository;
import com.jetec.shop.repository.ProductOptionRepository;
import com.jetec.shop.repository.ProductRepository;
import com.jetec.shop.repository.ProductSpecificationRepository;
import com.jetec.shop.repository.ProductTypeRepository;
import com.jetec.shop.repository.UserRepository;
import com.jetec.shop.repository.service.BackstageService;

@Controller
@RequestMapping("/backstage")
public class BackstageControl {
	@Autowired
	OrderRepository orderRepository;
	@Autowired
	OrderDetailRepository orderDetailRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	BackstageService backstageService;
	@Autowired
	ProductSpecificationRepository psr;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ProductTypeRepository ptr;
	@Autowired
	MessageRepository messageRepository;
	@Autowired
	ProductOptionRepository por;
	@Autowired
	ZeroTools zTools;
	@Autowired
	AdminRepository adminRepository;

//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ????????????
	@RequestMapping("/selectOrder")
	public String selectOrder(Model model, @RequestParam("state") String state) {
		System.out.println("*****????????????*****" + state);
		model.addAttribute("orderList", orderRepository.findByOrderstatus(state));
		return "/backstage/order";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ????????????
	@RequestMapping("/orderDetail/{id}")
	@ResponseBody
	public List<Map<String, String>> orderDetail(@PathVariable("id") Integer orderid) {
		System.out.println("*****?????????????????? *****");
		Optional<OrderBean> optional = orderRepository.findById(orderid);
		OrderBean orderBean = optional.get();
		List<OrderDetail> list = orderBean.getOrderDetail();

//        List<OrderDetail> list = orderDetailRepository.findByOrderid(orderid);
		List<Map<String, String>> result = new ArrayList<>();
		for (OrderDetail odb : list) {
			Map<String, String> map = new HashMap<>();
			map.put("orderId", odb.getOrderid() + "");
			ProductOptionBean productOptionBean = por.getById(odb.getProductid());
			Optional<ProductBean> op = productRepository.findById(productOptionBean.getProductid());
			ProductBean b = op.get();
			map.put("product_type", productOptionBean.getProduct_type());
			map.put("product_group", productOptionBean.getProduct_group());
			map.put("product_option", productOptionBean.getProduct_option());
			map.put("product_group2", productOptionBean.getProduct_group2());
			map.put("product_option2", productOptionBean.getProduct_option2());
			map.put("product_group3", productOptionBean.getProduct_group3());
			map.put("product_option3", productOptionBean.getProduct_option3());

			map.put("productname", b.getName());
			map.put("SellingPrice", productOptionBean.getProduct_price() + "");
			map.put("Unit", odb.getQuantity() + "");
			result.add(map);
		}
		System.out.println(result);
		return result;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//??????????????????
	@RequestMapping("/state/{orderId}")
	public String state(@PathVariable("orderId") Integer orderId, @RequestParam("orderStatus") String orderStatus) {
		Optional<OrderBean> op = orderRepository.findById(orderId);
		OrderBean bean = op.get();
		bean.setOrderstatus(orderStatus);
		orderRepository.save(bean);
		if (orderStatus.equals("4"))
			orderStatus = "3";
		return "redirect:/backstage/selectOrder?pag=1&state=" + orderStatus;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//?????????????????? ?????????
	@RequestMapping("/product")
	public String productList(Model model, @RequestParam("pag") Integer p, @RequestParam("state") String state) {
		System.out.println("*****?????????????????? *****");
		List<ProductBean> result = productRepository.findByProductstatus(state);
//		List<ProductBean> result = productRepository.findAll();
		model.addAttribute("productList", result);
		return "/backstage/productList";
	}

// ??????????????????
	@RequestMapping("/product/{productId}")
	public String product(Model model, @PathVariable("productId") Integer productId) {
		System.out.println("*****?????????????????? *****" + productId);
		// ????????????
		List<String> love1 = ptr.findLove1();
		List<ProductTypeBean> love2 = ptr.findAll();
		model.addAttribute("love1", love1);
		model.addAttribute("love2", love2);

		ProductBean productBean = backstageService.getProduct(productId);
		// ????????????
		try {
			System.out.println(productBean.getProductOptionBean());
			model.addAttribute("group", productBean.getProductOptionBean().get(0).getProduct_group());
			model.addAttribute("group2", productBean.getProductOptionBean().get(0).getProduct_group2());
			model.addAttribute("group3", productBean.getProductOptionBean().get(0).getProduct_group3());

		} catch (Exception e) {
			// TODO: handle exception
		}

//		Optional<ProductBean> optional = productRepository.findById(productId);
//		ProductBean productBean = optional.get();		
		model.addAttribute("product", productBean);
		return "/backstage/product";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//??????????????????
	@RequestMapping("/updataProduct/{productId}")
	public String updataProduct(Model model, @PathVariable("productId") Integer productId, ProductBean productBean) {
		System.out.println("*****?????????????????? *****");
		System.out.println(productBean);
		// ??????????????????????????????
		model.addAttribute("product", productBean);
		// ????????????
		// ????????????
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);

		// ??????????????????
		if (productBean.getName() == null || productBean.getName().length() == 0) {
			errors.put("name", "????????????");
		}
		if (productBean.getType() == null) {
			errors.put("type", "????????????");
		}
		if (productBean.getModel() == null || productBean.getModel().length() == 0) {
			errors.put("model", "????????????");
		}
		if (productBean.getQuantity() == null) {
			errors.put("quantity", "????????????");
		}
		if (productBean.getSellprice() == null) {
			errors.put("sellprice", "????????????");
		}
		if (productBean.getPurchaseprice() == null) {
			errors.put("purchaseprice", "????????????");
		}
		if (productBean.getProducttext() == null || productBean.getProducttext().length() == 0) {
			errors.put("producttext", "????????????");
		}
		if (productId == 0)
			if (productRepository.existsByModel(productBean.getModel()) && productId == 0)
				errors.put("model2", "????????????");

		// ???????????? ???????????????
		if (errors != null && !errors.isEmpty()) {
			System.out.println("errors");
			// ????????????
			List<String> love1 = ptr.findLove1();
			List<ProductTypeBean> love2 = ptr.findAll();
			model.addAttribute("love1", love1);
			model.addAttribute("love2", love2);
			return "/backstage/product";
		}
		productBean.setVendorid(1);
		productBean.setProductcategoryid(1);
		ProductBean saveBean = null;
		// ?????????????????????????????????
		if (productId == 0) {
			productBean.setCount(0);
			productBean.setProductstatus("1");
			saveBean = backstageService.saveProduct(productBean);
			ProductOptionBean pob = new ProductOptionBean();
			pob.setProductid(saveBean.getId());
			pob.setProduct_group("1");
			pob.setPurchase_price(saveBean.getPurchaseprice());
			pob.setProduct_price(saveBean.getSellprice());
			pob.setProduct_quantity(saveBean.getQuantity());
			backstageService.savegroup(pob);
			System.out.println("saveBean =" + saveBean);

		} else {
			productBean.setProductOptionBean(productRepository.getById(productId).getProductOptionBean());
			productBean.setId(productId);
			productBean.setPictureurl(productRepository.getById(productId).getPictureurl());
			productBean.setCount(productRepository.getById(productId).getCount());
			saveBean = backstageService.saveProduct(productBean);
			productRepository.delNull();
			System.out.println(saveBean);
		}
		productRepository.delNull2();
		return "redirect:/backstage/product/" + saveBean.getId();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/addPic/{Productid}")
	@ResponseBody
	public String pic(MultipartHttpServletRequest multipartRequest, @PathVariable("Productid") Integer Productid,
			Model model) {
		System.out.println("*****???????????? *****");
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		System.out.println(fileMap);
		Optional<ProductBean> op = productRepository.findById(Productid);
		ProductBean bean = op.get();
		String Productmodel = bean.getModel();
		// ????????????
		try {
			for (int i = 0; i <= fileMap.size(); i++) {
				// 2. ????????????????????????
				if (fileMap.get("file" + i) != null) {
					System.out.println(fileMap.get("file" + i).getOriginalFilename());
					// ??????+??????
					String lastname = fileMap.get("file" + i).getOriginalFilename()
							.substring(fileMap.get("file" + i).getOriginalFilename().indexOf("."));
					System.out.println(lastname);
					fileMap.get("file" + i).transferTo(new File(
							"E:\\shop\\src\\main\\resources\\static\\images\\product\\" + Productmodel + lastname));
//                    fileMap.get("file" + i).transferTo(new File("classpath:/resources/static\\images\\product\\" + Productmodel + ".jpg"));
					// 3. ??????????????????????????????
					bean.setPictureurl(Productmodel + lastname);
					productRepository.save(bean);

//                    ProductPictureBean pBean = productPictureJpaReposit.findProducturl(Productmodel + "-" + i);
//                    if (pBean == null) {
//                        pBean = new ProductPictureBean();
//                    }
//                    pBean.setProducturl(Productmodel + "-" + i);
//                    pBean.setProductid(Productid);
//                    productPictureJpaReposit.save(pBean);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "ok";

	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
// ????????????
	@RequestMapping("/selectProduct/{product}")
	public String selectProduct(Model model, @PathVariable("product") String product) {
		System.out.println("*****????????????*****" + product);
		ProductBean productBean = null;
		List<ProductBean> list = new ArrayList<ProductBean>();
		// ????????????
		try {
			// ??????id
			System.out.println(Integer.parseInt(product));
			System.out.println(backstageService.getProduct(Integer.parseInt(product)));
			productBean = backstageService.getProduct(Integer.parseInt(product));
			list.add(productBean);

		} catch (Exception e) {

		}
		// ???????????? ????????????
		for (ProductBean p : productRepository.findByModelLikeIgnoreCase("%" + product + "%")) {
			System.out.println(p);
			list.add(p);
		}

		// ??????????????? ???????????????
		for (ProductBean p : productRepository.findByNameLikeIgnoreCase("%" + product + "%")) {
			if (null != p)
				list.add(p);
		}
		model.addAttribute("productList", list);
		return "/backstage/productList";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/userList/{AAA}")
	public String userList(Model model, @PathVariable("AAA") String product) {
		System.out.println("*****????????????*****" + product);
		model.addAttribute("userList", userRepository.findAll());
		return "/backstage/userList";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/selectUser/{userId}")
	public String selectUser(Model model, @PathVariable("userId") String userId) {
		System.out.println("*****????????????*****");
		List<UserBean> userList = new ArrayList<UserBean>();
		try {
			int AAA = Integer.parseInt(userId);
			if (userRepository.existsById(AAA)) {
				userList.add(userRepository.getById(AAA));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (userRepository.existsByPhone(userId)) {
			userList.add(userRepository.findByPhone(userId));
		}
		if (userRepository.existsByEmail(userId)) {
			userList.add(userRepository.findByEmail(userId));
		}
		if (userRepository.existsByFirstname(userId)) {
			for (UserBean b : userRepository.findByFirstname(userId)) {
				userList.add(b);
			}
		}
		if (userRepository.existsByLastname(userId)) {
			for (UserBean b : userRepository.findByLastname(userId)) {
				userList.add(b);
			}
		}
		model.addAttribute("userList", userList);
		System.out.println(userList);
		return "/backstage/userList";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/userDetail/{userId}")
	@ResponseBody
	public UserBean userDetail(@PathVariable("userId") Integer userId) {
		System.out.println("*****????????????*****");
		System.out.println(userRepository.getById(userId));
		return userRepository.getById(userId);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/selectOrder/{userId}")
	public String selectOrder(Model model, @PathVariable("userId") Integer userId) {
		System.out.println("*****????????????by??????*****");
		model.addAttribute("orderList", orderRepository.findByUserid(userId));
		return "/backstage/order";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/addType")
	public String addType(HttpServletRequest req,Model model) {
		System.out.println("*****????????????*****");
		ServletContext app = req.getServletContext();
		List<String> love1 = ptr.findLove1();
		List<ProductTypeBean> love2 = ptr.findAll();
		app.setAttribute("love1", love1);
		app.setAttribute("love2", love2);	
//		List<String> love1 = ptr.findLove1();
//		List<ProductTypeBean> love2 = ptr.findAll();
//		model.addAttribute("love1", love1);
//		model.addAttribute("love2", love2);
		return "/backstage/addType";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/saveLove2/{l1}")
	public String saveLove2(Model model, @RequestParam("love2") String l2, @PathVariable("l1") String l1) {
		System.out.println("*****????????????*****");
		if (l2 == null || l2.length() == 0) {
			model.addAttribute("mess", "????????????,????????????");
			return "/backstage/addType";
		}
		backstageService.saveType(l1, l2);
		return "redirect:/backstage/addType";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/saveLove1")
	public String saveLove1(Model model, @RequestParam("love1") String l1) {
		System.out.println("*****????????????*****");

		if (l1 == null || l1.length() == 0) {
			model.addAttribute("mess", "????????????,????????????");
			return "/backstage/addType";
		}
		backstageService.saveType(l1, null);
		return "redirect:/backstage/addType";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/delLove2/{typeid}")
	public String delLove2(Model model, @PathVariable("typeid") Integer typeid) {
		System.out.println("*****????????????*****");
		if (productRepository.existsByType(typeid)) {
			model.addAttribute("mess", "???????????????,????????????");
			return "/backstage/addType";
		}
		backstageService.delType(typeid);
		List<String> love1 = ptr.findLove1();
		List<ProductTypeBean> love2 = ptr.findAll();
		model.addAttribute("love1", love1);
		model.addAttribute("love2", love2);
		model.addAttribute("mess", "????????????");
		return "redirect:/backstage/addType";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/messList")
	public String messList(Model model, @RequestParam("state") String state) {
		System.out.println("*****????????????*****");
		List<MessageBean> messageBeans = messageRepository.findByState(state);
		model.addAttribute("mess", messageBeans);
		return "/backstage/messList";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//??????????????????
	@RequestMapping("/mess/{messid}")
	public String mess(Model model, @PathVariable("messid") Integer messid) {
		System.out.println("*****??????????????????*****");
		MessageBean messageBeans = messageRepository.getById(messid);
		model.addAttribute("mess", messageBeans);
		return "/backstage/mess";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/reply")
	public String reply(Model model, @RequestParam("reply") String reply, @RequestParam("id") Integer id) {
		System.out.println("*****????????????*****");
		MessageBean messageBean = messageRepository.getById(id);
		messageBean.setReply(reply);
		messageBean.setState("2");
		messageRepository.save(messageBean);
		model.addAttribute("mess", messageBean);
		model.addAttribute("susses", "????????????");

		String Subject = "????????????";// ??????
		String text = reply;// ??????
		zTools.mail(messageBean.getEmail(), text, Subject);

		return "/backstage/mess";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/addGroup")
	public String addGroup(Model model, ProductOptionBean pob) {
		System.out.println("*****????????????*****");
		if (pob.getProduct_group() == null || pob.getProduct_group().length() == 0) {
			model.addAttribute("mess", "????????????,????????????");
			return "/backstage/addType";
		}
		if (pob.getProduct_option() != null) {
			backstageService.delOptionNull();
		}
		backstageService.savegroup(pob);
		return "redirect:/backstage/product/" + pob.getProductid();
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/delgroup/{group}/{productid}")
	public String delgroup(Model model, @PathVariable("group") String group,
			@PathVariable("productid") String productid) {
		System.out.println("*****????????????*****");
		backstageService.delGroup(group);
		return "redirect:/backstage/product/" + productid;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/saveOption/{productId}")
	@ResponseBody
	public String saveOption(ProductOptionBean pob, @PathVariable("productId") Integer productId) {
		System.out.println("*****????????????*****");
		System.out.println(pob);
		if (pob.getProduct_group3().isEmpty() && !pob.getProduct_option3().isEmpty()) {
			return "????????????????????????3";
		}
		if (pob.getProduct_group2().isEmpty() && !pob.getProduct_option2().isEmpty()) {
			return "????????????????????????2";
		}
		if (pob.getProduct_group().isEmpty() && !pob.getProduct_option().isEmpty()) {
			return "????????????????????????1";
		}

		if (pob.getPurchase_price() == null || pob.getProduct_price() == null || pob.getProduct_quantity() == null) {
			return "??????	??????	?????? ????????????";
		}
		if (!pob.getProduct_option3().isEmpty() && pob.getProduct_option2().isEmpty()
				&& pob.getProduct_option().isEmpty()) {
			return "???2???????????????";
		}
		if (!pob.getProduct_option2().isEmpty() && pob.getProduct_option().isEmpty()) {
			return "???1???????????????";
		}
		pob.setProduct_model(pob.getProduct_option() + "-" + pob.getProduct_option2() + "-" + pob.getProduct_option3());
		pob.setProductid(productId);
		System.out.println(pob);
		backstageService.savegroup(pob);
		return "save ok";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("/saveGroup/{productId}")
	@ResponseBody
	public String saveGroup(ProductOptionBean pob, @PathVariable("productId") Integer productId) {
		System.out.println("*****????????????*****");
		System.out.println(pob);
		System.out.println(por.existsByProductid(productId));
		if (por.existsByProductid(productId)) {
			if (!pob.getProduct_group().isEmpty()) {
				por.saveGroup1(pob.getProduct_group(), productId);
			}
			if (!pob.getProduct_group2().isEmpty()) {
				por.saveGroup2(pob.getProduct_group2(), productId);
			}
			if (!pob.getProduct_group3().isEmpty()) {
				por.saveGroup3(pob.getProduct_group3(), productId);
			}
			return "????????????";
		} else {
			pob.setProductid(productId);
			backstageService.savegroup(pob);
			return "save ok";
		}

//		backstageService.savegroup(pob);
//		return "??????";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/upFile/{Productid}")
	@ResponseBody
	public String upFile(MultipartHttpServletRequest multipartRequest, @PathVariable("Productid") Integer Productid,
			Model model) {
		System.out.println("*****????????????*****");
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		System.out.println("fileMap " + fileMap);
		Optional<ProductBean> op = productRepository.findById(Productid);
		ProductBean bean = op.get();
		String Productmodel = bean.getModel();
// ????????????
		try {
			for (int i = 0; i <= fileMap.size(); i++) {
// 2. ????????????????????????
				if (fileMap.get("file" + i) != null) {
					System.out.println(fileMap.get("file" + i).getOriginalFilename());
// ??????+??????
					String lastname = fileMap.get("file" + i).getOriginalFilename()
							.substring(fileMap.get("file" + i).getOriginalFilename().indexOf("."));
					System.out.println(lastname);
					fileMap.get("file" + i).transferTo(new File("E:\\shop\\src\\main\\resources\\static\\PDF\\"
							+ fileMap.get("file" + i).getOriginalFilename()));
//fileMap.get("file" + i).transferTo(new File("classpath:/resources/static\\images\\product\\" + Productmodel + ".jpg"));
// 3. ??????????????????????????????
					PdfUrlBean pdfUrlBean = new PdfUrlBean();
					pdfUrlBean.setProductid(Productid);
					pdfUrlBean.setUrl(fileMap.get("file" + i).getOriginalFilename());
					backstageService.saveUrl(pdfUrlBean);

//ProductPictureBean pBean = productPictureJpaReposit.findProducturl(Productmodel + "-" + i);
//if (pBean == null) {
//pBean = new ProductPictureBean();
//}
//pBean.setProducturl(Productmodel + "-" + i);
//pBean.setProductid(Productid);
//productPictureJpaReposit.save(pBean);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "ok";

	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//??????pdf
	@RequestMapping("removePDF/{url}/{productid}")
	public String removePDF(@PathVariable("url") String url, @PathVariable("productid") Integer productid) {
		System.out.println("*****??????pdf*****");
		backstageService.delPDF(url, productid);
		return "redirect:/backstage/product/" + productid;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("admin")
	public String admin(Model model) {
		System.out.println("*****????????????*****");
		model.addAttribute("admin", backstageService.findAdmin());
		return "/backstage/admin";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//????????????
	@RequestMapping("admin/{id}")
	public String admindetail(Model model, @PathVariable("id") Integer id) {
		System.out.println("*****????????????*****");
		if (id == 0) {

		} else {
			model.addAttribute("admin", backstageService.findAdminDetail(id));
		}
		return "/backstage/adminDetail";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//??????????????????
	@RequestMapping("saveAdmin")
	public String saveAdmin(Model model, AdminBean bean) {
		System.out.println("*****??????/??????????????????*****");
		System.out.println(bean);
		// ????????????
		// ????????????
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);
		if (bean.getId() == null) {
			if (adminRepository.existsByEmail(bean.getEmail())) {
				errors.put("result", "eamil??????");
			}
		}

		// ??????????????????
		if (bean.getPhone() == null || bean.getPhone().length() == 0) {
			errors.put("phone", "????????????");
		}
		if (bean.getEmail() == null || bean.getEmail().length() == 0) {

			errors.put("email", "??????Email");
		}
		if (bean.getName() == null || bean.getName().length() == 0) {

			errors.put("name", "??????Name");
		}
		if (bean.getAddress() == null || bean.getAddress().length() == 0) {

			errors.put("address", "??????address");
		}
		if (bean.getPassword() == null || bean.getPassword().length() == 0) {
			errors.put("password", "????????????");
		}

		// ???????????? ???????????????
		if (errors != null && !errors.isEmpty()) {
			System.out.println("errors");
			model.addAttribute("admin", bean);
			if (bean.getId() == null) {
				return "/backstage/adminDetail";
			}
			return "/backstage/adminDetail";
		}
		AdminBean save = backstageService.saveAdmin(bean);
		model.addAttribute("admin", save);
		errors.put("result", "????????????");
		return "redirect:/backstage/admin";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////55
//??????Option
	@RequestMapping("removeOption/{Optionid}/{productid}")
	public String removeOption(@PathVariable("Optionid") Integer Optionid,
			@PathVariable("productid") Integer productid) {
		System.out.println("*****??????Option*****");
		backstageService.delOption(Optionid);
		return "redirect:/backstage/product/" + productid;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//????????????
	@RequestMapping("/findOrder/{orderId}")
	public String findOrder(Model model, @PathVariable("orderId") String orderId) {
		System.out.println("*****????????????*****");
		List<OrderBean> list = new ArrayList<OrderBean>();
		OrderBean orderBean = new OrderBean();
		try {
			// ??????id
			if (orderRepository.existsById(Integer.parseInt(orderId)))
				orderBean = orderRepository.getById(Integer.parseInt(orderId));
			list.add(orderBean);

		} catch (Exception e) {

		}

		// ???????????????
		for (OrderBean p : orderRepository.Ordename(orderId)) {
			if (null != p)
				list.add(p);
		}
		//
		for (OrderBean p : orderRepository.findByFirstname(orderId)) {
			if (null != p)
				list.add(p);
		}
		//
		for (OrderBean p : orderRepository.findByLastname(orderId)) {
			if (null != p)
				list.add(p);
		}
		//
		for (OrderBean p : orderRepository.findByCompany(orderId)) {
			if (null != p)
				list.add(p);
		}
		//
		for (OrderBean p : orderRepository.findByPhone(orderId)) {
			if (null != p)
				list.add(p);
		}
		//
		for (OrderBean p : orderRepository.findByOrderemail(orderId)) {
			if (null != p)
				list.add(p);
		}

		if (list.isEmpty()) {
			System.out.println(list);
		}

//		list.add(orderRepository.getById(orderId));
		model.addAttribute("orderList", list);
		return "/backstage/order";
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//??????
	@RequestMapping("/Signout")
	public String Signout(HttpSession session) {
		System.out.println("*****??????******");
		session.invalidate();
		return "redirect:/time.jsp";
	}



}
