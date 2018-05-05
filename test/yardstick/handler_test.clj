(ns yardstick.handler-test
  (:require [clojure.test :refer :all]
            [yardstick.handler :refer :all]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.string :as s])
  (:import (clojure.lang ExceptionInfo)))

(deftest test-not-implemented
  (try
    (apply handle-action (parse-spec "Hi, my name is \"kermit \\\"t\\\" frog\"! I am \"55\" years old!"))
    (is false "should throw exception")
    (catch ExceptionInfo e
      (let [{:keys [action args]} (.getData e)]
        ()
        (is (= action "Hi, my name is %1s! I am %2s years old!"))
        (is (= args ["kermit \"t\" frog" "55"]))))))

(defmethod handle-action "Hi, my name is %1s! I am %2s years old!" [_ & args] args)

(deftest test-implemented
  (is (= ["Steve Dave" "24"] (apply handle-action (parse-spec "Hi, my name is \"Steve Dave\"! I am \"24\" years old!"))))
  (is (= ["kermit \"t\" frog" "55"] (apply handle-action (parse-spec "Hi, my name is \"kermit \\\"t\\\" frog\"! I am \"55\" years old!"))))
  )
