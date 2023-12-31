## 数据字典

### 供应商数据（supplier.csv）
#### 属性
- **Supplier_ID**
  - **类型**：字符串
  - **描述**：供应商的唯一编号。
  - **约束**：主键，唯一。
- **Name**
  - **类型**：字符串
  - **描述**：供应商的名称。
- **Contact_Info**
  - **类型**：字符串
  - **描述**：供应商的联系信息，主要为邮箱地址。
- **Product_Range**
  - **类型**：字符串
  - **描述**：供应商提供的产品范围。可能包括多个类别，用分号分隔。
- **Performance_Metrics**
  - **类型**：字符串
  - **描述**：对供应商提供的产品的性能和质量的评价。
  - **选项**：例如“Excellent”，“Very Good”，“Good”等。
#### 数据关系和完整性
- **Supplier_ID**作为主键，保证了供应商信息的唯一性。
- 数据项如**Product_Range**和**Performance_Metrics**提供了有关供应商的关键信息，有助于评估供应商的能力和绩效。
### 商店数据（store.csv）
#### 属性
- **Store_ID**
  - **类型**：字符串
  - **描述**：商店的唯一编号。
  - **约束**：主键，唯一。
- **Location**
  - **类型**：字符串
  - **描述**：商店所在的城市。
- **Province**
  - **类型**：字符串
  - **描述**：商店所在的省份。
- **Size**
  - **类型**：字符串
  - **描述**：商店的规模大小。
  - **选项**：Large、Medium、Small。
- **Type**
  - **类型**：字符串
  - **描述**：商店的类型。
  - **选项**：Urban、Suburban、Rural。
- **Operating_Hours**
  - **类型**：字符串
  - **描述**：商店的营业时间。
#### 数据关系和完整性
- **Store_ID**作为主键，保证了商店信息的唯一性。
- 数据项**Size**和**Type**提供了有关商店特征的关键信息，有助于理解商店的运营模式和市场定位。
### 产品数据（product.csv）
#### 粒度
- SKU级别
#### 属性
- **Product_ID**
  - **类型**：字符串
  - **描述**：产品的唯一编码。
  - **约束**：主键，唯一。
- **Name**
  - **类型**：字符串
  - **描述**：产品名称。
- **Category**
  - **类型**：字符串
  - **描述**：产品所属的大类别。
- **Sub-category**
  - **类型**：字符串
  - **描述**：产品所属的子类别。
- **Supplier_ID**
  - **类型**：字符串（外键）
  - **描述**：供应商的编号。
  - **约束**：引用供应商数据。
- **Cost**
  - **类型**：货币值
  - **描述**：产品的成本。
- **Product Specifications**
  - **类型**：字符串
  - **描述**：产品的详细描述信息。
- **Ratings**
  - **类型**：数值
  - **描述**：产品的评分。
  - **范围**：最高为5.0。
- **Reviews**
  - **类型**：字符串
  - **描述**：对产品的评价。
#### 数据关系和完整性
- **Product_ID**作为主键，保证了产品信息的唯一性。
- **Supplier_ID**作为外键，确保了产品与其供应商之间的关联。
- 数据项如**Category**、**Sub-category**、**Ratings**和**Reviews**提供了关键信息，有助于理解产品的市场定位和消费者反馈。
### 客户数据（customer.csv）
#### 粒度
- 每个客户（会员）
#### 属性
- **customer_id**
  - **类型**：字符串
  - **描述**：客户的唯一编号。
  - **约束**：主键，唯一。
- **first_name**
  - **类型**：字符串
  - **描述**：客户的名字。
- **last_name**
  - **类型**：字符串
  - **描述**：客户的姓氏。
- **city**
  - **类型**：字符串
  - **描述**：客户所在的城市。
- **province**
  - **类型**：字符串
  - **描述**：客户所在城市的省份。
- **gender**
  - **类型**：字符串
  - **描述**：客户的性别。
- **email**
  - **类型**：字符串
  - **描述**：客户的邮箱地址。
- **registration_date**
  - **类型**：日期
  - **描述**：客户的注册日期。
#### 数据关系和完整性
- **customer_id**作为主键，保证了客户信息的唯一性。
- 特定编号`C00000`用于表示非会员客户，确保在销售数据中的参照完整性。
### 销售数据（sales1.csv,sales2.csv）
#### 粒度
- 事务级别
#### 属性
- **Transaction_ID**
  - **类型**：字符串（7位）
  - **描述**：唯一标识每笔交易的ID，格式为`T000000`
  - **范围**：从`T000000`开始，连续至`T588799`。
  - **约束**：主键，唯一。
- **DateTime**
  - **类型**：时间戳
  - **描述**：交易发生的具体日期和时间。
  - **范围**：从2023年3月1日至2023年5月31日。
- **Store_ID**
  - **类型**：外键
  - **描述**：标识交易发生的商店。
  - **约束**：引用商店数据表。
- **Product_ID**
  - **类型**：外键
  - **描述**：标识交易中购买的产品。
  - **约束**：引用产品数据表。
- **Quantity**
  - **类型**：整数
  - **描述**：交易中购买产品的数量。
  - **范围**：1-4。
- **Unit_Price**
  - **类型**：货币值,整数，量纲为美元。
  - **描述**：产品的销售价格。
- **Total_Amount**
  - **类型**：货币值，整数，量纲为美元。
  - **描述**：交易的总金额，计算方式为数量乘以单价。
- **Payment_Method**
  - **类型**：字符串
  - **描述**：交易的付款方式。
  - **选项**：Cash、Debit Card、Wechat Pay、AliPay、Gift Card。
- **Customer_ID**
  - **类型**：外键
  - **描述**：标识购买者的ID。非会员客户使用固定ID `C00000`，会员客户则使用其在客户数据表中的对应ID。
  - **约束**：引用客户数据表。
#### 数据关系和完整性
- **商店ID**和**产品ID**作为外键，确保数据的引用完整性。
- **事务ID**作为主键，保证了每笔交易的唯一性。
- **客户ID**通过使用固定`C00000`为非会员客户，以及引用客户数据表中的ID为会员客户，保证参照完整性。
