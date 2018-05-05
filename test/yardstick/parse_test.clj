(ns yardstick.parse-test
  (:require [clojure.test :refer :all]
            [yardstick.parse :refer :all]
            [clojure.pprint :as pp]))

(deftest test-parse-customer
  (is (= (parse-test-file (slurp "resources/examples/customer.spec"))
        {:spec "Customers",
         :tags #{},
         :for-each [["On the customer page"]],
         :scenarios
         [{:scenario "Search for a customer",
           :tags #{"admin" "customer" "search"},
           :steps [["Search for customer %1s" "ScroogeMcduck"]
                   ["The customer %1s is listed" "ScroogeMcduck"]]}
          {:scenario "Verify a bunch of customers",
           :tags #{"admin" "customer" "search"},
           :steps [["Search for customers &lt;table:resources/user.csv&gt;"]]}]})))

(deftest test-parse-products
  (pp/pprint (parse-test-file (slurp "resources/examples/products.spec")))
  (is (= (parse-test-file (slurp "resources/examples/products.spec"))
         {:spec "Products",
          :tags #{},
          :for-each [],
          :scenarios
          [{:scenario "Create a new product",
            :tags #{"admin" "product" "create"},
            :steps [["Create a product  |Title |Description |Author |Price|  |--------------|--------------------|--------------|-----|  |Go Programming|ISBN: 978-1453636671|John P. Baugh |25.00|  |The Way to Go |ISBN: 978-1469769165|Ivo Balbaert |20.00|  |Go In Action |ISBN: 9781617291784 |Brian Ketelsen|30.00|  |Learning Go |ebook |Miek Gieben |0.00 |"]]}
           {:scenario "Search for a product",
            :tags #{"admin" "product" "search"},
            :steps [["Find and Open product page for %1s" "Go Programming"]
                    ["Verify product %1s as %2s" "author" "John P. Baugh"]]}
           {:scenario "Search and edit and existing product",
            :tags #{"admin" "product" "edit"},
            :steps [["Open product edit page for stored productId"]
                    ["Update product specifier to new value &lt;table:resources/product_data.csv&gt;"]
                    ["Check product specifier has new value &lt;table:resources/product_data.csv&gt;"]]}
           {:scenario "Delete a product",
            :tags #{"delete" "admin" "product"},
            :steps [["Delete product %1s" "Learning Go"]]}]})))

(deftest test-parse-user
  (is (= (parse-test-file (slurp "resources/examples/user.spec"))
         {:spec "Signup",
          :tags #{},
          :for-each [],
          :scenarios
          [{:scenario "Register a customer",
            :tags #{"high" "user" "signup" "final" "smoke"},
            :steps [["Sign up a new customer"]
                    ["On the customer page"]
                    ["Just registered customer is listed"]]}]})))
