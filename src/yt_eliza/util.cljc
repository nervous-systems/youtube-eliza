(ns yt-eliza.util
  (:require #?(:cljs [cljs.nodejs] :clj [clojure.java.io])
            #?(:cljs [cljs.reader :refer [read-string]])
            [clojure.string :as str]))

(defn read-config []
  #?(:cljs
     (-> (cljs.nodejs/require "fs")
         (.readFileSync "static/config.edn" "UTF-8")
         read-string)
     :clj
     (-> (clojure.java.io/resource "config.edn") slurp read-string)))

(defn unquery-url [s]
  (first (str/split s #"\?")))

(defn contains-html? [s]
  (re-find #"<" s))

(defn weighted-choice [m]
  (let [weights (reductions + (vals m))
        choice  (rand (last weights))]
    (nth (keys m) (count (take-while #(<= % choice) weights)))))
