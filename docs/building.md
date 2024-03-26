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

#### 客户分析
1. **会员购买频率分析**：
   - 统计每个会员在2023年3月至5月的购买次数，以识别常回购会员和低频会员。
   - 分类购买频率，如1-2次为低频，3-5次为中频，6次以上为高频，了解会员活跃度。

2. **会员地理分布分析**：
   - 按城市和省份对会员进行分组，计算每个地区的会员数量和总销售额。
   - 分析不同地区的会员密度和市场潜力，确定重点服务和扩展区域。

3. **会员年龄和性别偏好分析**：
   - 将会员按年龄段和性别分组，分析每组的购买行为和最受欢迎的产品类别。
   - 识别不同年龄和性别群体的购买偏好，定制有针对性的营销策略。

</details>


<details>
<summary><strong>数据仓库分层策略</strong></summary>

由于业务相对单一，考虑简化的三层分层策略，专注于处理和组织关键业务实体的数据。
### 原始层
- 对源数据清洗后加载到定义好的Hive表中。
### 数据仓库层
- 根据维度模型对原始数据进行转化和标准化后加载到Hive中。
- 加载好后进行初步汇总和聚合数据，以便加快后续查询。
### 数据集市层
- 根据业务用户的需求进行数据的进一步汇总和优化。

</details>


<details>
<summary><strong>逻辑模型设计</strong></summary>

## 数据建模策略
在本项目中，数据仓库的设计采用了维度模型，这一策略遵循了《数据仓库工具箱》中的推荐做法。维度模型是理解业务过程、促进数据分析和支持决策制定的强有力工具。以下是建模过程的核心步骤概览：

### 数据集
- **销售数据：** 事务ID、日期和时间、商店ID、产品ID、数量、单价、总金额、付款方式、客户ID。
- **产品数据：** 产品ID、名称、类别、子类别、供应商ID、成本、产品规格、评级和评论。
- **客户数据:** 客户ID、姓名、城市、省、年龄、性别、联系方式、注册日期。
- **供应商数据:** 供应商ID、名称、联系信息、产品范围、绩效指标。
- **商店数据:** 店铺ID、位置、规模、类型(城市，郊区，农村)、营业时间。

### 业务过程与数据粒度
- **业务过程选择**：我们将销售流程置于核心，这是因为它直接关联到公司的收入和市场表现。
- **数据粒度确定**：数据模型的粒度被设定为事务级别，确保每笔销售交易的细节都能被精确捕获，以便进行深入的分析。

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


### ODS层数据
##### 店铺数据
```sql
CREATE TABLE IF NOT EXISTS ods_retail.ods_store(  
store_id CHAR(4),
location VARCHAR(20),
province VARCHAR(20),
size VARCHAR(6),
type VARCHAR(8),
operating_hours CHAR(11),
PRIMARY KEY (store_id) DISABLE NOVALIDATE
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```

##### 客户数据
```sql
CREATE TABLE IF NOT EXISTS ods_retail.ods_customer(
customer_id CHAR(6),
first_name VARCHAR(20),
last_name VARCHAR(20),
city VARCHAR(20),
province VARCHAR(20),
gender VARCHAR(6),
email STRING,
registration DATE,
PRIMARY KEY (customer_id) DISABLE NOVALIDATE
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' ;
```
##### 供应商数据
```sql
CREATE TABLE IF NOT EXISTS ods_retail.ods_supplier(
supplier_id VARCHAR(6),
name STRING,
contact_info STRING,
product_range STRING,
performance_metrics VARCHAR(20),
PRIMARY KEY (supplier_id) DISABLE NOVALIDATE
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```
##### 产品数据
```sql
CREATE TABLE IF NOT EXISTS ods_retail.ods_product(
product_id CHAR(6),
name STRING,
category VARCHAR(20),
subcategory VARCHAR(20),
supplier_id VARCHAR(6),
cost FLOAT,
product_specifications STRING,
ratings FLOAT,
reviews STRING,
PRIMARY KEY (product_id) DISABLE NOVALIDATE,
FOREIGN KEY (supplier_id) REFERENCES ods_retail.ods_supplier(supplier_id) DISABLE NOVALIDATE
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```
##### 销售数据
```sql
CREATE TABLE IF NOT EXISTS ods_retail.ods_sales(
transaction_id CHAR(7),
event_time TIMESTAMP,
store_id CHAR(4),
product_id CHAR(6),
quantity SMALLINT,
unit_price FLOAT,
total_amount FLOAT,
payment_method VARCHAR(15),
customer_id CHAR(6),
PRIMARY KEY (transaction_id) DISABLE NOVALIDATE,
FOREIGN KEY (store_id) REFERENCES ods_retail.ods_store(store_id) DISABLE NOVALIDATE,
FOREIGN KEY (product_id) REFERENCES ods_retail.ods_product(product_id) DISABLE NOVALIDATE,
FOREIGN KEY (customer_id) REFERENCES ods_retail.ods_customer(customer_id) DISABLE NOVALIDATE
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```
### DW层数据

##### 店铺维度表（`store_dim`）
```sql
CREATE TABLE IF NOT EXISTS dm_retail.store_dim(
    store_id CHAR(4),
    location VARCHAR(20),
    province VARCHAR(20),
    size VARCHAR(6),
    type VARCHAR(8),
    operating_hours CHAR(11),
    PRIMARY KEY (store_id) DISABLE NOVALIDATE
)
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 客户维度表（`customer_dim`）
```sql
CREATE TABLE IF NOT EXISTS dm_retail.customer_dim(
    customer_id CHAR(6),
    city VARCHAR(20),
    province VARCHAR(20),
    gender VARCHAR(6),
    registration DATE,
    PRIMARY KEY (customer_id) DISABLE NOVALIDATE
)
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 供应商维度表（`supplier_dim`）
```sql
CREATE TABLE IF NOT EXISTS dm_retail.supplier_dim(
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
CREATE TABLE IF NOT EXISTS dm_retail.product_dim(
    product_id CHAR(6),
    name STRING NOT NULL,
    category VARCHAR(20),
    subcategory VARCHAR(20),
    supplier_id VARCHAR(6),
    product_specifications STRING,
    ratings FLOAT,
    reviews STRING,
    PRIMARY KEY (product_id) DISABLE NOVALIDATE,
    FOREIGN KEY (supplier_id) REFERENCES supplier_dim(supplier_id) DISABLE NOVALIDATE
)
CLUSTERED BY (product_id) INTO 4 BUCKETS 
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

##### 时间维度表（`date_dim`）
```sql
CREATE TABLE IF NOT EXISTS dm_retail.date_dim(
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
CREATE TABLE IF NOT EXISTS dm_retail.sales_fact(
    transaction_id CHAR(7),
    store_id CHAR(4),
    product_id CHAR(6),
    customer_id CHAR(6),
    date_key INT,
    payment_method VARCHAR(15),
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
CLUSTERED BY (product_id) INTO 4 BUCKETS 
STORED AS ORC
TBLPROPERTIES ('transactional'='true');
```

### 物化视图定义
通过定义物化视图可以预计算并存储查询结果，使得在后续的查询中，优化器能够利用其定义语义自动使用物化视图重写传入查询，从而加快查询执行。
基于业务需求考虑建立以下物化视图：

##### 月度销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.monthly_sales_summary
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
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.product_sales_summary
AS
  SELECT
        sf.product_id,
        pd.name,
        sum(sf.quantity) as total_quantity,
        sum(sf.total_amount) as total_sales
  FROM
        product_dim pd
        inner join sales_fact sf
        on pd.product_id = sf.product_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY sf.product_id, pd.name;
```

##### 店铺销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.store_sales_performance
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
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.regional_sales_comparison
AS
  SELECT
        sd.province,
        sd.location,
        sum(sf.total_amount) as total_sales,
        sum(sf.quantity) as total_quantity
  FROM
        store_dim sd
        inner join sales_fact sf
        on sd.store_id = sf.store_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY sd.province, sd.location;
```
##### 店铺利润分析
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.profit_analysis_by_store
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
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.profit_analysis_by_category
AS
  SELECT
        pd.category,
        sum(sf.total_amount) as total_sales,
        sum(sf.cost) as total_cost,
        (sum(sf.total_amount) - sum(sf.cost)) / sum(sf.total_amount) * 100 as profit_margin_percentage
  FROM
        product_dim pd
        inner join sales_fact sf
        on pd.product_id = sf.product_id
  GROUP BY pd.category;
```
##### 产品类别销售
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.category_sales_dynamics
AS
  SELECT
        pd.category,
        sum(sf.total_amount) as total_sales,
        sum(sf.quantity) as total_quantity
  FROM
        product_dim pd
        inner join sales_fact sf
        on pd.product_id = sf.product_id
  WHERE year = 2023 AND month in (3,4,5)
  GROUP BY pd.category;
```
##### 供应商绩效
```sql
CREATE MATERIALIZED VIEW IF NOT EXISTS dm_retail.supplier_performance
AS
  SELECT
        pd.supplier_id,
        sum(sf.total_amount) as total_sales,
        sum(sf.quantity) as total_quantity
  FROM
        product_dim pd
        inner join sales_fact sf
        on pd.product_id = sf.product_id
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
<summary><strong>Hive配置</strong></summary>
	
### 参考资源
以下是在配置过程中参考的一些重要资源：
- [Optimizing Hive on Tez Performance](https://blog.cloudera.com/optimizing-hive-on-tez-performance/).
- [APACHE HIVE PERFORMANCE TUNING](https://docs.cloudera.com/cdw-runtime/cloud/hive-performance-tuning/topics/hive-query-results-cache.html).
- [	Hive 调优总结    ](https://developer.aliyun.com/article/59635).

 ```XML
        <property>
                <name>hive.execution.engine</name>
                <value>tez</value>
        </property>
		<property>
			<name>hive.tez.container.size</name>
			<value>1024</value>
		</property>
		<property>
			<name>hive.tez.java.opts</name>
			<value>-Xmx840m</value> 
		</property>
        <property>
                <name>datanucleus.autoStartMechanism</name>
                <value>SchemaTable</value>
        </property>
        <property>
                <name>javax.jdo.option.ConnectionURL</name>
                <value>jdbc:postgresql://localhost:5432/hive?createDatabaseIfNotExist=true</value>
        </property>
        <property>
                <name>javax.jdo.option.ConnectionDriverName</name>
                <value>org.postgresql.Driver</value>
        </property>
        <property>
                <name>javax.jdo.option.ConnectionUserName</name>
                <value>postgres</value>
        </property>
        <property>
                <name>javax.jdo.option.ConnectionPassword</name>
                <value>06173152</value>
        </property>
        <property>
                <name>hive.server2.enable.doAs</name>
                <value>false</value>
        </property>
  
    	<!-- 启动并发控制和事务支持 -->
        <property>
                <name>hive.support.concurrency</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.exec.dynamic.partition.mode</name>
                <value>nonstrict</value>
        </property>
        <property>
                <name>hive.txn.manager</name>
                <value>org.apache.hadoop.hive.ql.lockmgr.DbTxnManager</value>
        </property>
        <property>
                <name>hive.compactor.initiator.on</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.compactor.worker.threads</name>
                <value>2</value>
        </property>
        
        
        <!-- 初始化Tez会话，以减少启动延迟 -->
        <property>
                <name>hive.server2.tez.default.queues</name>
                <value>default</value>
        </property>
        <property>
                <name>hive.server2.tez.sessions.per.default.queue</name>
                <value>1</value>
        </property>
        <property>
                <name>hive.server2.tez.initialize.default.sessions</name>
                <value>true</value>
        </property>
        
        
        <!-- 向量化查询 -->
        <property>
                <name>hive.vectorized.execution.enabled</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.vectorized.execution.reduce.enabled</name>
                <value>true</value>
        </property>
        
        <!-- 基于成本的优化 -->
        <property>
                <name>hive.cbo.enable</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.compute.query.using.stats</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.stats.fetch.column.stats</name>
                <value>true</value>
        </property>
        
        
        <!-- 查询结果缓存 -->
        <property>
                <name>hive.query.results.cache.enabled</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.query.results.cache.max.size</name>
                <value>1073741824</value>   <!-- 1 GiB -->
        </property>

        <!-- 连接优化 -->
        <property>
                <name>hive.auto.convert.join</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.optimize.skewjoin</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.groupby.skewindata</name>
                <value>true</value>
        </property>
        <property>
                <name>hive.optimize.bucketmapjoin</name>
                <value>true</value>
        </property>
	</configuration>
```

</details>


<details>
<summary><strong>查询测试</strong></summary>

<details>
<summary><strong>查询3月销售额排名前10的产品</strong></summary>

```sql
select 
	year, 
	month, 
	pd.product_id, 
	pd.name,
	sum(sf.total_amount) as total_sales
from 
	product_dim pd
	inner join sales_fact sf
	on pd.product_id=sf.product_id
where year = 2023  and month = 3
group by year, month, pd.product_id, pd.name
order by year, month, total_sales desc
limit 10;
```
#### 执行计划
![](/src/TestPng/monthly_sales_exec_plan.png)
#### 结果
![](/src/TestPng/monthly_sales.png)
</details>

<details>
<summary><strong>不同类型商店在指定月份的总销售额和平均单笔销售额</strong></summary>

```sql
select 
	sd.type, 
	month,
    	sum(sf.total_amount) as total_sales,
    	avg(sf.total_amount) as avg_sales
from 
	store_dim sd
	inner join sales_fact sf
	on sd.store_id=sf.store_id
where year = 2023 and month in (3, 4, 5)
group by sd.type, month
order by sd.type, month;
```

#### 执行计划
![](/src/TestPng/storeType_sales_exec_plan.png)
#### 结果
![](/src/TestPng/storeType_sales.png)

</details>

<details>
<summary><strong>查询3月至5月销售额前10的产品类别和子类别</strong></summary>
	
```sql
select 
	pd.category, 
	pd.subcategory,
    	sum(sf.quantity) as total_quantity,
    	sum(sf.total_amount) as total_sales
from
	product_dim pd
    	inner join sales_fact sf
    	on pd.product_id=sf.product_id
where year = 2023 and month in (3, 4, 5)
group by pd.category, pd.subcategory
order by total_sales desc, total_quantity desc
limit 10;
```
	
#### 执行计划
![](/src/TestPng/category_sales_exec_plan.png)
#### 结果
![](/src/TestPng/category_sales.png)

</details>

<details>
<summary><strong>毛利润分析</strong></summary>
	
```sql
select
	year,
	month,
	sum(total_amount) as total_sales,
	sum(total_amount - cost*quantity) as total_profit
from
	sales_fact
where year=2023 and month in (3,4,5)
group by year,month
order by total_profit desc;
```
	
#### 执行计划
![](/src/TestPng/profit_exec_plan.png)
#### 结果
![](/src/TestPng/profit.png)

</details>

### 查询性能总结
- **执行策略遵循代数优化规则**：根据查询执行计划可知，所有的查询操作均遵循了先进行选择和投影操作，随后执行连接和聚合的顺序。这种策略不仅符合数据库查询的代数优化原则，而且有效地减少了处理数据的量，提高了查询效率。

- **连接算法选择**：查询中主要采用了MapJoin和BucketMapJoin两种连接算法。当涉及到分桶表的连接时，优先选择BucketMapJoin算法，以充分利用数据的分桶特性，提高连接效率；而在处理普通表连接时，则倾向于使用MapJoin算法，以减少数据传输和处理时间。这种算法选择策略进一步优化了查询性能，特别是在处理大数据量时。

- **分区修剪优化**：实际测试显示，利用表的时间分区进行数据筛选的查询性能明显优于通过连接date_dim维度表进行筛选的查询。这表明基于时间分区修剪的策略在数据过滤中更为高效，能够显著减少不必要的数据扫描，从而加快查询响应速度。因此，在设计查询时，优先考虑使用分区来实现数据的筛选和减少。

### 性能瓶颈
- **伪分布式部署的限制**：当前的部署模式为伪分布式，其中所有的Hadoop和Hive服务运行在单一节点上。这种配置并行处理能力有限，CPU、内存和磁盘I/O资源都受限于单节点。
- **缺少LLAP配置**：未启用LLAP导致无法利用其为Hive查询提供的数据缓存和快速执行能力。
- **YARN和Tez的配置未充分优化**：当前项目中YARN和Tez的大部分默认配置可能未能最佳匹配具体工作负载和资源状况，导致资源利用不充分，查询效率低下。

</details>

<details>
<summary><strong>项目总结</strong></summary>

### 项目收获
- **深化维度建模理解**：通过本项目，我深入学习和实践了Kimball维度建模方法，这加强了我对一些理论的理解。
- **熟悉Linux开发环境**：在项目过程中，熟悉并掌握了一系列常用的Linux命令。
- **熟悉Hive的应用**：通过本项目，我熟悉了Hive的基本操作及常用的HiveQL语句。

 ### 局限性
1. **数据预处理经验不足**：由于使用的是模拟生成的数据，该数据已经相对整洁，因此项目中没有涉及到数据清洗、处理缺失值或确保数据一致性等实际操作。这意味着我在数据预处理方面的实战经验还不够丰富，未来需要在这方面加强学习和实践。
2. **模拟数据与真实业务场景的差距**：虽然模拟数据帮助我理解和应用了数据仓库建模和查询分析的基础，但这些数据无法完全模拟真实业务环境的复杂性。在实际应用中，数据通常来自多样化的数据源，包括大量的结构化和非结构化数据，且往往涉及更复杂的数据集成和同步问题。
3. **缺少数据治理实践**：项目中没有涉及到数据治理的过程，这是因为模拟数据的生成和使用相对简单，不需要复杂的数据治理工作。然而，数据治理是确保数据质量和支持数据驱动决策的关键环节，在未来的项目中，我需要更深入地了解和实践数据治理的方法和工具。
4. **查询性能优化有待加强**：虽然项目中尝试了一些基本的性能优化措施，如分区、分桶以及优化连接算法，但对于查询性能的深入优化还有较大的提升空间。特别是在处理大规模数据集时，如何有效地优化查询性能，减少查询响应时间，将是我需要进一步学习的重点。


 
</details>




