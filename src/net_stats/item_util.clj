(ns net-stats.item-util
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]))

(def separating_line "--- net ---")

(def date_format "yyyy-MM-dd'T'HH:mm:SSzzz")

(def formatter (java.text.SimpleDateFormat. date_format))

(defn extract_timestamp
  [chunk]
    (->> chunk
       (str/split-lines)
       (filter #(re-find #"(?m)^\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d" %))
       (first)))

(defn parse_timestamp
  [text]
  (.parse formatter text))

(defn get_timestamp
  [chunk]
  (let [text_timestamp (extract_timestamp chunk)]
    (parse_timestamp text_timestamp)))


