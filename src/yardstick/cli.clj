(ns yardstick.cli
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.tools.cli :as cli]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.spec.alpha :as s])
  (:import (java.util Properties)))

(defn usage "Print usage info."
  ([options-summary program-name] (usage options-summary program-name false))
  ([options-summary program-name is-daemon?]
   (->> [(format "Usage: %s [options]" program-name)
         (if is-daemon?
           "With no options specified the program will operate as a daemon."
           "")
         "Options:"
         options-summary]
        (str/join \newline))))

(defn error-msg "Display errors." [errors]
  (str "The following errors occurred while parsing your command:\n\n" (str/join \newline errors)))

(defn is-in-repl? "Check if running in a REPL." []
  (some #(and (= "clojure.main$repl" (.getClassName %)) (= "doInvoke" (.getMethodName %)))
        (.getStackTrace (Thread/currentThread))))

(defn is-in-test? "Check if running tests." []
  (some #(and (= "clojure.test$run_tests" (.getClassName %)) (= "doInvoke" (.getMethodName %)))
        (.getStackTrace (Thread/currentThread))))

(defn exit "Log an optional exit message, then exit if not running in a REPL or running tests."
  ([status]
   (when-not (or (is-in-repl?) (is-in-test?))
     (System/exit status)))
  ([status msg]
   (println msg)
   (exit status)))

(def ^:private options-key-set #{:tags :env :parallel :nodes :specsDir})

(s/def ::options )

(def ^:private default-options {:tags "" :env "default" :nodes 8 :specsDir "specs"})

(def ^:private cli-options
  [["-t" "--tags TAGEXPRESSION" "Executes the specs and scenarios tagged with given tags"]
   ["-e" "--env ENVIRONMENT" "Specifies the environment to use (default \"default\")"]
   ["-p" "--parallel" "Execute specs in parallel"]
   ["-n" "--nodes NODES" "Specify number of parallel execution streams (default 8)"
    :parse-fn #(Integer/parseInt %)]
   ["--specsDir SPECSDIR" "Root path for all tests"]
   ["-h" "--help"]])

(comment "
      --fail-safe           Force return 0 exit code, even in case of execution failures. Parse errors will return non-zero exit codes.
  -f, --failed              Run only the scenarios failed in previous run. This cannot be used in conjunction with any other argument
  -g, --group int           Specify which group of specification to execute based on -n flag (default -1)
      --hide-suggestion     Prints a step implementation stub for every unimplemented step
  -i, --install-plugins     Install All Missing Plugins (default true)
      --repeat              Repeat last run. This cannot be used in conjunction with any other argument
      --simple-console      Removes colouring and simplifies the console output
  -s, --sort                Run specs in Alphabetical Order
      --strategy eager      Set the parallelization strategy for execution. Possible options are: eager, `lazy` (default \"lazy\")
  -r, --table-rows string   Executes the specs and scenarios only for the selected rows. It can be specified by range as 2-4 or as list 2,4
  -v, --verbose             Enable step level reporting on console, default being scenario level
")

(defn -config-file-stream [filename]
  (edn/read-string (slurp filename)))

(defn -config-from-cli [cli-args]
  (let [{:keys [options errors summary] [path] :args} (cli/parse-opts cli-args cli-options)]
    (let [options (merge default-options
                         (if (nil? path)
                           options
                           (assoc options :specsDir path)))]
      (cond
        errors (exit 1 (error-msg errors))
        (:help options) (exit 0 (usage summary "yardstick" true))
        (empty? (set/difference (set (keys options)) options-key-set)) options
        :default
        (do
          (println "Not a recognized pattern.  Here is the available rollup patterns.")
          (exit 0 (usage summary "formulary-rollup.sh" true)))))))

(defn -config-from-json [json-file]
  (json/read-str
    (slurp json-file)
    :key-fn keyword))

(defn -config-from-props [props-file]
  (let [^Properties props (Properties.)
        _ (.load props (io/input-stream props-file))
        options (reduce
                  (fn [options prop-name]
                    (let [prop (.getProperty props prop-name)]
                      (if (nil? prop)
                        options
                        (assoc options prop-name prop))))
                  {}
                  (.stringPropertyNames props))]
    (if-not (contains? options :nodes)
      options
      (if-not (string? (:nodes options))
        options
        (assoc options :nodes (Integer/parseInt (:nodes options)))))))