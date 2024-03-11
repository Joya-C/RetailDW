<details>
<summary><strong>环境和工具</strong></summary>
  
### 开发与设计工具
- **ER/Studio**：用于数据建模和设计。
- **Java 8**：整个数据处理（包括Hadoop和Hive）都建立在Java 8上。
### 数据处理和存储
- **Hadoop 3.3.6**：作为基础数据处理框架，单机伪分布式部署。
- **Hive 3.1.2**：用于数据仓库的构建和管理，在Hadoop之上操作。
- **PostgreSQL 15.5**：作为元数据存储数据库，确保其版本为15.5以保证兼容性和性能。
### 系统环境
- **CentOS 7**：操作系统环境，项目在CentOS 7上进行开发和测试。
- **硬件**：16GiB RAM。

</details>

<details>
<summary><strong>业务需求</strong></summary>
  
#### 销售分析：
1. **月度销售趋势**：
   - 从每天的销售事实数据中汇总月度销售额和数量，以识别2023年3月、4月和5月每月的销售趋势。
   - 计算每月的总销售额、平均销售额以及总销售量。

2. **产品销售细分**：
   - 按产品ID和名称分组，计算每个产品在2023年3月至5月的总销售量和总销售额。

#### 店铺分析：
1. **店铺销售性能**：
   - 对每个店铺的销售额和销售量进行汇总，以确定2023年3月至5月每家店铺的销售绩效。
   - 分析各店铺的销售数据，了解每家店铺的客流量和客单价表现。

2. **地区销售比较**：
   - 将销售数据按店铺所在的城市和省份进行分段，分析2023年3月至5月期间不同地区的销售情况。
   - 基于地区销售数据，评估区域市场的饱和度和市场份额。

#### 财务指标：
1. **利润分析**：
   - 计算每个店铺和每个产品类别的利润率，将销售收入与相关成本（如采购成本、运营成本）进行比较。
   - 对2023年3月至5月的数据进行分析，确定最具利润潜力的店铺和产品。

#### 产品类别分析：
1. **类别销售动态**：
   - 按类别对产品销售额和数量进行汇总，评估2023年3月至5月各类别产品的市场表现。
   - 分析类别销售数据来识别消费者偏好的变化和市场趋势。

#### 供应商分析：
1. **供应商绩效评估**：
   - 根据供应商提供的产品销售数据，分析2023年3月至5月各供应商的绩效。
   - 通过供应商产品的销售数据来评估供应链效率和供应商可靠性。

</details>


<details>
  <summary><strong>数据建模</strong></summary>

## 数据建模策略
在本项目中，数据仓库的设计采用了维度模型，这一策略遵循了《数据仓库工具箱》中的推荐做法。维度模型是理解业务过程、促进数据分析和支持决策制定的强有力工具。以下是建模过程的核心步骤概览：

### 数据集
- **销售数据：** 事务ID、日期和时间、商店ID、产品ID、数量、单价、总金额、付款方式、客户ID
- **产品数据：** 产品ID、名称、类别、子类别、供应商ID、成本、产品规格、评级和评论。
- **客户数据:** 客户ID、姓名、城市、省、年龄、性别、联系方式、注册日期。
- **供应商数据:** 供应商ID、名称、联系信息、产品范围、绩效指标。
- **商店数据:** 店铺ID、位置、规模、类型(城市，郊区，农村)、营业时间。

### 业务过程与数据粒度
- **业务过程选择**：我们将销售流程置于核心，这是因为它直接关联到公司的收入和市场表现。
- **数据粒度确定**：数据模型的粒度被设定为事务级别，确保每笔销售交易的细节都能被精确捕获，以便进行深入的数据分析。

### 维度定义
- **产品维度**：涵盖了产品的核心属性，包括**产品ID**、**名称**、**类别**、**子类别**、**供应商ID**、**产品规格**、**评级**和**评论**。由于产品成本主要用于计算利润，所以在产品维度中将不作为主要属性。
- **客户维度**：包含**客户ID**、**年龄**、**城市**、**省份**、**性别**和**注册日期**。客户姓名和联系方式对业务分析没有实质帮助，且涉及用户隐私，所以该属性不作为维度属性。
- **商店维度**：**店铺ID**、**位置**、**规模**、**类型**、**营业时间**。
- **时间维度**：构建了一个全面的时间框架，从日期键到具体的时间单位如日、月、年和季度。
- **供应商维度**：**供应商ID**、**名称**、**联系信息**、**产品范围**和**绩效指标**。
- **付款方式维度**：作为退化维度，简化了付款方式信息的记录。

### 事实表确定
- **销售事实**：综合了各维度的关键数据点，包括**事务ID**、**商店ID**、**产品ID**、**客户ID**、**日期ID**、**付款方式**，以及关键的财务指标，如**数量**、**成本**、**单价**和**总价**，为分析提供了必要的量度。

## 逻辑模型图
以下是本项目数据仓库设计的逻辑模型图，它展示了不同数据实体之间的关系，包括事实表和各个维度表的链接：
![逻辑模型图](/src/model/logical.png)

</details>

<details>
  <summary><strong>基于Hive的物理结构设计</strong></summary>

### 分区策略
- 鉴于查询主要关注月度销售数据，事实表将按月进行分区。

### 分桶策略
- 考虑到销售事实表是查询中使用最频繁的表，并且经常与产品维度表进行连接，所以选择product_id作为分桶键。

### 存储格式
- 选择ORC列式存储格式，它提供了高效的压缩和性能，支持快速的数据检索和分析。

 
### 表类型——Managed 表
- 选择由Hive来管理表的生命周期。

### Hive表结构定义

##### 店铺维度表（`store_dim`）
```sql
CREATE TABLE IF NOT EXISTS retail.store_dim(
    store_id CHAR(4),
    location VARCHAR(20) NOT NULL,
    province VARCHAR(20) NOT NULL,
    size VARCHAR(6) CHECK (size IN ('Large', 'Medium', 'Small')),
    type VARCHAR(8) CHECK (type IN ('Urban', 'Suburban', 'Rural')),
    operating_hours CHAR(11),
    PRIMARY KEY (store_id) DISABLE NOVALIDATE
)
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 客户维度表（`customer_dim`）
```sql
CREATE TABLE IF NOT EXISTS retail.customer_dim(
    customer_id CHAR(6),
    city VARCHAR(20) NOT NULL,
    province VARCHAR(20) NOT NULL,
    gender VARCHAR(6) CHECK (gender IN ('male', 'female')),
    registration DATE,
    PRIMARY KEY (customer_id) DISABLE NOVALIDATE
)
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 供应商维度表（`supplier_dim`）
```sql
CREATE TABLE IF NOT EXISTS retail.supplier_dim(
    supplier_id VARCHAR(6),
    name STRING,
    contact_info STRING,
    product_range STRING,
    performance_metrics VARCHAR(20),
    PRIMARY KEY (supplier_id) DISABLE NOVALIDATE
)
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 产品维度表（`product_dim`）
```sql
CREATE TABLE IF NOT EXISTS retail.product_dim(
    product_id CHAR(6),
    name STRING NOT NULL,
    category VARCHAR(20) NOT NULL,
    subcategory VARCHAR(20) NOT NULL,
    supplier_id VARCHAR(6),
    product_specifications STRING,
    ratings FLOAT,
    reviews STRING,
    PRIMARY KEY (product_id) DISABLE NOVALIDATE,
    FOREIGN KEY (supplier_id) REFERENCES supplier_dim(supplier_id) DISABLE NOVALIDATE
)
CLUSTERED BY (product_id) INTO 3 BUCKETS 
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 时间维度表（`date_dim`）
```sql
CREATE TABLE IF NOT EXISTS retail.date_dim(
    date_key INT,
    full_date DATE,
    dayofmonth SMALLINT,
    dayofweek SMALLINT,
    month SMALLINT,
    year SMALLINT,
    quarter SMALLINT,
    PRIMARY KEY (date_key) DISABLE NOVALIDATE
)
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 销售事实表（`sales_fact`）
```sql
CREATE TABLE IF NOT EXISTS retail.sales_fact(
    transaction_id CHAR(7),
    store_id CHAR(4),
    product_id CHAR(6),
    customer_id CHAR(6),
    date_key INT,
    payment_method VARCHAR(15) CHECK (payment_method IN ('Debit Card', 'Cash', 'Gift Card', 'Credit Card', 'WeChat Pay', 'Alipay')),
    quantity SMALLINT,
    cost FLOAT,
    unit_price FLOAT,
    total_amount FLOAT,
    PRIMARY KEY (transaction_id) DISABLE NOVALIDATE,
    FOREIGN KEY (store_id) REFERENCES store_dim(store_id) DISABLE NOVALIDATE,
    FOREIGN KEY (product_id) REFERENCES product_dim(product_id) DISABLE NOVALIDATE,
    FOREIGN KEY (customer_id) REFERENCES customer_dim(customer_id) DISABLE NOVALIDATE,
    FOREIGN KEY (date_key) REFERENCES date_dim(date_key) DISABLE NOVALIDATE
)
PARTITIONED BY (year SMALLINT,month SMALLINT)
CLUSTERED BY (product_id) INTO 6 BUCKETS 
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

### 物化视图定义
通过定义物化视图可以预计算并存储查询结果，使得在后续的查询中，优化器能够利用其定义语义自动使用物化视图重写传入查询，从而加快查询执行。
基于业务需求考虑建立以下物化视图：

##### 月度销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.monthly_sales_summary
AS
  SELECT
        year,
        month,
        sum(total_amount) as total_sales,
        avg(total_amount) as avg_sales,
        sum(quantity) as total_quantity
  FROM sales_fact
  WHERE year = 2023 AND month in (3, 4, 5)
  GROUP BY year, month;
```

##### 产品销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.product_sales_summary
AS
  SELECT
        sf.product_id,
        pd.name,
        sum(sf.quantity) as total_quantity,
        sum(sf.total_amount) as total_sales
  FROM
        sales_fact sf
        inner join product_dim pd
        on sf.product_id = pd.product_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY sf.product_id, pd.name;
```

##### 店铺销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.store_sales_performance
AS
  SELECT
        store_id,
        sum(total_amount) as total_sales,
        sum(quantity) as total_quantity
  FROM sales_fact
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY store_id;
```

##### 地区销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.regional_sales_comparison
AS
  SELECT
        sd.province,
        sd.location,
        sum(sf.total_amount) as total_sales,
        sum(sf.quantity) as total_quantity
  FROM
        sales_fact sf
        inner join store_dim sd
        on sf.store_id = sd.store_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY sd.province, sd.location;
```
##### 店铺利润分析
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.profit_analysis_by_store
AS
  SELECT
        store_id,
        sum(total_amount) as total_sales,
        sum(cost) as total_cost,
        (sum(total_amount) - sum(cost)) / sum(total_amount) * 100 as profit_margin_percentage
  FROM sales_fact
  GROUP BY store_id;
```

##### 产品类别利润分析
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.profit_analysis_by_category
AS
  SELECT
        pd.category,
        sum(sf.total_amount) as total_sales,
        sum(sf.cost) as total_cost,
        (sum(sf.total_amount) - sum(sf.cost)) / sum(sf.total_amount) * 100 as profit_margin_percentage
  FROM
        sales_fact sf
        inner join product_dim pd
        on sf.product_id = pd.product_id
  GROUP BY pd.category;
```
##### 产品类别销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.category_sales_dynamics
AS
  SELECT
        pd.category,
        sum(sf.total_amount) as total_sales,
        sum(sf.quantity) as total_quantity
  FROM
        sales_fact sf
        inner join product_dim pd
        on sf.product_id = pd.product_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY pd.category;
```
##### 供应商绩效
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS retail.supplier_performance
AS
  SELECT
        pd.supplier_id,
        sum(sf.total_amount) as total_sales,
        sum(sf.quantity) as total_quantity
  FROM
        sales_fact sf
        inner join product_dim pd
        on sf.product_id = pd.product_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY pd.supplier_id;
```

</details>

<details>
<summary><strong>ETL</strong></summary>
本项目通过Java程序实现了一个自动化的ETL，用于将CSV格式的源数据有效地转移到Hive数据仓库中。整个过程分为以下几个主要步骤：

### 1. 数据提取
使用Apache Common CSV库处理源数据。

### 2. 数据转换
对提取的数据进行处理，生成满足数据仓库模型需求的维度数据（客户、产品、日期）和销售事实数据。

### 3. 数据加载
将转换后的数据首先加载到Hive的CSV格式的临时表中，然后转移数据到以ORC格式存储的最终表中。ORC格式表提高了数据存储效率和查询性能。

### 4. 物化视图创建
基于ORC格式的维度表和事实表，创建物化视图以优化查询性能，使得频繁的查询操作更加高效。

### 实现
使用Java与Hive JDBC驱动进行交互，实现数据的提取、转换、加载过程，并通过HiveQL语句在Hive中创建所需的数据库、表和物化视图。

</details>




<details>
<summary><strong>测试</strong></summary>

</details>







