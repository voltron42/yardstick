(defproject org.clojars.voltron42/yardstick "0.1.0-SNAPSHOT"
  :description "Yardstick is an implementation of Gauge testing in Clojure. Yardstick is built to run any valid Gauge \".spec\" test files."
  :url "http://github.com/voltron42/yardstick"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.csv "0.1.4"]
                 [markdown-clj "1.0.2"]
                 [junit/junit "4.12"]
                 [instaparse "1.4.9"]])
