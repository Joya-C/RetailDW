import java.io.*;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;



import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;



public class HiveETL {

    private static final String DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";

    private static final String CONNECTION_URL = "jdbc:hive2://localhost:10000/default";

    private static final String USERNAME = "hive";

    private static final String PASSWORD = "";





    public static void main(String[] args) {

        if (args.length != 2) {

            System.err.println("Usage:HiveETL <input_Dir>  <output_Dir>");

            System.exit(1);

        }

        String inputDir = args[0];

        String outputDir = args[1];



        try {



            Class.forName(DRIVER_NAME);



            HiveETL.extractAndTransformData(inputDir, outputDir);



            try (Connection connection = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);

                 Statement statement = connection.createStatement()) {



                HiveETL.createDatabase(statement);

                HiveETL.createTables(statement);

                HiveETL.loadData(outputDir,statement);

                HiveETL.createMaterializedViews(statement);


            } catch (SQLException e) {

                e.printStackTrace();

            }





        } catch (ClassNotFoundException e) {

            e.printStackTrace();

            System.exit(1);

        } catch (IOException e) {

            e.printStackTrace();

            System.exit(1);

        }





    }







    public static void extractAndTransformData(String FileInputDir,String OutputDir) throws IOException {

        File CustomerCSVFile = new File(FileInputDir + "/Customer.csv");

        File ProductCSVFile = new File(FileInputDir + "/Product.csv");

        File SalesCSVFile = new File(FileInputDir + "/Sales.csv");



        try (BufferedReader CustomerReader = new BufferedReader(new FileReader(CustomerCSVFile));

             BufferedReader ProductReader = new BufferedReader(new FileReader(ProductCSVFile));

             BufferedReader SalesReader = new BufferedReader(new FileReader(SalesCSVFile));



             BufferedWriter CustomerWriter = new BufferedWriter(new FileWriter(OutputDir + "/Customer_dim.csv"));

             BufferedWriter ProductWriter = new BufferedWriter(new FileWriter(OutputDir + "/Product_dim.csv"));

             BufferedWriter SalesWriter = new BufferedWriter(new FileWriter(OutputDir + "/Sales_dim.csv"));

             BufferedWriter DateWriter = new BufferedWriter(new FileWriter(OutputDir + "/date_dim.csv"));



             CSVPrinter Customer_dim = new CSVPrinter(CustomerWriter, CSVFormat.DEFAULT);

             CSVPrinter Product_dim = new CSVPrinter(ProductWriter, CSVFormat.DEFAULT);

             CSVPrinter Sales_dim = new CSVPrinter(SalesWriter, CSVFormat.DEFAULT);

             CSVPrinter Date_dim = new CSVPrinter(DateWriter, CSVFormat.DEFAULT)



        ) {





            // process Customer.csv

            CSVParser CustomerParser = CSVParser.parse(CustomerReader, CSVFormat.DEFAULT.withFirstRecordAsHeader());



            for (CSVRecord record : CustomerParser) {

                Customer_dim.printRecord(record.get("customer_id"), record.get("city"), record.get("province"), record.get("gender"), record.get("registration_date"));

            }

            Customer_dim.flush();





            //process Product.csv

            CSVParser ProductParser = CSVParser.parse(ProductReader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            HashMap<String, Double> product_cost = new HashMap<>(); //把成本数据冗余到销售事实表中



            for (CSVRecord record : ProductParser) {

                Product_dim.printRecord(record.get("Product_ID"), record.get("Name"), record.get("Category"), record.get("Sub-category"),

                        record.get("Supplier_ID"), record.get("Product Specifications"), record.get("Ratings"), record.get("Reviews"));

                product_cost.put(record.get("Product_ID"), Double.parseDouble(record.get("Cost")));

            }

            Product_dim.flush();





            //process Sale.csv

            CSVParser SalesParser = CSVParser.parse(SalesReader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            String DateKey = null;

            int Quarter = -1;

            int preDayOfMonth = -1;

            int preMonthValue = -1;



            for (CSVRecord record : SalesParser) {

                LocalDateTime SalesDate = LocalDateTime.parse(record.get("DateTime"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                DateKey = SalesDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                switch (SalesDate.getMonthValue()) {

                    case 1,2,3:
                        Quarter = 1;
                        break;
                    case 4,5,6:
                        Quarter = 2;
                        break;
                    case 7,8,9:
                        Quarter = 3;
                        break;
                    case 10,11,12:
                        Quarter = 4;
                        break;
                }

                if (SalesDate.getMonthValue() != preMonthValue || SalesDate.getDayOfMonth() != preDayOfMonth) {

                    Date_dim.printRecord(DateKey, SalesDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), SalesDate.getDayOfMonth(), SalesDate.getDayOfWeek(),

                            SalesDate.getMonthValue(), SalesDate.getYear(), Quarter);

                }

                Sales_dim.printRecord(record.get("Transaction_ID"), record.get("Store_ID"), record.get("Product_ID"), record.get("Customer_ID"), DateKey,

                        record.get("Payment_Method"), record.get("Quantity"), product_cost.get(record.get("Product_ID")),

                        record.get("Unit_Price"), record.get("Total_Amount"));



                preMonthValue = SalesDate.getMonthValue();

                preDayOfMonth = SalesDate.getDayOfMonth();

            }

            Date_dim.flush();

            Sales_dim.flush();





        }

    }



    private static void loadData(String InputPath, Statement statement) throws SQLException {

        // 定义CSV文件和对应的临时表映射

        String[][] csvToTempTableMappings = {

                {"Customer_dim.csv", "retail.customer_temp_dim"},

                {"Store.csv", "retail.store_temp_dim"},

                {"date_dim.csv", "retail.date_temp_dim"},

                {"Supplier.csv", "retail.supplier_temp_dim"},

                {"Product_dim.csv", "retail.product_temp_dim"},

                {"Sales_dim.csv", "retail.sales_temp_fact"}

        };



        // 加载CSV数据到临时表

        for (String[] mapping : csvToTempTableMappings) {

            loadCsvToTempTable(InputPath + "/" + mapping[0], mapping[1], statement);

        }



        // 将数据从临时表转移到ORC格式的表

        transferDataToFinalTable("retail.customer_temp_dim", "retail.customer_dim", statement);

        transferDataToFinalTable("retail.store_temp_dim", "retail.store_dim", statement);

        transferDataToFinalTable("retail.supplier_temp_dim", "retail.supplier_dim", statement);

        transferDataToFinalTable("retail.product_temp_dim", "retail.product_dim", statement);

        transferDataToFinalTable("retail.date_temp_dim", "retail.date_dim", statement);



        // 特殊处理：销售数据的分区插入

        transferSalesData(statement);

    }



    private static void loadCsvToTempTable(String csvFilePath, String tempTableName, Statement statement) throws SQLException {

        String loadSql = "load data local inpath '" + csvFilePath + "' into table " + tempTableName;

        statement.execute(loadSql);

    }



    private static void transferDataToFinalTable(String tempTableName, String finalTableName, Statement statement) throws SQLException {

        String transferSql = "insert into table " + finalTableName + " select * from " + tempTableName;

        statement.execute(transferSql);

    }



    private static void transferSalesData(Statement statement) throws SQLException {

        String salesTransferSql = "from retail.sales_temp_fact " +

                "insert into table retail.sales_fact partition(year = '2023', month = '3') " +

                "select * where date_key <= '20230331' " +

                "insert into table retail.sales_fact partition(year = '2023', month = '4') " +

                "select * where date_key <= '20230430' and date_key >= '20230401' " +

                "insert into table retail.sales_fact partition(year = '2023', month = '5') " +

                "select * where date_key >= '20230501'";

        statement.execute(salesTransferSql);

    }




    private static void createTables(Statement statement) throws SQLException {

        //创建与CSV格式匹配的临时表

        statement.execute(

                "create table if not exists retail.store_temp_dim(" +

                        "store_id char(4)," +

                        "location varchar(20)," +

                        "province varchar(20)," +

                        "size varchar(6)," +

                        "type varchar(8)," +

                        "operating_hours char(11)," +

                        "primary key (store_id) disable novalidate" +

                        ")" +

                        "row format delimited fields terminated by ','"

        );



        statement.execute(

                "create table if not exists retail.customer_temp_dim(" +

                        "customer_id char(6)," +

                        "city varchar(20)," +

                        "province varchar(20)," +

                        "gender varchar(6)," +

                        "registration date," +

                        "primary key (customer_id) disable novalidate" +

                        ")" +

                        "row format delimited fields terminated by ','"

        );

        statement.execute(

                "create table if not exists retail.supplier_temp_dim(" +

                        "supplier_id varchar(6)," +

                        "name string," +

                        "contact_info string," +

                        "product_range string," +

                        "performance_metrics varchar(20)," +

                        "primary key (supplier_id) disable novalidate" +

                        ")" +

                        "row format delimited fields terminated by ','"

        );

        statement.execute(

                "create table if not exists retail.product_temp_dim(" +

                        "product_id char(6)," +

                        "name string," +

                        "category varchar(20)," +

                        "subcategory varchar(20)," +

                        "supplier_id varchar(6)," +

                        "product_specifications string," +

                        "ratings float," +

                        "reviews string," +

                        "primary key (product_id) disable novalidate," +

                        "foreign key (supplier_id) references supplier_temp_dim(supplier_id) disable novalidate" +

                        ")" +

                        "row format delimited fields terminated by ','"

        );

        statement.execute(

                "create table if not exists retail.date_temp_dim(" +

                        "date_key int," +

                        "full_date date," +

                        "dayofmonth smallint," +

                        "dayofweek varchar(20)," +

                        "month smallint," +

                        "year smallint," +

                        "quarter smallint," +

                        "primary key (date_key) disable novalidate" +

                        ")" +

                        "row format delimited fields terminated by ','"

        );

        statement.execute(

                "create table if not exists retail.sales_temp_fact(" +

                        "transaction_id char(7)," +

                        "store_id char(4)," +

                        "product_id char(6)," +

                        "customer_id char(6)," +

                        "date_key int," +

                        "payment_method varchar(15)," +

                        "quantity smallint," +

                        "cost float," +

                        "unit_price float," +

                        "total_amount float," +

                        "primary key (transaction_id) disable novalidate," +

                        "foreign key (store_id) references store_temp_dim(store_id) disable novalidate," +

                        "foreign key (product_id) references product_temp_dim(product_id) disable novalidate," +

                        "foreign key (customer_id) references customer_temp_dim(customer_id) disable novalidate," +

                        "foreign key (date_key) references date_temp_dim(date_key) disable novalidate" +

                        ")" +

                        "row format delimited fields terminated by ','"

        );





        //创建ORC格式的目标表

        statement.execute(

                "create table if not exists retail.store_dim(" +

                        "store_id char(4)," +

                        "location varchar(20)," +

                        "province varchar(20)," +

                        "size varchar(6)," +

                        "type varchar(8)," +

                        "operating_hours char(11)," +

                        "primary key (store_id) disable novalidate" +

                        ")" +

                        "STORED AS ORC" +
                        "TBLPROPERTIES ('transactional'='true')"

        );



        statement.execute(

                "create table if not exists retail.customer_dim(" +

                        "customer_id char(6)," +

                        "city varchar(20)," +

                        "province varchar(20)," +

                        "gender varchar(6)," +

                        "registration date," +

                        "primary key (customer_id) disable novalidate" +

                        ")" +

                        "STORED AS ORC" +
                        "TBLPROPERTIES ('transactional'='true')"

        );

        statement.execute(

                "create table if not exists retail.supplier_dim(" +

                        "supplier_id varchar(6)," +

                        "name string," +

                        "contact_info string," +

                        "product_range string," +

                        "performance_metrics varchar(20)," +

                        "primary key (supplier_id) disable novalidate" +

                        ")" +

                        "STORED AS ORC" +
                        "TBLPROPERTIES ('transactional'='true')"

        );



        statement.execute(

                "create table if not exists retail.product_dim(" +

                        "product_id char(6)," +

                        "name string," +

                        "category varchar(20)," +

                        "subcategory varchar(20)," +

                        "supplier_id varchar(6)," +

                        "product_specifications string," +

                        "ratings float," +

                        "reviews string," +

                        "primary key (product_id) disable novalidate," +

                        "foreign key (supplier_id) references supplier_dim(supplier_id) disable novalidate" +

                        ")" +

                        "STORED AS ORC" +
                        "TBLPROPERTIES ('transactional'='true')"

        );



        statement.execute(

                "create table if not exists retail.date_dim(" +

                        "date_key int," +

                        "full_date date," +

                        "dayofmonth smallint," +

                        "dayofweek varchar(20)," +

                        "month smallint," +

                        "year smallint," +

                        "quarter smallint," +

                        "primary key (date_key) disable novalidate" +

                        ")" +

                        "STORED AS ORC" +
                        "TBLPROPERTIES ('transactional'='true')"

        );



        statement.execute(

                "create table if not exists retail.sales_fact(" +

                        "transaction_id char(7)," +

                        "store_id char(4)," +

                        "product_id char(6)," +

                        "customer_id char(6)," +

                        "date_key int," +

                        "payment_method varchar(15)," +

                        "quantity smallint," +

                        "cost float," +

                        "unit_price float," +

                        "total_amount float," +

                        "primary key (transaction_id) disable novalidate," +

                        "foreign key (store_id) references store_dim(store_id) disable novalidate," +

                        "foreign key (product_id) references product_dim(product_id) disable novalidate," +

                        "foreign key (customer_id) references customer_dim(customer_id) disable novalidate," +

                        "foreign key (date_key) references date_dim(date_key) disable novalidate" +

                        ")" +

                        "PARTITIONED BY (year smallint,month smallint)" +

                        "STORED AS ORC" +
                        "TBLPROPERTIES ('transactional'='true')"

        );

    }



    private static void createDatabase(Statement statement) throws SQLException {

        statement.execute("CREATE DATABASE Retail");

        statement.execute("USE Retail");

    }


    private static void createMaterializedViews(Statement statement) throws SQLException {
        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.monthly_sales_summary" +
                        "AS" +
                        "SELECT" +
                        "        year," +
                        "        month," +
                        "        sum(total_amount) as total_sales," +
                        "        avg(total_amount) as avg_sales," +
                        "        sum(quantity) as total_quantity" +
                        "  FROM sales_fact" +
                        "  WHERE year = 2023 AND month in (3, 4, 5)" +
                        "  GROUP BY year, month"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.product_sales_summary" +
                        "AS" +
                        "  SELECT" +
                        "        sf.product_id," +
                        "        pd.name," +
                        "        sum(sf.quantity) as total_quantity," +
                        "        sum(sf.total_amount) as total_sales" +
                        "  FROM" +
                        "        sales_fact sf" +
                        "        inner join product_dim pd" +
                        "        on sf.product_id = pd.product_id" +
                        "  WHERE year = 2023 AND month in (3,4,5)" +
                        "  GROUP BY sf.product_id, pd.name"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.store_sales_performance" +
                        "AS" +
                        "  SELECT" +
                        "        store_id," +
                        "        sum(total_amount) as total_sales," +
                        "        sum(quantity) as total_quantity" +
                        "  FROM sales_fact" +
                        "  WHERE year = 2023 AND month in (3,4,5)" +
                        "  GROUP BY store_id"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.regional_sales_comparison" +
                        "AS" +
                        "  SELECT" +
                        "        sd.province," +
                        "        sd.location," +
                        "        sum(sf.total_amount) as total_sales," +
                        "        sum(sf.quantity) as total_quantity" +
                        "  FROM" +
                        "        sales_fact sf" +
                        "        inner join store_dim sd" +
                        "        on sf.store_id = sd.store_id" +
                        "  WHERE year = 2023 AND month in (3,4,5)" +
                        "  GROUP BY sd.province, sd.location"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.profit_analysis_by_store" +
                        "AS" +
                        "  SELECT" +
                        "        store_id," +
                        "        sum(total_amount) as total_sales," +
                        "        sum(cost) as total_cost," +
                        "        (sum(total_amount) - sum(cost)) / sum(total_amount) * 100 as profit_margin_percentage" +
                        "  FROM sales_fact" +
                        "  GROUP BY store_id"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.profit_analysis_by_category" +
                        "AS" +
                        "  SELECT" +
                        "        pd.category," +
                        "        sum(sf.total_amount) as total_sales," +
                        "        sum(sf.cost) as total_cost," +
                        "        (sum(sf.total_amount) - sum(sf.cost)) / sum(sf.total_amount) * 100 as profit_margin_percentage" +
                        "  FROM" +
                        "        sales_fact sf" +
                        "        inner join product_dim pd" +
                        "        on sf.product_id = pd.product_id" +
                        "  GROUP BY pd.category"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.category_sales_dynamics" +
                        "AS" +
                        "  SELECT" +
                        "        pd.category," +
                        "        sum(sf.total_amount) as total_sales," +
                        "        sum(sf.quantity) as total_quantity" +
                        "  FROM" +
                        "        sales_fact sf" +
                        "        inner join product_dim pd" +
                        "        on sf.product_id = pd.product_id" +
                        "  WHERE year = 2023 AND month in (3,4,5)" +
                        "  GROUP BY pd.category"
        );

        statement.execute(
                "CREATE MATERIALIZED VIEW IF NOT EXISTS retail.supplier_performance" +
                        "AS" +
                        "  SELECT" +
                        "        pd.supplier_id," +
                        "        sum(sf.total_amount) as total_sales," +
                        "        sum(sf.quantity) as total_quantity" +
                        "  FROM" +
                        "        sales_fact sf" +
                        "        inner join product_dim pd" +
                        "        on sf.product_id = pd.product_id" +
                        "  WHERE year = 2023 AND month in (3,4,5)" +
                        "  GROUP BY pd.supplier_id"
        );
    }


}