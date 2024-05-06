/** **
* ITCS 3160-0002, Spring 2024
* Ashton Cox, ashtonmcox@outlook.com
* Andy Pham, apham21@uncc.edu
* Connor Schwab, cschwab3@uncc.edu
* David Saldivar, dsaldiva@uncc.edu
* Jamison Heinrich, jheinri2@uncc.edu
* University of North Carolina at Charlotte

package edu.charlotte.cs.itcs3160;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.sql.*;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ITCS 3160-0002, Spring 2024
 * Marco Vieira, marco.vieira@charlotte.edu
 * University of North Carolina at Charlotte
 *
 * IMPORTANT: this file includes the Java implementation of the REST API
 * It is in this file that you should implement the functionalities/transactions
 */
@RestController
public class DemoProj {

    public enum StatusCode {

        SUCCESS("success", 200),
        API_ERROR("api_error", 400),
        INTERNAL_ERROR("internal_error", 500);

        private final String description;
        private final int code;

        private StatusCode(String description, int code) {
            this.description = description;
            this.code = code;
        }

        public String description() {
            return description;
        }

        public int code() {
            return code;
        }
    }

    private boolean validateToken(String token) {
        Boolean validated = false;

        Connection conn = RestServiceApplication.getConnection();

        try {
            Statement stmt = conn.createStatement();
            int affectedRows = stmt.executeUpdate("delete from tokens where timeout<current_timestamp");
            conn.commit();

            PreparedStatement ps = conn.prepareStatement("select username from tokens where token = ?");
            ps.setString(1, token);
            ResultSet rows = ps.executeQuery();

            if (!rows.next())
                validated=false;                 
            else
                validated=true;
        } 
        catch (SQLException ex) {
            logger.error("Error in DB", ex);
            validated=false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
                validated=false;
            }
        }

        return validated;
    }

    private boolean validateTokenWithUsername(String token, String username) {
        Boolean validated = false;

        Connection conn = RestServiceApplication.getConnection();

        try {
            Statement stmt = conn.createStatement();
            int affectedRows = stmt.executeUpdate("delete from tokens where timeout<current_timestamp");
            conn.commit();

            PreparedStatement ps = conn.prepareStatement("select username from tokens where token = ?");
            ps.setString(1, token);
            ResultSet rows = ps.executeQuery();

            if (rows.next()) {
            String dbUsername = rows.getString("username");
            if (dbUsername.equals(username) || dbUsername.equals("admin")) {
                validated = true;
            }
        } else {
            validated = false;
        }
        }
        catch (SQLException ex) {
            logger.error("Error in DB", ex);
            validated=false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
                validated=false;
            }
        }

        return validated;
    }

        private boolean validateAdmin(String token) {
        Boolean validated = false;

        Connection conn = RestServiceApplication.getConnection();

        try {
            Statement stmt = conn.createStatement();
            int affectedRows = stmt.executeUpdate("delete from tokens where timeout<current_timestamp");
            conn.commit();

            PreparedStatement ps = conn.prepareStatement("select username from tokens where token = ?");
            ps.setString(1, token);
            ResultSet rows = ps.executeQuery();

            if (rows.next()) {
            String dbUsername = rows.getString("username");
            if (dbUsername.equals("admin")) {
                validated = true;
            }
        } else {
            validated = false;
        }
        }
        catch (SQLException ex) {
            logger.error("Error in DB", ex);
            validated=false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
                validated=false;
            }
        }

        return validated;
    }


    private Map<String, Object> invalidToken() {
        Map<String, Object> returnData = new HashMap<String, Object>();

        returnData.put("status", 400);
        returnData.put("message", "a valid token is missing");

        return returnData;
    }

private List<Map<String, Object>> invalidTokenList() {
    Map<String, Object> returnData = new HashMap<>();
    List<Map<String, Object>> returnList = new ArrayList<>();
    returnData.put("status", 400);
    returnData.put("message", "a valid token is missing");
    returnList.add(returnData);
    return returnList;
}


    

    private static final Logger logger = LoggerFactory.getLogger(DemoProj.class);

    @GetMapping("/")
    public String landing() {
        return "Hello World (Java)!  <br/>\n"
                + "<br/>\n"
                + "Check the sources for instructions on how to use the endpoints!<br/>\n"
                + "<br/>\n"
                + "ITCS 3160-002, Spring 2024<br/>\n"
                + "<br/>";
    }

    // Login user using simple tokens
    // curl -X PUT http://localhost:8080/login -H "Content-Type: application/json" -d '{"username": "ssmith", "password": "ssmith_pass"}'

    @PutMapping("/login")
    public Map<String, Object> loginUser(@RequestBody Map<String, Object> payload) {
        Map<String, Object> returnData = new HashMap<String, Object>();
        
        if (!payload.containsKey("username") || !payload.containsKey("password"))
        {
            logger.warn("missing credentials");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "missing credentials");
            return returnData;    
        }
        
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");

        Connection conn = RestServiceApplication.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement("select 1 from users where username = ? and password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rows = stmt.executeQuery();

            if (!rows.next()) {
                logger.warn("invalid credentials");
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "invalid credentials");
                return returnData;                 
            }
            else {
                int rnd=(int) (Math.random()*999999999+111111111);
                String token = username + rnd;

                PreparedStatement stmt1 = conn.prepareStatement("insert into tokens values( ?, ? , current_timestamp + (60 * interval '1 min'))");
                stmt1.setString(1, username);
                stmt1.setString(2, token);
                int affectedRows = stmt1.executeUpdate();

                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("token", token);
            }

            conn.commit();
        } 
        catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }

        return returnData;
    }


    /**
     * Demo GET
     *
     * Obtain all users, in JSON format
     *
     * To use it, access: <br>
     * http://localhost:8080/users/
     *
     * @return
     */
    @GetMapping(value = "/users/", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getAllUsers(@RequestHeader("x-access-tokens") String token) {
        if(!validateAdmin(token))
            return invalidToken();

        logger.info("###              DEMO: GET /users              ###");
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> returnData = new HashMap<String, Object>();
        List<Map<String, Object>> results = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT username, name, phone, address, banned FROM users");
            logger.debug("---- users  ----");
            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();
                logger.debug("'username': {}, 'name': {}, 'phone': {}, 'address': {}, 'banned': {},",
                        rows.getString("username"), rows.getString("name"), rows.getLong("phone"),
                        rows.getString("address"), rows.getBoolean("banned")
                );
                content.put("username", rows.getString("username"));
                content.put("name", rows.getString("name"));
                content.put("phone", rows.getLong("phone"));
                content.put("address", rows.getString("address"));
                content.put("banned", rows.getBoolean("banned"));
                results.add(content);
            }

            returnData.put("status", StatusCode.SUCCESS.code());
            returnData.put("results", results);

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }
        return returnData;
    }

    /**
     * Demo GET
     *
     * Obtain user with {@code username}
     *
     * To use it, access: <br>
     * http://localhost:8080/users/ssmith
     *
     * @param username id of the user to be selected
     * @return data of the user
     */
    @GetMapping(value = "/users/{username}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getuser(
        @RequestHeader("x-access-tokens") String token,
        @PathVariable("username") String username
    ) {
        if(!validateTokenWithUsername(token, username))
            return invalidToken();

        logger.info("###              DEMO: GET /users/{username}              ###");
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> returnData = new HashMap<String, Object>();
        Map<String, Object> content = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement("SELECT username, name, phone, address, banned FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rows = ps.executeQuery();
            logger.debug("---- selected user  ----");
            if (rows.next()) {
                logger.debug("'username': {}, 'name': {}, 'phone': {}, 'address': {}, 'banned': {}",
                        rows.getString("username"), rows.getString("name"), rows.getLong("phone"),
                        rows.getString("address"), rows.getBoolean("banned")
                );
                content.put("username", rows.getString("username"));
                content.put("name", rows.getString("name"));
                content.put("phone", rows.getLong("phone"));
                content.put("address", rows.getString("address"));
                content.put("banned", rows.getBoolean("banned"));
            }

            if (content.isEmpty()) {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "User not found");
            } else {
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("results", content);
            }

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }
        return returnData;
    }

    /**
     * Demo POST
     *
     * Add a new user in a JSON payload
     *
     * To use it, you need to use postman or curl:
     *
     * {@code curl -X POST http://localhost:8080/users/ -H "Content-Type: application/json" -d
     * '{"username": "ppopov", "name": "Peter Popov", "phone": 7049609660, "address": "1212 12th street", "buyer": true, "seller": false}'
     *
     * @param payload JSON payload containing user information
     * @return status and results of the operation
     */
    @PostMapping(value = "/users/", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> createuser(
            @RequestBody Map<String, Object> payload
    ) {

        logger.info("###              DEMO: POST /users              ###");
        Connection conn = RestServiceApplication.getConnection();

        logger.debug("---- new user  ----");
        logger.debug("payload: {}", payload);

        Map<String, Object> returnData = new HashMap<String, Object>();

        // validate all the required inputs and types, e.g.,
        if (!payload.containsKey("username")) {
            logger.warn("username is required to create a user");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "username is required to create a user");
            return returnData;
        }
        if (!payload.containsKey("name")) {
            logger.warn("name is required to create a user");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "name is required to create a user");
            return returnData;
        }
        if (!payload.containsKey("phone")) {
            logger.warn("phone is required to create a user");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "phone is required to create a user");
            return returnData;
        }
        if (!payload.containsKey("address")) {
            logger.warn("address is required to create a user");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "address is required to create a user");
            return returnData;
        }
        if (!payload.containsKey("banned")) {
            logger.warn("Banned is required to create a user");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "banned is required to create a user");
            return returnData;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password, name, phone, address, banned) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, (String) payload.get("username"));
            ps.setString(2, (String) payload.get("password"));
            ps.setString(3, (String) payload.get("name"));
            ps.setLong(4, (Long) payload.get("phone"));
            ps.setString(5, (String) payload.get("address"));
            ps.setBoolean(6, (Boolean) payload.get("banned"));

            int affectedRows = ps.executeUpdate();
            conn.commit();

            returnData.put("status", StatusCode.SUCCESS.code());
            returnData.put("results", "user inserted successfully");

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }

            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return returnData;
    }

    @PostMapping(value = "/auction", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> createAuction(@RequestHeader("x-access-tokens") String token, @RequestBody Map<String, Object> payload) 
    {

        if(!validateTokenWithUsername(token, (String) payload.get("seller")))
            return invalidToken();

        logger.info("###              DEMO: POST /auction              ###");
        Connection conn = RestServiceApplication.getConnection();

            try {
                if (userBanned(conn, (String) payload.get("seller"))) {
                    return userHasBeenBanned();
                }
            } catch (SQLException e) {
                logger.error("Error in DB", e);
            }


        logger.debug("---- new auction  ----");
        logger.debug("payload: {}", payload);

        Map<String, Object> returnData = new HashMap<>();

        // validate all the required inputs and types
        if (!payload.containsKey("minimumPrice") || !payload.containsKey("condition") || 
                !payload.containsKey("description") || !payload.containsKey("seller") ||
                !payload.containsKey("title") || !payload.containsKey("start_time") || 
                !payload.containsKey("end_time") || !payload.containsKey("status") ) {
            logger.warn("Required fields are missing to create an auction");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "Required fields are missing to create an auction");
            return returnData;
        }

        try 
        {
            conn.setAutoCommit(false);
            int item_id;
            // Insert item for auction
            try (PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO item (description, condition,  min_price, users_username) VALUES (?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) 
            {
                psItem.setString(1, (String) payload.get("description"));
                psItem.setString(2, (String) payload.get("condition"));
                psItem.setDouble(3, (Double) payload.get("minimumPrice"));
                psItem.setString(4, (String) payload.get("seller")); // Assuming seller info is provided in payload

                int affectedRowsItem = psItem.executeUpdate();
                
                if (affectedRowsItem == 0) 
                {
                    throw new SQLException("Creating item failed, no rows were affected");
                }
                try (ResultSet generatedKeys = psItem.getGeneratedKeys()) 
                {
                if (generatedKeys.next()) {
                    item_id = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating item failed, no ID obtained.");
                }
                }

                        // Insert auction
                        try (PreparedStatement psAuction = conn.prepareStatement(
                                "INSERT INTO auction (title, start_time, end_time, status, item_id) VALUES (?, ?, ?, ?, ?)",
                                PreparedStatement.RETURN_GENERATED_KEYS)) 
                        {
                            psAuction.setString(1, (String) payload.get("title"));
                            psAuction.setTimestamp(2, Timestamp.valueOf((String) payload.get("start_time")));
                            psAuction.setTimestamp(3, Timestamp.valueOf((String) payload.get("end_time")));
                            psAuction.setBoolean(4, (Boolean) payload.get("status")); 
                            psAuction.setInt(5, item_id);

                            int affectedRowsAuction = psAuction.executeUpdate();
                            if (affectedRowsAuction == 0) 
                            {
                                throw new SQLException("Creating Auction failed, no rows were affected");
                            }
                            
                        }
            } 
              conn.commit();
              returnData.put("status", StatusCode.SUCCESS.code());  
        }
        catch (SQLException ex) 
        {
            logger.error("Error in DB", ex);
            try 
            {
                conn.rollback();
            } 
            catch (SQLException ex1) 
            {
                logger.warn("Couldn't rollback", ex1);
            }

            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        } 
        finally 
        {
            try 
            {
                conn.close();
            } 
            catch (SQLException ex) 
            {
                logger.error("Error in DB", ex);
            }
        }
    return returnData;
    } 

    @GetMapping(value = "/auctions/", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getAllAuctions(@RequestHeader("x-access-tokens") String token) {
        if(!validateToken(token))
            return invalidToken();
        logger.info("###              DEMO: GET /auctions              ###");
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> returnData = new HashMap<String, Object>();
        List<Map<String, Object>> results = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT id, title, start_time, end_time, status, highest_bid, item_id FROM auction");
            logger.debug("---- auctions ----");

            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();
                logger.debug("'id': {}, 'title': {}, 'start_time': {}, 'end_time': {}, 'status': {}, 'highest_bid': {}, 'item_id': {}", 
                rows.getInt("id"), rows.getString("title"), rows.getTimestamp("start_time"), rows.getTimestamp("end_time"), 
                rows.getBoolean("status"),rows.getDouble("highest_bid"), rows.getInt("item_id"));

                content.put("id", rows.getInt("id"));
                content.put("title", rows.getString("title"));
                content.put("start_time", rows.getTimestamp("start_time"));
                content.put("end_time", rows.getTimestamp("end_time"));
                content.put("status", rows.getBoolean("status"));
                content.put("highest_bid", rows.getDouble("highest_bid"));
                content.put("item_id", rows.getInt("item_id"));
                results.add(content);

            }

            returnData.put("status", StatusCode.SUCCESS.code());
            returnData.put("results", results);

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());

        }
        return returnData;

    }
    /**
 * Search existing auction by ID code or item description.
 * {@code curl -X GET "http://localhost:8080/auction/search?q=your-search-query"}
 * @param searchQuery The EAN/ISBN code or item description to search for.
 * @return List of auction identifiers and descriptions that match the search criteria.
 */
@GetMapping(value = "/auction/search", produces = "application/json")
@ResponseBody
public Map<String, Object> searchAuction(@RequestHeader("x-access-tokens") String token, @RequestParam("q") String searchQuery) {
    if(!validateToken(token))
        return invalidToken();
    logger.info("### DEMO: Searching Auction for '{}' ###", searchQuery);
    Connection conn = RestServiceApplication.getConnection();
    Map<String, Object> returnData = new HashMap<>();

    try (PreparedStatement ps = conn.prepareStatement(
            "SELECT a.id, a.title FROM auction a " +
            "JOIN item i ON a.item_id = i.id " +
            "WHERE i.id = ? OR i.description LIKE ?")) {
        try {
            int id = Integer.parseInt(searchQuery); 
            ps.setInt(1, id);
        } catch (NumberFormatException e) {
            ps.setInt(1, 0); 
        }
        ps.setString(2, "%" + searchQuery + "%");

        try (ResultSet rows = ps.executeQuery()) {
            List<Map<String, Object>> results = new ArrayList<>();

            while (rows.next()) {
                Map<String, Object> auctionInfo = new HashMap<>();
                auctionInfo.put("auction_id", rows.getInt("id"));
                auctionInfo.put("auction_title", rows.getString("title"));
                results.add(auctionInfo);
            }

            returnData.put("status", StatusCode.SUCCESS.code());
            returnData.put("results", results);
        }
    } catch (SQLException ex) {
        logger.error("Error in DB", ex);
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", ex.getMessage());
    } finally {
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.error("Error closing connection", ex);
        }
    }
    return returnData;
}
@GetMapping("/auction/{auctionId}/details")
    public Map<String, Object> getAuctionDetails(@RequestHeader("x-access-tokens") String token, @PathVariable("auctionId") int auctionId) {
        if(!validateToken(token))
            return invalidToken();
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> returnData = new HashMap<>();

        try {

            // Retrieve auction details
            Map<String, Object> auctionDetails = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.title, a.start_time, a.end_time, a.status, i.description, i.condition FROM auction a " +
                            "JOIN item i ON a.item_id = i.id WHERE a.id = ?")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        auctionDetails.put("title", rs.getString("title"));
                        auctionDetails.put("start_time", rs.getTimestamp("start_time"));
                        auctionDetails.put("end_time", rs.getTimestamp("end_time"));
                        auctionDetails.put("status", rs.getBoolean("status"));
                        auctionDetails.put("description", rs.getString("description"));
                        auctionDetails.put("condition", rs.getString("condition"));
                    } else {
                        returnData.put("status", "error");
                        returnData.put("message", "Auction with ID " + auctionId + " not found.");
                        return returnData;
                    }
                }
            }

            // Retrieve exchanged messages
            List<Map<String, Object>> messages = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM message WHERE auction_id = ?")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("sender", rs.getString("users_username"));
                        message.put("comment", rs.getString("comment"));
                        message.put("post_time", rs.getTimestamp("post_time"));
                        messages.add(message);
                    }
                }
            }

            // Retrieve bidding history
            List<Map<String, Object>> biddingHistory = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM bid WHERE auction_id = ? ORDER BY bid_time")) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> bid = new HashMap<>();
                        bid.put("bidder", rs.getString("users_username"));
                        bid.put("amount", rs.getDouble("amount"));
                        bid.put("bid_time", rs.getTimestamp("bid_time"));
                        biddingHistory.add(bid);
                    }
                }
            }

            returnData.put("status", "success");
            returnData.put("auction_details", auctionDetails);
            returnData.put("messages", messages);
            returnData.put("bidding_history", biddingHistory);
        } catch (SQLException ex) {
            ex.printStackTrace();
            returnData.put("status", "error");
            returnData.put("message", "Error retrieving auction details: " + ex.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return returnData;
    }

@GetMapping(value = "/user/{username}/auctions/", produces = "application/json")
@ResponseBody
public Map<String, Object> getUserAuctions(@RequestHeader("x-access-tokens") String token, @PathVariable("username") String username) {
    if(!validateTokenWithUsername(token, username))
        return invalidToken();
    logger.info("###              GET /user/{username}/auctions/              ###");
    Connection conn = RestServiceApplication.getConnection();
    Map<String, Object> returnData = new HashMap<>();
    List<Map<String, Object>> results = new ArrayList<>();

    try (PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT a.id, a.title, a.start_time, a.end_time, a.status, " + "CASE WHEN i.users_username = ? THEN TRUE ELSE FALSE END AS is_creator, " + "CASE WHEN b.users_username = ? THEN TRUE ELSE FALSE END AS placed_bid " + "FROM auction a " + "LEFT JOIN bid b ON a.id = b.auction_id " + "LEFT JOIN item i ON a.item_id = i.id " + "WHERE i.users_username = ? OR b.users_username = ?")) {
        ps.setString(1, username);
        ps.setString(2, username);
        ps.setString(3, username);
        ps.setString(4, username);
        ResultSet rows = ps.executeQuery();
        logger.debug("---- auctions with user activity ----");
        
        while (rows.next()) {
            Map<String, Object> content = new HashMap<>();
            logger.debug("'id': {}, 'title': {}, 'start_time': {}, 'end_time': {}, 'status': {}, 'is_creator': {}, 'placed_bid': {}", rows.getInt("id"), rows.getString("title"), rows.getTimestamp("start_time"), rows.getTimestamp("end_time"), rows.getBoolean("status"), rows.getBoolean("is_creator"), rows.getBoolean("placed_bid"));
            
            content.put("id", rows.getInt("id"));
            content.put("title", rows.getString("title"));
            content.put("start_time", rows.getTimestamp("start_time"));
            content.put("end_time", rows.getTimestamp("end_time"));
            content.put("status", rows.getBoolean("status"));
            content.put("is_creator", rows.getBoolean("is_creator"));
            content.put("placed_bid", rows.getBoolean("placed_bid"));
            results.add(content);
            
        }

        returnData.put("status", StatusCode.SUCCESS.code());
        returnData.put("results", results);

    } catch (SQLException ex) {
        logger.error("Error in DB", ex);
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", ex.getMessage());
        
    } finally {  
        try {
            conn.close();
            
        } catch (SQLException ex) {
            logger.error("Error closing connection", ex);
            
        }
        
    }    
    return returnData;
    
}
 /**
 * Endpoint to create a new bid for an auction.
 * Expects a JSON payload with "amount" and "username".
 * @code curl -X POST http://localhost:8080/bids \
-H "Content-Type: application/json" \
-d '{
  "amount": 240.0,
  "username": "peter_parker",
  "auction_id": 1
}'

 *
 * @param payload JSON payload containing "amount" and "username"
 * @return Map with status and message indicating success or error
 */
@PostMapping(value = "/bids", consumes = "application/json")
@ResponseBody
public Map<String, Object> createBid(@RequestHeader("x-access-tokens") String token, @RequestBody Map<String, Object> payload) {
    if(!validateTokenWithUsername(token, (String) payload.get("username")))
        return invalidToken();
    logger.info("###              DEMO: POST /bids              ###");
    Connection conn = RestServiceApplication.getConnection();


            try {
                if (userBanned(conn, (String) payload.get("username"))) {
                    return userHasBeenBanned();
                }
            } catch (SQLException e) {
                logger.error("Error in DB", e);
            }


    logger.debug("---- new bid  ----");
    logger.debug("Payload: {}", payload);

    Map<String, Object> returnData = new HashMap<>();

    try {
        // validate all the required inputs and types, e.g.,
        if (!payload.containsKey("amount")) {
            logger.warn("Amount is required to create a bid");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "Amount is required to create a bid");
            return returnData;
        }
        if (!payload.containsKey("username")) {
            logger.warn("Username is required to create a bid");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "Username is required to create a bid");
            return returnData;
        }
        if (!payload.containsKey("valid")) {
            logger.warn("valid is required to create a bid");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "valid is required to create a bid");
            return returnData;
        }

        double amount = (double) payload.get("amount");
        String username = (String) payload.get("username");
        int auctionID = (int) payload.get("auction_id");
        boolean valid = (boolean) payload.get("valid");

        // Check if the auction status is true
    try (PreparedStatement checkAuctionStatusStmt = conn.prepareStatement(
        "SELECT status FROM auction WHERE id = ?")) {
    checkAuctionStatusStmt.setInt(1, auctionID);
    ResultSet auctionStatusResult = checkAuctionStatusStmt.executeQuery();
    if (auctionStatusResult.next()) {
        boolean auctionStatus = auctionStatusResult.getBoolean("status");
        if (!auctionStatus) {
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "Cannot place bid on inactive auction");
            return returnData;
        }
    } else {
        returnData.put("status", StatusCode.API_ERROR.code());
        returnData.put("errors", "Auction not found");
        return returnData;
    }
}

// Check if the new bid amount is higher than all previous bids
try (PreparedStatement checkMaxBidStmt = conn.prepareStatement(
        "SELECT MAX(amount) AS max_amount FROM bid WHERE auction_id = ?")) {
    checkMaxBidStmt.setInt(1, auctionID);
    ResultSet maxBidResult = checkMaxBidStmt.executeQuery();
    if (maxBidResult.next()) {
        double maxAmount = maxBidResult.getDouble("max_amount");
        if (amount <= maxAmount) {
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "The amount for the bid must be higher than the current highest bid amount");
            return returnData;
        }
    }
}

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO bid (amount, bid_time, auction_id, users_username, valid) VALUES (?, NOW(), ?, ?, ?)")) {
            ps.setDouble(1, amount);
            ps.setInt(2, auctionID);
            ps.setString(3, username);
            ps.setBoolean(4, valid);

            int affectedRows = ps.executeUpdate();
            conn.commit();

            if (affectedRows > 0) {
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("results", "Bid created successfully");

                outBidNoti(conn, auctionID, amount, username);

            } else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "Failed to create bid");
            }

        } catch (SQLException ex) {
            logger.error("SQL Error:", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex1);
            }

            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", "Internal Server Error");

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error closing connection", ex);
            }
        }
    } catch (Exception e) {
        logger.error("Unexpected Error:", e);
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", "Internal Server Error");
    }

    return returnData;
}
/**
 * Endpoint to edit an existing auction description.
 * Expects a JSON payload.
 *
 * @code curl -X PUT \
  http://localhost:8080/auction/2/edit \
  -H 'Content-Type: application/json' \
  -d '{
        "title": "New Title"
}'
 * @param auctionId ID of the auction to be edited
 * @param payload JSON payload containing the new title
 * @return Map with status and message indicating success or error
 */
@PutMapping(value = "/auction/{auctionId}/edit", consumes = "application/json")
@ResponseBody
public Map<String, Object> editAuction(@RequestHeader("x-access-tokens") String token, @PathVariable("auctionId") int auctionId, @RequestBody Map<String, Object> payload) {
    
    logger.info("###              DEMO: PUT /auction/{auctionId}/edit              ###");
    Connection conn = RestServiceApplication.getConnection();

    logger.debug("---- edit auction {} ----", auctionId);
    logger.debug("payload: {}", payload);
    List<Map<String, Object>> results = new ArrayList<>();

    Map<String, Object> returnData = new HashMap<>();

// validate all the required inputs and types
    if (!payload.containsKey("title")) {
        logger.warn("title field is missing to edit the auction");
        returnData.put("status", StatusCode.API_ERROR.code());
        returnData.put("errors", "title field is missing to edit the auction");
        return returnData;
    }

    try {
        conn.setAutoCommit(false);

        // Retrieve current auction details
        Map<String, Object> currentAuction = new HashMap<>();
        String sellerUsername = null;

        try (PreparedStatement psCurrentAuction = conn.prepareStatement(
                "SELECT * FROM auction WHERE id =?")) {
            psCurrentAuction.setInt(1, auctionId);
            try (ResultSet rs = psCurrentAuction.executeQuery()) {
                if (rs.next()) {
                    currentAuction.put("title", rs.getString("title"));
                    currentAuction.put("start_time", rs.getTimestamp("start_time"));
                    currentAuction.put("end_time", rs.getTimestamp("end_time"));
                    currentAuction.put("status", rs.getBoolean("status"));
                    currentAuction.put("item_id", rs.getInt("item_id"));

                    // Get seller information based on item ID
                    try (PreparedStatement getSellerStmt = conn.prepareStatement(
                            "SELECT users_username FROM item WHERE id = ?")) {
                        getSellerStmt.setInt(1, (int) currentAuction.get("item_id"));
                        ResultSet sellerResult = getSellerStmt.executeQuery();
                        if (sellerResult.next()) {
                            sellerUsername = sellerResult.getString("users_username");
                        } else {
                            returnData.put("status", StatusCode.API_ERROR.code());
                            returnData.put("errors", "Seller not found for the auction");
                            return returnData;
                        }
                    }

                } else {
                    returnData.put("status", StatusCode.INTERNAL_ERROR.code());
                    returnData.put("errors", "Auction not found");
                    return returnData;
                }
            }
        }
                if(!validateTokenWithUsername(token, sellerUsername))
                    return invalidToken();


        // Insert new auction with the same details as the current auction
        int newAuctionId;
        try (PreparedStatement psInsertNewAuction = conn.prepareStatement(
                "INSERT INTO auction (title, start_time, end_time, status, item_id) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            psInsertNewAuction.setString(1, (String) payload.get("title"));
            psInsertNewAuction.setTimestamp(2, (Timestamp) currentAuction.get("start_time"));
            psInsertNewAuction.setTimestamp(3, (Timestamp) currentAuction.get("end_time"));
            psInsertNewAuction.setBoolean(4, (Boolean) currentAuction.get("status"));
            psInsertNewAuction.setInt(5, (int) currentAuction.get("item_id"));

            int affectedRows = psInsertNewAuction.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating new auction failed, no rows were affected");
            }

            try (ResultSet generatedKeys = psInsertNewAuction.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newAuctionId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating new auction failed, no ID obtained.");
                }
            }
        }

        // Copy the old auction exactly, changing only the auction ID
        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT title, start_time, end_time, status, item_id FROM auction");
            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();
                content.put("title", rows.getString("title"));
                content.put("start_time", rows.getTimestamp("start_time"));
                content.put("end_time", rows.getTimestamp("end_time"));
                content.put("status", rows.getBoolean("status"));
                content.put("item_id", rows.getInt("item_id"));
                results.add(content);
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex1);
            }
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }

        try (PreparedStatement psInsertNewAuction = conn.prepareStatement(
        "INSERT INTO auction (title, start_time, end_time, status, item_id) VALUES (?, ?, ?, ?, ?)")){
            
            Map<String, Object> auctionData = results.get(0); // Assuming you only need the first row
            psInsertNewAuction.setString(1, (String) auctionData.get("title"));
            psInsertNewAuction.setTimestamp(2, (Timestamp) auctionData.get("start_time"));
            psInsertNewAuction.setTimestamp(3, (Timestamp) auctionData.get("end_time"));
            psInsertNewAuction.setBoolean(4, (Boolean) auctionData.get("status"));
            psInsertNewAuction.setInt(5, (int) auctionData.get("item_id"));
        }
        catch (SQLException ex) {
        logger.error("Error in DB", ex);
        try {
            conn.rollback();
        } catch (SQLException ex1) {
            logger.warn("Couldn't rollback", ex1);
        }
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", ex.getMessage());
    }



        // Save previous version of the description
        try (PreparedStatement psUpdateDesc = conn.prepareStatement(
                "UPDATE auction SET title = ? WHERE id = ?")) {
            psUpdateDesc.setString(1, "(edited auction, new auction id: " + newAuctionId + ") " + (String) currentAuction.get("title"));
            psUpdateDesc.setInt(2, auctionId);
            int affectedRows = psUpdateDesc.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating auction title failed, no rows were affected");
            }
            conn.commit();
            returnData.put("status", StatusCode.SUCCESS.code());
        }

    } catch (SQLException ex) {
        logger.error("Error in DB", ex);
        try {
            conn.rollback();
        } catch (SQLException ex1) {
            logger.warn("Couldn't rollback", ex1);
        }
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", ex.getMessage());
    } finally {
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
        }
    }
    return returnData;
}
/**
     * Demo POST
     *
     * Add a new message to the auction board
     *
     * curl -X POST \
  http://localhost:8080/auctions/1/messages/ \
  -H 'Content-Type: application/json' \
  -d '{
    "auction_id": 1, 
    "comment": "Welcome to the message system for auction 1", 
    "users_username": "john_doe"
}'
     *
     * @param auction_id the id of the auction to which the message belongs
     * @param payload JSON payload containing the message information
     * @return status and results of the operation
     */
    @PostMapping(value = "/auctions/{auction_id}/messages/", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> createMessage(
            @RequestHeader("x-access-tokens") String token,
            @PathVariable("auction_id") int auction_id,
            @RequestBody Map<String, Object> payload
    ) {
        if(!validateTokenWithUsername(token, (String) payload.get("users_username")))
            return invalidToken();

        logger.info("### DEMO: POST /auctions/{}/messages ###", auction_id);
        Connection conn = RestServiceApplication.getConnection();

        Map<String, Object> returnData = new HashMap<>();

        try {
            String comment = (String) payload.get("comment");
            String users_username = (String) payload.get("users_username");

            if (comment == null || comment.isEmpty()) {
                throw new IllegalArgumentException("Comment is required to create a message");
            }
            if (users_username == null || users_username.isEmpty()) {
                throw new IllegalArgumentException("User username is required to create a message");
            }

            // Insert message into the database
            String sql = "INSERT INTO message (comment, post_time, users_username, auction_id) VALUES (?, NOW(), ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, comment);
                ps.setString(2, users_username);
                ps.setInt(3, auction_id);

                int affectedRows = ps.executeUpdate();
                conn.commit();

                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("results", "Message inserted successfully");
            }
        } catch (SQLException ex) {
            logger.error("Error executing SQL query", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", "Internal server error occurred");
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback transaction", ex1);
            }
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid request payload", ex);
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error closing database connection", ex);
            }
        }

        return returnData;
    }
    //Notification & INBOX (ANDY)

    public static void notifyUser(String recipientUsername, String message) {
        Connection conn = RestServiceApplication.getConnection();
        try {
            String sql = "INSERT INTO notification (message, noti_time, recipient_username) VALUES (?, NOW(), ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, message);
                ps.setString(2, recipientUsername);
                
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating Notification failed, no rows were affected");
                }

                conn.commit();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    @GetMapping("/user/{recipient_username}/inbox")
    public List<Map<String, Object>> getInbox(@RequestHeader("x-access-tokens") String token, @PathVariable("recipient_username") String recipient_username) {
        if(!validateTokenWithUsername(token, recipient_username))
            return invalidTokenList();
        Connection conn = RestServiceApplication.getConnection();
        List<Map<String, Object>> inbox = new ArrayList<>();
        try {
            String sql = "SELECT message, noti_time, recipient_username FROM notification WHERE recipient_username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, recipient_username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("message", rs.getString("message"));
                        message.put("noti_time", rs.getTimestamp("noti_time"));
                        message.put("recipient_username", rs.getString("recipient_username"));
                        inbox.add(message);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error("Error fetching inbox messages for user '{}': {}", recipient_username, ex.getMessage());
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (inbox.isEmpty()) {
            logger.info("No messages found in the inbox for user '{}'", recipient_username);
        } else {
            logger.info("Fetched {} messages for user '{}'", inbox.size(), recipient_username);
        }

        return inbox;
    }
    //outBidNoti (ANDY)
    private static void outBidNoti(Connection conn, int auctionID, double newBid, String username) {
        try {
            conn.setAutoCommit(false);
    
            double currentHigh = getCurrentHigh(conn, auctionID);
            System.out.println("Current highest bid: " + currentHigh);

            String previousBidder = getPreviousHighBidder(conn, auctionID);
            System.out.println("Previous highest bidder: " + previousBidder);
    
            if (newBid > currentHigh) {
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE auction SET highest_bid = ? WHERE id = ?")) {
                    update.setDouble(1, newBid);
                    update.setInt(2, auctionID);
                    update.executeUpdate();
                    conn.commit();
    
                    
                    if (!previousBidder.equals(username)) {

                        notifyUser(previousBidder, "You have been outbid on the auction with ID " + auctionID);
                        
                    }
                }

            }
        } catch (SQLException ex) {
            logger.error("SQL Error:", ex);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback transaction", ex1);
            }
        }
    }
    
    
    private static double getCurrentHigh(Connection conn, int auctionID) throws SQLException {
        double currentHighestBid = 0;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT highest_bid FROM auction WHERE id = ?")) {
            stmt.setInt(1, auctionID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    currentHighestBid = rs.getDouble("highest_bid");
                }
            }
        }
        return currentHighestBid;
    }
    
    private static String getPreviousHighBidder(Connection conn, int auctionID) throws SQLException {
        String previousBidder = "null";

        try (PreparedStatement stmt = conn.prepareStatement("SELECT users_username FROM bid WHERE auction_id = ? ORDER BY amount DESC LIMIT 1 OFFSET 1")) {
            stmt.setInt(1, auctionID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    previousBidder = rs.getString("users_username");
                }
            }
        }
        return previousBidder;
    }
      @PostMapping("/auction/{auctionId}/close")
    public Map<String, Object> closeAuction(@RequestHeader("x-access-tokens") String token, @PathVariable("auctionId") int auctionId) 
    {
        
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> returnData = new HashMap<>();

        try 
        {
            conn.setAutoCommit(false);

            // Check if the specified date, hour, and minute have passed
            boolean isTimeExpired = checkIfAuctionEnded(conn, auctionId);
            String sellerUsername = null;

            if (isTimeExpired) {
                // Retrieve the winning bid details
                double highestBidAmount = 0;
                String winnerUsername = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT amount, users_username FROM bid WHERE auction_id = ? ORDER BY amount DESC LIMIT 1")) {
                    ps.setInt(1, auctionId);
                    try (ResultSet rs = ps.executeQuery()) 
                    {
                        if (rs.next()) 
                        {
                            highestBidAmount = rs.getDouble("amount");
                            winnerUsername = rs.getString("users_username");

                            // Get seller information based on item ID
                    try (PreparedStatement getSellerStmt = conn.prepareStatement(
                            "SELECT users_username FROM item WHERE id = ?")) {
                        getSellerStmt.setInt(1, auctionId);
                        ResultSet sellerResult = getSellerStmt.executeQuery();
                        if (sellerResult.next()) {
                            sellerUsername = sellerResult.getString("users_username");
                        } else {
                            returnData.put("status", StatusCode.API_ERROR.code());
                            returnData.put("errors", "Seller not found for the auction");
                            return returnData;
                        }
                    }

                        }
                    }
                }

                if(validateTokenWithUsername(token, sellerUsername))
                    return invalidToken();

                // Update the auction with winning bid details
                try (PreparedStatement psUpdateItem = conn.prepareStatement(
                        "UPDATE auction SET highest_bid = ? WHERE id = (SELECT item_id FROM auction WHERE id = ?)")) 
                {
                    psUpdateItem.setDouble(1, highestBidAmount);
                    psUpdateItem.setInt(2, auctionId);
                    psUpdateItem.executeUpdate();
                }

                // Update the auction status to closed
                try (PreparedStatement psUpdateAuction = conn.prepareStatement(
                        "UPDATE auction SET status = false WHERE id = ?")) 
                {
                    psUpdateAuction.setInt(1, auctionId);
                    psUpdateAuction.executeUpdate();
                }

                conn.commit(); // Commit the transaction

                returnData.put("status", "success");
                returnData.put("message", "Auction closed successfully.");
            }
             else 
            {
                returnData.put("status", "error");
                returnData.put("message", "Cannot close auction. End time not reached yet.");
            }
        } 
        catch (SQLException ex) 
        {
            if (conn != null) 
            {
                try 
                {
                    conn.rollback();
                } catch (SQLException ex1) 
                {
                    ex1.printStackTrace();
                }
            }
            returnData.put("status", "error");
            returnData.put("message", "Error closing auction: " + ex.getMessage());
        } 
        finally 
        {
            if (conn != null) {
                try 
                {
                    conn.close();
                } catch (SQLException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }

        return returnData;
    }
    private boolean checkIfAuctionEnded(Connection conn, int auctionId) throws SQLException
    {
        Timestamp endTime = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT end_time FROM auction WHERE id = ?")) 
        {
            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) 
            {
                if (rs.next()) 
                {
                    endTime = rs.getTimestamp("end_time");
                } else 
                {
                    throw new SQLException("Auction with ID " + auctionId + " not found.");
                }
            }
        }
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        return currentTime.after(endTime);
    }
    @PutMapping("/auction/{auctionId}/cancel")
    public Map<String, Object> cancelAuction(@RequestHeader("x-access-tokens") String token, @PathVariable("auctionId") int auctionId) {
                if(validateTokenWithUsername(token, "admin"))
            return invalidToken();
        Connection conn = RestServiceApplication.getConnection();
        Map<String, Object> returnData = new HashMap<>();


        try {

            conn.setAutoCommit(false);

            // Update the auction status to closed
            try (PreparedStatement psUpdateAuction = conn.prepareStatement(
                    "UPDATE auction SET status = false WHERE id = ?")) {
                psUpdateAuction.setInt(1, auctionId);
                psUpdateAuction.executeUpdate();
            }
          try (PreparedStatement psNotifyUsers = conn.prepareStatement(
                    "INSERT INTO notification (message, recipient_username, noti_time) " +
                            "SELECT 'Auction with ID " + auctionId + " has been canceled.', users_username, CURRENT_TIMESTAMP " +
                            "FROM bid WHERE auction_id = ?")) {
                psNotifyUsers.setInt(1, auctionId);
                psNotifyUsers.executeUpdate();
            }
            conn.commit(); // Commit the transaction

            returnData.put("status", "success");
            returnData.put("message", "Auction canceled successfully.");
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                }
            }
            returnData.put("status", "error");
            returnData.put("message", "Error canceling auction: " + ex.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return returnData;
    } 
    /**
 * Get application activity statistics.
 * Returns the top 10 users with the most auctions created, the top 10 users who have won the most auctions,
 * and the total number of auctions in the last 10 days.
 * {@code curl -X GET "http://localhost:8080/statistics"}
 * @return Map containing the requested statistics
 */
@GetMapping(value = "/statistics", produces = "application/json")
@ResponseBody
public Map<String, Object> getApplicationStatistics(@RequestHeader("x-access-tokens") String token) {
    if(validateTokenWithUsername(token, "admin"))
        return invalidToken();
    logger.info("### DEMO: Getting Application Statistics ###");
    Connection conn = RestServiceApplication.getConnection();
    Map<String, Object> returnData = new HashMap<>();

    try {
        // Top 10 users with the most auctions created
        List<String> topAuctionCreators = new ArrayList<>();
        try (PreparedStatement psTopCreators = conn.prepareStatement(
                "SELECT u.username, COUNT(*) AS auction_count FROM item i " +
                "JOIN users u ON i.users_username = u.username " +
                "GROUP BY u.username ORDER BY auction_count DESC LIMIT 10")) {
            ResultSet topCreatorsResult = psTopCreators.executeQuery();
            while (topCreatorsResult.next()) {
                topAuctionCreators.add(topCreatorsResult.getString("username"));
            }
        }
        
        // Top 10 users who have won the most auctions
        List<String> topAuctionWinners = new ArrayList<>();
        try (PreparedStatement psTopWinners = conn.prepareStatement(
              "SELECT u.username, COUNT(DISTINCT a.id) AS win_count " +
                          "FROM auction a " +
                          "JOIN bid b ON a.id = b.auction_id " +
                          "JOIN users u ON b.users_username = u.username " +
                          "WHERE a.status = FALSE " + 
                          "AND b.amount = a.highest_bid " + 
                          "GROUP BY u.username " +
                          "ORDER BY win_count DESC " +
                          "LIMIT 10")) {
            ResultSet topWinnersResult = psTopWinners.executeQuery();
            while (topWinnersResult.next()) {
                topAuctionWinners.add(topWinnersResult.getString("username"));
            }
        }

        // Total number of auctions in the last 10 days
        int totalAuctionsLast10Days = 0;
        try (PreparedStatement psTotalAuctions = conn.prepareStatement(
                "SELECT COUNT(*) AS total_count FROM auction WHERE start_time >= NOW() - INTERVAL '10 days'")) {
            ResultSet totalAuctionsResult = psTotalAuctions.executeQuery();
            if (totalAuctionsResult.next()) {
                totalAuctionsLast10Days = totalAuctionsResult.getInt("total_count");
            }
        }

        returnData.put("status", StatusCode.SUCCESS.code());
        returnData.put("topAuctionCreators", topAuctionCreators);
        returnData.put("topAuctionWinners", topAuctionWinners);
        returnData.put("totalAuctionsLast10Days", totalAuctionsLast10Days);
    } catch (SQLException ex) {
        logger.error("Error in DB", ex);
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", ex.getMessage());
    } finally {
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.error("Error closing connection", ex);
        }
    }

    return returnData;
}
@GetMapping(value = "/auctions/completed", produces = "application/json")
@ResponseBody
public Map<String, Object> getCompletedAuctions(@RequestHeader("x-access-tokens") String token) {
    if(!validateTokenWithUsername(token, "admin"))
        return invalidToken();
    logger.info("### GET /auctions/completed ###");
    Connection conn = RestServiceApplication.getConnection();
    Map<String, Object> returnData = new HashMap<>();
    List<Map<String, Object>> results = new ArrayList<>();

    try (PreparedStatement ps = conn.prepareStatement("SELECT a.id, a.title, a.start_time, a.end_time, a.highest_bid, b.users_username AS winner " + "FROM auction a " + "LEFT JOIN bid b ON a.id = b.auction_id " + "WHERE a.status = FALSE " + "ORDER BY a.end_time")) {
        
        ResultSet rows = ps.executeQuery();
        logger.debug("---- completed auctions ----");
        
        while (rows.next()) {
            Map<String, Object> content = new HashMap<>();
            logger.debug("'id': {}, 'title': {}, 'start_time': {}, 'end_time': {}, 'highest_bid': {}, 'winner': {}", rows.getInt("id"), rows.getString("title"), rows.getTimestamp("start_time"), rows.getTimestamp("end_time"), rows.getDouble("highest_bid"), rows.getString("winner"));
                    
            content.put("id", rows.getInt("id"));
            content.put("title", rows.getString("title"));
            content.put("start_time", rows.getTimestamp("start_time"));
            content.put("end_time", rows.getTimestamp("end_time"));
            content.put("highest_bid", rows.getDouble("highest_bid"));
            content.put("winner", rows.getString("winner")); 
            results.add(content);
            
        }

        returnData.put("status", StatusCode.SUCCESS.code());
        returnData.put("results", results);

    } catch (SQLException ex) {
        logger.error("Error in DB", ex);
        returnData.put("status", StatusCode.INTERNAL_ERROR.code());
        returnData.put("errors", ex.getMessage());
        
    } finally {
        try {
            conn.close();
            
        } catch (SQLException ex) {
            logger.error("Error closing connection", ex);
            
        }
        
    }
    return returnData;
    
}

    /**
 * Ban a user:
 * - Cancel all auctions created by the user.
 * - Invalidate all bids placed by the user (except the highest bid).
 * - Create an automatic message on the wall of the affected auctions.
 * {@code curl -X POST "http://localhost:8080/users/username/ban" }
 * @param username The username of the user to be banned.
 * @return Map with status and message indicating success or error.
 */
 @PutMapping("/users/{username}/ban")
@ResponseBody
public Map<String, Object> banUser(@RequestHeader("x-access-tokens") String token, @PathVariable("username") String username) {
    if(!validateTokenWithUsername(token, "admin"))
        return invalidToken();

    Connection conn = RestServiceApplication.getConnection();
    Map<String, Object> returnData = new HashMap<>();

    try {
        conn.setAutoCommit(false);

        banUserAndCreateRecord(conn, username); // Call the method to ban the user and create records

                  try (PreparedStatement psNotifyUsers = conn.prepareStatement(
                    "UPDATE users SET banned = true WHERE username = ?;")) {
                psNotifyUsers.setString(1, username);
                psNotifyUsers.executeUpdate();
            }

        conn.commit(); // Commit the transaction

        returnData.put("status", "success");
        returnData.put("message", "User banned successfully.");
    } catch (SQLException ex) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                ex1.printStackTrace();
            }
        }
        returnData.put("status", "error");
        returnData.put("message", "Error banning user: " + ex.getMessage());
    } finally {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    return returnData;
}

private List<Integer> getAuctionsCreatedByUser(Connection conn, String username) throws SQLException {
    List<Integer> affectedAuctionIds = new ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM auction WHERE item_id IN (SELECT id FROM item WHERE users_username = ?)")) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                affectedAuctionIds.add(rs.getInt("id"));
            }
        }
    }
    return affectedAuctionIds;
}

private void cancelAuctions(Connection conn, List<Integer> auctionIds) throws SQLException {
    try (PreparedStatement psUpdateAuction = conn.prepareStatement(
            "UPDATE auction SET status = false WHERE id = ?")) {
        for (int auctionId : auctionIds) {
            psUpdateAuction.setInt(1, auctionId);
            psUpdateAuction.executeUpdate();
        }
    }
}

private boolean userBanned(Connection conn, String username) throws SQLException {
    try (PreparedStatement psUpdateAuction = conn.prepareStatement(
            "SELECT banned FROM users WHERE username = ?")) {
        psUpdateAuction.setString(1, username);
        try (ResultSet rs = psUpdateAuction.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean("banned");
            } else {
                // If no user found, consider them not banned
                return false;
            }
        }
    }
    catch(SQLException e){
        e.printStackTrace(); 
        return false;
    }
}


private Map<String, Object> userHasBeenBanned() {
        Map<String, Object> returnData = new HashMap<String, Object>();

        returnData.put("status", 400);
        returnData.put("message", "user has been banned");

        return returnData;
    }

private void createAutomaticMessage(Connection conn, int auctionId) throws SQLException {
    String message = "We regret to inform you that this auction has been canceled due to the user being banned.";
    try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO message (comment, post_time, users_username, auction_id) VALUES (?, NOW(), ?, ?)")) {
        ps.setString(1, message);
        ps.setString(2, "admin");
        ps.setInt(3, auctionId);
        ps.executeUpdate();
    }
}

private void createRecord(Connection conn, String bannedUser, int canceledAuctionId, List<Integer> invalidatedBids) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO records (banned_user, banned_auction, banned_bid) VALUES (?, ?, ?)")) {
        for (int bidId : invalidatedBids) {
            ps.setString(1, bannedUser);
            ps.setInt(2, canceledAuctionId);
            ps.setInt(3, bidId);
            ps.executeUpdate();
        }
    }
}

private void banUserAndCreateRecord(Connection conn, String username) throws SQLException {
    List<Integer> canceledAuctionIds = getAuctionsCreatedByUser(conn, username);
    cancelAuctions(conn, canceledAuctionIds);

    List<Integer> invalidatedBids = invalidateBids(conn, username);

    for (Integer auctionId : canceledAuctionIds) {
        createAutomaticMessage(conn, auctionId); // Create automatic message
        createRecord(conn, username, auctionId, invalidatedBids);
    }
}

private List<Integer> invalidateBids(Connection conn, String username) throws SQLException {
    List<Integer> invalidatedBids = new ArrayList<>();
    try (PreparedStatement psUpdateBid = conn.prepareStatement(
            "UPDATE bid SET valid = FALSE WHERE users_username = ?")) {
        psUpdateBid.setString(1, username);
        psUpdateBid.executeUpdate();
    }

    // Find the invalidated bids
    try (PreparedStatement psSelectInvalidatedBids = conn.prepareStatement(
            "SELECT id FROM bid WHERE users_username = ? AND valid = FALSE")) {
        psSelectInvalidatedBids.setString(1, username);
        try (ResultSet rs = psSelectInvalidatedBids.executeQuery()) {
            while (rs.next()) {
                invalidatedBids.add(rs.getInt("id"));
            }
        }
    }

    // Update highest bid in affected auctions
    try (PreparedStatement psUpdateHighestBid = conn.prepareStatement(
            "UPDATE auction SET highest_bid = (SELECT MAX(amount) FROM bid WHERE auction_id = ? AND valid = TRUE) " +
            "WHERE id = ?")) {
        for (Integer auctionId : getAuctionsCreatedByUser(conn, username)) {
            psUpdateHighestBid.setInt(1, auctionId);
            psUpdateHighestBid.setInt(2, auctionId);
            psUpdateHighestBid.executeUpdate();
        }
    }

    return invalidatedBids;
}
}
