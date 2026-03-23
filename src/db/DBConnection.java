package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String HOST = "localhost";
    private static final int PORT =5432;
    private static final String DATABASE="expense_tracker";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    /*when our app wants to talk to the db, this asks for a
    specific instance. If it doesnt exist, it created one.
    * */
    private static DBConnection instance;
    /*
    * provided by db driver holding the open network connection to our db.
    * the physical comm. commands travel through here.
    * */
    private Connection connection;

    private DBConnection(){
        try{
            Class.forName("org.postgresql.Driver");
            this.connection=DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DB connected successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found");
            System.err.println("Make sure you have postgresql-xx.jar is in classpath.");
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance(){
        if (instance==null){
            instance=new DBConnection();
        }
        return instance;
    }

    public Connection getConnection(){
        try{
            if (connection==null || connection.isClosed()){
                System.out.println("Connection lost. Reconnecting...");
                connection= DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Reconnection failed: "+ e.getMessage());
            throw new RuntimeException(e);
        }

        return connection;
    }

    public void close(){
        try {
            if (connection!=null && !connection.isClosed()){
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
