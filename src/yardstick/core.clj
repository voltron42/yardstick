(ns yardstick.core
  (:require [clojure.tools.cli :as cli]
            [yardstick.parse :as p]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.pprint :as pp]
            [clojure.data.json :as json])
  (:import (clojure.lang ExceptionInfo)
           (java.io File)))

(defn- get-step [action args]
  (apply format action (map json/write-str args)))

(defmulti do-step (fn [action & _] action))

(defmethod do-step :default [action & args]
  (throw (IllegalArgumentException.
           (str "Step Not Implemented: "
                (get-step action args)))))

(defprotocol Printer
  (print-to [this event]))

(def ^:private default-printer
  (reify Printer
    (print-to [_ event]
      (pp/pprint event))))

(defprotocol Hooks
  (before-suite [this])
  (after-suite [this])
  (before-spec [this])
  (after-spec [this])
  (before-scenario [this])
  (after-scenario [this]))

(def ^:private default-hooks
  (reify Hooks
    (before-suite [_] nil)
    (after-suite [_] nil)
    (before-spec [_] nil)
    (after-spec [_] nil)
    (before-scenario [_] nil)
    (after-scenario [_] nil)))

(def ^:private cli-options
  [["-i" "--include INCLUDE" "Include tags"
    :default #{}
    :parse-fn #(set (str/split % #","))]
   ["-x" "--exclude EXCLUDE" "Exclude tags"
    :default #{}
    :parse-fn #(set (str/split % #","))]])

(defn- parse-args [args]
  (let [{:keys [arguments summary errors] {:keys [include exclude] :as options} :options} (cli/parse-opts args cli-options)]
    (when-not (empty? errors)
      (throw (ExceptionInfo. "Parsing Errors:" {:errors errors})))
    (when (empty? arguments)
      (throw (ExceptionInfo. "Missing Test Path(s):" {:summary summary
                                                      :usage "[name] [options] file-paths"})))
    (assoc options :paths arguments :tags {:include include :exclude exclude})))

(defn- get-tests [file-list ^String test-file-or-folder]
  (let [file-obj (File. test-file-or-folder)]
    (if (.isDirectory file-obj)
      (reduce get-tests file-list (map #(.getAbsolutePath %) (.listFiles file-obj)))
      (conj file-list test-file-or-folder))))

(defn- resolve-tags [{:keys [include exclude]}]
  (fn [tags]
    (and
      (empty? (set/intersection exclude tags))
      (or
        (empty? include)
        (empty? tags)
        (not (empty? (set/intersection include tags)))))))

(defn -run
  ([] (-run []))
  ([cli-args & {:keys [^Hooks hooks ^Printer printer]
                :or {hooks default-hooks
                     printer default-printer}}]
   (let [{:keys [paths] run-tags :tags} (parse-args cli-args)
         files (filter #(.endsWith ^String % ".spec") (reduce get-tests [] paths))
         {:keys [specs bad-files]} (reduce
                                     (fn [out file]
                                       (try
                                          (update-in out [:specs] conj (p/parse-test-file (slurp file)))
                                          (catch Throwable t
                                            (update-in out [:bad-files] conj {:event :bad-file :file file :error t}))))
                                     {:specs []
                                      :bad-files []}
                                     files)
         results-atom (atom bad-files)
         tag-resolve (resolve-tags run-tags)]
     (doseq [bad-file bad-files]
       (print-to printer bad-file))
     (try
       (before-suite hooks)
       (doseq [{:keys [spec for-each scenarios]} (filter tag-resolve specs)]
         (try
           (before-spec hooks)
           (doseq [{:keys [scenario steps]} (filter tag-resolve scenarios)]
             (try
               (before-scenario hooks)
               (doseq [step for-each]
                 (let [event {:event :step-before-each-scenario :spec spec :scenario scenario :step (get-step (first step) (rest step))}]
                   (try
                     (apply do-step step)
                     (print-to printer event)
                     (swap! results-atom conj event)
                     (catch Throwable t
                       (let [event (assoc event :error t)]
                         (print-to printer event)
                         (swap! results-atom conj event))))))
               (doseq [step steps]
                 (let [event {:event :step :spec spec :scenario scenario :step (get-step (first step) (rest step))}]
                   (try
                     (apply do-step step)
                     (print-to printer event)
                     (swap! results-atom conj event)
                     (catch Throwable t
                       (let [event (assoc event :error t)]
                         (print-to printer event)
                         (swap! results-atom conj event))))))
               (catch Throwable t
                 (let [event {:error t :event :before-scenario :spec spec :scenario scenario}]
                   (print-to printer event)
                   (swap! results-atom conj event)))
               (finally
                 (try
                   (after-scenario hooks)
                   (catch Throwable t
                     (let [event {:error t :event :after-scenario :spec spec :scenario scenario}]
                       (print-to printer event)
                       (swap! results-atom conj event)))))))
           (catch Throwable t
             (let [event {:error t :event :before-spec :spec spec}]
               (print-to printer event)
               (swap! results-atom conj event)))
           (finally
             (try
               (after-spec hooks)
               (catch Throwable t
                 (let [event {:error t :event :after-spec :spec spec}]
                   (print-to printer event)
                   (swap! results-atom conj event)))))))
       (catch Throwable t
         (let [event {:error t :event :before-suite}]
           (print-to printer event)
           (swap! results-atom conj event)))
       (finally
         (try
           (after-suite hooks)
           (catch Throwable t
             (let [event {:error t :event :after-suite}]
               (print-to printer event)
               (swap! results-atom conj event))))))
     @results-atom)))
