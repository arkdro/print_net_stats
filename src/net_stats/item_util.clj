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

(defn get_interface_content
  [chunk interface]
  (->> chunk
       (str/split-lines)
       (filter #(re-find (re-pattern interface) %))
       (first)))

(defn parse_interface_content
  [text]
  (json/read-str text))

(defn is_interface_matched
  [iface iface_data]
  (let [iface_in_data (get iface_data "ifname")]
    (= iface iface_in_data)))

(defn find_interface_part
  [iface all_interface_data]
  (->> all_interface_data
       (filter #(is_interface_matched iface %))
       (first)))

(defn extract_one_interface_data
  [iface all_interface_data]
  (let [iface_data (find_interface_part iface all_interface_data)
        rx_bytes (get-in iface_data ["stats64" "rx" "bytes"])
        tx_bytes (get-in iface_data ["stats64" "tx" "bytes"])]
    {:iface iface
     :rx rx_bytes
     :tx tx_bytes}))

(defn get_data_from_chunk
  [chunk interface]
  (let [
        timestamp (get_timestamp chunk)
        text (get_interface_content chunk interface)
        all_interface_data (parse_interface_content text)
        data (extract_one_interface_data interface all_interface_data)]
    {:ts timestamp
     :data data}))

