import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper {

	private MyModel model = null;
	private String connectionUrlSqlServer = "jdbc:sqlserver://localhost\\sqlexpress;databaseName=StockMarketDB;integratedSecurity=true";
	private HashMap<String,ArrayList<String>> currentCountriesWithCities;
	public DBHelper() {
		/*try {
			Connection connection = DriverManager.getConnection(connectionUrlSqlServer);
			System.out.println("Connected succsfuly.");
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		*/
		 currentCountriesWithCities = this.getCountriesWithCitiesFromDataBase();
	}
	
	private int getMonthNumber(String monthString) {
		
		switch(monthString) {
		case "Jan": return 1;
		case "feb":return 2;	
		case "Mar":return 3;	
		case "Apr":return 4;
		case "May":return 5;
		case "Jun":return 6;
		case "July":return 7;
		case "Aug":return 8;
		case "Sep":return 9;
		case "Oct":return 10;
		case "Nov":return 11;
		case "Dec":return 12;
		default:return 1;
		}
		
	}
	
	// 0 three-names, 1- Country, 2- City, 3 date of Birth, 4-email, 5-user name, 6 - password
	public void registerNewUser(String[] userDetails) {
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			
			 Statement statementToGetCityID = connection.createStatement();
				ResultSet currentCityIDResult = statementToGetCityID.executeQuery("SELECT ID FROM Cities\r\n"
						+ "WHERE CityName LIKE '"+userDetails[2]+"'");
				
			 currentCityIDResult.next();
			 int cityID = currentCityIDResult.getInt("ID");
			 
			 PreparedStatement preparedStatementUserDetails = connection.prepareStatement("EXEC dbo.uspInsertUser"
			 		+ " @FirstName =?,"
			 		+ "@MiddleName =?,"
			 		+ "@LastName=?,"
			 		+ "@Age=?,"
			 		+ "@Username=?,"
			 		+ "@EmailAddress =?,"
			 		+ "@PasswordString=?,"
			 		+ "@CityID=?,"
			 		+ "@DateOfBirth=?"); 
			 
					 
			 
			 String[] datePartsFromUserDetails = userDetails[3].split("-");
			 LocalDate today = LocalDate.now();                     
			 LocalDate birthday = LocalDate.of(Integer.parseInt(datePartsFromUserDetails[0])
					 ,this.getMonthNumber(datePartsFromUserDetails[1]),
					 Integer.parseInt(datePartsFromUserDetails[2]));
			 
			 Period p = Period.between(birthday, today);
			 
			 String[] userThreeNames = userDetails[0].split(" ");
			 preparedStatementUserDetails.setString(1, userThreeNames[0]);
			 preparedStatementUserDetails.setString(2, userThreeNames[1]);
			 preparedStatementUserDetails.setString(3, userThreeNames[2]);
			 
			 preparedStatementUserDetails.setInt(4, p.getYears());
			 
			 preparedStatementUserDetails.setString(5, userDetails[5]);
			 preparedStatementUserDetails.setString(6, userDetails[4]);
			 preparedStatementUserDetails.setString(7, userDetails[6]);
			 preparedStatementUserDetails.setInt(8, cityID);
			 preparedStatementUserDetails.setString(9, userDetails[3]);
			
			 int rowAddedUserDetails = preparedStatementUserDetails.executeUpdate();			 
			 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	//0-ProductName, 1-ProductPriceOnDelivery,2-ProductPriceOnPurchase,3-ProductType
	public void createProduct(String[] productDataGiven) {
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			
			 PreparedStatement preparedStatementProductDetails = connection.prepareStatement("INSERT INTO Products(ProductName,ProductPriceOnDelivery,ProductPriceOnPurchase,ProductType)\r\n"
			 		+ "	VALUES\r\n"
			 		+ "		(?,?,?,(SELECT ID FROM ProductTypes WHERE ProductTypeName LIKE(?)))");	
			
			 preparedStatementProductDetails.setString(1,productDataGiven[0]);
			 
			 BigDecimal decimalDelivery = new BigDecimal(Integer.parseInt(productDataGiven[1]));
			 BigDecimal decimalPurchase = new BigDecimal(Integer.parseInt(productDataGiven[2]));
			 
			 preparedStatementProductDetails.setBigDecimal(2, decimalDelivery);
			 preparedStatementProductDetails.setBigDecimal(3, decimalPurchase);
			 
			 preparedStatementProductDetails.setString(4, productDataGiven[3]);
			 
			 preparedStatementProductDetails.executeUpdate();
			 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
	
	public void deleteProduct(int id) {
		
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			
			PreparedStatement preparedStatementProductDetails = connection.prepareStatement("UPDATE Products SET IsAvaialble = 0 WHERE ID =?");
			preparedStatementProductDetails.setInt(1, id);		
			preparedStatementProductDetails.executeUpdate();
			 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	
	
	private int getCategoryKeyNumber(String categoryName) {
		switch(categoryName) {
		case "Electronics": return 1;
		case "Food": return 2;
		case "Clothes": return 3;
		case "Vehicle": return 4;
		case "Cryptocurrency": return 5;
		case "Housing": return 6;
		}
		return 0;
	}
	
	private HashMap<String,ArrayList<String>> getCountriesWithCitiesFromDataBase() {
		HashMap<String,ArrayList<String>> countriesWithCities = null;
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			
			Statement statementToGetCountries = connection.createStatement();
			ResultSet currentCountriesWithCities = statementToGetCountries.executeQuery("SELECT CountryName, CityName FROM Countries\r\n"
					+ "JOIN Cities ON Countries.ID = Cities.CountryID\r\n"
					+ "ORDER BY CountryName ASC,CityName ASC");
			
			countriesWithCities = new HashMap<String,ArrayList<String>>();
			
			while(currentCountriesWithCities.next()) {
				String countryName = currentCountriesWithCities.getString("CountryName");
				String cityName = currentCountriesWithCities.getString("CityName");
				if(!(countriesWithCities.containsKey(countryName))) {	
					countriesWithCities.put(countryName, new ArrayList<String>());
					countriesWithCities.get(countryName).add(cityName);
				}else {
					countriesWithCities.get(countryName).add(cityName);
				}
			}			
			
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		
		return countriesWithCities;
	}
	
	public boolean checkLoginUser(String usernameEmail, String password) {
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			
			String sqlStatement ="";
			if(usernameEmail.contains("@")) {
				sqlStatement = "SEECT ID FROM Users WHERE EmailAddress LIKE (?) AND";
			}else {
				sqlStatement = "EXEC   dbo.uspCheckLoginUserWithUsername  @Username = ?,@Password =?";
			}
			
			 PreparedStatement statementToCheckLoging = connection.prepareStatement(sqlStatement);
			 statementToCheckLoging.setString(1, usernameEmail);
			 statementToCheckLoging.setString(2, password);
			 
				ResultSet currentLoginCheckBool = statementToCheckLoging.executeQuery();
				
			 if(currentLoginCheckBool.next()) {
				 return true;
			 }
			 		 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		
		return false;
		
	}
	
	public void buyProductFromOffers(int productOfferID, int currentUserLogedID) {
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement state=connection.prepareStatement("INSERT INTO PurcahsesMade(OfferID,UserID) VALUES (?,?)");
			state.setInt(1, productOfferID);
			state.setInt(2,currentUserLogedID);
			state.execute();
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
	
	//use for purchases made button to visualise the data
public MyModel getAllPurchasesMadeModelOffers() {
		
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement state=connection.prepareStatement("SELECT ProductName AS [Product bought], Username AS [Bought by], Products.ProductPriceOnPurchase AS [Bought for], ProductOffers.DateOfOffer AS [Bought on]  FROM PurcahsesMade\r\n"
					+ "JOIN Users ON Users.ID = PurcahsesMade.UserID\r\n"
					+ "JOIN ProductOffers ON ProductOffers.ID  = PurcahsesMade.OfferID\r\n"
					+ "JOIN Products ON ProductOffers.ProductID = Products.ID");
			ResultSet result = state.executeQuery();
			 model = new MyModel(result);
			 		 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return model;
		
	}


public MyModel getAllPurchasedProductsMadeModel() {
	
	try {
		Connection connection = DriverManager.getConnection(this.getConnectionString());
		PreparedStatement state=connection.prepareStatement("SELECT ProductName FROM PurcahsesMade\r\n"
				+ "JOIN ProductOffers ON ProductOffers.ID = PurcahsesMade.OfferID\r\n"
				+ "JOIN Products ON Products.ID = ProductOffers.ProductID");
		ResultSet result = state.executeQuery();
		 model = new MyModel(result);
		 		 
	}catch(SQLException e){
		System.out.println(e.getMessage());
	} catch (Exception e) {
		// TODO Auto-generated catch block
		System.out.println(e.getMessage());
	}
	return model;
	
}
	
	public void createOfferByUser(int userID) {
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement state=connection.prepareStatement("INSERT INTO ProductOffers(UserID,ProductID,DateOfOffer) VALUES(?,(SELECT IDENT_CURRENT('Products')),GETDATE())");
			state.setInt(1, userID);
			state.execute();
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
	
	public int getUserIDLogin(String userText) {
		String sqlQuery = "";
		int ID =0;
		if(userText.contains("@")) {
			sqlQuery = "SELECT ID FROM Users WHERE EmailAddress LIKE(?)";
		}else{
			sqlQuery = "SELECT ID FROM Users WHERE Username LIKE(?)";
		}
		
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement state=connection.prepareStatement(sqlQuery);
			state.setString(1, userText);
			ResultSet result = state.executeQuery();
			
			result.next();
			 ID = result.getInt("ID");
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		
		return ID;
	}
	
	public MyModel getAllDataProducts() {
		
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement state=connection.prepareStatement("SELECT Products.ID as ID, ProductTypes.ProductTypeName as Category,ProductName as [Product name], ProductPriceOnDelivery as [Delivary price],ProductPriceOnPurchase AS [PurchasePrice] FROM Products JOIN ProductTypes ON ProductTypes.ID=Products.ProductType WHERE Products.IsAvaialble = 1");
			ResultSet result = state.executeQuery();
			 model = new MyModel(result);
			 		 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return model;
		
	}
	
	public MyModel getAllDataSales() {
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement state=connection.prepareStatement("\r\n"
					+ "SELECT ProductOffers.ID AS [Offer ID], ProductTypes.ProductTypeName AS [Category],ProductName AS [Product], Users.Username AS [User],Products.ProductPriceOnPurchase AS [Price] FROM ProductOffers\r\n"
					+ " JOIN Products ON Products.ID = ProductOffers.ProductID\r\n"
					+ " JOIN Users ON Users.ID = ProductOffers.UserID\r\n"
					+ " JOIN ProductTypes ON Products.ProductType = ProductTypes.ID\r\n"
					+ " WHERE Products.IsAvaialble=1");
			ResultSet result = state.executeQuery();
			 model = new MyModel(result);
			 		 
		}catch(SQLException e){
			System.out.println(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
		
	}
	
	
	
	//0-priceDelivery,1price purchase, 2 productName, 3 categoryProduct
	public void editProducts(String[] productEditDetails, int ID) {
		String sqlQuerry="UPDATE Products SET ProductPriceOnDelivery=?, "
				+ "ProductPriceOnPurchase=?, ProductName=?,ProductType=? WHERE ID=?";
		
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			PreparedStatement product_state;
			 product_state = connection.prepareStatement(sqlQuerry);
			
			//these are with array parameter given in editProducts(HERE)
			 BigDecimal decimalDelivery = new BigDecimal(Integer.parseInt(productEditDetails[0]));
			 BigDecimal decimalPurchase = new BigDecimal(Integer.parseInt(productEditDetails[1]));
			product_state.setBigDecimal(1,decimalDelivery);
			product_state.setBigDecimal(2,decimalPurchase);
			product_state.setString(3,productEditDetails[2]);
			product_state.setInt(4,this.getCategoryKeyNumber(productEditDetails[3]));
			product_state.setInt(5, ID);
			
			product_state.execute();
			
			//this is for main frame action lsitener
			//product_table.setModel(DBHelper.getAllDataProducts());
			
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	
	
	public ResultSet searchProducts(String[] productSearchDetails) {
		ResultSet resultFromSearch = null;
		String sqlQuerry="SELECT Products.ID, ProductTypes.ProductTypeName AS [Category], Products.ProductName AS [Product Name],\r\n"
				+ "		   Products.ProductPriceOnDelivery AS [Delivery price],\r\n"
				+ "		   Products.ProductPriceOnPurchase AS [Purchase price]\r\n"
				+ "		   FROM Products\r\n"
				+ "		   JOIN ProductTypes ON ProductTypes.ID = Products.ProductType\r\n"                               //'%Alien%'
				+ "		   WHERE Products.ProductPriceOnDelivery<=? AND Products.ProductPriceOnPurchase<=? AND ProductName LIKE(?) AND Products.ProductType=?";
		
		try {
			Connection connection = DriverManager.getConnection(this.getConnectionString());
			
			PreparedStatement searchProduct = connection.prepareStatement(sqlQuerry);
			searchProduct.setBigDecimal(1,new BigDecimal(Integer.parseInt(productSearchDetails[0])));
			searchProduct.setBigDecimal(2,new BigDecimal(Integer.parseInt(productSearchDetails[1])));
			searchProduct.setString(3, "%"+productSearchDetails[2]+"%");
			searchProduct.setInt(4, this.getCategoryKeyNumber(productSearchDetails[3]));
			
			 resultFromSearch = searchProduct.executeQuery();
			
			//this is for main frame action listener
			//product_table.setModel(DBHelper.getAllDataProducts());
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return resultFromSearch; 
	}
	
	public String getConnectionString() {
		return this.connectionUrlSqlServer;
    }
	
	private void setConnectionString() {
		this.connectionUrlSqlServer =  "jdbc:sqlserver://localhost\\sqlexpress;databaseName=StockMarketDB;integratedSecurity=true";
	}
	
	public HashMap<String,ArrayList<String>> getCountriesWithCities(){
		return this.currentCountriesWithCities;
	}
	
}
