
## 🧠 AI_instructions.md

### 📌 Project: UIUonePOINT
**An Integrated Management System for Canteen and Grocery Store Management**  
**Technologies**: Java, JavaFX, MySQL

---

### 🎯 Purpose of this File
This file provides instructions for any AI module, rule engine, or automated logic built into or interfacing with the system. It also outlines how the AI (or rule-based logic) should behave in various scenarios and decision-making processes within UIUonePOINT.

---

### 👤 User Roles & Behavior Logic

#### 1. **Customer**
- Can only **view**, **select**, and **place orders** for food or grocery items.
- AI should:
    - Auto-suggest popular or most frequently ordered items.
    - Recommend combo offers or related products (e.g., if a user orders "Rice", suggest "Curry" or "Soft Drink").
    - Highlight low-stock items to create urgency.
    - Maintain user preferences using simple history-based behavior prediction.

#### 2. **Admin**
- Can manage inventory, sales reports, and order processing.
- AI should:
    - Suggest restocking based on item consumption rate (e.g., “Restock Eggs — only 10 left, used 40 this week”).
    - Highlight trends (e.g., “Snacks sell more between 3PM-5PM”).
    - Predict best-selling items based on previous days/months.
    - Automatically flag expired or soon-to-expire items.
    - Alert on low-stock or zero-stock items.

---

### 🧠 Section Selection Logic

- Upon login, users are asked to select:
    - 🥘 Canteen
    - 🛒 Grocery Store

AI should:
- Remember the last accessed section and **auto-suggest** that section next time.
- Keep a record of frequently visited items or categories.

---

### 📦 Smart Inventory Suggestions

For Admins:
- If item consumption rate > 80% within 3 days → Suggest early restocking.
- If item has had 0 sales in 15+ days → Suggest item removal or discount.
- For perishable items (e.g., food with expiry) → Auto-reminder 3 days before expiry.

---

### 📊 Sales Insights & Recommendations

- Generate simple trend summaries:
    - “Most sold item this week: Chicken Roll”
    - “Least popular: Mineral Water”
- Recommend bundle creation if two items are frequently ordered together.

---

### 🔐 Security Logic (Basic AI behavior)

- Detect failed login attempts and suggest account lock after 5 tries.
- Log suspicious activities (e.g., rapid item deletion) and alert admin.

---

### 📅 Future Enhancement Suggestions for AI

- Integrate a chatbot for ordering assistance.
- Enable speech-based ordering for canteen users.
- Use computer vision (with webcam) to recognize food items or scan receipts.
- Add predictive analytics for semester-wise demand planning.

---

### 🧪 Test Cases (For AI or Rule Engine)

| **Scenario** | **Input** | **Expected AI Response** |
|--------------|-----------|---------------------------|
| Customer always orders "Noodles" at lunch | Login at 12PM | Suggest "Noodles" automatically |
| Admin checks inventory | Milk has 5 items left | Suggest restocking milk |
| Item not sold in 20 days | Biscuits | Suggest discount or removal |
