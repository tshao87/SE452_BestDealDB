import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Payment")

public class Payment extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();

		Utilities utility = new Utilities(request, pw);
		if (!utility.isLoggedin()) {
			HttpSession session = request.getSession(true);
			session.setAttribute("login_msg", "Please Login to Pay");
			response.sendRedirect("Login");
			return;
		}

		int orderID = MySQLDataStoreUtilities.getNextOrderID();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, Properties.ORDER_PROCESSING_DAYS);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String dDate = dateFormat.format(calendar.getTime());
		String userAddress = request.getParameter("userAddress");
		String creditCardNo = request.getParameter("creditCardNo");
		

		utility.printHtml("Header.html");
		utility.printHtml("LeftNavigationBar.html");
		pw.print("<div id='content'><div class='post'><h2 class='title meta'>");
		pw.print("<a style='font-size: 24px;'>Order Confirmed</a>");
		pw.print("</h2><div class='entry'>");
		
		if (request.getParameter("AddNewOrder") != null) {
			Map<String, String[]> parameterMap = request.getParameterMap();
			String userName = request.getParameter("customerUserName");
			
			for (String s : parameterMap.keySet()) {
				if (parameterMap.get(s)[0].equals("on")) {
					String itemID = s;
					String type = "";
					if (s.contains("phone")) {
						type = "smartphones";
					} else if (s.contains("laptop")) {
						type = "laptops";
					} else if (s.contains("tablet")) {
						type = "tablets";
					} else if (s.contains("tv")) {
						type = "tvs";
					} else if (s.contains("accessory")) {
						type = "accessories";
					}
					int amount = Integer.parseInt(parameterMap.get("amount"+s)[0]);
					utility.storeCurrentOrderItems(userName, itemID, type, amount);
				}
			}
			
			if (!OrdersHashMap.orders.containsKey(userName)) {
				pw.print("<h4 style='color:red'>You did not select any items</h4>");
				pw.print("</div></div></div>");
				utility.printHtml("Footer.html");
				return;
			} else {
				ArrayList<OrderItem> orderItemList = OrdersHashMap.orders.get(userName);
				double total = 0.0;
				for (OrderItem oi : orderItemList) {
					total = total + (utility.calculatePrice(oi) * oi.getAmount());
				}
				total = Double.parseDouble(new DecimalFormat("#.00").format(total));
				utility.storePayment(userName, orderID, total, userAddress, creditCardNo, dDate);
				utility.storeOrderItems(orderID, OrdersHashMap.orders.get(userName));
				OrdersHashMap.orders.remove(userName);
			}

		} else {
			String orderTotal = request.getParameter("orderTotal");

			utility.storePayment(utility.username(), orderID, Double.parseDouble(orderTotal), userAddress, creditCardNo, dDate);
			utility.storeOrderItems(orderID, OrdersHashMap.orders.get(utility.username()));
			OrdersHashMap.orders.remove(utility.username());
		}

		pw.print("<h2>Your Order");
		pw.print("&nbsp&nbsp");
		pw.print("is stored ");
		pw.print("<br>Your Order No is " + (orderID));
		pw.print("<br>Your Order will be deliveried by " + dDate);
		pw.print("</h2></div></div></div>");
		utility.printHtml("Footer.html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		@SuppressWarnings("unused")
		Utilities utility = new Utilities(request, pw);
	}
}
