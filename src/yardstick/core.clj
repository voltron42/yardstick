(ns yardstick.core
  (:require [clojure.tools.cli :as cli]
            [yardstick.parse :as p]
            [yardstick.tags :as t]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.pprint :as pp]
            [clojure.data.json :as json])
  (:import (clojure.lang PersistentVector Keyword)
           (java.io File)))

(def ^:private steps (atom {}))

(defn def-step [step-str step-func]
  (swap! steps assoc step-str step-func)
  nil)

(defn- get-step [action args]
  (apply format action (map json/write-str args)))

(defn- do-step [action & args]
  (println action)
  (println (contains? @steps action))
  (if-let [step (get @steps action)]
    (apply step args)
    (throw (IllegalArgumentException.
             (str "Step Not Implemented: "
                  (get-step action args))))))

(def ^:private hooks (atom {:before-spec []
                  :after-spec []
                  :before-scenario []
                  :after-scenario []
                  :before-step []
                  :after-step []}))

(def ^:private valid-hooks #{:before-spec :after-spec :before-scenario :after-scenario :before-step :after-step})

(defn def-hook
  ([^Keyword hook func]
   (def-hook hook "" func))
  ([^Keyword hook ^String tags func]
  (when-not (valid-hooks hook)
    (throw (IllegalArgumentException. (str hook " is invalid, must be one of " valid-hooks))))
  (swap! hooks update-in [hook] conj [(t/parse-tag-validator tags) func])))

(defn- resolve-hook [hook tags]
  (let [my-hooks (filter #((first %) tags) (get @hooks hook))]
    (if (empty? my-hooks)
      (constantly nil)
      (->> my-hooks
           (map second)
           (apply juxt)))))

(def ^:private consumers (atom (fn [_] nil)))

(defn def-consumer [consumer-func]
  (swap! consumers juxt consumer-func))

(defn print-to [event]
  (@consumers event))

(defn- get-tests [file-list ^String test-file-or-folder]
  (let [file-obj (File. test-file-or-folder)]
    (if (.isDirectory file-obj)
      (reduce get-tests file-list (map #(.getAbsolutePath %) (.listFiles file-obj)))
      (conj file-list test-file-or-folder))))

(defn run
  ([^PersistentVector paths] (run paths ""))
  ([^PersistentVector paths ^String tags]
   (println "steps:")
   (pp/pprint @steps)
   (println)
   (println "hooks:")
   (pp/pprint @hooks)
   (println)
   (let [tag-resolver (t/parse-tag-validator tags)
         tag-resolve #(tag-resolver (:tags %))
         files (filter #(.endsWith ^String % ".spec") (reduce get-tests [] paths))
         {:keys [specs bad-files]} (reduce
                                     (fn [out file]
                                       (try
                                         (update-in out [:specs] conj (p/parse-test-file (slurp file)))
                                          (catch Throwable t
                                            (update-in out [:bad-files] conj {:event :bad-file :file file :error t}))))
                                     {:specs []
                                      :bad-files []}
                                     files)]
     (doseq [bad-file bad-files]
       (print-to bad-file))
     (print-to {:event :suite-start})
     (doseq [{:keys [spec for-each scenarios] spec-tags :tags} (filter tag-resolve specs)]
       (let [before-spec (resolve-hook :before-spec spec-tags)
             after-spec (resolve-hook :after-spec spec-tags)]
         (try
           (print-to {:event :spec-start :spec spec})
           (before-spec spec)
           (doseq [{:keys [scenario steps] scenario-tags :tags} (filter tag-resolve scenarios)]
             (let [all-tags (set/union spec-tags scenario-tags)
                   before-scenario (resolve-hook :before-scenario all-tags)
                   after-scenario (resolve-hook :after-scenario all-tags)
                   before-step (resolve-hook :before-step all-tags)
                   after-step (resolve-hook :after-step all-tags)]
               (try
                 (print-to {:event :scenario-start :spec spec :scenario scenario})
                 (before-scenario spec scenario)
                 (doseq [step (concat for-each steps)]
                   (let [event {:event :step :spec spec :scenario scenario :step (get-step (first step) (rest step))}]
                     (try
                       (before-step spec scenario step)
                       (try
                         (apply do-step step)
                         (print-to event)
                         (catch Throwable t
                           (print-to (assoc event :error t))))
                       (catch Throwable t
                         (print-to {:error t :event :before-step :spec spec :scenario scenario :step step}))
                       (finally
                         (try
                           (after-step spec scenario step)
                           (catch Throwable t
                             (print-to {:error t :event :after-step :spec spec :scenario scenario :step step})))))))
                 (catch Throwable t
                   (print-to {:error t :event :before-scenario :spec spec :scenario scenario}))
                 (finally
                   (try
                     (after-scenario spec scenario)
                     (catch Throwable t
                       (let [event {:error t :event :after-scenario :spec spec :scenario scenario}]
                         (print-to event)))
                     (finally
                       (print-to {:event :scenario-end :spec spec :scenario scenario})))))))
           (catch Throwable t
             (let [event {:error t :event :before-spec :spec spec}]
               (print-to event)))
           (finally
             (try
               (after-spec spec)
               (catch Throwable t
                 (let [event {:error t :event :after-spec :spec spec}]
                   (print-to event)))
               (finally
                 (print-to {:event :spec-end :spec spec})))))))
     nil)))
